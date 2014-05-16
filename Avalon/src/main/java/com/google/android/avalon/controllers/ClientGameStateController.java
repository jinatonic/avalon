package com.google.android.avalon.controllers;

import android.content.Context;
import android.util.Log;

import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.messages.RoleAssignment;

/**
 * Created by jinyan on 5/14/14.
 */
public class ClientGameStateController extends GameStateController {

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

    public ClientGameState getCurrentGameState() {
        return new ClientGameState();
    }

    @Override
    public boolean processAvalonMessage(AvalonMessage msg) {
        // TODO: finish
        if (msg instanceof RoleAssignment) {
            mStarted = true;
            Log.i(TAG, "Game starting, role: " + msg);
        }

        return true;
    }
}
