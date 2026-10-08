# Changelog

## 0.2.1 - 2026-10-08

- Handle the opposite camera-facing alignment of north/west `CutawayWall` runs by checking a conservative south/east fallback tile.
- Preserve vanilla behavior when either primary square belongs to a normal `IsoRoom` or both primary sides identify player regions.

## 0.2.0 - 2026-10-08

- Add a render-only wall-cutaway fallback for fully roofed player regions that have no `IsoRoom` in Project Zomboid 42.21.0.
- Preserve the existing `IsoTree.isPlayerInsideARoom` null-room guard.
- Add behavior tests, an isolated ZombieBuddy weaving test, guarded installation, and targeted rollback.

## 0.1.0 - 2026-10-08

- Initial one-method guard for the Build 42.21.0 XL-tree fade null-room exception.
