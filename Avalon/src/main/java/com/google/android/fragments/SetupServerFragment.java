package com.google.android.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
public class SetupServerFragment extends Fragment {

    private BluetoothController mBtController;
    private TextView mStatusTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mBtController = (BluetoothController) getActivity();

        View v = inflater.inflate(R.layout.server_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.server_status_text);

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        new AcceptThread().start();

        Log.i(AvalonActivity.TAG, "onCreate complete.");
        return v;
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        mStatusTextView.setText("Connection established");
        Log.i(AvalonActivity.TAG, "Connection established");
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBtController.getBluetoothAdapter().listenUsingRfcommWithServiceRecord(
                        "Avalon", AvalonActivity.CLIENT_SERVER_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                Log.i(AvalonActivity.TAG, "Waiting for connection...");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusTextView.setText("Waiting for connection...");
                    }
                });
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        return;
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
}
