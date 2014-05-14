package com.google.android.avalon.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.avalon.interfaces.BluetoothController;
import com.google.android.avalon.model.PlayerInfo;

import java.util.Set;

/**
 * Created by jinyan on 5/13/14.
 */
public abstract class BluetoothService extends Service implements BluetoothController {

    private static final String TAG = BluetoothService.class.getSimpleName();

    protected BroadcastReceiver mMsgReceiver;

    private Handler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // not using binding
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMsgReceiver = getServiceMessageReceiver();
        IntentFilter intentFilter = new IntentFilter(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        registerReceiver(mMsgReceiver, intentFilter);

        mHandler = new Handler();

        // Notify that we currently do not have the connection
        broadcastConnectionStatus();

        // This code will restart the service with the same intent if it's ever destroyed by OS
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMsgReceiver);

        // Notify that we are losing all of our connections
        broadcastConnectionStatus();
    }

    protected abstract BroadcastReceiver getServiceMessageReceiver();

    protected abstract Set<PlayerInfo> getConnected();

    /**
     * Helper function to debug
     */
    protected void showToast(final String msg) {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(BluetoothService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper function to broadcast connection status to the activity
     */
    protected void broadcastConnectionStatus() {
        Set<PlayerInfo> player = getConnected();
        Log.i(TAG, "broadcastConnectionStatus: " + (player != null && player.size() > 0));
        showToast("Connection status: " + (player != null && player.size() > 0));
    }

    /**
     * Helper function to broadcast that the service has errored out
     */
    protected int broadcastErrorAndStop() {
        Log.e(TAG, "broadcasting error");
        Bundle extra = new Bundle();
        extra.putBoolean(ServiceMessageProtocol.SERVICE_ERROR, true);
        ServiceMessageProtocol.broadcastFromBt(this, extra);

        // Stop self and return the no-restart flag
        stopSelf();
        return START_NOT_STICKY;
    }
}
