package com.google.android.avalon.model;

import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.QuestExecution;
import com.google.android.avalon.model.messages.QuestExecutionResponse;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.QuestProposalResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinyan on 5/14/14.
 *
 * This class should fully represent the current game state so UI has all the information it needs.
 */
public class ServerGameState implements Serializable {
    public List<PlayerInfo> players = new LinkedList<PlayerInfo>();
    public InitialAssignments assignments;
    public BoardCampaignInfo campaignInfo;

    // State variables
    public PlayerInfo currentKing;
    public PlayerInfo currentLady;  // can be null

    public boolean waitingForLady;  // has priority over needQuestProposal for UI

    public int questNum;    // 0 based
    public List<Boolean> quests = new LinkedList<Boolean>();
    public int currentNumAttempts;

    // Quest proposal approval
    public boolean needQuestProposal;
    public QuestProposal lastQuestProposal;
    public List<QuestProposalResponse> lastQuestProposalResponses =
            new ArrayList<QuestProposalResponse>();

    // Quest pass/fail
    public QuestExecution lastQuestExecution;
    public List<QuestExecutionResponse> lastQuestExecutionResponses =
            new ArrayList<QuestExecutionResponse>();

    public int numPlayers() {
        return players.size();
    }
}
