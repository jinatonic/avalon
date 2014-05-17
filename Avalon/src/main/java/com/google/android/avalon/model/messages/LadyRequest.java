package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/16/14.
 */
public class LadyRequest extends AvalonMessage {
    public PlayerInfo player;

    public LadyRequest(PlayerInfo p) {
        player = p;
    }

    @Override
    public String toString() {
        return "[msg ladyreq " + player.name + "]";
    }
}
