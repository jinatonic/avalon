package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.controllers.ClientGameStateController;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.messages.ClientSetupDoneMessage;
import com.google.android.avalon.model.messages.RoleAssignment;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupClientFragment extends Fragment {

    private static final String SHOW_KEY = "show_key";
    private static final int TIME_DELAY_FOR_ANIM = 5000;    // ms

    private ClientGameStateController mClientGameStateController;

    // UI state variables and pointers
    private TextView mStatusTextView;
    private boolean mShowStatus = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mClientGameStateController = ClientGameStateController.get(getActivity());

        View v = inflater.inflate(R.layout.client_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.client_status_text);

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

        // First, we show the INSTRUCTIONS
        handler.post(new Runnable() {
            @Override public void run() {
                // TODO (jin)
            }
        });

        // Then, we show the image of your role
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO (jin) download the images
            }
        }, TIME_DELAY_FOR_ANIM);

        // Next, we show the other players that we know of
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                // TODO (jin)
            }
        }, TIME_DELAY_FOR_ANIM);

        // Finally, we notify the controller that we can start the game
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                mClientGameStateController.processAvalonMessage(new ClientSetupDoneMessage());
            }
        }, TIME_DELAY_FOR_ANIM);
    }

    private void show(final boolean discovered) {
        mStatusTextView.setText(discovered ? "Connected!" : "Searching for server...");
    }
}
