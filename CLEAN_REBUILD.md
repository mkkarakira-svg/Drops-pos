# Drops POS — Clean Rebuild

This is a separate development branch. Do not publish it over the live app until backup/restore and Android package migration are verified.

## Data safety contract
- The user's verified JSON backup is the source of truth for final migration.
- No demo inventory, recipe, price or product seeding on application startup.
- No implicit migrations that change quantities, prices, or recipe ingredients.
- Restore must validate the file, display a summary, require confirmation, and verify persistence.
- Export must produce an actual user-accessible JSON file and be tested on Android.
- No installation or uninstall instruction until application ID and signing compatibility are verified.

## Modules to rebuild and verify
1. Backup / Restore / persistence
2. Products / pack sizes / pricing
3. Raw materials / purchasing
4. Recipes / production / finished stock
5. POS / invoices / returns
6. Expenses / cashbox / reports
7. Android packaging / update

## Acceptance tests
- Export JSON, open it externally, validate content and restore on a disposable test install.
- Reopen the app and verify restored data persists.
- Create a purchase and confirm raw material stock and last purchase cost update once.
- Produce a batch and confirm raw stock decreases and finished goods increase exactly once.
- Sell a packaged item and confirm finished stock decreases exactly once.
- Reload repeatedly; verify no inventory, recipe, or price changes.
- Install an update over the same package and verify local data remains intact.
- Verify no second app icon is created.

## Current status
Branch initialized from the safety-fix branch. This is NOT a finished rebuild or a tested APK.
