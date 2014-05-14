package com.google.android.avalon.interfaces;

import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.PlayerInfo;

/**
 * Created by jinyan on 5/14/14.
 */
public interface AvalonMessageListener {
    public void onAvalonMessageReceived(PlayerInfo src, AvalonMessage msg);
}
