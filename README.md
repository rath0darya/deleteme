# DeleteMe

Open-source digital footprint removal workspace.

## What it is

DeleteMe is designed around a **discover → request → verify** workflow for personal-data exposure. It can be extended with source adapters for data brokers, search engines, social platforms, public web pages, developer platforms and breach-intelligence providers.

The current UI is an MVP shell and includes a server-side Have I Been Pwned adapter when `HIBP_API_KEY` is configured.

## Important limitation

There is no universal API that can delete somebody's data from "the internet". A breach record can be detected, but the underlying breached database may be outside the user's control and may have been copied repeatedly. Removal therefore needs to distinguish between:

- **Discoverable**: evidence was found.
- **Removable**: a controller/host provides a valid deletion or takedown path.
- **Requested**: a request was submitted.
- **Verified removed**: a later check confirms the exposure is gone.
- **Reappeared**: the data returned after a successful removal.

## Local development

```bash
npm install
cp .env.example .env.local
npm run dev
```

Set `HIBP_API_KEY` in `.env.local` to enable the breach lookup endpoint. Keep provider credentials server-side.

## Planned source adapter model

Each source adapter should expose:

```ts
type SourceAdapter = {
  id: string;
  name: string;
  capabilities: Array<'discover' | 'requestRemoval' | 'verifyRemoval'>;
  discover(input: NormalizedIdentity): Promise<Finding[]>;
  requestRemoval?(finding: Finding): Promise<RemovalRequest>;
  verifyRemoval?(finding: Finding): Promise<VerificationResult>;
};
```

This keeps provider-specific behaviour isolated instead of pretending every site supports the same deletion mechanism.

## Privacy principles

- Minimise identity data collected.
- Do not log raw scan identifiers.
- Encrypt stored removal evidence in production.
- Use provider APIs server-side when credentials are required.
- Give users explicit control over deletion of their DeleteMe workspace.
- Never claim a breach has been deleted when only the notification has been processed.
