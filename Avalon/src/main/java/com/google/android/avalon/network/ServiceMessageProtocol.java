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

    // FROM_BT_SERVICE_INTENT extras
    public static final String SERVICE_ERROR = "service_error_must_quit";
    public static final String DATA_CHANGED = "data_changed";

    // TO_BT_SERVICE_INTENT extras
    public static final String PLAYER_INFO_KEY = "player_info_key";
    public static final String AVALON_MESSAGE_KEY = "avalon_message_key";

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
