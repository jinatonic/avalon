package com.google.android.avalon.model;

import com.google.android.avalon.model.messages.AvalonMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mikewallstedt on 5/13/14.
 */
public class GameConfiguration extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public int numPlayers;
    public boolean enableLadyOfTheLake;
    public Set<AvalonRole> specialRoles = new HashSet<AvalonRole>();
}
