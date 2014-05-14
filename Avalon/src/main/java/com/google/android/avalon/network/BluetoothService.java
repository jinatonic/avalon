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

import com.google.android.avalon.interfaces.AvalonMessageHandler;
import com.google.android.avalon.model.PlayerInfo;

/**
 * Created by jinyan on 5/13/14.
 */
public abstract class BluetoothService extends Service implements AvalonMessageHandler {

    private static final String TAG = BluetoothService.class.getSimpleName();

    protected SocketReader mReader;
    protected SocketWriter mWriter;
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
        broadcastConnectionStatus(false, null);

        // This code will restart the service with the same intent if it's ever destroyed by OS
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMsgReceiver);

        // Notify that we currently do not have the connection
        broadcastConnectionStatus(false, null);
    }

    protected abstract BroadcastReceiver getServiceMessageReceiver();

    /**
     * Helper function to debug
     */
    protected void showToast(final String msg) {
        mHandler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(BluetoothService.this, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * Helper function to broadcast connection status to the activity
     */
    protected void broadcastConnectionStatus(boolean connected, PlayerInfo player) {
        Log.i(TAG, "broadcastConnectionStatus: " + connected);
        showToast("Connection status: " + connected + ", " + player);
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
