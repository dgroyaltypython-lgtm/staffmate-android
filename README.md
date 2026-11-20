# StaffMate – Attendance & Payroll (Android)

A native Android starter application for offline staff management. Data is stored in the app's private SQLite database on the phone. No login, server, internet connection, or cloud database is required.

## Included
- Staff directory: add, edit, deactivate staff
- Monthly salary or daily wage setup
- Daily attendance: Present, Half Day, Absent, Leave, Weekly Off
- Optional IN and OUT time fields
- Attendance history
- Advance and deduction entries
- Attendance-based salary estimate
- Share/export attendance CSV text
- Dark premium gradient/glass-inspired cards

## Salary calculation defaults
- Monthly salary: 30-day basis
- Present = 1 paid day
- Half Day = 0.5 paid day
- Weekly Off = 1 paid day
- Leave and Absent = unpaid
- Daily wage = Present + Half Day only
- Net payable = Gross - advances - deductions (minimum zero)

These are defaults, not legal/payroll advice. Confirm your workplace's paid leave, weekly-off, overtime, and payroll rules before using totals for payment.

## Build APK with Android Studio
1. Install Android Studio (current stable) and its Android SDK.
2. Extract this ZIP.
3. Android Studio → Open → select the `StaffMate_Android` folder.
4. Allow Gradle sync and install Android SDK Platform 35 if prompted.
5. Build → Build Bundle(s) / APK(s) → Build APK(s).
6. APK output: `app/build/outputs/apk/debug/app-debug.apk`.
7. Transfer the APK to your Android phone and install it. You may need to allow installation from that file manager.

## Notes
- This ZIP contains the complete Android Studio source project, not a precompiled APK. The build environment used to package this project did not have Android SDK/Gradle installed, so an APK could not be compiled here.
- App data is private to this app on this phone. Uninstalling the app or clearing its storage deletes its database. Use CSV sharing regularly for backups.
- This initial version uses a 30-day monthly salary model and a simple share-sheet CSV export. It does not yet generate PDF payslips or provide biometric attendance, fingerprint, or automatic geolocation.


## Build on Codemagic (included `codemagic.yaml`)
1. Extract the ZIP on your computer.
2. Create a GitHub repository, for example `staffmate-android`, and upload the contents of the `StaffMate_Android` folder. Ensure `codemagic.yaml` is at the repository root, alongside `settings.gradle` and `build.gradle`.
3. In Codemagic, add/connect the GitHub repository and select the `staffmate-android-debug` workflow.
4. Start a build. The workflow uses Java 17, installs Android SDK Platform 35 / Build Tools 35.0.0, and runs `gradle :app:assembleDebug`.
5. When the build succeeds, open the build's Artifacts section and download `app-debug.apk`.
6. Transfer the APK to your Android phone and install it. If Android blocks installation, allow installation from the app used to open the APK.

This workflow creates a debug APK. It is suitable for testing. For a distributable release APK, configure Android signing in Codemagic and create a separate release workflow.
