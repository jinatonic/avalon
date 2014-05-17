package com.google.android.avalon.model.messages;

import java.util.UUID;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class PlayerInfo extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public String oldName;  // used to either change name or notify to remove
    public String name;
    public boolean participating = true;

    public PlayerInfo(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[msg " + name + "]";
    }
}
