# FIGHT//FRAME — Android prototype

An offline, native Kotlin Android Studio starter project for the tactical fighting-game concept.

## Open and run
1. Install Android Studio (current stable version).
2. Choose **Open** and select this `FightFrameAndroid` folder.
3. Let Gradle sync. If prompted, install Android SDK Platform 35.
4. Run on an Android emulator or connected phone.

## Build an APK
In Android Studio: **Build → Build APK(s)**. The debug APK is usually created at:
`app/build/outputs/apk/debug/app-debug.apk`

Or use the Gradle task `:app:assembleDebug`.

## Prototype mechanics
- Offline one-player practice against a deterministic CPU opponent.
- Select distance, movement and an action (punch, kick, block, energy).
- Resolves hit, block, range miss, stamina cost, HP, wins and progression.
- Custom Canvas-drawn schematic fighters and arena.

## Current limitations
This is a source starter/prototype, not a finished commercial game. The current CPU is deterministic, and the app uses native Android buttons. The full 5-second free movement / 3-second slow-motion selection loop, multi-hit combos, sound assets, unlockable skills, drop bonuses, polished menus/levels, and online matchmaking still need implementation. Online play requires a backend and networking protocol; this build is offline only.
