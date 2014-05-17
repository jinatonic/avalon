package com.google.android.avalon.model;

import com.google.android.avalon.model.messages.LadyResponse;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.QuestExecution;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.RoleAssignment;

import java.io.Serializable;

/**
 * Created by jinyan on 5/14/14.
 */
public class ClientGameState implements Serializable {
    public PlayerInfo player;
    public RoleAssignment assignment;
    public boolean started;

    public QuestProposal proposal;     // null if we are not waiting for user input
    public QuestExecution execution;   // null if we are not waiting for user input

    public LadyResponse ladyRsp;

    public boolean gameOver;
    public boolean goodWon;     // shouldn't be checked unless gameOver = true

    public boolean started() {
        return started;
    }
}
