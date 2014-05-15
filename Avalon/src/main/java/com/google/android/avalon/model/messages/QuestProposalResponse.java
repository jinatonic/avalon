package com.google.android.avalon.model.messages;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class QuestProposalResponse extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public PlayerInfo player;
    public boolean approve;
    public int propNum; // Must be the same as the QuestProposal

    public QuestProposalResponse(PlayerInfo p, boolean a, int n) {
        player = p;
        approve = a;
        propNum = n;
    }
}