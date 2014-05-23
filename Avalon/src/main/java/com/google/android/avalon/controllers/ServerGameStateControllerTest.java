package com.google.android.avalon.controllers;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import android.util.Log;

import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.messages.GameOverMessage;
import com.google.android.avalon.model.messages.GameStartMessage;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.PlayerPositionChange;
import com.google.android.avalon.model.messages.QuestExecution;
import com.google.android.avalon.model.messages.QuestExecutionResponse;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.QuestProposalResponse;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.network.ServiceMessageProtocol;
import com.google.android.avalon.model.messages.ToBtMessageWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jinyan on 5/15/14.
 */
public class ServerGameStateControllerTest extends AndroidTestCase {

    Context mContext;
    Context realContext;
    ServerGameStateController mController;

    // Note that mMessages should be cleared after every event
    Map<PlayerInfo, AvalonMessage> mMessages;
    int numToasts;

    // global vars for easier validation
    ServerGameState gameState;
    PlayerInfo king;
    PlayerInfo lady;
    QuestProposal proposal;
    List<QuestProposalResponse> propResponses;
    PlayerInfo[] proposedPlayers;
    QuestExecution exec;
    List<QuestExecutionResponse> execResponses;


    // Player pointers
    PlayerInfo player0 = new PlayerInfo("Player0");
    PlayerInfo player1 = new PlayerInfo("Player1");
    PlayerInfo player2 = new PlayerInfo("Player2");
    PlayerInfo player3 = new PlayerInfo("Player3");
    PlayerInfo player4 = new PlayerInfo("Player4");
    PlayerInfo player5 = new PlayerInfo("Player5");
    PlayerInfo player6 = new PlayerInfo("Player6");
    List<PlayerInfo> players = Arrays.asList(new PlayerInfo[] {
            player0, player1, player2, player3, player4, player5, player6
    });

    private class MyMockContext extends MockContext {
        @Override
        public void sendBroadcast(Intent intent) {
            ToBtMessageWrapper wrapper = (ToBtMessageWrapper) intent.getSerializableExtra(
                    ServiceMessageProtocol.DATA_WRAPPER_ARRAY_KEY);
            Log.i("Testing", System.currentTimeMillis() + " Message received: " + wrapper);
            for (int i = 0; i < wrapper.size(); i++) {
                if (mMessages.containsKey(wrapper.player.get(i))) {
                    fail("Duplicate message was sent to the same receiver!");
                } else {
                    mMessages.put(wrapper.player.get(i), wrapper.message.get(i));
                }
            }
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }

        // these methods are here to support capturing Toast.makeText(...).show()
        @Override
        public String getPackageName() {
            return realContext.getPackageName();
        }
        @Override
        public Object getSystemService(String name) {
            numToasts++;
            return realContext.getSystemService(name);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = new MyMockContext();
        realContext = getContext();
        mController = ServerGameStateController.get(mContext);

        mMessages = new ConcurrentHashMap<PlayerInfo, AvalonMessage>();
        numToasts = 0;

        propResponses = new ArrayList<QuestProposalResponse>();
        execResponses = new ArrayList<QuestExecutionResponse>();
    }

