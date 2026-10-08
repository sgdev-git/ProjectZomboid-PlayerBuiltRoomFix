package io.shaun.playerbuiltroomfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.iso.IsoCamera;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.areas.isoregion.regions.IWorldRegion;
import zombie.iso.fboRenderChunk.FBORenderCutaways;

/**
 * Supplies a render-only room association for enclosed player-built regions
 * that Build 42.21.0 did not convert into an IsoRoom because they adjoin a
 * predefined building.
 *
 * The patch never changes a square, room ID, building, region, or save. It
 * only corrects the final wall-cutaway decision for a wall bordering one of
 * the affected regions:
 *
 * - the wall cuts away when the camera player is in that region;
 * - the wall remains rendered when the camera player is outside it.
 */
@Patch(
    className = "zombie.iso.fboRenderChunk.FBORenderCutaways",
    methodName = "IsCutawaySquare",
    warmUp = true,
    strictMatch = true
)
public final class PlayerBuiltExtensionCutawayPatch {
    private static volatile boolean loggedRenderFailure;

    private PlayerBuiltExtensionCutawayPatch() {
    }

    @Patch.OnExit
    public static void correctNullRoomRegionCutaway(
        @Patch.Argument(0) FBORenderCutaways.CutawayWall wall,
        @Patch.Argument(2) IsoGridSquare wallSquare,
        @Patch.Return(readOnly = false) boolean result
    ) {
        try {
            IsoGridSquare playerSquare = IsoCamera.frameState == null
                ? null
                : IsoCamera.frameState.camCharacterSquare;
            result = resolveCutaway(result, wall, wallSquare, playerSquare);
        } catch (Throwable failure) {
            // Rendering must fail open to the unmodified vanilla result.
            logRenderFailureOnce(failure);
        }
    }

    // Public because ZombieBuddy inlines the advice into FBORenderCutaways;
    // the transformed game class must be able to invoke this helper.
    public static boolean resolveCutaway(
        boolean vanillaResult,
        FBORenderCutaways.CutawayWall wall,
        IsoGridSquare wallSquare,
        IsoGridSquare playerSquare
    ) {
        if (wall == null || wallSquare == null || playerSquare == null) {
            return vanillaResult;
        }

        IWorldRegion affectedRegion = findAffectedBoundaryRegion(wall, wallSquare);
        if (affectedRegion == null) {
            return vanillaResult;
        }

        IWorldRegion playerRegion = playerSquare.getIsoWorldRegion();
        return playerRegion == affectedRegion;
    }

    public static void logRenderFailureOnce(Throwable failure) {
        if (loggedRenderFailure) {
            return;
        }
        loggedRenderFailure = true;
        System.err.println(
            "[PlayerBuiltRoomFix] Extension cutaway guard disabled after an unexpected error: "
                + failure
        );
    }

    private static IWorldRegion findAffectedBoundaryRegion(
        FBORenderCutaways.CutawayWall wall,
        IsoGridSquare wallSquare
    ) {
        boolean horizontal = wall.y1 == wall.y2;
        IsoGridSquare oppositeSquare = wallSquare.getAdjacentSquare(
            horizontal ? IsoDirections.N : IsoDirections.W
        );

        IWorldRegion nearRegion = getAffectedRegion(wallSquare);
        IWorldRegion farRegion = getAffectedRegion(oppositeSquare);

        // Both geometric sides resolving to a player region means this is an
        // internal or otherwise ambiguous wall. Leave vanilla in control.
        if (nearRegion != null && farRegion != null) {
            return null;
        }

        if (nearRegion != null) {
            return nearRegion;
        }
        if (farRegion != null) {
            return farRegion;
        }

        // CutawayWall coordinates can be aligned one tile before the owning
        // square for the opposite camera-facing side of a wall run. Probe only
        // that orientation-specific forward tile, and only when neither of the
        // primary squares belongs to a normal IsoRoom.
        if (hasIsoRoom(wallSquare) || hasIsoRoom(oppositeSquare)) {
            return null;
        }

        IsoGridSquare forwardSquare = wallSquare.getAdjacentSquare(
            horizontal ? IsoDirections.S : IsoDirections.E
        );
        return getAffectedRegion(forwardSquare);
    }

    private static boolean hasIsoRoom(IsoGridSquare square) {
        return square != null && square.getRoom() != null;
    }

    private static IWorldRegion getAffectedRegion(IsoGridSquare square) {
        if (square == null || square.getRoom() != null) {
            return null;
        }

        IWorldRegion region = square.getIsoWorldRegion();
        if (region == null || !region.isPlayerRoom() || !region.isFullyRoofed()) {
            return null;
        }

        return region;
    }
}
