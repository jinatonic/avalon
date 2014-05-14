package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.R;
import com.google.android.avalon.interfaces.RoleSelectorCallback;
import com.google.android.avalon.model.PlayerInfo;

import java.util.UUID;

public class RoleSelectionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.role_selection_fragment, parent, false);

        final RoleSelectorCallback callback = (RoleSelectorCallback) getActivity();

        // This fragment literally just selects server or client role
        v.findViewById(R.id.server_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onRoleSelected(true /* isServer */);
            }
        });
        v.findViewById(R.id.client_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onRoleSelected(false /* isServer */);
            }
        });

        return v;
    }
}
