package com.google.android.avalon.model.messages;

import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class QuestProposal extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public Set<PlayerInfo> questMembers;
    public PlayerInfo proposer;
    public int propNum; // 0-based index

    public QuestProposal(Set<PlayerInfo> q, PlayerInfo p, int n) {
        questMembers = q;
        proposer = p;
        propNum = n;
    }
}
