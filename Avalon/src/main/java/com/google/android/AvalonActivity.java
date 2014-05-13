package com.google.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.fragments.RoleSelectionFragment;
import com.google.android.fragments.SetupClientFragment;
import com.google.android.fragments.SetupFragment;
import com.google.android.fragments.SetupServerFragment;
import com.google.android.interfaces.RoleController;

import java.util.UUID;


public class AvalonActivity extends Activity implements RoleController {
    private static final int REQUEST_ENABLE_BT = 1;

    public static final UUID CLIENT_SERVER_UUID = new UUID(123456789, 987654321);
    public static final String TAG = AvalonActivity.class.getSimpleName();

    private SetupFragment mSetupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avalon);

        // Resume fragment states and reset pointers
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            // Initially show the role selection fragment if fragment is null
            RoleSelectionFragment frag = new RoleSelectionFragment();
            frag.setRoleController(this);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragment instanceof SetupFragment) {
            mSetupFragment = (SetupFragment) fragment;
        }

        // Check for bluetooth adapter and retrieve it
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported, gracefully exit
            bluetoothNotSupported();
        }

        else if (!bluetoothAdapter.isEnabled()) {
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
        if (role == Role.SERVER) {
            mSetupFragment = new SetupServerFragment();
        } else {
            mSetupFragment = new SetupClientFragment();
        }

        // Instantiate and show the new fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSetupFragment)
                .commit();
    }

    /**
     * Helper function to show a dialog that tells the user bluetooth is required for this app
     * and then exit the app.
     */
    protected void bluetoothNotSupported() {
        Log.e(TAG, "bluetoothNotSupported not implemented");
        // TODO
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ServiceMessageProtocol.FROM_BT_SERVICE_INTENT)) {
                // TODO
            }
        }
    }

}
