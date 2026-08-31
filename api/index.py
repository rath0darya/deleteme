from __future__ import annotations

import html
import json
import re
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from urllib.parse import quote, urlparse
from urllib.request import Request, urlopen

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(title="DeleteMe Public Privacy API")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["GET", "POST", "OPTIONS"], allow_headers=["*"])

REGISTRY_URL = "https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json"
XON_URL = "https://api.xposedornot.com/v1/check-email/{}"
_cache: list[dict] | None = None


def get_json(url: str, timeout: int = 10):
    req = Request(url, headers={"User-Agent": "DeleteMe/1.0 open-source privacy tool"})
    with urlopen(req, timeout=timeout) as r:
        return json.loads(r.read().decode("utf-8"))


def registry() -> list[dict]:
    global _cache
    if _cache is None:
        data = get_json(REGISTRY_URL, 20)
        _cache = [x for x in data if x.get("company") and (x.get("optOutUrl") or x.get("optOutEmail"))]
    return _cache


class ScanRequest(BaseModel):
    identifier: str = Field(min_length=2, max_length=320)
    offset: int = Field(default=0, ge=0)
    limit: int = Field(default=12, ge=1, le=20)


class RemoveRequest(BaseModel):
    items: list[dict] = Field(default_factory=list, max_length=1000)
    identifier: str = Field(min_length=2, max_length=320)


def search_broker(item: dict, identifier: str) -> dict:
    domain = item.get("domain", "")
    if not domain:
        return {"found": False, "broker": item}
    query = quote(f'site:{domain} "{identifier}"')
    url = f"https://html.duckduckgo.com/html/?q={query}"
    try:
        req = Request(url, headers={"User-Agent": "Mozilla/5.0 DeleteMe/1.0"})
        with urlopen(req, timeout=8) as r:
            body = r.read().decode("utf-8", "ignore")
        found = identifier.lower() in html.unescape(re.sub(r"<[^>]+>", " ", body)).lower()
        return {"found": found, "broker": item, "checked": True}
    except Exception as e:
        return {"found": False, "broker": item, "checked": False, "error": str(e)[:120]}


@app.get("/api/health")
def health():
    return {"ok": True, "service": "DeleteMe Public Privacy API", "paid_api": False, "registry": len(registry())}


@app.get("/api/brokers")
def brokers():
    data = registry()
    return {"count": len(data), "brokers": data}


@app.post("/api/scan")
def scan(req: ScanRequest):
    identifier = req.identifier.strip()
    result = {"identifier": identifier, "offset": req.offset, "limit": req.limit, "sources": [], "breaches": []}
    if re.fullmatch(r"[^\s@]+@[^\s@]+\.[^\s@]+", identifier):
        try:
            result["breaches"] = get_json(XON_URL.format(quote(identifier, safe="")), 12)
        except Exception as e:
            result["breach_error"] = str(e)[:160]
    data = registry()[req.offset:req.offset + req.limit]
    with ThreadPoolExecutor(max_workers=min(6, len(data) or 1)) as pool:
        futures = [pool.submit(search_broker, item, identifier) for item in data]
        for f in as_completed(futures):
            result["sources"].append(f.result())
    result["sources"].sort(key=lambda x: (not x.get("found", False), x["broker"].get("company", "").lower()))
    result["next_offset"] = req.offset + len(data) if req.offset + len(data) < len(registry()) else None
    result["registry_count"] = len(registry())
    return result


@app.post("/api/remove")
def remove(req: RemoveRequest):
    # The public service never submits arbitrary cross-origin forms or impersonates a user.
    # It returns the official removal endpoints and mail actions for explicit user approval.
    tasks = []
    emails = []
    for item in req.items:
        broker = item.get("broker", item)
        if not broker.get("company"):
            continue
        task = {"company": broker.get("company"), "domain": broker.get("domain", ""), "status": "ready"}
        if broker.get("optOutUrl"):
            task["optOutUrl"] = broker["optOutUrl"]
        if broker.get("optOutEmail"):
            task["optOutEmail"] = broker["optOutEmail"]
            emails.append(broker["optOutEmail"])
        tasks.append(task)
    return {"count": len(tasks), "tasks": tasks, "email_count": len(emails), "note": "Only official broker endpoints are returned. CAPTCHAs, identity checks and final submissions remain under user control."}
