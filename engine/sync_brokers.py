"""Download the public broker registry used by DeleteMe.

No paid API is used. The registry is CC BY 4.0 and contains official
opt-out URLs/emails. Run this periodically to refresh broker endpoints.
"""
from pathlib import Path
import json
import urllib.request

SOURCE = "https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json"
OUT = Path(__file__).resolve().parent.parent / "data" / "brokers.json"


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with urllib.request.urlopen(SOURCE, timeout=30) as r:
        data = json.load(r)
    if not isinstance(data, list) or len(data) < 400:
        raise RuntimeError(f"Unexpected broker registry: {len(data) if isinstance(data, list) else 'invalid'}")
    OUT.write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Saved {len(data)} brokers to {OUT}")


if __name__ == "__main__":
    main()
