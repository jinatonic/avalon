package com.google.android.avalon.model;

import java.util.HashSet;
import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class RoleAssignment implements AvalonMessage {
    public AvalonRole role;
    public Set<String> seenPlayers = new HashSet<String>();
}
