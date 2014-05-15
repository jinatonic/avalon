package com.google.android.avalon.interfaces;

import android.bluetooth.BluetoothSocket;

import com.google.android.avalon.model.messages.AvalonMessage;

/**
 * Created by jinyan on 5/14/14.
 */
public interface BluetoothController {
    public void onSocketClosed(BluetoothSocket socket);
    public void onBtMessageReceived(BluetoothSocket socket, AvalonMessage msg);
}
