package com.google.android.interfaces;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

/**
 * Created by jinyan on 5/12/14.
 */
public interface BluetoothController {

    public BluetoothAdapter getBluetoothAdapter();

    public boolean hasServerSocket();
    public void setServerSocket(BluetoothSocket socket);
    public void addClientSocket(BluetoothSocket socket);
}
