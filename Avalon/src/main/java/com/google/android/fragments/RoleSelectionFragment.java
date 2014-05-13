package com.google.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.R;
import com.google.android.interfaces.RoleController;

public class RoleSelectionFragment extends Fragment {

    private RoleController mRoleController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.role_selection_fragment, parent, false);

        // This fragment literally just selects server or client role
        v.findViewById(R.id.server_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRoleController.onRoleSelected(RoleController.Role.SERVER);
            }
        });
        v.findViewById(R.id.client_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRoleController.onRoleSelected(RoleController.Role.CLIENT);
            }
        });

        return v;
    }

    /**
     * Must be called in order for the fragment to relay user selection back to the controller.
     * @param roleController the parent controller
     */
    public void setRoleController(RoleController roleController) {
        mRoleController = roleController;
    }
}
