package com.google.android.avalon.interfaces;

import com.google.android.avalon.model.AvalonMessage;

/**
 * Created by jinyan on 5/13/14.
 */
public interface AvalonMessageHandler {
    public void onMessageReceived(AvalonMessage msg);
}
