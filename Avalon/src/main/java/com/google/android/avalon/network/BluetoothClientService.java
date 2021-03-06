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
import android.util.Log;

import com.google.android.avalon.AvalonActivity;
import com.google.android.avalon.controllers.ClientGameStateController;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.messages.PlayerDisconnected;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.ToBtMessageWrapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jinyan on 5/13/14.
 */
public class BluetoothClientService extends BluetoothService {
    private static final String TAG = BluetoothClientService.class.getSimpleName();

    public static final String PLAYER_INFO_KEY = "player_info_key";
    public static final String BLUETOOTH_CLIENT_SERVICE_RESET = "bluetooth_client_service_reset";

    // Handler for running the blocking connect calls. It serializes them so we don't have more
    // than one connect call running at once.
    private Handler mHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private HashSet<String> mSeenAddresses;

    // custom broadcast receiver to detect bluetooth scan
    private BtScanReceiver mBtScanReceiver;

    private BluetoothSocket mServerSocket;
    private SocketReader mReader;
    private SocketWriter mWriter;
    private PlayerInfo mPlayerInfo;
    private Set<PlayerInfo> mPlayerInfoSet; // for convenience

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BluetoothClientService starting");
        mController = ClientGameStateController.get(this);

        int result = super.onStartCommand(intent, flags, startId);

        // Check for required fields for launching this service
        Serializable player = intent.getSerializableExtra(PLAYER_INFO_KEY);
        if (player == null) {
            Log.e(TAG, "BluetoothClientService launched without PlayerInfo");
            return broadcastErrorAndStop();
        }
        mPlayerInfo = (PlayerInfo) player;
        mPlayerInfoSet = new HashSet<PlayerInfo>();
        mPlayerInfoSet.add(mPlayerInfo);
        mSeenAddresses = new HashSet<String>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mServerSocket == null || !mServerSocket.isConnected()) {
            Log.d(TAG, "Starting discovery process");
            resetHandler();

            // First we post all the runnables for paired devices
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                mHandler.post(new ConnectRunnable(device));
            }

            // Register the BroadcastReceiver
            mBtScanReceiver = new BtScanReceiver();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBtScanReceiver, intentFilter);

            // Start discovery
            mBluetoothAdapter.startDiscovery();
        }

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

        mHandler.getLooper().quit();
    }

    @Override
    protected BroadcastReceiver getServiceMessageReceiver() {
        return new ServiceMessageReceiver();
    }

    /**
     * Callback interface for SocketReader to inform the service of new data
     */
    @Override
    public void onBtMessageReceived(BluetoothSocket socket, AvalonMessage msg) {
        Log.i(TAG, "broadcasting avalon message " + msg);
        showToast("Received: " + msg);  // TODO remove me

        notifyControllerAndUi(msg);
    }

    @Override
    public void onSocketClosed(BluetoothSocket socket) {
        try {
            mServerSocket.close();
        } catch (IOException e) { }
        mServerSocket = null;
        notifyControllerAndUi(new PlayerDisconnected(mPlayerInfo));
    }

    /**
     * Helper method to destroy and terminate the old handler and start a new one.
     */
    private void resetHandler() {
        if (mHandler != null) {
            mHandler.getLooper().quit();
        }
        HandlerThread thread = new HandlerThread("Client connection thread");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    // A BroadcastReceiver for our custom messages between app and service
    private class ServiceMessageReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(ServiceMessageProtocol.DATA_WRAPPER_ARRAY_KEY)) {
                ToBtMessageWrapper wrappers = (ToBtMessageWrapper) intent.getSerializableExtra(
                        ServiceMessageProtocol.DATA_WRAPPER_ARRAY_KEY);

                // Tell UI of updates just to keep everything in sync
                broadcastUpdate();

                // Write each message down the socket
                for (int i = 0; i < wrappers.size(); i++) {
                    mWriter.send(wrappers.message.get(i));
                }
            }
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
                showToast("BtScanReceiver found: " + device.getName());

                // Try to connect if we haven't tried already
                if (!mSeenAddresses.contains(device.getAddress())) {
                    mBluetoothAdapter.cancelDiscovery();

                    Log.d(TAG, "Attempting to connect: " + device.getName());
                    mHandler.post(new ConnectRunnable(device));
                }
            }
        }
    };

    /**
     * Custom runnable to run connect bt on the background.
     */
    private class ConnectRunnable implements Runnable {
        private BluetoothDevice mDevice;

        public ConnectRunnable(BluetoothDevice device) {
            mDevice = device;
        }

        public void run() {
            // If we already found a server, let's skip
            if (mServerSocket != null && mServerSocket.isConnected()) {
                return;
            }

            try {
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
                    } catch (IOException el) {
                    }
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
                mWriter = new SocketWriter(mServerSocket, BluetoothClientService.this);

                // Send out client information
                mWriter.send(mPlayerInfo);

                // Notify the controller of the change
                notifyControllerAndUi(mPlayerInfo);

                // Unregister the receiver since we found all the players we need
                unregisterReceiver(mBtScanReceiver);
            } finally {
                if (mServerSocket == null) {
                    mSeenAddresses.add(mDevice.getAddress());
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    }
}
