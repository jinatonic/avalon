package com.google.android.avalon.network;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import com.google.android.avalon.AvalonActivity;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.interfaces.AvalonMessageHandler;

import java.io.IOException;

/**
 * Created by jinyan on 5/13/14.
 */
public class BluetoothClientService extends Service implements AvalonMessageHandler {

    private static final String TAG = BluetoothClientService.class.getSimpleName();

    private static Handler sHandler;
    static {
        HandlerThread thread = new HandlerThread("Client connection thread");
        thread.start();
        sHandler = new Handler(thread.getLooper());
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mServerSocket;
    private SocketReader mReader;
    private SocketWriter mWriter;

    private MessageReceiver mMsgReceiver;
    private BtScanReceiver mBtScanReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;    // not using binding
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mMsgReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter(ServiceMessageProtocol.TO_BT_SERVICE_INTENT);
        registerReceiver(mMsgReceiver, intentFilter);

        // Register the BroadcastReceiver
        mBtScanReceiver = new BtScanReceiver();
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBtScanReceiver, intentFilter);

        // Notify that we currently do not have the connection
        broadcastConnectionStatus(false);

        // Start discovery
        mBluetoothAdapter.startDiscovery();

        // This code will restart the service with the same intent if it's ever destroyed by OS
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMsgReceiver);
        unregisterReceiver(mBtScanReceiver);

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) { }
        }

        // Notify that we currently do not have the connection
        broadcastConnectionStatus(false);
    }

    /**
     * Helper function to broadcast connection status to the activity
     */
    private void broadcastConnectionStatus(boolean connected) {
        Log.i(TAG, "broadcastConnectionStatus: " + connected);
        Bundle extra = new Bundle();
        extra.putBoolean(ServiceMessageProtocol.CONNECTION_STATUS_KEY, connected);
        ServiceMessageProtocol.broadcastFromBt(this, extra);
    }

    /**
     * Callback interface for SocketReader to inform the service of new data
     */
    @Override
    public void onMessageReceived(AvalonMessage msg) {
        // TODO
    }

    // A BroadcastReceiver for our custom messages
    private class MessageReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            // TODO: handle
        }
    }

    // A BroadcastReceiver for ACTION_FOUND
    private class BtScanReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);

                boolean found = false;
                for (ParcelUuid uuid : device.getUuids()) {
                    if (uuid.getUuid().equals(AvalonActivity.CLIENT_SERVER_UUID)) {
                        mBluetoothAdapter.cancelDiscovery();
                        found = true;
                    }
                }

                if (!found) {
                    return;
                }

                sHandler.post(new ConnectRunnable(device));
            }
        }
    };

    public class ConnectRunnable implements Runnable {
        private BluetoothDevice mDevice;

        public ConnectRunnable(BluetoothDevice device) {
            mDevice = device;
        }

        public void run() {
            BluetoothSocket socket = null;
            try {
                socket = mDevice.createRfcommSocketToServiceRecord(
                        AvalonActivity.CLIENT_SERVER_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                // Try to establish the connection with the server
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException el) { }
                return;
            }

            // Connection established, so stop discovery and set the instance socket
            mBluetoothAdapter.cancelDiscovery();
            mServerSocket = socket;

            // Start the reader thread
            mReader = new SocketReader(mServerSocket, BluetoothClientService.this);
            mReader.start();

            // Send broadcast that connection has been established
            broadcastConnectionStatus(true);
        }
    }
}
