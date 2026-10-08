package zombie.iso.fboRenderChunk;

public final class FBORenderCutaways {
    public FBORenderCutaways() {
    }

    private boolean IsCutawaySquare(
        CutawayWall wall,
        zombie.iso.IsoGridSquare sourceSquare,
        zombie.iso.IsoGridSquare wallSquare,
        long vanillaResult
    ) {
        return vanillaResult != 0L;
    }

    public boolean invokeIsCutawaySquare(
        CutawayWall wall,
        zombie.iso.IsoGridSquare sourceSquare,
        zombie.iso.IsoGridSquare wallSquare,
        boolean vanillaResult
    ) {
        return IsCutawaySquare(wall, sourceSquare, wallSquare, vanillaResult ? 1L : 0L);
    }

    public static final class CutawayWall {
        public int x1;
        public int x2;
        public int y1;
        public int y2;
    }
}
