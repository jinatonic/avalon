package com.google.android.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.AvalonActivity;
import com.google.android.R;
import com.google.android.interfaces.BluetoothController;

import java.io.IOException;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupClientFragment extends Fragment {

    private BluetoothController mBtController;

    private TextView mStatusTextView;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);

                new Thread(new Runnable() {
                    public void run() {
                        BluetoothSocket socket = null;
                        try {
                            socket = device.createRfcommSocketToServiceRecord(
                                    AvalonActivity.CLIENT_SERVER_UUID);
                        } catch (IOException e) {
                            return;
                        }

                        try {
                            // Try to establish the connection with the server
                            socket.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                socket.close();
                            } catch (IOException el) { }
                            return;
                        }

                        // Connection established, so stop discovery and set the instance socket
                        mBtController.getBluetoothAdapter().cancelDiscovery();
                        mBtController.setServerSocket(socket);

                        show(true /* discovered */);
                    }
                });

            }
        }
    };

    public void setBtController(BluetoothController controller) {
        mBtController = controller;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.client_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.client_status_text);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Start discovery
        mBtController.getBluetoothAdapter().startDiscovery();
        show(false /* discovered */);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mBtController.getBluetoothAdapter().cancelDiscovery();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void show(boolean discovered) {
        mStatusTextView.setText(discovered ?
            "Connected!" : "Searching for server...");
    }
}
