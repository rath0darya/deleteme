"""DeleteMe local-first removal engine."""
from __future__ import annotations

import asyncio
import json
import os
import re
import smtplib
import sqlite3
import threading
import time
from email.message import EmailMessage
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from playwright.async_api import async_playwright
import uvicorn

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "data"
BROKERS = DATA / "brokers.json"
STATE = DATA / "removal.sqlite3"
DATA.mkdir(exist_ok=True)

app = FastAPI(title="DeleteMe Local Removal Engine")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])
RUN = {"running": False, "total": 0, "completed": 0, "submitted": 0, "manual": 0, "failed": 0, "items": []}
LOCK = threading.Lock()

class Profile(BaseModel):
    identifier: str = Field(min_length=1)
    first_name: str = ""
    last_name: str = ""
    email: str = ""
    phone: str = ""
    address: str = ""
    city: str = ""
    state: str = ""
    postal_code: str = ""
    country: str = ""
    aliases: list[str] = []

class StartRequest(BaseModel):
    profile: Profile

def load_brokers() -> list[dict[str, Any]]:
    if not BROKERS.exists(): raise HTTPException(503, "Broker registry missing. Run: python engine/sync_brokers.py")
    data = json.loads(BROKERS.read_text(encoding="utf-8"))
    return [x for x in data if x.get("company") and (x.get("optOutUrl") or x.get("optOutEmail"))]

def init_db():
    con = sqlite3.connect(STATE)
    con.execute("CREATE TABLE IF NOT EXISTS requests (domain TEXT PRIMARY KEY, company TEXT, status TEXT, detail TEXT, updated INTEGER)")
    con.commit(); con.close()

def save(domain: str, company: str, status: str, detail: str):
    con = sqlite3.connect(STATE)
    con.execute("INSERT OR REPLACE INTO requests VALUES (?,?,?,?,?)", (domain, company, status, detail, int(time.time())))
    con.commit(); con.close()

def text_tokens(profile: Profile) -> dict[str, str]:
    identifier = profile.identifier
    email = profile.email or (identifier if re.match(r"^[^\s@]+@[^\s@]+\.[^\s@]+$", identifier) else "")
    phone = profile.phone or (identifier if re.match(r"^[+\d][\d\s().-]{6,}$", identifier) else "")
    return {"first": profile.first_name, "last": profile.last_name, "full": f"{profile.first_name} {profile.last_name}".strip(), "email": email, "phone": phone, "address": profile.address, "city": profile.city, "state": profile.state, "postal": profile.postal_code, "zip": profile.postal_code, "country": profile.country}

def field_value(name: str, label: str, profile: Profile) -> str:
    s = f"{name} {label}".lower(); t = text_tokens(profile)
    if "email" in s: return t["email"]
    if "phone" in s or "mobile" in s or "tel" in s: return t["phone"]
    if "first" in s or "given" in s: return t["first"]
    if "last" in s or "surname" in s or "family" in s: return t["last"]
    if "full name" in s or s.strip() in {"name", "fullname"}: return t["full"]
    if "address" in s or "street" in s: return t["address"]
    if "city" in s: return t["city"]
    if "state" in s or "province" in s: return t["state"]
    if "zip" in s or "postal" in s: return t["postal"]
    if "country" in s: return t["country"]
    return ""

async def automate_page(page, broker: dict, profile: Profile) -> tuple[str, str]:
    url = broker.get("optOutUrl", "")
    if not url: return "manual", "No web opt-out URL; use the broker email route."
    try:
        await page.goto(url, wait_until="domcontentloaded", timeout=30000)
        controls = await page.locator("input, textarea").all()
        for el in controls:
            try:
                typ = (await el.get_attribute("type") or "text").lower()
                if typ in {"hidden", "submit", "button", "file", "password"}: continue
                name = await el.get_attribute("name") or ""
                aria = await el.get_attribute("aria-label") or ""
                placeholder = await el.get_attribute("placeholder") or ""
                value = field_value(name, f"{aria} {placeholder}", profile)
                if value: await el.fill(value)
            except Exception: pass
        body = (await page.locator("body").inner_text())[:12000].lower()
        if any(x in body for x in ("captcha", "recaptcha", "turnstile", "verify you are human", "phone verification")):
            return "manual", "Verification/CAPTCHA detected; browser is waiting for user action."
        buttons = page.locator("button, input[type=submit]")
        for i in range(min(await buttons.count(), 30)):
            b = buttons.nth(i); txt = ((await b.inner_text()) + " " + (await b.get_attribute("value") or "")).lower()
            if any(k in txt for k in ("submit", "opt out", "remove", "delete", "suppress", "request")):
                await b.click(timeout=5000); await page.wait_for_timeout(1200)
                return "submitted", "Removal form submitted; broker confirmation may still be required."
        return "manual", "Opt-out page opened, but no safe generic submit control was identified."
    except Exception as e: return "failed", str(e)[:300]

