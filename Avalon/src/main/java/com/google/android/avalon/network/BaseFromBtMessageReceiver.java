package com.google.android.avalon.network;

/**
 * Created by jinyan on 5/13/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Base broadcast receiver class that handles connection updates.
 * Extend this class to cover more updates that we care about.
 */
public class BaseFromBtMessageReceiver extends BroadcastReceiver {

    public void attach(Context context) {
        IntentFilter filter = new IntentFilter(ServiceMessageProtocol.FROM_BT_SERVICE_INTENT);
        context.registerReceiver(this, filter);
    }

    public void detach(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}