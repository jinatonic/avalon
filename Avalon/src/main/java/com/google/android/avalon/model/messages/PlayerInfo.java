package com.google.android.avalon.model.messages;

import java.util.UUID;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class PlayerInfo extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public final UUID id;
    public String oldName;  // used to either change name or notify to remove
    public String name;
    public boolean participating;

    public PlayerInfo(String name) {
        this(UUID.randomUUID(), name);
    }

    public PlayerInfo(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerInfo that = (PlayerInfo) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
