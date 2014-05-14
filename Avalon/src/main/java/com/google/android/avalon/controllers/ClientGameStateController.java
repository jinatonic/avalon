package com.google.android.avalon.controllers;

import android.content.Context;

import com.google.android.avalon.interfaces.AvalonMessageListener;
import com.google.android.avalon.model.AvalonMessage;

/**
 * Created by jinyan on 5/14/14.
 */
public class ClientGameStateController implements AvalonMessageListener {

    private Context mContext;

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

    @Override
    public void onAvalonMessageReceived(AvalonMessage msg) {

    }
}
