package com.google.android.avalon.model.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinyan on 5/15/14.
 */
public class ToBtMessageWrapper implements Serializable {
    public List<PlayerInfo> player = new ArrayList<PlayerInfo>();
    public List<AvalonMessage> message = new ArrayList<AvalonMessage>();

    public void add(PlayerInfo p, AvalonMessage m) {
        player.add(p);
        message.add(m);
    }

    public int size() {
        return player.size();   // just use player, the two should NEVER have different sizes.
    }

    @Override
    public String toString() {
        return "From " + player + ": " + message;
    }
}
