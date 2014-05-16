package com.google.android.avalon;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.google.android.R;
import com.google.android.avalon.fragments.SetupServerFragment;

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
    }
}
