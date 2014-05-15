package com.google.android.avalon.model.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by mikewallstedt on 5/12/14.
 */
public abstract class AvalonMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID uuid = UUID.randomUUID();

    @Override
    public boolean equals(Object o) {
        if (o instanceof AvalonMessage) {
            return this.uuid.equals(((AvalonMessage) o).uuid);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }
}
