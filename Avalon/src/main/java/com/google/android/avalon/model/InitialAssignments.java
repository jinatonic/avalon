package com.google.android.avalon.model;

import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;

import java.util.Collections;
import java.util.Set;

/**
 * Created by mikewallstedt on 5/13/14.
 */
public class InitialAssignments extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public final Set<RoleAssignment> assignments;
    public final PlayerInfo king;
    public final PlayerInfo lady;   // can be null

    public InitialAssignments(Set<RoleAssignment> assignments, PlayerInfo king, PlayerInfo lady) {
        this.assignments = Collections.unmodifiableSet(assignments);
        this.king = king;
        this.lady = lady;
    }
}
