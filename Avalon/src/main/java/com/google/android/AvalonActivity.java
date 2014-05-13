package com.google.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.fragments.ClientFragment;
import com.google.android.fragments.RoleSelectionFragment;
import com.google.android.fragments.ServerFragment;
import com.google.android.fragments.SetupClientFragment;
import com.google.android.fragments.SetupServerFragment;
import com.google.android.interfaces.BluetoothController;
import com.google.android.interfaces.RoleController;

import java.util.UUID;


public class AvalonActivity extends Activity implements RoleController, BluetoothController {
    private static final int REQUEST_ENABLE_BT = 1;

    public static final UUID CLIENT_SERVER_UUID = new UUID(123456789, 987654321);
    public static final String TAG = AvalonActivity.class.getSimpleName();

    protected BluetoothAdapter mBluetoothAdapter;
    protected BluetoothSocket mServerSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avalon);

        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentById(R.id.fragment_container) == null) {
            // Initially show the role selection fragment if fragment is null
            RoleSelectionFragment fragment = new RoleSelectionFragment();
            fragment.setRoleController(this);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        // Check for bluetooth adapter and retrieve it
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Bluetooth is not supported, gracefully exit
            bluetoothNotSupported();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) {
                bluetoothNotSupported();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRoleSelected(Role role) {
        Fragment fragment = null;
        if (role == Role.SERVER) {
            fragment = new SetupServerFragment();
            ((SetupServerFragment) fragment).setBtController(this);
        } else {
            fragment = new SetupClientFragment();
            ((SetupClientFragment) fragment).setBtController(this);
        }

        // Instantiate and show the new fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Helper function to show a dialog that tells the user bluetooth is required for this app
     * and then exit the app.
     */
    protected void bluetoothNotSupported() {
        // TODO
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    @Override
    public void setServerSocket(BluetoothSocket socket) {
        mServerSocket = socket;
    }

    @Override
    public void addClientSocket(BluetoothSocket socket) {
        // TODO
    }

}