def send_email(broker: dict, profile: Profile, smtp: dict) -> tuple[str, str]:
    to = broker.get("optOutEmail", "")
    if not to: return "manual", "No opt-out email available."
    t = text_tokens(profile)
    msg = EmailMessage(); msg["From"] = smtp["username"]; msg["To"] = to; msg["Subject"] = "Personal Data Deletion / Opt-Out Request"
    msg.set_content(f"Hello Privacy Team,\n\nPlease delete/suppress the personal data you hold about me and stop processing or selling it where applicable.\n\nIdentifier: {profile.identifier}\nName: {t['full']}\nEmail: {t['email']}\nPhone: {t['phone']}\nAddress: {profile.address}\nCity: {profile.city}\nState/Province: {profile.state}\nPostal Code: {profile.postal_code}\nCountry: {profile.country}\n\nPlease confirm receipt and completion of this request.\n\nRegards,\n{t['full'] or profile.identifier}\n")
    with smtplib.SMTP_SSL(smtp["host"], smtp["port"], timeout=30) as s:
        s.login(smtp["username"], smtp["password"]); s.send_message(msg)
    return "submitted", "Deletion request email sent. Confirmation may be required."

async def run_all(profile: Profile):
    brokers = load_brokers()
    with LOCK: RUN.update(running=True, total=len(brokers), completed=0, submitted=0, manual=0, failed=0, items=[])
    smtp = None
    if os.getenv("DELETEME_SMTP_HOST") and os.getenv("DELETEME_SMTP_USER") and os.getenv("DELETEME_SMTP_PASSWORD"):
        smtp = {"host": os.getenv("DELETEME_SMTP_HOST"), "port": int(os.getenv("DELETEME_SMTP_PORT", "465")), "username": os.getenv("DELETEME_SMTP_USER"), "password": os.getenv("DELETEME_SMTP_PASSWORD")}
    async with async_playwright() as pw:
        browser = await pw.chromium.launch(headless=False); context = await browser.new_context()
        for broker in brokers:
            company = broker["company"]; domain = broker.get("domain", company)
            try:
                if smtp and broker.get("optOutEmail"): status, detail = await asyncio.to_thread(send_email, broker, profile, smtp)
                else:
                    page = await context.new_page(); status, detail = await automate_page(page, broker, profile)
                    if status == "submitted": await page.close()
                save(domain, company, status, detail)
            except Exception as e:
                status, detail = "failed", str(e)[:300]; save(domain, company, status, detail)
            with LOCK:
                RUN["completed"] += 1; RUN[status] = RUN.get(status, 0) + 1; RUN["items"].append({"company": company, "domain": domain, "status": status, "detail": detail})
        await browser.close()
    with LOCK: RUN["running"] = False

@app.get("/api/health")
def health(): return {"ok": True, "engine": "local", "paid_api": False}
@app.get("/api/brokers")
def brokers():
    b = load_brokers(); return {"count": len(b), "brokers": b}
@app.get("/api/status")
def status():
    with LOCK: return dict(RUN)
@app.post("/api/remove-all")
def remove_all(req: StartRequest):
    with LOCK:
        if RUN["running"]: raise HTTPException(409, "A removal run is already active")
    init_db(); threading.Thread(target=lambda: asyncio.run(run_all(req.profile)), daemon=True).start(); return {"started": True}
if __name__ == "__main__":
    init_db(); uvicorn.run(app, host="127.0.0.1", port=8765)
