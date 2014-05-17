package com.google.android.avalon.controllers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.network.ServiceMessageProtocol;
import com.google.android.avalon.model.messages.ToBtMessageWrapper;

/**
 * Created by jinyan on 5/14/14.
 */
public abstract class GameStateController {
    protected static final String TAG = GameStateController.class.getSimpleName();

    private static final boolean FORCE_SHOW_TOAST_FOR_DEBUGGING = true;

    protected Context mContext;
    private boolean isForeground;

    /**
     * @return true on success (valid message), false otherwise
     */
    public abstract boolean processAvalonMessage(AvalonMessage msg);

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
        Log.i(TAG, "Sending " + data);
        // send intent to service
        Intent i = new Intent(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        i.putExtra(ServiceMessageProtocol.DATA_WRAPPER_ARRAY_KEY, data);
        mContext.sendBroadcast(i);
    }

    public void isForeground(boolean foreground) {
        isForeground = foreground;
    }

    /**
     * Helper function to show a warning toast (if we are in the foreground) if we received an
     * unexpected or out-of-state message.
     */
    protected boolean showWarningToast(AvalonMessage msg) {
        if (isForeground || FORCE_SHOW_TOAST_FOR_DEBUGGING) {
            Toast.makeText(mContext, "Received unexpected " + msg, Toast.LENGTH_SHORT).show();
        }
        // the return value is just for AvalonMessageListener#processAvalonMessage
        return false;
    }
}
