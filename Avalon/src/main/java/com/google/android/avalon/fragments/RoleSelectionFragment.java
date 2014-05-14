package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.R;
import com.google.android.avalon.interfaces.GameStateController;

public class RoleSelectionFragment extends Fragment {

    private GameStateController mController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.role_selection_fragment, parent, false);

        mController = (GameStateController) getActivity();

        // This fragment literally just selects server or client role
        v.findViewById(R.id.server_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.onRoleSelected(true /* isServer */);
            }
        });
        v.findViewById(R.id.client_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.onRoleSelected(false /* isServer */);
            }
        });

        return v;
    }
}