    /**
     * This test mostly tests for the following:
     *   - player connected via PlayerInfo
     *   - place switching on server-side via PlayerPositionChange
     *   - role assignment for all normal players
     *   - basic quest voting and game logic (keyword: basic)
     *   - game completion states (3 wons or 3 losses)
     */
    public void testSevenPlayersNoSpecial() {
        // Check initial configuration
        assertEquals(0, mMessages.size());

        gameState = mController.getCurrentGameState();
        validateGameState(false, 0, 0, false, false);

        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 7;

        mController.setConfig(config);

        // Start sending it player data
        mController.processAvalonMessage(player0);
        mController.processAvalonMessage(player1);
        mController.processAvalonMessage(player2);
        mController.processAvalonMessage(player3);
        mController.processAvalonMessage(player4);
        mController.processAvalonMessage(player5);
        mController.processAvalonMessage(player6);

        // Check for initial player infos
        gameState = mController.getCurrentGameState();
        assertEquals(7, gameState.numPlayers());
        assertEquals(player0, gameState.players.get(0));
        assertEquals(player1, gameState.players.get(1));
        assertEquals(player2, gameState.players.get(2));
        assertEquals(player3, gameState.players.get(3));
        assertEquals(player4, gameState.players.get(4));
        assertEquals(player5, gameState.players.get(5));
        assertEquals(player6, gameState.players.get(6));

        // Assert that we didn't send back any responses yet
        assertEquals(0, mMessages.size());

        // Test position swapping
        mController.processAvalonMessage(new PlayerPositionChange(player6, true));

        // Check for resulting state
        gameState = mController.getCurrentGameState();
        assertEquals(7, gameState.numPlayers());
        assertEquals(player6, gameState.players.get(0));
        assertEquals(player5, gameState.players.get(6));
        assertEquals(0, mMessages.size());

        // Revert position
        mController.processAvalonMessage(new PlayerPositionChange(player6, false));

        // Check for resulting state
        gameState = mController.getCurrentGameState();
        assertEquals(7, gameState.numPlayers());
        assertEquals(player6, gameState.players.get(6));
        assertEquals(player0, gameState.players.get(0));
        assertEquals(0, mMessages.size());


        // START GAME!!!!!
        mController.processAvalonMessage(new GameStartMessage());

        // Check for resulting state
        gameState = mController.getCurrentGameState();
        assertTrue(gameState.started());

        king = gameState.currentKing;

        // Check the outgoing messages and game state
        validateGameState(false, 0, 0, true, false);
        validateOutgoingBroadcast(RoleAssignment.class);
        mMessages.clear();

        // FIRST QUEST - 2 players, auto-pass
        passQuestProposalAndQuest(0, 2);

        // Quest #2 - 3 players, just auto-pass to progress the game
        passQuestProposalAndQuest(1, 3);

        // Quest #3 - 3 players go on
        proposedPlayers = new PlayerInfo[] {player2, player4, player6};
        proposal = new QuestProposal(proposedPlayers, king, gameState.currentNumAttempts);
        mController.processAvalonMessage(proposal);
        propResponses.clear();

        // Check the outgoing messages and game state
        gameState = mController.getCurrentGameState();
        validateGameState(false, 2, 0, false, false);
        mMessages.clear();

        // Fail it by 3 to 4 vote
        int i = 0;
        for (PlayerInfo player : gameState.players) {
            QuestProposalResponse rsp = new QuestProposalResponse(player, i++ % 2 != 0, proposal.propNum);
            mController.processAvalonMessage(rsp);
            propResponses.add(rsp);
        }
        proposal = null;

        king = validateKing(gameState, king);
        validateGameState(false, 2, 1, true, false);
        assertEquals(0, mMessages.size());

        proposedPlayers = new PlayerInfo[] {player1, player5, player6};
        proposal = new QuestProposal(proposedPlayers, king, gameState.currentNumAttempts);
        mController.processAvalonMessage(proposal);
        propResponses.clear();

        // Check the outgoing messages and game state
        gameState = mController.getCurrentGameState();
        validateGameState(false, 2, 1, false, false);
        mMessages.clear();

        // Pass it by 4 to 3 vote
        i = 1;
        for (PlayerInfo player : gameState.players) {
            QuestProposalResponse rsp = new QuestProposalResponse(player, i++ % 2 != 0, proposal.propNum);
            mController.processAvalonMessage(rsp);
            propResponses.add(rsp);
        }
        proposal = null;
        exec = new QuestExecution(gameState.currQuestIndex());
        execResponses.clear();

        // Validate outgoing messages and game state (should have progressed to quest execution)
        validateGameState(false, 2, 1, false, false);
        assertEquals(3, mMessages.size());
        assertEquals(gameState.quests.size(), ((QuestExecution) mMessages.get(player1)).questNum);
        assertEquals(gameState.quests.size(), ((QuestExecution) mMessages.get(player5)).questNum);
        assertEquals(gameState.quests.size(), ((QuestExecution) mMessages.get(player6)).questNum);
        mMessages.clear();

        // Send out QuestExecutionResponse messages
        for (i = 0; i < proposedPlayers.length; i++) {
            PlayerInfo player = proposedPlayers[i];
            QuestExecutionResponse rsp = new QuestExecutionResponse(player, true, gameState.quests.size());
            mController.processAvalonMessage(rsp);
            execResponses.add(rsp);

            if (i < proposedPlayers.length - 1) {
                assertEquals(0, mMessages.size());
                validateGameState(false, 2, 1, false, false);
            } else {
                validateOutgoingBroadcast(GameOverMessage.class);
                exec = null;
                // Validate resulting state (should be waiting for a new proposal)
                gameState = mController.getCurrentGameState();
                assertTrue(gameState.gameOver);
            }
        }

        // This test should not raise any warning toasts
        assertEquals(0, numToasts);
    }

