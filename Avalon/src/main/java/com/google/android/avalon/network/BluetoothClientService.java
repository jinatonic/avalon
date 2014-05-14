package com.google.android.avalon.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.util.Log;

import com.google.android.avalon.AvalonActivity;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.PlayerInfo;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by jinyan on 5/13/14.
 */
public class BluetoothClientService extends BluetoothService {
    private static final String TAG = BluetoothClientService.class.getSimpleName();

    public static final String PLAYER_INFO_KEY = "player_info_key";

    // Handler for running the blocking connect calls. It serializes them so we don't have more
    // than one connect call running at once.
    private static Handler sHandler;
    static {
        HandlerThread thread = new HandlerThread("Client connection thread");
        thread.start();
        sHandler = new Handler(thread.getLooper());
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mServerSocket;

    // custom broadcast receiver to detect bluetooth scan
    private BtScanReceiver mBtScanReceiver;

    private PlayerInfo mPlayerInfo;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BluetoothClientService starting");
        int result = super.onStartCommand(intent, flags, startId);

        // Check for required fields for launching this service
        Serializable player = intent.getSerializableExtra(PLAYER_INFO_KEY);
        if (player == null) {
            Log.e(TAG, "BluetoothClientService launched without PlayerInfo");
            return broadcastErrorAndStop();
        }
        mPlayerInfo = (PlayerInfo) player;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register the BroadcastReceiver
        mBtScanReceiver = new BtScanReceiver();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBtScanReceiver, intentFilter);

        // Start discovery
        mBluetoothAdapter.startDiscovery();

        return result;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "BluetoothClientService getting destroyed");
        super.onDestroy();
        unregisterReceiver(mBtScanReceiver);

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) { }
        }

        sHandler.getLooper().quit();
    }

    @Override
    protected BroadcastReceiver getServiceMessageReceiver() {
        return new ServiceMessageReceiver();
    }

    /**
     * Callback interface for SocketReader to inform the service of new data
     */
    @Override
    public void onBtMessageReceived(AvalonMessage msg) {
        Log.i(TAG, "broadcasting avalon message " + msg);
        showToast("Received: " + msg);
    }

    // A BroadcastReceiver for our custom messages
    private class ServiceMessageReceiver extends BroadcastReceiver {
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
                Log.d(TAG, "BtScanReceiver found: " + device.getName());

                boolean found = false;
                ParcelUuid[] uuids = device.getUuids();
                if (uuids != null) {
                    for (ParcelUuid uuid : uuids) {
                        if (uuid.getUuid().equals(AvalonActivity.CLIENT_SERVER_UUID)) {
                            mBluetoothAdapter.cancelDiscovery();
                            found = true;
                        }
                    }
                }

                if (!found) {
                    return;
                }

                Log.d(TAG, "Attempting to connect: " + device.getName());
                sHandler.post(new ConnectRunnable(device));
            }
        }
    };

    /**
     * Custom runnable to run connect bt on the background.
     */
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

            Log.d(TAG, "Connection established");

            // Connection established, so stop discovery and set the instance socket
            mBluetoothAdapter.cancelDiscovery();
            mServerSocket = socket;

            // Start the reader thread
            mReader = new SocketReader(mServerSocket, BluetoothClientService.this);
            mReader.start();

            // writer manages a handler, so don't need to start it
            mWriter = new SocketWriter(mServerSocket);

            // Send broadcast that connection has been established
            broadcastConnectionStatus(true, mPlayerInfo);
        }
    }
}
