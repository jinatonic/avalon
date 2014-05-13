package com.google.android.avalon.model;

import java.util.Set;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class QuestProposal implements AvalonMessage {
    public Set<String> names;
    public String proposer;
    public int propNum;
}
