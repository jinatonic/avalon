package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.controllers.ClientGameStateController;
import com.google.android.avalon.interfaces.UiChangedListener;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.messages.ClientSetupDoneMessage;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.utils.AnimationUtils;

import java.util.HashSet;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupClientFragment extends Fragment {

    private static final String SHOW_KEY = "show_key";
    private static final long TIME_DELAY_FOR_ANIM = 7000;    // ms

    private ClientGameStateController mClientGameStateController;
    private UiChangedListener mUiChangedListener;

    // UI state variables and pointers
    private TextView mStatusTextView;
    private View mTeaserContainer;
    private boolean mShowStatus = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mClientGameStateController = ClientGameStateController.get(getActivity());
        mUiChangedListener = (UiChangedListener) getActivity();

        View v = inflater.inflate(R.layout.client_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.client_status_text);
        mTeaserContainer = v.findViewById(R.id.client_setup_teaser_container);

        if (savedInstanceState != null) {
            mShowStatus = savedInstanceState.getBoolean(SHOW_KEY);
        }
        show(mShowStatus);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_KEY, mShowStatus);
    }

    /**
     * Update the UI based on the current game state.
     */
    public void update() {
        ClientGameState gameState = mClientGameStateController.getCurrentGameState();
        mShowStatus = gameState.player != null;     // we are connected if we sent out PlayerInfo
        show(mShowStatus);

        // If we have role assignment, we should show it to the user
        if (gameState.assignment != null) {
            runAnimation(gameState.assignment);
        }
    }

    private void runAnimation(RoleAssignment assignment) {
        Handler handler = new Handler();

        mTeaserContainer.setVisibility(View.VISIBLE);
        final View instruction = mTeaserContainer.findViewById(
                R.id.client_setup_teaser_instruction_text);
        final ImageView image = (ImageView) mTeaserContainer.findViewById(
                R.id.client_setup_teaser_image);
        final TextView other = (TextView) mTeaserContainer.findViewById(
                R.id.client_setup_teaser_other_info);

        // TODO: get actual images
        image.setImageResource(R.drawable.ic_launcher);
        // TODO: actually format this
        other.setText("You see " + assignment.seenPlayers);

        // First, we show the INSTRUCTIONS (see R.string.assignment_instructions)
        AnimationUtils.fadeIn(instruction);

        // Then, we show the image of your role
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimationUtils.fadeOut(instruction);
                AnimationUtils.fadeIn(image);
            }
        }, TIME_DELAY_FOR_ANIM);

        // Next, we show the other players that we know of
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                AnimationUtils.fadeOut(image);
                AnimationUtils.fadeIn(other);
            }
        }, TIME_DELAY_FOR_ANIM * 2);

        // Finally, we notify the controller that we can start the game
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                AnimationUtils.fadeOut(other);
                mClientGameStateController.processAvalonMessage(new ClientSetupDoneMessage());

                // We have to manually notify the activity
                mUiChangedListener.notifyDataChanged();
            }
        }, TIME_DELAY_FOR_ANIM * 3);
    }

    private void show(final boolean discovered) {
        mStatusTextView.setText(discovered ? "Connected!" : "Searching for server...");
    }
}
