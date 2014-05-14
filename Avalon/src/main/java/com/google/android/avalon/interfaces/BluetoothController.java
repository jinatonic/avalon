package com.google.android.avalon.interfaces;

import android.bluetooth.BluetoothSocket;

import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.PlayerInfo;

/**
 * Created by jinyan on 5/14/14.
 */
public interface BluetoothController {
    public void onSocketClosed(BluetoothSocket socket);
    public void onBtMessageReceived(BluetoothSocket socket, AvalonMessage msg);
}
