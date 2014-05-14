package com.google.android.avalon.controllers;

import android.content.Context;

import com.google.android.avalon.interfaces.AvalonMessageListener;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.RoleAssignment;

/**
 * Created by jinyan on 5/14/14.
 */
public class ClientGameStateController implements AvalonMessageListener {

    private Context mContext;
    private boolean mStarted;

    // private for singleton
    private static ClientGameStateController sClientGameStateController;
    private ClientGameStateController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static ClientGameStateController get(Context context) {
        if (sClientGameStateController == null) {
            sClientGameStateController = new ClientGameStateController(context);
        }
        return sClientGameStateController;
    }

    public boolean started() {
        return mStarted;
    }

    public ClientGameState getCurrentGameState() {
        return new ClientGameState();
    }

    @Override
    public void onAvalonMessageReceived(AvalonMessage msg) {
        // TODO: finish
        if (msg instanceof RoleAssignment) {
            mStarted = true;
        }
    }
}
