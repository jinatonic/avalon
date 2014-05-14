package com.google.android.avalon.network;

/**
 * Created by jinyan on 5/13/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.android.avalon.interfaces.ConnectionListener;
import com.google.android.avalon.model.PlayerInfo;

/**
 * Base broadcast receiver class that handles connection updates.
 * Extend this class to cover more updates that we care about.
 */
public class BaseFromBtMessageReceiver extends BroadcastReceiver {

    private ConnectionListener mListener;

    public BaseFromBtMessageReceiver(ConnectionListener listener) {
        mListener = listener;
    }

    public void attach(Context context) {
        IntentFilter filter = new IntentFilter(ServiceMessageProtocol.FROM_BT_SERVICE_INTENT);
        context.registerReceiver(this, filter);
    }

    public void detach(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(ServiceMessageProtocol.CONNECTION_STATUS_KEY)) {
            boolean connected = intent.getBooleanExtra(
                    ServiceMessageProtocol.CONNECTION_STATUS_KEY, false);
            PlayerInfo info = (intent.hasExtra(ServiceMessageProtocol.CONNECTION_PLAYER_KEY)) ?
                    (PlayerInfo) intent.getSerializableExtra(
                            ServiceMessageProtocol.CONNECTION_PLAYER_KEY) : null;
            mListener.onConnectionStatusChanged(connected, info);
        }
    }
}