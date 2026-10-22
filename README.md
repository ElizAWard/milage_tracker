# Mileage Tracker App

This is an android app designed to track fuel economy.

## Opening in Android Studio

Open the **repository root** (`milage_tracker/`), not the `mileageTrackerApp/` module folder.

Opening the module folder directly makes the IDE resolve Gradle against the subproject, and
the sync fails with `Task 'wrapper' not found in project ':mileageTrackerApp'` — the
`wrapper` task only exists on the root project, which is where `settings.gradle` lives.

If Android Studio is already open on the module folder, use **File > Close Project**, then
**File > Open** and select the repository root.
