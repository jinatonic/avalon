package com.google.android.avalon.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by mikewallstedt on 5/13/14.
 */
public class InitialAssignments implements AvalonMessage {
    private static final long serialVersionUID = 1L;

    public final Set<RoleAssignment> assignments;
    public final PlayerName king;
    public final PlayerName lady;

    public InitialAssignments(Set<RoleAssignment> assignments, PlayerName king, PlayerName lady) {
        this.assignments = Collections.unmodifiableSet(assignments);
        this.king = king;
        this.lady = lady;
    }
}
