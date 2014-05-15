package com.google.android.avalon.network;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.android.avalon.interfaces.BluetoothController;
import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.MessageParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jinyan on 5/13/14.
 */
public class SocketReader extends Thread {
    private static final String TAG = SocketReader.class.getSimpleName();

    private final BluetoothSocket mSocket;
    private final BluetoothController mController;

    public SocketReader(BluetoothSocket socket, BluetoothController controller) {
        mSocket = socket;
        mController = controller;
    }

    public void terminate() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) { }
        }
    }

    /**
     * The thread will simply keep running until the socket gets closed or IOException occurs on
     * the input stream.
     */
    @Override
    public void run() {
        Log.d(TAG, "SocketReader is starting");
        InputStream is = null;
        try {
            is = mSocket.getInputStream();

            int bytes = 0;
            byte[] buffer = new byte[MessageParser.MAX_NUM_BYTES];
            while (mSocket.isConnected() && bytes >= 0) {
                bytes = is.read(buffer);
                AvalonMessage msg = MessageParser.parse(buffer);
                if (msg != null) {
                    mController.onBtMessageReceived(mSocket, msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "SocketReader is terminating");
            mController.onSocketClosed(mSocket);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { }
            }
        }
    }

}
