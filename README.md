# Wellbeing Tracker

An Android app that records daily mood check-ins alongside screen-time
data (with consent), to explore how digital habits relate to wellbeing.

## Status
🚧 In development — Milestone 1: mood check-ins stored on-device.

## Features (planned)
- Daily mood, energy, stress and sleep check-ins
- Screen-time and app-category usage (Android UsageStatsManager, opt-in)
- Data export for analysis in Python (within-person and lagged models)

## Privacy & Ethics
- **Local-first:** all data is stored only on the user's device.
- **Opt-in:** usage tracking requires explicit permission, which can be revoked.
- **Data minimisation:** no message content, contacts, location or keystrokes.
- **User control:** users can view and delete their data at any time.
- **Not a clinical tool:** this app does not diagnose or treat any condition.

## Tech stack
Kotlin, Jetpack Compose, Room (SQLite)

## Screen-time measurement
- Source: Android `UsageStatsManager` app open/close events (requires user-granted Usage Access).
- Stored: minutes per **app category** per day. Individual app names are not saved.
- The last 7 days are re-read every time the app opens, so days are filled in even if the app was not opened.

### Known limitations
- Android keeps detailed usage events for a limited time only; the app must be opened at least every few days.
- Many apps don't declare a category; common apps are labelled manually, others fall into "other".
- Totals differ slightly from Digital Wellbeing because of how sessions are counted.
- Slider default values (4/7) may bias check-in responses (anchoring effect).