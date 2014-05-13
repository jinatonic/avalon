package com.google.android.avalon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.R;
import com.google.android.avalon.fragments.ClientFragment;
import com.google.android.avalon.fragments.RoleSelectionFragment;
import com.google.android.avalon.fragments.SetupClientFragment;
import com.google.android.avalon.fragments.SetupServerFragment;
import com.google.android.avalon.interfaces.GameStateController;
import com.google.android.avalon.model.RoleAssignment;

import java.util.UUID;


public class AvalonActivity extends Activity implements GameStateController {
    private static final int REQUEST_ENABLE_BT = 1;

    public static final UUID CLIENT_SERVER_UUID = new UUID(123456789, 987654321);
    public static final String TAG = AvalonActivity.class.getSimpleName();

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
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
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
    public void onRoleSelected(boolean isServer) {
        Log.d(TAG, "on role selected: isServer? " + isServer);
        Fragment fragment;
        if (isServer) {
            fragment = new SetupServerFragment();
        } else {
            fragment = new SetupClientFragment();
        }

        // Instantiate and show the new fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onSetupCompleted(RoleAssignment role) {
        Log.d(TAG, "onSetupCompleted: " + role);
        Fragment fragment = new ClientFragment();

        // Show the new client fragment after the game starts
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Helper function to show a dialog that tells the user bluetooth is required for this app
     * and then exit the app.
     */
    protected void bluetoothNotSupported() {
        FragmentManager fm = getFragmentManager();
        DialogFragment dialog = new ShowBtErrorFragment();
        dialog.show(fm, "error");
    }

    private class ShowBtErrorFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Bluetooth required.")
                    .setNeutralButton("OK", new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getActivity().finish();
                        }
                    })
                    .setCancelable(false)
                    .create();
        }
    }
}
