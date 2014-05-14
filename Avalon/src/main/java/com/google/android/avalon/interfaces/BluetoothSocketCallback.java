package com.google.android.avalon.interfaces;

import android.bluetooth.BluetoothSocket;

/**
 * Created by jinyan on 5/14/14.
 */
public interface BluetoothSocketCallback {
    public void onSocketClosed(BluetoothSocket socket);
}