    // Pass the quest normally (for testing game progressions), the VERY last state is NOT checked
    // as it can be different depending on what characters are available
    private void passQuestProposalAndQuest(int questNum, int numPlayers) {
        // Quest #2 - 3 players go on
        proposedPlayers = new PlayerInfo[numPlayers];
        proposedPlayers = players.subList(0, numPlayers).toArray(proposedPlayers);
        proposal = new QuestProposal(proposedPlayers, king, gameState.currentNumAttempts);
        mController.processAvalonMessage(proposal);
        propResponses.clear();

        // Check the outgoing messages and game state
        gameState = mController.getCurrentGameState();
        validateGameState(false, questNum, 0, false, false);
        validateOutgoingBroadcast(QuestProposal.class);
        // Check one QuestProposal for sanity
        assertEquals(proposal, mMessages.get(player0));
        mMessages.clear();

        // Pass it (barely, should have 2 fails)
        int i = 0;
        for (PlayerInfo player : gameState.players) {
            QuestProposalResponse rsp = new QuestProposalResponse(player, i++ % 3 != 0, proposal.propNum);
            mController.processAvalonMessage(rsp);
            propResponses.add(rsp);
        }
        // We set the expected proposal to null and exec to not null after we get all the responses
        proposal = null;
        exec = new QuestExecution(questNum);
        execResponses.clear();

        // Validate outgoing messages and game state (should have progressed to quest execution)
        validateGameState(false, questNum, 0, false, false);
        assertEquals(numPlayers, mMessages.size());
        for (PlayerInfo player : proposedPlayers) {
            assertEquals(questNum, ((QuestExecution) mMessages.get(player)).questNum);
        }
        mMessages.clear();

        // Send out QuestExecutionResponse messages
        for (i = 0; i < proposedPlayers.length; i++) {
            PlayerInfo player = proposedPlayers[i];
            QuestExecutionResponse rsp = new QuestExecutionResponse(player, true, gameState.quests.size());
            mController.processAvalonMessage(rsp);
            execResponses.add(rsp);

            assertEquals(0, mMessages.size());
            if (i < proposedPlayers.length - 1) {
                validateGameState(false, questNum, 0, false, false);
            } else {
                exec = null;    // we are no longer waiting for responses
                // Validate resulting state (should be waiting for a new proposal)
                gameState = mController.getCurrentGameState();
                king = validateKing(gameState, king);
                validateGameState(false, questNum+1, 0, true, false);
                assertTrue(gameState.quests.get(questNum));
            }
        }
    }

    private void validateGameState(boolean waitingForLady, int questsSize, int currAttempts,
            boolean needQuestProposal, boolean gameOver) {
        assertEquals(king, gameState.currentKing);
        assertEquals(lady, gameState.currentLady);
        assertEquals(waitingForLady, gameState.waitingForLady);
        assertEquals(questsSize, gameState.quests.size());
        assertEquals(currAttempts, gameState.currentNumAttempts);
        assertEquals(needQuestProposal, gameState.needQuestProposal);

        assertEquals(proposal, gameState.lastQuestProposal);
        assertEquals(propResponses.size(), gameState.lastQuestProposalResponses.size());
        // For lists, we don't care about ordering
        assertTrue(new HashSet<QuestProposalResponse>(propResponses)
                .containsAll(gameState.lastQuestProposalResponses.values()));

        // Can't really verify QuestExecution object itself
        assertEquals(exec != null, gameState.lastQuestExecution != null);
        assertEquals(execResponses.size(), gameState.lastQuestExecutionResponses.size());
        // For lists, we don't care about ordering
        assertTrue(new HashSet<QuestExecutionResponse>(execResponses)
                .containsAll(gameState.lastQuestExecutionResponses.values()));
        assertEquals(gameOver, gameState.gameOver);
    }

    private void validateOutgoingBroadcast(Class expectedClass) {
        assertEquals(7, mMessages.size());
        assertTrue(mMessages.containsKey(player0));
        assertEquals(expectedClass, mMessages.get(player0).getClass());
        assertTrue(mMessages.containsKey(player1));
        assertEquals(expectedClass, mMessages.get(player1).getClass());
        assertTrue(mMessages.containsKey(player2));
        assertEquals(expectedClass, mMessages.get(player2).getClass());
        assertTrue(mMessages.containsKey(player3));
        assertEquals(expectedClass, mMessages.get(player3).getClass());
        assertTrue(mMessages.containsKey(player4));
        assertEquals(expectedClass, mMessages.get(player4).getClass());
        assertTrue(mMessages.containsKey(player5));
        assertEquals(expectedClass, mMessages.get(player5).getClass());
        assertTrue(mMessages.containsKey(player6));
        assertEquals(expectedClass, mMessages.get(player6).getClass());
    }

    private PlayerInfo validateKing(ServerGameState gameState, PlayerInfo currentKing) {
        assertEquals(gameState.players.indexOf(gameState.currentKing),
                (gameState.players.indexOf(currentKing) + 1) % gameState.players.size());
        return gameState.currentKing;
    }
}
