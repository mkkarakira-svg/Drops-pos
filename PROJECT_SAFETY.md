# Drops POS — Safety checkpoint (2026-11-03)

## Reference
- Repository: mkkarakira-svg/Drops-pos
- Last reported installed app: v4.3.136 (user screenshot). This is **not** a verified GitHub source snapshot of that installed APK.
- Current GitHub index.html UI version: v4.3.137.
- Known code commit for dashboard cleanup: 55c6ca8d575b594ac73bb7ad488a6393e430430b
- Version bump commit: f85ef0d8bb5fcfac727adb8aae0fea2dfd17611d
- Signed-build workflow edit: e758f33b930511c981ecda10c6f2c02a54442077

## Rules
1. Never overwrite or delete user device data; do not instruct uninstalling the installed app.
2. Back up user data and verify restore before attempting migrations.
3. Work on a separate development branch; never push untested changes directly to main.
4. Do not declare a release ready without verified CI build, matching Android signing identity, and device install/update test.
5. Record each change, test result, and release commit in this repository so continuity does not depend on chat history.
6. Do not confuse GitHub Pages updates with bundled Capacitor APK updates; they are distinct mechanisms.

## Known risks / unfinished verification
- The Android build workflow generates a new Android project and its debug APK may not match installed signing.
- Signed release workflow was changed but build success and signature compatibility are unverified.
- Existing in-app Update function fetches relative index.html and may resolve to packaged local assets, not the published site.
- No verified backup/restore or installed-app signing certificate has been obtained.
- Avoid further deployment until these checks are completed.

## Next safe actions
- Verify GitHub Actions build status and signing setup.
- Compare installed APK identity/signature to signed release candidate before update.
- Obtain a user-approved verified backup and test restoration separately.
- Only then prepare and test an update; preserve v4.3.136 on device until success.
