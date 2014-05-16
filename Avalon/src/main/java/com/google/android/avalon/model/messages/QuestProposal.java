package com.google.android.avalon.model.messages;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class QuestProposal extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public PlayerInfo[] questMembers;
    public PlayerInfo proposer;
    public int propNum; // 0-based index

    public QuestProposal(PlayerInfo[] q, PlayerInfo p, int n) {
        questMembers = q;
        proposer = p;
        propNum = n;
    }

    @Override
    public String toString() {
        return "[msg prop by " + proposer.name + ": " + playerArrayToString(questMembers) +
                " for " + propNum + "]";
    }
}
