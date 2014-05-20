package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/18/14.
 */
public class AssassinResponse extends AvalonMessage {
    public PlayerInfo target;

    public AssassinResponse(PlayerInfo p) {
        target = p;
    }

    @Override
    public String toString() {
        return "[msg assassin: " + target.name + "]";
    }
}
