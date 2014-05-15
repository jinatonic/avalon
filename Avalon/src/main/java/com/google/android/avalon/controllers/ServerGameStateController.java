package com.google.android.avalon.controllers;

import android.content.Context;
import android.util.Log;

import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.PlayerInfo;
import com.google.android.avalon.model.RoleAssignment;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.rules.AssignmentFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jinyan on 5/14/14.
 *
 * Used by BluetoothServerService mainly to keep track of game state.
 * UI classes can query the controller for the current game state.
 */
public class ServerGameStateController extends GameStateController {

    // Initial configurations
    private GameConfiguration mConfig;
    private Set<PlayerInfo> mPlayers = new HashSet<PlayerInfo>();

    // State variables that cannot change once the game starts
    private InitialAssignments mAssignments;

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

    public void setConfig(GameConfiguration config) {
        mConfig = config;
        startGame();
    }

    /**
     * Attempts to start the game, returns the initial assignments on success or null on failure.
     */
    public void startGame() {
        if (mConfig == null || mPlayers == null || mConfig.numPlayers != mPlayers.size()) {
            return;
        }

        // Started successfully
        Log.i(TAG, "Game starting");
        mStarted = true;
        mAssignments = new AssignmentFactory(mConfig).getAssignments(
                new ArrayList<PlayerInfo>(mPlayers));

        // Notify everyone of their assignments
        for (RoleAssignment assignment : mAssignments.assignments) {
            sendAvalonMessage(assignment.player, assignment);
        }
    }

    // TODO
    public ServerGameState getCurrentGameState() {
        return new ServerGameState();
    }

    @Override
    public void onAvalonMessageReceived(PlayerInfo info, AvalonMessage msg) {
        // case through each type and perform appropriate action
        if (msg instanceof PlayerInfo) {
            mPlayers.add((PlayerInfo) msg);
            // try to start game
            startGame();
        }
    }
}
