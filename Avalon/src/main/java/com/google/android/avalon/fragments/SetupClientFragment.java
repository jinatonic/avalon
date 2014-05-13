package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.interfaces.ConnectionListener;
import com.google.android.avalon.interfaces.GameStateController;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.RoleAssignment;
import com.google.android.avalon.network.BaseFromBtMessageReceiver;
import com.google.android.avalon.network.BluetoothClientService;
import com.google.android.avalon.network.ServiceMessageProtocol;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupClientFragment extends Fragment implements ConnectionListener {

    private static final String SHOW_KEY = "show_key";

    private TextView mStatusTextView;
    private boolean mShowStatus = false;
    private BaseFromBtMessageReceiver mReceiver;

    private GameStateController mController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.client_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.client_status_text);

        mController = (GameStateController) getActivity();

        if (savedInstanceState != null) {
            mShowStatus = savedInstanceState.getBoolean(SHOW_KEY);
        }
        show(mShowStatus);

        // Attach broadcast listeners
        mReceiver = new MessageReceiver();
        mReceiver.attach(getActivity());

        // Start the bt client service
        getActivity().startService(new Intent(getActivity(), BluetoothClientService.class));

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.detach(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_KEY, mShowStatus);
    }

    public void show(final boolean discovered) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mStatusTextView.setText(discovered ?
                        "Connected!" : "Searching for server...");
            }
        });
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        show(connected);
    }

    private class MessageReceiver extends BaseFromBtMessageReceiver {

        public MessageReceiver() {
            super(SetupClientFragment.this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(ServiceMessageProtocol.AVALON_MESSAGE_KEY)) {
                AvalonMessage msg = (AvalonMessage) intent.getSerializableExtra(
                        ServiceMessageProtocol.AVALON_MESSAGE_KEY);
                if (msg instanceof RoleAssignment) {
                    mController.onSetupCompleted((RoleAssignment) msg);
                }
            }
            super.onReceive(context, intent);
        }
    }
}
