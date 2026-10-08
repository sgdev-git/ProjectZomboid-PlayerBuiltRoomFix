package zombie.iso;

import zombie.iso.areas.IsoRoom;
import zombie.iso.areas.isoregion.regions.IWorldRegion;

public class IsoGridSquare {
    private IsoRoom room;
    private IWorldRegion region;
    private IsoGridSquare north;
    private IsoGridSquare west;

    public IsoGridSquare() {
    }

    public IsoGridSquare(IsoRoom room, IWorldRegion region) {
        this.room = room;
        this.region = region;
    }

    public void setAdjacentSquare(IsoDirections direction, IsoGridSquare square) {
        if (direction == IsoDirections.N) {
            north = square;
        } else if (direction == IsoDirections.W) {
            west = square;
        }
    }

    public IsoRoom getRoom() {
        return room;
    }

    public IWorldRegion getIsoWorldRegion() {
        return region;
    }

    public IsoGridSquare getAdjacentSquare(IsoDirections direction) {
        return direction == IsoDirections.N ? north : west;
    }
}
