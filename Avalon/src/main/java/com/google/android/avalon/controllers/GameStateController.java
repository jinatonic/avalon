package com.google.android.avalon.controllers;

import android.content.Context;
import android.content.Intent;

import com.google.android.avalon.interfaces.AvalonMessageListener;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.PlayerInfo;
import com.google.android.avalon.network.ServiceMessageProtocol;

/**
 * Created by jinyan on 5/14/14.
 */
public abstract class GameStateController implements AvalonMessageListener {

    protected Context mContext;

    protected boolean mStarted;

    /**
     * Create an intent for the BluetoothService to send the message to the appropriate player.
     */
    public void sendAvalonMessage(PlayerInfo info, AvalonMessage msg) {
        Intent i = new Intent(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        i.putExtra(ServiceMessageProtocol.PLAYER_INFO_KEY, info);
        i.putExtra(ServiceMessageProtocol.AVALON_MESSAGE_KEY, msg);
        mContext.sendBroadcast(i);
    }

    public boolean started() {
        return mStarted;
    }
}
