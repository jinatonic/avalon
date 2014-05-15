package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/14/14.
 */
public class PlayerPositionChange extends AvalonMessage {
    public PlayerInfo player;
    public boolean isInc;

    public PlayerPositionChange(PlayerInfo p, boolean i) {
        player = p;
        isInc = i;
    }
}
