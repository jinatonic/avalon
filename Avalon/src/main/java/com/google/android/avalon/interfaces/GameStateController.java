package com.google.android.avalon.interfaces;

import com.google.android.avalon.model.RoleAssignment;

/**
 * Created by jinyan on 5/13/14.
 */
public interface GameStateController {
    public void onRoleSelected(boolean isServer);
    public void onSetupCompleted(RoleAssignment role);
}
