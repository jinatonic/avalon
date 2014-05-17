package com.google.android.avalon;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.R;
import com.google.android.avalon.controllers.ServerGameStateController;
import com.google.android.avalon.fragments.SetupServerFragment;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.messages.PlayerInfo;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/*
 *  Provides a convenient way to manually test specific fragments without navigating the real
 *  workflow.
 *
 * Typical usage:
 *   1) Change getFragment to return the fragment you are interested in.
 *   2) Use Run to build the APK and push it to your device/emulator.
 *   3) Run: adb shell am start -n com.google.android/com.google.android.avalon.ScratchpadActivity
 *
 */
public class ScratchpadActivity extends Activity {

    private Fragment getFragment() {
        return new SetupServerFragment();
    }

    private int playerNum = 0;
    private SetupServerFragment frag = new SetupServerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_avalon);
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = getFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

        final SetupServerFragment f2 = (SetupServerFragment) fragment;
        TimerTask t = new TimerTask() {

            @Override
            public void run() {

                List<PlayerInfo> players = ServerGameStateController.get(ScratchpadActivity.this).getCurrentGameState().players;
                Set<AvalonRole> specialRoles = ServerGameStateController.get(ScratchpadActivity.this).getConfig().specialRoles;
                if (new Random().nextInt(10) < 7 && players.size() < 9) {
                    PlayerInfo pi = new PlayerInfo("Player " + playerNum++);
                    //specialRoles.add(AvalonRole.MERLIN);
                    players.add(pi);
                    Log.i("MWALL DEBUG", "Adding player");
                }
                else if (players.size() > 0) {
                    //specialRoles.remove(AvalonRole.MERLIN);
                    players.remove(players.size() - 1);
                    Log.i("MWALL DEBUG", "Dropping player");
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                f2.update();
                            }
                        }
                );
            }
        };

        new Timer().schedule(t, 0, 3000);
    }
}
