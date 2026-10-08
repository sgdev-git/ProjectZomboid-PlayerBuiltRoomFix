package io.shaun.playerbuiltroomfix;

import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.areas.IsoRoom;
import zombie.iso.areas.isoregion.regions.IWorldRegion;
import zombie.iso.fboRenderChunk.FBORenderCutaways;

public final class PlayerBuiltExtensionCutawayPatchTest {
    private PlayerBuiltExtensionCutawayPatchTest() {
    }

    public static void main(String[] args) {
        StubRegion affected = new StubRegion(true, true);
        StubRegion outside = new StubRegion(false, false);
        StubRegion otherAffected = new StubRegion(true, true);

        FBORenderCutaways.CutawayWall horizontal = new FBORenderCutaways.CutawayWall();
        horizontal.y1 = 10;
        horizontal.y2 = 10;

        StubSquare affectedSide = new StubSquare(null, affected);
        StubSquare outsideSide = new StubSquare(null, outside);
        affectedSide.north = outsideSide;

        assertResult(
            true,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                false, horizontal, affectedSide, new StubSquare(null, affected)
            ),
            "inside affected region cuts the wall away"
        );
        assertResult(
            false,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                true, horizontal, affectedSide, new StubSquare(null, outside)
            ),
            "outside affected region keeps the wall rendered"
        );

        StubSquare forwardOwnedSide = new StubSquare(null, outside);
        forwardOwnedSide.north = new StubSquare(null, outside);
        forwardOwnedSide.south = new StubSquare(null, affected);
        assertResult(
            false,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                true, horizontal, forwardOwnedSide, new StubSquare(null, outside)
            ),
            "south-aligned affected region keeps the wall rendered while outside"
        );
        assertResult(
            true,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                false, horizontal, forwardOwnedSide, new StubSquare(null, affected)
            ),
            "south-aligned affected region cuts the wall away while inside"
        );

        FBORenderCutaways.CutawayWall vertical = new FBORenderCutaways.CutawayWall();
        vertical.x1 = 10;
        vertical.x2 = 10;
        vertical.y1 = 10;
        vertical.y2 = 15;
        StubSquare eastOwnedSide = new StubSquare(null, outside);
        eastOwnedSide.west = new StubSquare(null, outside);
        eastOwnedSide.east = new StubSquare(null, affected);
        assertResult(
            false,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                true, vertical, eastOwnedSide, new StubSquare(null, outside)
            ),
            "east-aligned affected region keeps the wall rendered while outside"
        );

        StubSquare normalRoomSide = new StubSquare(new IsoRoom(), affected);
        normalRoomSide.north = outsideSide;
        assertResult(
            true,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                true, horizontal, normalRoomSide, new StubSquare(null, outside)
            ),
            "normal IsoRoom preserves vanilla"
        );

        StubSquare unroofedSide = new StubSquare(null, new StubRegion(true, false));
        unroofedSide.north = outsideSide;
        assertResult(
            false,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                false, horizontal, unroofedSide, new StubSquare(null, affected)
            ),
            "unroofed region preserves vanilla"
        );

        StubSquare ambiguousSide = new StubSquare(null, affected);
        ambiguousSide.north = new StubSquare(null, otherAffected);
        assertResult(
            true,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(
                true, horizontal, ambiguousSide, new StubSquare(null, affected)
            ),
            "wall between affected regions preserves vanilla"
        );

        assertResult(
            true,
            PlayerBuiltExtensionCutawayPatch.resolveCutaway(true, null, affectedSide, null),
            "missing context preserves vanilla"
        );

        System.out.println("PlayerBuiltExtensionCutawayPatchTest: PASS");
    }

    private static void assertResult(boolean expected, boolean actual, String description) {
        if (expected != actual) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class StubSquare extends IsoGridSquare {
        private final IsoRoom room;
        private final IWorldRegion region;
        private IsoGridSquare north;
        private IsoGridSquare south;
        private IsoGridSquare east;
        private IsoGridSquare west;

        private StubSquare(IsoRoom room, IWorldRegion region) {
            this.room = room;
            this.region = region;
        }

        @Override
        public IsoRoom getRoom() {
            return room;
        }

        @Override
        public IWorldRegion getIsoWorldRegion() {
            return region;
        }

        @Override
        public IsoGridSquare getAdjacentSquare(IsoDirections direction) {
            if (direction == IsoDirections.N) {
                return north;
            }
            if (direction == IsoDirections.S) {
                return south;
            }
            if (direction == IsoDirections.E) {
                return east;
            }
            return west;
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
