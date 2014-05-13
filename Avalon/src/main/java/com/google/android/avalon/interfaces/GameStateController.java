package com.google.android.avalon.interfaces;

import com.google.android.avalon.model.RoleAssignment;

/**
 * Created by jinyan on 5/13/14.
 */
public interface GameStateController {
    public enum GameState { ROLE_SELECT, SETUP, INITIALIZED }

    public void onRoleSelected(boolean isServer);
    public void onSetupCompleted(RoleAssignment role);
}
