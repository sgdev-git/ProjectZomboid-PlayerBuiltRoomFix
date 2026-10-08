package zombie.iso.objects;

import zombie.characters.IsoPlayer;

public final class IsoTree {
    private boolean isPlayerInsideARoom(IsoPlayer player) {
        return true;
    }

    public boolean invokeIsPlayerInsideARoom(IsoPlayer player) {
        return isPlayerInsideARoom(player);
    }
}
