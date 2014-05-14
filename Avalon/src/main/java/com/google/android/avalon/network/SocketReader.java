package com.google.android.avalon.network;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.MessageParser;
import com.google.android.avalon.interfaces.AvalonMessageHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jinyan on 5/13/14.
 */
public class SocketReader extends Thread {
    private static final String TAG = SocketReader.class.getSimpleName();

    private final BluetoothSocket mSocket;
    private final AvalonMessageHandler mHandler;

    public SocketReader(BluetoothSocket socket, AvalonMessageHandler handler) {
        mSocket = socket;
        mHandler = handler;
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
                    mHandler.onBtMessageReceived(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "SocketReader is terminating");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { }
            }
        }
    }

}
