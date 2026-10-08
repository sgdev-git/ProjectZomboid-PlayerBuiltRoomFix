package io.shaun.playerbuiltroomfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoGridSquare;

/**
 * Prevents the Build 42.21.0 XL-tree fade path from dereferencing a missing
 * IsoRoom for an enclosed player-built region.
 */
@Patch(
    className = "zombie.iso.objects.IsoTree",
    methodName = "isPlayerInsideARoom",
    warmUp = true,
    strictMatch = true
)
public final class IsoTreeRoomGuardPatch {
    private IsoTreeRoomGuardPatch() {
    }

    @Patch.OnEnter(skipOn = true)
    public static boolean guardMissingRoom(@Patch.Argument(0) IsoPlayer player) {
        if (player == null || !player.isInARoom()) {
            return false;
        }

        IsoGridSquare square = player.getSquare();
        return square != null && square.getRoom() == null;
    }
}
