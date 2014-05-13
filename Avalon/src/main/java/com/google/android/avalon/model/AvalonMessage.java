package com.google.android.avalon.model;

import java.io.Serializable;

/**
 * Created by mikewallstedt on 5/12/14.
 */
public class AvalonMessage implements Serializable {
    public int sequenceNum;
    public boolean isAck = false;   // Used by messages that need client/server ACK
}
