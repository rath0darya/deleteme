# DeleteMe: one-click removal

The public GitHub Pages interface is only the frontend. For actual removal automation, run the local Python engine so your identity never passes through a DeleteMe server.

```bash
cd engine
python -m venv .venv
# Windows
.venv\\Scripts\\activate
# Linux/macOS
# source .venv/bin/activate
pip install -r requirements.txt
python sync_brokers.py
python -m playwright install chromium
python server.py
```

Keep `server.py` running, then open the GitHub Pages site. Enter an identity and click **Remove my data**.

The engine uses a public, free broker registry and local Playwright automation. It can submit supported web forms and send broker opt-out emails when local SMTP is configured. CAPTCHAs, phone checks and identity verification remain user-controlled.

Registry source: PersProtect's public 499-broker dataset, CC BY 4.0. Attribution is required when redistributed.
