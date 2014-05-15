package com.google.android.avalon.controllers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.avalon.interfaces.AvalonMessageListener;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.network.ServiceMessageProtocol;
import com.google.android.avalon.model.messages.ToBtMessageWrapper;

/**
 * Created by jinyan on 5/14/14.
 */
public abstract class GameStateController implements AvalonMessageListener {
    protected static final String TAG = GameStateController.class.getSimpleName();

    protected Context mContext;
    protected boolean mStarted;

    /**
     * Always use the bulk operation if you are sending more than one message. This is simply for
     * convenience and readability.
     */
    protected void sendSingleMessage(PlayerInfo dest, AvalonMessage msg) {
        ToBtMessageWrapper wrapper = new ToBtMessageWrapper();
        wrapper.add(dest, msg);
        sendBulkMessages(wrapper);
    }

    /**
     * Create an intent for the BluetoothService to send the message to the appropriate player.
     */
    protected void sendBulkMessages(ToBtMessageWrapper data) {
        Log.i(TAG, System.currentTimeMillis() + " Sending " + data);
        // send intent to service
        Intent i = new Intent(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        i.putExtra(ServiceMessageProtocol.DATA_WRAPPER_ARRAY_KEY, data);
        mContext.sendBroadcast(i);
    }

    public boolean started() {
        return mStarted;
    }
}
