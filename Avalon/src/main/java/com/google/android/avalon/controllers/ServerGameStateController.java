package com.google.android.avalon.controllers;

import android.content.Context;
import android.util.Log;

import com.google.android.avalon.model.BoardCampaignInfo;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.messages.GameOverMessage;
import com.google.android.avalon.model.messages.PlayerDisconnected;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.PlayerPositionChange;
import com.google.android.avalon.model.messages.QuestExecution;
import com.google.android.avalon.model.messages.QuestExecutionResponse;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.QuestProposalResponse;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.rules.AssignmentFactory;

/**
 * Created by jinyan on 5/14/14.
 *
 * Used by BluetoothServerService mainly to keep track of game state.
 * UI classes can query the controller for the current game state.
 */
public class ServerGameStateController extends GameStateController {

    // Initial configurations
    private GameConfiguration mConfig;

    // Game state
    private ServerGameState mGameState = new ServerGameState();

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
    private void startGame() {
        if (mConfig == null || mGameState.players == null ||
                mConfig.numPlayers != mGameState.numPlayers()) {
            return;
        }

        try {
            mGameState.assignments = new AssignmentFactory(mConfig).getAssignments(
                    mGameState.players);

            // Started successfully
            Log.i(TAG, "Game starting");
            mStarted = true;
        } catch (IllegalStateException e) {
            // We failed to assign players.
            // TODO: Notify UI.
            return;
        }

        // Set some default game states
        mGameState.needQuestProposal = true;
        mGameState.currentNumAttempts = 0;
        mGameState.questNum = 0;
        mGameState.campaignInfo = new BoardCampaignInfo(mGameState.players.size());
        mGameState.currentKing = mGameState.assignments.king;
        mGameState.currentLady = mGameState.assignments.lady;

        // Notify everyone of their assignments
        for (RoleAssignment assignment : mGameState.assignments.assignments) {
            sendAvalonMessage(assignment.player, assignment);
        }
    }

    // TODO
    public ServerGameState getCurrentGameState() {
        return new ServerGameState();
    }

    @Override
    public void processAvalonMessage(AvalonMessage msg) {
        Log.d(TAG, "processAvalonMessage: " + msg);
        // case through each type and perform appropriate action

        // PlayerInfo, simply add the new info into mPlayers
        if (msg instanceof PlayerInfo) {
            mGameState.players.add((PlayerInfo) msg);
            // try to start game
            startGame();
        }

        // PlayerPositionChange, do exactly that
        if (msg instanceof PlayerPositionChange) {
            PlayerPositionChange pos = (PlayerPositionChange) msg;
            if (mGameState.players.contains(pos.player)) {
                int lastPos = mGameState.players.indexOf(pos.player);
                int newPos = (pos.isInc) ? inc(lastPos) : dec(lastPos);
                mGameState.players.remove(lastPos);
                mGameState.players.add(newPos, pos.player);
            }
        }

        // PlayerDisconnected, do reverse of PlayerInfo
        // TODO: maybe add support to pause game?
        else if (msg instanceof PlayerDisconnected) {
            mGameState.players.remove(((PlayerDisconnected) msg).info);
        }

        // QuestProposal, save it in the state variable and broadcast it to all players
        else if (msg instanceof QuestProposal) {
            mGameState.needQuestProposal = false;
            mGameState.lastQuestProposal = (QuestProposal) msg;
            mGameState.lastQuestProposalResponses.clear();
            broadcastMessageToAllPlayers(msg);
        }

        // QuestProposalResponse, record it. Only advance once all player's responses are received.
        else if (msg instanceof QuestProposalResponse && mGameState.lastQuestProposal != null &&
                mGameState.lastQuestProposalResponses.size() < mGameState.players.size()) {
            QuestProposalResponse rsp = (QuestProposalResponse) msg;
            if (rsp.propNum == mGameState.lastQuestProposal.propNum) {
                // Make sure we don't accidentally record duplicate response
                for (QuestProposalResponse prev : mGameState.lastQuestProposalResponses) {
                    if (prev.player.equals(rsp.player)) {
                        return;
                    }
                }
                mGameState.lastQuestProposalResponses.add(rsp);

                // Check for state advance criteria
                if (mGameState.lastQuestProposalResponses.size() == mGameState.players.size()) {
                    // Set quest state
                    int difference = 0;
                    for (QuestProposalResponse prev : mGameState.lastQuestProposalResponses) {
                        difference += ((prev.approve) ? 1 : -1);
                    }

                    boolean proposalPassed = difference > 0;
                    if (proposalPassed) {
                        // Send votes to proposed team
                        for (PlayerInfo player : mGameState.lastQuestProposal.questMembers) {
                            sendAvalonMessage(player, new QuestExecution(mGameState.quests.size()));
                        }
                    } else {
                        mGameState.currentNumAttempts++;
                        if (mGameState.currentNumAttempts > 4) {
                            // auto-fail quests after 5 proposal attempts
                            addQuestResultAndCheckCompletion(false);
                        } else {
                            advanceKing();
                        }
                    }
                }
            }
        }

        // QuestExecution

        // QuestExecutionResponse
        else if (msg instanceof QuestExecutionResponse && mGameState.lastQuestExecution != null) {
            QuestExecutionResponse rsp = (QuestExecutionResponse) msg;
            if (rsp.questNum == mGameState.lastQuestExecution.questNum) {
                // Make sure we don't accidentally record duplicate response
                for (QuestExecutionResponse prev : mGameState.lastQuestExecutionResponses) {
                    if (prev.player.equals(rsp.player)) {
                        return;
                    }
                }
                mGameState.lastQuestExecutionResponses.add(rsp);

                // Set quest state
                int numFailed = 0;
                for (QuestExecutionResponse prev : mGameState.lastQuestExecutionResponses) {
                    if (!prev.pass) {
                        numFailed++;
                    }
                }

                boolean questPassed = numFailed < mGameState.campaignInfo.numPeopleNeedToFail[
                        mGameState.questNum];

                addQuestResultAndCheckCompletion(questPassed);
            }
        }
    }

    private void broadcastMessageToAllPlayers(AvalonMessage msg) {
        for (PlayerInfo player : mGameState.players) {
            sendAvalonMessage(player, msg);
        }
    }

    private void addQuestResultAndCheckCompletion(boolean passed) {
        mGameState.quests.add(passed);

        // If only I can use a fold operation here.. cmon Java 8
        int numSuccess = 0;
        for (Boolean b : mGameState.quests) {
            if (b) {
                numSuccess++;
            }
        }
        if (numSuccess >= 3 || mGameState.quests.size() - numSuccess >= 3) {
            broadcastMessageToAllPlayers(new GameOverMessage(numSuccess >= 3));
            return;
        }

        advanceKing();
        doLadyStuff();
    }

    private void advanceKing() {
        int currKingIndex = mGameState.players.indexOf(mGameState.currentKing);
        mGameState.currentKing = mGameState.players.get(inc(currKingIndex));
        mGameState.needQuestProposal = true;
    }

    private void doLadyStuff() {
        // If there is a lady of the lake and it's past 2nd quest, do lady stuff
        if (mGameState.currentLady != null && mGameState.quests.size() >= 2) {
            // TODO
        }
    }

    private int inc(int i) {
        return (i + 1) % mGameState.numPlayers();
    }

    private int dec(int i) {
        return (i - 1 + mGameState.numPlayers()) % mGameState.numPlayers();
    }
}
