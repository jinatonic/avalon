package com.google.android.avalon.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.AndroidTestCase;

import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.network.ServiceMessageProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinyan on 5/15/14.
 */
public class ServerGameStateControllerTest extends AndroidTestCase {

    ServerGameStateController mController;
    BroadcastReceiver mReceiver;

    // Note that mMessages should be cleared after every event
    Map<PlayerInfo, AvalonMessage> mMessages;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mController = ServerGameStateController.get(getContext());
        mMessages = new HashMap<PlayerInfo, AvalonMessage>();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PlayerInfo info = (PlayerInfo) intent.getSerializableExtra(
                        ServiceMessageProtocol.PLAYER_INFO_KEY);
                AvalonMessage msg = (AvalonMessage) intent.getSerializableExtra(
                        ServiceMessageProtocol.AVALON_MESSAGE_KEY);
                if (mMessages.containsKey(info)) {
                    fail("Duplicate message was sent to the same receiver!");
                } else {
                    mMessages.put(info, msg);
                }
            }
        };
        IntentFilter filter = new IntentFilter(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        getContext().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().unregisterReceiver(mReceiver);
    }

    public void testSevenPlayersNoSpecial() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 7;

        mController.setConfig(config);

        // Start sending it data
        mController.processAvalonMessage(new PlayerInfo("Jin"));
    }

}
