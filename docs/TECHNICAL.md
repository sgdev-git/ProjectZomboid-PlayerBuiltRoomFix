# Technical notes

## Why the room states disagree

In 42.21.0, `IsoGridSquare.isInARoom()` falls back to `IWorldRegion.isPlayerRoom()`. `IsoGridSquare.getRoom()` and `getRoomID()` instead use the generated meta-grid room fields. An enclosed extension adjoining a predefined building can therefore have:

```text
isInARoom() == true
getIsoWorldRegion().isPlayerRoom() == true
getRoom() == null
getRoomID() == -1
```

## Guard patch

`IsoTreeRoomGuardPatch` is an `OnEnter(skipOn = true)` advice. It skips only the null-room state after vanilla has classified the player as indoors. Returning `true` from the advice skips the target; the target's primitive boolean return retains its default `false` value.

## Cutaway patch

`PlayerBuiltExtensionCutawayPatch` is an `OnExit` advice on the private FBO wall decision. It uses only the current camera square, the wall square, its orientation-specific neighboring squares, and `IWorldRegion` identity. Build 42.21.0 can align a `CutawayWall` run one tile before the owning square on its opposite camera-facing side, so a conservative south/east fallback is used only when the owning and north/west squares have no normal `IsoRoom`.

The affected-region test is intentionally narrow:

1. the square has no `IsoRoom`;
2. its world region exists;
3. that region is a player room;
4. that region is fully roofed;
5. exactly one unambiguous side of the wall identifies a single affected region.

Exceptions preserve the vanilla return value. The render path performs no logging and allocates no collections.

## Why room metadata is not repaired

Creating or merging `IsoRoom`, `RoomDef`, or `BuildingDef` objects would change shared world state and introduce save, reconnect, and multiplayer ownership risks. The visual fallback intentionally leaves the server and persistent world untouched.

## Exact 42.21.0 signatures

```text
private boolean zombie.iso.objects.IsoTree.isPlayerInsideARoom(
    zombie.characters.IsoPlayer
)
descriptor: (Lzombie/characters/IsoPlayer;)Z

private boolean zombie.iso.fboRenderChunk.FBORenderCutaways.IsCutawaySquare(
    zombie.iso.fboRenderChunk.FBORenderCutaways$CutawayWall,
    zombie.iso.IsoGridSquare,
    zombie.iso.IsoGridSquare,
    long
)
descriptor: (
    Lzombie/iso/fboRenderChunk/FBORenderCutaways$CutawayWall;
    Lzombie/iso/IsoGridSquare;
    Lzombie/iso/IsoGridSquare;
    J
)Z
```
