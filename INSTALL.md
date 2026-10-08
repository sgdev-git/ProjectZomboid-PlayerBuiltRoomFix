# Installation and rollback

## Requirements

1. Project Zomboid must be closed.
2. Project Zomboid must be exactly 42.21.0 revision `4a0e9546ec`.
3. ZombieBuddy 2.3.2 must already be installed for the normal 64-bit executable.

## Install

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install.ps1
```

The installer:

- verifies the exact game and ZombieBuddy JAR hashes;
- builds and tests the patch;
- backs up `ProjectZomboid64.json` and any prior `PlayerBuiltRoomFix` folder under `%USERPROFILE%\Zomboid\backups\PlayerBuiltRoomFix`;
- installs `PlayerBuiltRoomFix.jar` under the game directory;
- adds only the documented ZombieBuddy `patches_jar` argument.

It does not modify `projectzomboid.jar`, saves, server files, Workshop lists, or the Project Zomboid mod list.

## Uninstall

With Project Zomboid closed, run the installed uninstaller:

```powershell
powershell -ExecutionPolicy Bypass -File "C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid\PlayerBuiltRoomFix\uninstall.ps1"
```

The uninstaller removes only this patch's `patches_jar` argument and installation folder. ZombieBuddy remains installed.
