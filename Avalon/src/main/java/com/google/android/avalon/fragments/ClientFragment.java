package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.avalon.AvalonActivity;
import com.google.android.R;
import com.google.android.avalon.interfaces.ConnectionListener;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.RoleAssignment;
import com.google.android.avalon.network.BaseFromBtMessageReceiver;
import com.google.android.avalon.network.ServiceMessageProtocol;

/**
 * Created by jinyan on 5/12/14.
 */
public class ClientFragment extends Fragment implements ConnectionListener {

    public static final String EXTRA_ROLE_ASSIGNMENT = "extra_role_assignment";

    private TextView mRoleAssignmentText;
    private TextView mSeenPlayersLabel;
    private TextView mSeenPlayersText;

    private BaseFromBtMessageReceiver mReceiver;

    private RoleAssignment mRoleAssignment;

    public static ClientFragment newInstance(RoleAssignment assignment) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_ROLE_ASSIGNMENT, assignment);

        ClientFragment fragment = new ClientFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.client_fragment, parent, false);

        mRoleAssignmentText = (TextView) v.findViewById(R.id.role_assignment_text);
        mSeenPlayersLabel = (TextView) v.findViewById(R.id.seen_players_label);
        mSeenPlayersText = (TextView) v.findViewById(R.id.seen_players_text);

        setRoleAssignment((RoleAssignment) getArguments().getSerializable(EXTRA_ROLE_ASSIGNMENT));

        mReceiver = new MessageReceiver();
        mReceiver.attach(getActivity());

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.detach(getActivity());
    }

    /**
     * Helper callback to perform action for each type of AvalonMessage
     */
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

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        // TODO: probably should have a connection status text somewhere in this fragment
    }

    private class MessageReceiver extends BaseFromBtMessageReceiver {

        public MessageReceiver() {
            super(ClientFragment.this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(ServiceMessageProtocol.AVALON_MESSAGE_KEY)) {
                AvalonMessage msg = (AvalonMessage) intent.getSerializableExtra(
                        ServiceMessageProtocol.AVALON_MESSAGE_KEY);
                handleMessage(msg);
            }
            super.onReceive(context, intent);
        }
    }
}
