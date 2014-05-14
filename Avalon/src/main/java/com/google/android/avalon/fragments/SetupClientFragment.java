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

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupClientFragment extends Fragment {

    private static final String SHOW_KEY = "show_key";

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
        // TODO
    }

    public void show(final boolean discovered) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mStatusTextView.setText(discovered ?
                        "Connected!" : "Searching for server...");
            }
        });
    }
}
