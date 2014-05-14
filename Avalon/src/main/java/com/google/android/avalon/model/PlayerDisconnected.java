package com.google.android.avalon.model;

/**
 * Created by jinyan on 5/14/14.
 */
public class PlayerDisconnected extends AvalonMessage {
    public PlayerInfo info;

    public PlayerDisconnected(PlayerInfo i) {
        info = i;
    }
}
