package com.google.android.avalon.model;

import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class InitialRoleAssignment implements AvalonMessage {
    public AvalonRoles role;
    public Set<String> otherInfo;
}
