package com.google.android.avalon.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.avalon.AvalonActivity;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.PlayerDisconnected;
import com.google.android.avalon.model.PlayerInfo;
import com.google.android.avalon.controllers.ServerGameStateController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jinyan on 5/13/14.
 */
public class BluetoothServerService extends BluetoothService {
    private static final String TAG = BluetoothServerService.class.getSimpleName();

    public static final String NUM_PLAYERS_KEY = "num_players_key";

    private ServerGameStateController mServerGameStateController;

    private int mNumPlayers;
    private Map<PlayerInfo, BluetoothSocket> mPlayerSocketMap =
            new HashMap<PlayerInfo, BluetoothSocket>();
    private Map<BluetoothSocket, SocketReaderWriterWrapper> mSocketReaderWriterMap =
            new HashMap<BluetoothSocket, SocketReaderWriterWrapper>();

    private AcceptThread mAcceptThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BluetoothServerService starting");
        mServerGameStateController = ServerGameStateController.get(this);
        mMessageListener = mServerGameStateController;

        int result = super.onStartCommand(intent, flags, startId);

        mNumPlayers = intent.getIntExtra(NUM_PLAYERS_KEY, 0);
        if (mNumPlayers <= 0) {
            Log.e(TAG, "BluetoothServerService launched with invalid number of players (or none)");
            return broadcastErrorAndStop();
        }


        if (mSocketReaderWriterMap.size() < mNumPlayers) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        return result;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "BluetoothServerService getting destroyed");
        super.onDestroy();
        mAcceptThread.cancel();
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
        showToast("Received: " + msg);

        // case through each message types
        if (msg instanceof PlayerInfo) {
            mPlayerSocketMap.put((PlayerInfo) msg, socket);
        }

        notifyControllerAndUi(msg);
    }

    @Override
    public void onSocketClosed(BluetoothSocket socket) {
        try {
            socket.close();
        } catch (IOException e) { }

        // remove from socket reader writer map
        if (mSocketReaderWriterMap.containsKey(socket)) {
            mSocketReaderWriterMap.get(socket).reader.interrupt();
            mSocketReaderWriterMap.get(socket).writer.terminate();
            mSocketReaderWriterMap.remove(socket);
        }

        // remove from player infos
        PlayerInfo oldInfo = null;
        for (PlayerInfo info : mPlayerSocketMap.keySet()) {
            if (mPlayerSocketMap.get(info) == socket) {
                oldInfo = info;
                mPlayerSocketMap.remove(info);
                break;
            }
        }

        if (oldInfo != null) {
            PlayerDisconnected disconnected = new PlayerDisconnected(oldInfo);
            notifyControllerAndUi(disconnected);
        }
    }

    // A BroadcastReceiver for our custom messages
    private class ServiceMessageReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            // TODO: handle
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(
                        "Avalon", AvalonActivity.CLIENT_SERVER_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                Log.i(TAG, "Waiting for connection...");
                try {
                    socket = mmServerSocket.accept();
                    Log.i(TAG, "Accept returned");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Start the reader thread
                    SocketReader reader = new SocketReader(socket, BluetoothServerService.this);
                    reader.start();

                    // writer manages a handler, so don't need to start it
                    SocketWriter writer = new SocketWriter(socket, BluetoothServerService.this);

                    mSocketReaderWriterMap.put(socket,
                            new SocketReaderWriterWrapper(reader, writer));

                    // check if we need to listen for more connections
                    if (mSocketReaderWriterMap.size() >= mNumPlayers) {
                        // We are done, close the server socket and break out of the accept loop
                        // TODO: what to do where there are more players?
                        try {
                            mmServerSocket.close();
                        } catch (IOException e) {
                            return;
                        }
                        break;
                    }
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private static class SocketReaderWriterWrapper {
        SocketReader reader;
        SocketWriter writer;

        SocketReaderWriterWrapper(SocketReader r, SocketWriter w) {
            reader = r;
            writer = w;
        }
    }
}