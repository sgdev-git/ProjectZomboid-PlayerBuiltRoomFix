package io.shaun.playerbuiltroomfix;

import zombie.characters.IsoPlayer;
import zombie.iso.IsoCamera;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.areas.IsoRoom;
import zombie.iso.areas.isoregion.regions.IWorldRegion;
import zombie.iso.fboRenderChunk.FBORenderCutaways;
import zombie.iso.objects.IsoTree;

public final class ZombieBuddyIntegrationTest {
    private ZombieBuddyIntegrationTest() {
    }

    public static void main(String[] args) {
        StubRegion affected = new StubRegion(true, true);
        StubRegion outside = new StubRegion(false, false);

        IsoGridSquare affectedSide = new IsoGridSquare(null, affected);
        affectedSide.setAdjacentSquare(IsoDirections.N, new IsoGridSquare(null, outside));

        FBORenderCutaways.CutawayWall horizontal = new FBORenderCutaways.CutawayWall();
        horizontal.y1 = 10;
        horizontal.y2 = 10;

        FBORenderCutaways renderer = new FBORenderCutaways();

        IsoCamera.frameState.camCharacterSquare = new IsoGridSquare(null, affected);
        assertResult(
            true,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                false, horizontal, affectedSide, IsoCamera.frameState.camCharacterSquare
            ),
            "direct region resolver returns true while inside"
        );
        assertResult(
            true,
            renderer.invokeIsCutawaySquare(horizontal, affectedSide, affectedSide, false),
            "ZombieBuddy writes true to the cutaway return while inside"
        );

        IsoCamera.frameState.camCharacterSquare = new IsoGridSquare(null, outside);
        assertResult(
            false,
            renderer.invokeIsCutawaySquare(horizontal, affectedSide, affectedSide, true),
            "ZombieBuddy writes false to the cutaway return while outside"
        );

        IsoTree tree = new IsoTree();
        IsoPlayer nullRoomPlayer = new IsoPlayer(true, new IsoGridSquare(null, affected));
        assertResult(
            false,
            tree.invokeIsPlayerInsideARoom(nullRoomPlayer),
            "ZombieBuddy skips the IsoTree method for a null room"
        );
        assertResult(
            true,
            tree.invokeIsPlayerInsideARoom(
                new IsoPlayer(true, new IsoGridSquare(new IsoRoom(), affected))
            ),
            "ZombieBuddy preserves vanilla when an IsoRoom exists"
        );
        assertResult(
            true,
            tree.invokeIsPlayerInsideARoom(new IsoPlayer(false, new IsoGridSquare(null, outside))),
            "ZombieBuddy preserves vanilla while the player is outside"
        );

        System.out.println("ZombieBuddyIntegrationTest: PASS");
    }

    private static void assertResult(boolean expected, boolean actual, String description) {
        if (expected != actual) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class StubRegion implements IWorldRegion {
        private final boolean playerRoom;
        private final boolean fullyRoofed;

        private StubRegion(boolean playerRoom, boolean fullyRoofed) {
            this.playerRoom = playerRoom;
            this.fullyRoofed = fullyRoofed;
        }

        @Override
        public boolean isPlayerRoom() {
            return playerRoom;
        }

        @Override
        public boolean isFullyRoofed() {
            return fullyRoofed;
        }
    }
}
