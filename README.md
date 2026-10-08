# Project Zomboid 42.21 Player-Built Room Fix

A tiny, client-side ZombieBuddy patch set for two related Build 42.21.0 rendering problems affecting enclosed player-built rooms and extensions attached to predefined buildings.

It does **not** include MinidoracatJavaPatchFor42, Peek a View, optimisation patches, networking changes, memory patches, vehicle changes, zombie changes, or server patches.

## Compatibility

- Project Zomboid **42.21.0**, revision `4a0e9546ec`
- `projectzomboid.jar` SHA-256: `E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33`
- ZombieBuddy **2.3.2**
- Client-side only

The build and installer deliberately refuse other game or ZombieBuddy binaries.

## Patch 1: XL-tree null-room guard

Target:

```text
zombie.iso.objects.IsoTree.isPlayerInsideARoom(IsoPlayer): boolean
```

Build 42.21.0 can report `player.isInARoom() == true` while `player.getSquare().getRoom() == null`. The vanilla XL-tree fade then dereferences the missing room and aborts the remainder of that render pass.

The ZombieBuddy prefix skips vanilla only for that impossible room state. A skipped boolean method returns `false`, treating the player as outside for this tree-fade calculation. Every other call continues into vanilla unchanged.

## Patch 2: attached-extension wall cutaway

Target:

```text
zombie.iso.fboRenderChunk.FBORenderCutaways.IsCutawaySquare(
    FBORenderCutaways.CutawayWall,
    IsoGridSquare,
    IsoGridSquare,
    long
): boolean
```

The same affected extensions can exist as fully roofed player regions without an `IsoRoom` or room ID. Vanilla's wall-cutaway code therefore cannot associate their boundary walls with the region occupied by the camera player.

The exit advice changes the final cutaway answer only when a wall borders a fully roofed player region whose square has no `IsoRoom`:

- player in that exact region: cut the boundary wall away;
- player outside that region: keep the boundary wall rendered;
- normal mapped rooms, freestanding player rooms with a real `IsoRoom`, ambiguous room boundaries, and missing data: retain vanilla's answer.

This is a render-only fallback. It never creates or changes rooms, building definitions, regions, squares, wall objects, saves, or multiplayer state.

## Build

Run from PowerShell:

```powershell
.\build.ps1
```

The script uses handwritten signature-only compile stubs so JDK 17 or newer can build the patch without redistributing Project Zomboid classes. It verifies the actual installed 42.21.0 method signatures with `javap`, runs behavior tests, and packages only `io.shaun.playerbuiltroomfix` classes.

To exercise the compiled advice through the real ZombieBuddy 2.3.2 transformer in an isolated JVM, including loading and verifying the actual 42.21.0 target classes, run:

```powershell
.\scripts\integration-test.ps1
```

## Install and uninstall

Close Project Zomboid before installation. See [INSTALL.md](INSTALL.md) for the guarded installer and rollback procedure.

At startup, successful discovery prints once:

```text
[PlayerBuiltRoomFix] v0.2.1 installed IsoTree null-room guard and extension cutaway guard
```

## Scope and limitations

This is an interim workaround for one exact game revision. The Indie Stone has reported the underlying player-built-room problem fixed internally for a future game build. Remove this patch before updating Project Zomboid, then retest without it.

No Project Zomboid or ZombieBuddy binaries are included in this repository.

## References

- [ZombieBuddy 2.3.2 Java modding guide](https://github.com/zed-0xff/ZombieBuddy/blob/v2.3.2/doc/ModdingGuide.md)
- [Indie Stone report: attached player-built rooms, exceptions, and visibility glitches](https://theindiestone.com/forums/topic/101887-42210-entering-player-built-rooms-adjoining-pre-built-structures-causes-exceptions-visibility-glitches/)
- [Indie Stone report: buildings and trees disappear in player-built structures](https://theindiestone.com/forums/topic/101935-42203-42210-stable-after-update-buildings-and-trees-dissapear-when-in-player-built-structure-vanilla/)

## License

MIT. See [LICENSE](LICENSE).
