# DeleteMe

Open-source Android privacy tool for finding reported personal-data exposure and opening official removal routes.

## Android-first

DeleteMe is now an Android application. The repository no longer ships the old GitHub Pages website or the local Python web engine.

## Current app

- Native Android UI with Material components
- Scan, Results and Settings navigation
- Free XposedOrNot breach lookup for email identifiers
- Public broker removal directory
- Official removal links opened only when the user chooses them
- No DeleteMe account
- No paid API dependency
- No DeleteMe server required for the Android client

## Build

The APK is built automatically with GitHub Actions whenever `android/**` changes.

The latest APK is published to `downloads/DeleteMe.apk` by the publish workflow after a successful build.

## Important limitation

A broker appearing in the removal directory is not evidence that the user's record exists there. The app keeps confirmed breach matches separate from available removal routes.

## Open source

All Android application source and build workflows are available in this repository.
