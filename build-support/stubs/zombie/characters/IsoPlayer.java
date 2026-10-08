package zombie.characters;

import zombie.iso.IsoGridSquare;

public class IsoPlayer {
    private boolean inRoom;
    private IsoGridSquare square;

    public IsoPlayer() {
    }

    public IsoPlayer(boolean inRoom, IsoGridSquare square) {
        this.inRoom = inRoom;
        this.square = square;
    }

    public boolean isInARoom() {
        return inRoom;
    }

    public IsoGridSquare getSquare() {
        return square;
    }
}
