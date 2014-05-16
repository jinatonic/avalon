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

    private BaseFromBtMessageReceiver mReceiver;

    private RoleAssignment mRoleAssignment;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.detach(getActivity());
    }

    public void update() {
        ClientGameState gameState = mClientGameStateController.getCurrentGameState();
        // TODO
    }

    private void setRoleAssignment(RoleAssignment mRoleAssignment) {
        this.mRoleAssignment = mRoleAssignment;
        if (mRoleAssignment != null) {
            showRoleAssignment();
        }
    }

    private void showRoleAssignment() {
        mRoleAssignmentText.setText(mRoleAssignment.role.name());

        if (mRoleAssignment.seenPlayers.size() == 0) {
            mSeenPlayersLabel.setVisibility(View.INVISIBLE);
        } else {
            StringBuilder seenPlayers = new StringBuilder();
            int i = 0;
            for (PlayerInfo player: mRoleAssignment.seenPlayers) {
                seenPlayers.append(player.name);
                if (i++ < mRoleAssignment.seenPlayers.size() - 1) {
                    seenPlayers.append(", ");
                }
            }
            mSeenPlayersText.setText(seenPlayers);
        }
    }

//    private class MessageReceiver extends BaseFromBtMessageReceiver {
//
//        public MessageReceiver() {
//            super(ClientFragment.this);
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.hasExtra(ServiceMessageProtocol.AVALON_MESSAGE_KEY)) {
//                AvalonMessage msg = (AvalonMessage) intent.getSerializableExtra(
//                        ServiceMessageProtocol.AVALON_MESSAGE_KEY);
//                handleMessage(msg);
//            }
//            super.onReceive(context, intent);
//        }
//    }
}
