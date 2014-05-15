package com.google.android.avalon.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.AndroidTestCase;
import android.util.Log;

import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.model.messages.AvalonMessage;
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

    ServerGameStateController mController;
    BroadcastReceiver mReceiver;

    // Note that mMessages should be cleared after every event
    Map<PlayerInfo, AvalonMessage> mMessages;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mController = ServerGameStateController.get(getContext());
        mMessages = new ConcurrentHashMap<PlayerInfo, AvalonMessage>();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
        };
        IntentFilter filter = new IntentFilter(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        getContext().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().unregisterReceiver(mReceiver);
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

        List<QuestProposalResponse> propResponses = new ArrayList<QuestProposalResponse>();
        List<QuestExecutionResponse> execResponses = new ArrayList<QuestExecutionResponse>();

        ServerGameState gameState = mController.getCurrentGameState();
        validateGameState(gameState, null, null, false, 0, 0, 0, false, null, propResponses, null, execResponses);

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
        // Assignments are random so we can only check its existence.
        // We'll rely on AssignmentFactoryTest to make sure that the assignments are correct.
        assertNotNull(gameState.assignments);

        PlayerInfo king = gameState.currentKing;
        int kingIndex = players.indexOf(king);

        // Check the outgoing messages and game state
        validateGameState(gameState, king, null, false, 0, 0, 0, true, null, propResponses, null, execResponses);
        validateOutgoingBroadcast(RoleAssignment.class);
        mMessages.clear();

        // FIRST QUEST - 2 players go on the quest

        // Proposal 1 (index 0)
        PlayerInfo[] proposedPlayers = new PlayerInfo[] { player0, player1 };
        QuestProposal proposal = new QuestProposal(proposedPlayers, king, gameState.currentNumAttempts);
        mController.processAvalonMessage(proposal);

        // Check the outgoing messages and game state
        validateGameState(gameState, king, null, false, 0, 0, 0, false, proposal, propResponses, null, execResponses);
        validateOutgoingBroadcast(QuestProposal.class);
        // Check one QuestProposal for sanity
        assertEquals(proposal, mMessages.get(player0));
    }

    private void validateGameState(ServerGameState gameState, PlayerInfo king, PlayerInfo lady,
            boolean waitingForLady, int questNum, int questsSize, int currAttempts,
            boolean needQuestProposal, QuestProposal lastProp,
            List<QuestProposalResponse> lastPropRsps, QuestExecution lastExec,
            List<QuestExecutionResponse> lastExecRsps) {
        assertEquals(king, gameState.currentKing);
        assertEquals(lady, gameState.currentLady);
        assertEquals(waitingForLady, gameState.waitingForLady);
        assertEquals(questNum, gameState.questNum);
        assertEquals(questsSize, gameState.quests.size());
        assertEquals(currAttempts, gameState.currentNumAttempts);
        assertEquals(needQuestProposal, gameState.needQuestProposal);

        assertEquals(lastProp, gameState.lastQuestProposal);
        assertEquals(lastPropRsps.size(), gameState.lastQuestProposalResponses.size());
        // For lists, we don't care about ordering
        assertTrue(new HashSet<QuestProposalResponse>(lastPropRsps)
                .containsAll(gameState.lastQuestProposalResponses));

        assertEquals(lastExec, gameState.lastQuestExecution);
        assertEquals(lastExecRsps.size(), gameState.lastQuestExecutionResponses.size());
        // For lists, we don't care about ordering
        assertTrue(new HashSet<QuestExecutionResponse>(lastExecRsps)
                .containsAll(gameState.lastQuestExecutionResponses));
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
}
