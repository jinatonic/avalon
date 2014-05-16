package com.google.android.avalon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.R;
import com.google.android.avalon.controllers.ClientGameStateController;
import com.google.android.avalon.controllers.ServerGameStateController;
import com.google.android.avalon.fragments.ClientFragment;
import com.google.android.avalon.fragments.RoleSelectionFragment;
import com.google.android.avalon.fragments.ServerFragment;
import com.google.android.avalon.fragments.SetupClientFragment;
import com.google.android.avalon.fragments.SetupServerFragment;
import com.google.android.avalon.interfaces.RoleSelectorCallback;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.network.BluetoothClientService;
import com.google.android.avalon.network.BluetoothServerService;
import com.google.android.avalon.network.ServiceMessageProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class AvalonActivity extends Activity implements RoleSelectorCallback {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String IS_SERVER_KEY = "is_server_key";
    private static final String GAME_PROGRESSION_KEY = "game_progression_key";

    private static final int ROLE_SELECTION = 0;
    private static final int SETTING_UP_BT = 1;
    private static final int GAME_IN_PROGRESS = 2;

    public static final UUID CLIENT_SERVER_UUID = new UUID(123456789, 987654321);

    public static final String TAG = AvalonActivity.class.getSimpleName();

    private BroadcastReceiver mReceiver;

    private boolean mIsServer;
    private int mGameProgression;

    // Fragment pointers
    private RoleSelectionFragment mRoleSelectionFragment;
    private SetupServerFragment mSetupServerFragment;
    private SetupClientFragment mSetupClientFragment;
    private ServerFragment mServerFragment;
    private ClientFragment mClientFragment;

    // Game controller pointers
    private ServerGameStateController mServerGameStateController;
    private ClientGameStateController mClientGameStateController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avalon);

        if (savedInstanceState != null) {
            mIsServer = savedInstanceState.getBoolean(IS_SERVER_KEY);
            mGameProgression = savedInstanceState.getInt(GAME_PROGRESSION_KEY);
        } else {
            mIsServer = false;  // will be replaced later anyway
            mGameProgression = ROLE_SELECTION;
        }

        // We always instantiate everything just for simplicity. Memory overhead should be minimal
        // since for the fragments we don't actually inflate any of the views unnecessarily.
        mRoleSelectionFragment = new RoleSelectionFragment();
        mSetupServerFragment = new SetupServerFragment();
        mSetupClientFragment = new SetupClientFragment();
        mServerFragment = new ServerFragment();
        mClientFragment = new ClientFragment();

        mServerGameStateController = ServerGameStateController.get(this);
        mClientGameStateController = ClientGameStateController.get(this);

        mReceiver = new BtMessageReceiver();
        IntentFilter filter = new IntentFilter(ServiceMessageProtocol.FROM_BT_SERVICE_INTENT);
        registerReceiver(mReceiver, filter);

        // Resume fragment states and reset pointers
        updateFragment();

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
    protected void onResume() {
        super.onResume();
        if (mIsServer) {
            mServerGameStateController.isForeground(true);
        } else {
            mClientGameStateController.isForeground(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsServer) {
            mServerGameStateController.isForeground(false);
        } else {
            mClientGameStateController.isForeground(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_SERVER_KEY, mIsServer);
        outState.putInt(GAME_PROGRESSION_KEY, mGameProgression);
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

    /**
     * This function basically tells the appropriate fragment to update its data from the
     * game state controller.
     */
    private void notifyDataChanged() {
        switch (mGameProgression) {
            case ROLE_SELECTION:
                // Should never happen, can't have any data without even choosing the role
                break;
            case SETTING_UP_BT:
                // Let's first check if the game has started and progress if so
                if ((mIsServer && mServerGameStateController.started()) ||
                        (!mIsServer && mClientGameStateController.started())) {
                    mGameProgression = GAME_IN_PROGRESS;
                    updateFragment();
                    // Don't need to actually notify data change because the fragment change will
                    // do that automatically in onCreateView
                    break;
                }
                if (mIsServer) {
                    mSetupServerFragment.update();
                } else {
                    mSetupClientFragment.update();
                }
                break;
            case GAME_IN_PROGRESS:
                if (mIsServer) {
                    mSetupServerFragment.update();
                } else {
                    mSetupClientFragment.update();
                }
                break;
            default:
                Log.w(TAG, "game progression not recognized: " + mGameProgression);
                break;
        }
    }

    private void updateFragment() {
        Fragment frag = null;
        switch (mGameProgression) {
            case ROLE_SELECTION:
                frag = mRoleSelectionFragment;
                break;
            case SETTING_UP_BT:
                frag = (mIsServer) ? mSetupServerFragment : mSetupClientFragment;
                break;
            case GAME_IN_PROGRESS:
                frag = (mIsServer) ? mServerFragment : mClientFragment;
                break;
            default:
                Log.w(TAG, "game progression not recognized: " + mGameProgression);
                break;
        }

        if (frag != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commit();
        }
    }

    @Override
    public void onRoleSelected(boolean isServer) {
        Log.d(TAG, "on role selected: isServer? " + isServer);
        // Set up the appropriate fragments and initialize the services
        mIsServer = isServer;
        if (isServer) {
            // TODO add flag to prevent spamming this.
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

            Intent i = new Intent(this, BluetoothServerService.class);
            // TODO: add input for this
            i.putExtra(BluetoothServerService.NUM_PLAYERS_KEY, 2);
            startService(i);

        } else {
            DialogFragment dialog = new NameInputDialog();
            dialog.show(getFragmentManager(), "name_input_dialog");
        }

        mGameProgression = SETTING_UP_BT;
        updateFragment();
    }

    /**
     * Helper function to show a dialog that tells the user bluetooth is required for this app
     * and then exit the app.
     */
    private void bluetoothNotSupported() {
        FragmentManager fm = getFragmentManager();
        DialogFragment dialog = new ShowBtErrorFragment();
        dialog.show(fm, "error_dialog");
    }

    private class BtMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(ServiceMessageProtocol.DATA_CHANGED)) {
                notifyDataChanged();
            }
        }
    }

    /**
     * Dialog to show an error message associated with bluetooth requirement.
     */
    public static class ShowBtErrorFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Bluetooth required")
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

    /**
     * Dialog to show an input box for player's name.
     */
    public static class NameInputDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final EditText v = new EditText(getActivity());
            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle("Please input your name")
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .create();

            // We attach button listener this way so it won't get dismissed until the user
            // inputs the appropriate name
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String name = v.getText().toString();
                            if (name != null && !name.isEmpty()) {
                                Intent intent = new Intent(getActivity(),
                                        BluetoothClientService.class);
                                PlayerInfo info = new PlayerInfo(v.getText().toString());
                                intent.putExtra(BluetoothClientService.PLAYER_INFO_KEY, info);
                                getActivity().startService(intent);

                                //Dismiss once everything is OK.
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });

            return dialog;
        }
    }
}
