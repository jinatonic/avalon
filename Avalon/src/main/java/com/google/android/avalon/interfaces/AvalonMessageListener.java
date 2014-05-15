package com.google.android.avalon.interfaces;

import com.google.android.avalon.model.messages.AvalonMessage;

/**
 * Created by jinyan on 5/14/14.
 */
public interface AvalonMessageListener {
    public void processAvalonMessage(AvalonMessage msg);
}
