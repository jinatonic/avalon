package com.google.android.avalon.interfaces;

import com.google.android.avalon.model.messages.AvalonMessage;

/**
 * Created by jinyan on 5/14/14.
 */
public interface AvalonMessageListener {
    /**
     * @return true on success (valid message), false otherwise
     */
    public boolean processAvalonMessage(AvalonMessage msg);
}
