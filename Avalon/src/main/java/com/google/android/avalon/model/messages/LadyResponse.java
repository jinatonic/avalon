package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/16/14.
 */
public class LadyResponse extends AvalonMessage {
    public PlayerInfo player;
    public boolean isGood;

    public LadyResponse(PlayerInfo p, boolean i) {
        player = p;
        isGood = i;
    }

    @Override
    public String toString() {
        return "[msg ladyrsp: " + player.name + " " + isGood + "]";
    }
}
