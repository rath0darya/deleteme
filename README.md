# DeleteMe

Open-source, static personal-data exposure and removal toolkit.

## Zero paid-API dependency

DeleteMe is designed to run entirely as a static GitHub Pages site. It does not require a backend, database, API key, subscription, or paid API.

The browser generates source-specific searches and removal workflows. Users choose what external sites to open and what information to submit. The project can also ship optional open datasets in the repository without making a third-party API a requirement.

## Features

- Email, phone, username and name search modes
- Exact-match public-web search generation
- Google, Bing and DuckDuckGo search links
- Public GitHub code search
- Social-platform exposure search helpers
- Data-broker removal workflow
- Public-document exposure workflow
- Breach-exposure workflow designed for local/open datasets
- Discover → Request → Verify lifecycle
- No application server
- No API credentials
- No paid provider dependency
- Mobile responsive

## GitHub Pages

The site is deployed by `.github/workflows/pages.yml` using GitHub Pages. GitHub Pages can publish static HTML, CSS and JavaScript directly from a repository.

## Privacy model

The scan value is processed in the browser. DeleteMe does not operate a central server that receives the user's identifier.

External searches only happen when the user deliberately opens the generated search link. The project does not claim that it can erase every copy of data from the internet. A removal result must be verified at the original source and can later reappear.

## Open-source extension model

New source coverage should be added as repository data/configuration rather than requiring a paid API. Each source can define:

- discovery/search URL
- official removal URL
- supported identifier types
- request instructions
- verification method
- jurisdiction or eligibility notes

Optional breach datasets can be imported and indexed locally. The application should never require a commercial breach API to function.

## License

Choose and add an OSI-approved open-source license before publishing a stable release.
