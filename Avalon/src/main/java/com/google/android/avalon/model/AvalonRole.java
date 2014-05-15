package com.google.android.avalon.model;

import java.io.Serializable;

/**
* Created by mikewallstedt on 5/12/14.
*/
public enum AvalonRole {
    LOYAL(true),
    MINION(false),
    MERLIN(true),
    PERCIVAL(true),
    ASSASSIN(false),
    MORDRED(false),
    MORGANA(false),
    OBERON(false);

    public final boolean isGood;

    AvalonRole(boolean isGood) {
        this.isGood = isGood;
    }
}