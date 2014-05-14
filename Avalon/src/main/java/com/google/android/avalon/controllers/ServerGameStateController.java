package com.google.android.avalon.controllers;

import android.content.Context;

import com.google.android.avalon.interfaces.AvalonMessageListener;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.PlayerInfo;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.rules.AssignmentFactory;

import java.util.Map;
import java.util.Set;

/**
 * Created by jinyan on 5/14/14.
 *
 * Used by BluetoothServerService mainly to keep track of game state.
 * UI classes can query the controller for the current game state.
 */
public class ServerGameStateController implements AvalonMessageListener {

    private Context mContext;

    // Initial configurations
    private GameConfiguration mConfig;
    private Set<PlayerInfo> mPlayers;

    // State variables that cannot change once the game starts
    private InitialAssignments mAssignments;
    private Map<PlayerInfo, Boolean> mPlayerInfoConnected;

    // private for singleton
    private static ServerGameStateController sServerGameStateController;
    private ServerGameStateController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static ServerGameStateController get(Context context) {
        if (sServerGameStateController == null) {
            sServerGameStateController = new ServerGameStateController(context);
        }
        return sServerGameStateController;
    }

    /**
     * Attempts to start the game, returns the initial assignments on success or null on failure.
     */
    public InitialAssignments startGame() {
        if (mConfig == null || mPlayers == null) {
            return null;
        }
        mAssignments = new AssignmentFactory(mConfig).getAssignments(mPlayers);
        return mAssignments;
    }

    // TODO
    public ServerGameState getCurrentGameState() {
        return new ServerGameState();
    }

    @Override
    public void onAvalonMessageReceived(AvalonMessage msg) {

    }
}
