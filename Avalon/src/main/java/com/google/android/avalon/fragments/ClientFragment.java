package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.controllers.ClientGameStateController;
import com.google.android.avalon.interfaces.UiChangedListener;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.network.BaseFromBtMessageReceiver;

import java.util.Arrays;

/**
 * Created by jinyan on 5/12/14.
 */
public class ClientFragment extends Fragment {
    private ClientGameStateController mClientGameStateController;
    private UiChangedListener mUiChangedListener;

    private TextView mRoleAssignmentText;
    private TextView mSeenPlayersLabel;
    private TextView mSeenPlayersText;
    private View mShowDetailsView;

    private TextView mStatusText;
    private TextView mOtherInfoText;
    private ImageView mNoButton;
    private ImageView mYesButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mClientGameStateController = ClientGameStateController.get(getActivity());
        mUiChangedListener = (UiChangedListener) getActivity();

        View v = inflater.inflate(R.layout.client_fragment, parent, false);

        mRoleAssignmentText = (TextView) v.findViewById(R.id.role_assignment_text);
        mSeenPlayersLabel = (TextView) v.findViewById(R.id.seen_players_label);
        mSeenPlayersText = (TextView) v.findViewById(R.id.seen_players_text);
        mShowDetailsView = v.findViewById(R.id.client_sensitive_data_wrapper);

        mStatusText = (TextView) v.findViewById(R.id.client_fragment_status_text);
        mOtherInfoText = (TextView) v.findViewById(R.id.client_fragment_other_info);
        mNoButton = (ImageView) v.findViewById(R.id.client_button_no);
        mYesButton = (ImageView) v.findViewById(R.id.client_button_yes);

        viewSetup();

        update();

        return v;
    }

    private void viewSetup() {
        setRoleAssignmentText(mClientGameStateController.getCurrentGameState().assignment);
        mShowDetailsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mShowDetailsView.setAlpha(0f);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mShowDetailsView.setAlpha(1f);
                }
                return true;
            }
        });

        mNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClientGameStateController.processClientResponse(false);
                mUiChangedListener.notifyDataChanged();
            }
        });

        mYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClientGameStateController.processClientResponse(true);
                mUiChangedListener.notifyDataChanged();
            }
        });
    }

    public void update() {
        ClientGameState gameState = mClientGameStateController.getCurrentGameState();
        if (gameState.proposal != null) {
            mStatusText.setText(getString(R.string.client_vote_for_proposal));
            mYesButton.setVisibility(View.VISIBLE);
            mNoButton.setVisibility(View.VISIBLE);
            // TODO: format this
            mOtherInfoText.setText(Arrays.toString(gameState.proposal.questMembers));
        } else if (gameState.execution != null) {
            mOtherInfoText.setText(null);
            mStatusText.setText(getString(R.string.client_vote_for_exec));
            mYesButton.setVisibility(View.VISIBLE);
            mNoButton.setVisibility(View.VISIBLE);
        } else {
            mOtherInfoText.setText(null);
            mStatusText.setText(getString(R.string.client_default_status));
            mYesButton.setVisibility(View.INVISIBLE);
            mNoButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setRoleAssignmentText(RoleAssignment assignment) {
        if (assignment == null) return;

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
