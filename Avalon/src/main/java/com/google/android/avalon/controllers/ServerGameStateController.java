package com.google.android.avalon.controllers;

import android.content.Context;
import android.util.Log;

import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.BoardCampaignInfo;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.messages.GameOverMessage;
import com.google.android.avalon.model.messages.GameStartMessage;
import com.google.android.avalon.model.messages.PlayerDisconnected;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.PlayerPositionChange;
import com.google.android.avalon.model.messages.QuestExecution;
import com.google.android.avalon.model.messages.QuestExecutionResponse;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.QuestProposalResponse;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.model.messages.ToBtMessageWrapper;
import com.google.android.avalon.rules.AssignmentFactory;
import com.google.android.avalon.rules.IllegalConfigurationException;

import java.util.Arrays;

/**
 * Created by jinyan on 5/14/14.
 *
 * Used by BluetoothServerService mainly to keep track of game state.
 * UI classes can query the controller for the current game state.
 */
public class ServerGameStateController extends GameStateController {

    // Initial configurations
    private GameConfiguration mConfig = getFullConfig();

    private GameConfiguration getFullConfig() {
        GameConfiguration cfg = new GameConfiguration();
        cfg.specialRoles.addAll(Arrays.asList(AvalonRole.MERLIN, AvalonRole.MORGANA, AvalonRole.MORDRED, AvalonRole.OBERON, AvalonRole.PERCIVAL, AvalonRole.ASSASSIN));
        cfg.numPlayers = 7;
        return cfg;
    }

    // Game state
    private ServerGameState mGameState = getTestGameState();

    private ServerGameState getTestGameState() {
        ServerGameState state = new ServerGameState();
        state.players.add(new PlayerInfo("Bob"));
        PlayerInfo pi = new PlayerInfo("Sam");
        pi.participating = true;
        state.players.add(pi);
        state.players.add(new PlayerInfo("Bill"));
        return state;
    }

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

    public GameConfiguration getConfig() {
        return mConfig;
    }

    public void setConfig(GameConfiguration config) {
        mConfig = config;
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
            mGameState.campaignInfo = new BoardCampaignInfo(mGameState.players.size());
            mGameState.assignments = new AssignmentFactory(mConfig).getAssignments(
                    mGameState.players);

            // Started successfully
            Log.i(TAG, "Game starting");
            mStarted = true;
        } catch (IllegalConfigurationException e) {
            // We failed to assign players.
            // TODO: Notify UI.
            mGameState.campaignInfo = null;
            return;
        }

        // Set some default game states
        mGameState.needQuestProposal = true;
        mGameState.currentNumAttempts = 0;
        mGameState.questNum = 0;
        mGameState.currentKing = mGameState.assignments.king;
        mGameState.currentLady = mGameState.assignments.lady;

        // Notify everyone of their assignments
        ToBtMessageWrapper wrapper = new ToBtMessageWrapper();
        for (RoleAssignment assignment : mGameState.assignments.assignments) {
            wrapper.add(assignment.player, assignment);
        }
        sendBulkMessages(wrapper);
    }

    // TODO
    public ServerGameState getCurrentGameState() {
        return mGameState;
    }

    @Override
    public void processAvalonMessage(AvalonMessage msg) {
        Log.d(TAG, "processAvalonMessage: " + msg);
        // case through each type and perform appropriate action

        // GameStartMessage, starts the game (or at least try to)
        if (msg instanceof GameStartMessage) {
            startGame();
        }

        // PlayerInfo, simply add the new info into mPlayers
        else if (msg instanceof PlayerInfo) {
            mGameState.players.add((PlayerInfo) msg);
        }

        // PlayerPositionChange, do exactly that
        else if (msg instanceof PlayerPositionChange) {
            PlayerPositionChange pos = (PlayerPositionChange) msg;
            if (mGameState.players.contains(pos.player)) {
                int lastPos = mGameState.players.indexOf(pos.player);
                int newPos = (pos.isIncrement) ? inc(lastPos) : dec(lastPos);
                mGameState.players.remove(lastPos);
                mGameState.players.add(newPos, pos.player);
            }
        }

        // PlayerDisconnected, do reverse of PlayerInfo
        // TODO: maybe add support to pause game?
        // TODO: replace this feature with an explicit command to remove the player?
        else if (msg instanceof PlayerDisconnected) {
            mGameState.players.remove(((PlayerDisconnected) msg).info);
        }

        // QuestProposal, save it in the state variable and broadcast it to all players
        else if (msg instanceof QuestProposal && mGameState.needQuestProposal) {
            // Check that the number of players match the number the quest needs for sanity
            // since this SHOULD be enforced by the UI.
            QuestProposal proposal = (QuestProposal) msg;
            if (proposal.questMembers.length ==
                    mGameState.campaignInfo.numPeopleOnQuests[mGameState.questNum]) {
                mGameState.setNewQuestProposal(proposal);
                broadcastMessageToAllPlayers(msg);
            }
            // TODO: For this and similar cases below, what is the client expected to do?
            // (i.e. do we need to send an explicit notification that the request was invalid?)
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
                        ToBtMessageWrapper wrapper = new ToBtMessageWrapper();
                        QuestExecution exec = new QuestExecution(mGameState.quests.size());
                        for (PlayerInfo member : mGameState.lastQuestProposal.questMembers) {
                            wrapper.add(member, exec);
                        }
                        sendBulkMessages(wrapper);
                        mGameState.setNewQuestExec(exec);
                    } else {
                        mGameState.currentNumAttempts++;
                        if (mGameState.currentNumAttempts > 4) {
                            // auto-fail quests after 5 proposal attempts
                            // TODO this should be auto-game over.
                            addQuestResultAndCheckCompletion(false);
                        }
                        advanceKing();  // TODO shouldn't this also be done if the proposal passed?
                    }

                    // Reset quest proposal so we know that we are not waiting for responses
                    mGameState.lastQuestProposal = null;
                }
            }
        }

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

                // Check if we need more
                if (mGameState.lastQuestExecutionResponses.size() ==
                        mGameState.campaignInfo.numPeopleOnQuests[mGameState.questNum]) {
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

                    // Reset execution so we know that we are not waiting for responses
                    mGameState.lastQuestExecution = null;
                }
            }
        }
    }

    private void broadcastMessageToAllPlayers(AvalonMessage msg) {
        ToBtMessageWrapper wrapper = new ToBtMessageWrapper();
        for (PlayerInfo player : mGameState.players) {
            wrapper.add(player, msg);
        }
        sendBulkMessages(wrapper);
    }

    private void addQuestResultAndCheckCompletion(boolean passed) {
        mGameState.quests.add(passed);
        mGameState.questNum++;
        mGameState.currentNumAttempts = 0;
        mGameState.lastQuestExecution = null;

        // If only I can use a fold operation here.. cmon Java 8
        int numSuccess = 0;
        for (Boolean b : mGameState.quests) {
            if (b) {
                numSuccess++;
            }
        }
        if (numSuccess >= 3 || mGameState.quests.size() - numSuccess >= 3) {
            // TODO if numSuccess > 3, handle assassination attempt.
            broadcastMessageToAllPlayers(new GameOverMessage(numSuccess >= 3));
            mGameState.gameOver = true;
            return;
        }

        mGameState.needQuestProposal = true;
        doLadyStuff();
    }

    private void advanceKing() {
        int currKingIndex = mGameState.players.indexOf(mGameState.currentKing);
        mGameState.currentKing = mGameState.players.get(inc(currKingIndex));
        // TODO this is a smell, since the method name implies only the king state should be affected.
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
