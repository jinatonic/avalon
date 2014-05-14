package com.google.android.avalon.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class RoleAssignment implements AvalonMessage {
    private static final long serialVersionUID = 1L;

    public final PlayerName player;
    public final AvalonRole role;
    public final Set<PlayerName> seenPlayers;

    public RoleAssignment(PlayerName player, AvalonRole role, Set<PlayerName> seenPlayers) {
        this.player = player;
        this.role = role;
        this.seenPlayers = Collections.unmodifiableSet(seenPlayers);
    }
}
