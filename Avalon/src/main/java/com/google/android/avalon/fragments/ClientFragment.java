package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.avalon.AvalonActivity;
import com.google.android.R;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.RoleAssignment;

/**
 * Created by jinyan on 5/12/14.
 */
public class ClientFragment extends Fragment {

    private TextView mRoleAssignmentText;
    private TextView mSeenPlayersLabel;
    private TextView mSeenPlayersText;

    private RoleAssignment mRoleAssignment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.client_fragment, parent, false);

        mRoleAssignmentText = (TextView) v.findViewById(R.id.role_assignment_text);
        mSeenPlayersLabel = (TextView) v.findViewById(R.id.seen_players_label);
        mSeenPlayersText = (TextView) v.findViewById(R.id.seen_players_text);

        return v;
    }

    public void handleMessage(AvalonMessage message) {
        if (message instanceof RoleAssignment) {
            setRoleAssignment((RoleAssignment) message);
        } else {
            Log.w(AvalonActivity.TAG,
                    "Ignoring unrecognized message of type: " + message.getClass());
        }
    }

    private void setRoleAssignment(RoleAssignment mRoleAssignment) {
        this.mRoleAssignment = mRoleAssignment;
        if (mRoleAssignment != null) {
            showRoleAssignment();
        }
    }

    private void showRoleAssignment() {
        mRoleAssignmentText.setText(mRoleAssignment.role.name());

        if (mRoleAssignment.seenPlayers.isEmpty()) {
            mSeenPlayersLabel.setVisibility(View.INVISIBLE);
        } else {
            StringBuilder seenPlayers = new StringBuilder();
            int i = 0;
            for (String player: mRoleAssignment.seenPlayers) {
                seenPlayers.append(player);
                if (i++ < mRoleAssignment.seenPlayers.size() - 1) {
                    seenPlayers.append(", ");
                }
            }
            mSeenPlayersText.setText(seenPlayers);
        }
    }
}
