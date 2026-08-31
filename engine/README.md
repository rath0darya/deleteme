# DeleteMe Local Removal Engine

This is the real execution layer for the GitHub Pages interface.

## Why a local Python engine?

GitHub Pages can host HTML/CSS/JS, but it cannot safely automate hundreds of third-party deletion forms or send email. The engine runs on the user's own computer so profile data stays local.

## Setup

```bash
cd engine
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
python sync_brokers.py
python -m playwright install chromium
python server.py
```

Then open the DeleteMe GitHub Pages site and use **Remove my data**.

The registry is sourced from the public 499-broker dataset by PersProtect (CC BY 4.0). The engine can also be extended with additional broker adapters. The project should attribute the dataset when redistributing it.

## Email automation

If a broker provides an opt-out email, set these local environment variables before starting the engine:

`DELETEME_SMTP_HOST`, `DELETEME_SMTP_PORT`, `DELETEME_SMTP_USER`, `DELETEME_SMTP_PASSWORD`

Use an app password, not the account password. SMTP is optional. Without it, the engine uses the broker's web opt-out URL.

## Safety

The engine only fills obvious identity fields. It never guesses passwords, payment information, government-ID numbers, or other sensitive verification fields. CAPTCHAs, phone verification, and identity checks are left for the user.
