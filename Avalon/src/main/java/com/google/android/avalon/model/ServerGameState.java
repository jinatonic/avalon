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

    public List<Boolean> quests = new LinkedList<Boolean>();
    public int currentNumAttempts;
    public boolean gameOver;

    // Quest proposal approval
    public boolean needQuestProposal;
    public QuestProposal lastQuestProposal; // null if we are not waiting for response
    public List<QuestProposalResponse> lastQuestProposalResponses =
            new ArrayList<QuestProposalResponse>();

    // Quest pass/fail
    public QuestExecution lastQuestExecution;   // null if we are not waiting for response
    public List<QuestExecutionResponse> lastQuestExecutionResponses =
            new ArrayList<QuestExecutionResponse>();

    public int numPlayers() {
        return players.size();
    }

    public boolean started() {
        return campaignInfo != null;
    }

    public void setNewQuestProposal(QuestProposal proposal) {
        needQuestProposal = false;
        lastQuestProposalResponses.clear();
        lastQuestProposal = proposal;
        lastQuestExecution = null;
    }

    public void setNewQuestExec(QuestExecution exec) {
        needQuestProposal = false;
        lastQuestProposal = null;
        lastQuestExecutionResponses.clear();
        lastQuestExecution = exec;
    }
}
