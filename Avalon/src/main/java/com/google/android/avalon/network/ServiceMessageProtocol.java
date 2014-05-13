package com.google.android.avalon.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by jinyan on 5/13/14.
 */
public class ServiceMessageProtocol {

    public static final String TO_BT_SERVICE_INTENT = "to_bt_service_intent";
    public static final String FROM_BT_SERVICE_INTENT = "from_bt_service_intent";

    public static final String CONNECTION_STATUS_KEY = "connection_status_key";

    public static void broadcastToBt(Context context, Bundle extra) {
        Intent i = new Intent(TO_BT_SERVICE_INTENT);
        i.putExtras(extra);
        context.sendBroadcast(i);
    }

    public static void broadcastFromBt(Context context, Bundle extra) {
        Intent i = new Intent(FROM_BT_SERVICE_INTENT);
        i.putExtras(extra);
        context.sendBroadcast(i);
    }
}
