package com.google.android.avalon.model.messages;

import com.google.android.avalon.model.AvalonRole;

import java.util.Collections;
import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class RoleAssignment extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public final PlayerInfo player;
    public final AvalonRole role;
    public final PlayerInfo[] seenPlayers;

    public RoleAssignment(PlayerInfo player, AvalonRole role, PlayerInfo[] seenPlayers) {
        this.player = player;
        this.role = role;
        this.seenPlayers = seenPlayers;
    }

    @Override
    public String toString() {
        return "[msg role for " + player + " - " + role + " - " + playerArrayToString(seenPlayers)
                + "]";
    }
}
