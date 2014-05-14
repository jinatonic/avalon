package com.google.android.avalon.network;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.MessageParser;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jinyan on 5/13/14.
 */
public class SocketWriter {
    private static final String TAG = SocketWriter.class.getSimpleName();

    private static final Handler sHandler;
    static {
        HandlerThread thread = new HandlerThread("SocketWriter Thread");
        thread.start();
        sHandler = new Handler(thread.getLooper());
    }

    private final BluetoothSocket mSocket;
    private OutputStream mOs;

    public SocketWriter(BluetoothSocket socket) {
        mSocket = socket;
        try {
            mOs = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            mOs = null;
        }
    }

    public void send(final AvalonMessage msg) {
        sHandler.post(new Runnable() {
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
