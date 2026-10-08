package zombie.iso;

public final class IsoCamera {
    public static final FrameState frameState = new FrameState();

    private IsoCamera() {
    }

    public static final class FrameState {
        public IsoGridSquare camCharacterSquare;
    }
}
