package com.google.android.fragments;

import android.app.Fragment;

import com.google.android.interfaces.BluetoothController;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupServerFragment extends Fragment {

    private BluetoothController mBtController;

    public void setBtController(BluetoothController controller) {
        mBtController = controller;
    }
}
