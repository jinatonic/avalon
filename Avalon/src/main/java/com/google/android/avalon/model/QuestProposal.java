package com.google.android.avalon.model;

import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class QuestProposal extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public Set<PlayerName> questMembers;
    public PlayerName proposer;
    public int propNum;
}
