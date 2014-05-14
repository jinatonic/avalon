package com.google.android.avalon.network;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.android.avalon.interfaces.BluetoothController;
import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.MessageParser;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jinyan on 5/13/14.
 */
public class SocketWriter {
    private static final String TAG = SocketWriter.class.getSimpleName();

    private final BluetoothSocket mSocket;
    private final BluetoothController mController;

    private OutputStream mOs;
    private Handler mHandler;

    public SocketWriter(BluetoothSocket socket, BluetoothController controller) {
        mSocket = socket;
        mController = controller;
        try {
            mOs = socket.getOutputStream();

            HandlerThread thread = new HandlerThread("SocketWriter Thread");
            thread.start();
            mHandler = new Handler(thread.getLooper());
        } catch (IOException e) {
            e.printStackTrace();
            mController.onSocketClosed(mSocket);
        }
    }

    public void terminate() {
        if (mOs != null) {
            try {
                mOs.close();
            } catch (IOException e) { }
        }
        if (mHandler != null) {
            mHandler.getLooper().quit();
        }
    }

    public void send(final AvalonMessage msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSocket.isConnected() && mOs != null) {
                    try {
                        byte[] data = MessageParser.construct(msg);
                        if (data != null) {
                            mOs.write(data);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
