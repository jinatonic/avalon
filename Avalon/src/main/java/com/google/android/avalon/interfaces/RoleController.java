package com.google.android.avalon.interfaces;

/**
 * Created by jinyan on 5/12/14.
 */
public interface RoleController {

    public enum Role { SERVER, CLIENT }

    /**
     * Prompts the controller that the user has chosen a role and should now show the role UI.
     */
    public void onRoleSelected(Role role);
}
