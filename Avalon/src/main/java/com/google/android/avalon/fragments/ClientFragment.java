package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.controllers.ClientGameStateController;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.network.BaseFromBtMessageReceiver;

/**
 * Created by jinyan on 5/12/14.
 */
public class ClientFragment extends Fragment {
    public static final String EXTRA_INITIAL_STATE = "extra_initial_state";

    private ClientGameStateController mClientGameStateController;

    private TextView mRoleAssignmentText;
    private TextView mSeenPlayersLabel;
    private TextView mSeenPlayersText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mClientGameStateController = ClientGameStateController.get(getActivity());

        View v = inflater.inflate(R.layout.client_fragment, parent, false);

        mRoleAssignmentText = (TextView) v.findViewById(R.id.role_assignment_text);
        mSeenPlayersLabel = (TextView) v.findViewById(R.id.seen_players_label);
        mSeenPlayersText = (TextView) v.findViewById(R.id.seen_players_text);

        update();

        return v;
    }

    public void update() {
        ClientGameState gameState = mClientGameStateController.getCurrentGameState();
        // TODO
    }

    private void showRoleAssignment(RoleAssignment assignment) {
        mRoleAssignmentText.setText(assignment.role.name());

        if (assignment.seenPlayers.size() == 0) {
            mSeenPlayersLabel.setVisibility(View.INVISIBLE);
        } else {
            StringBuilder seenPlayers = new StringBuilder();
            int i = 0;
            for (PlayerInfo player: assignment.seenPlayers) {
                seenPlayers.append(player.name);
                if (i++ < assignment.seenPlayers.size() - 1) {
                    seenPlayers.append(", ");
                }
            }
            mSeenPlayersText.setText(seenPlayers);
        }
    }
}
