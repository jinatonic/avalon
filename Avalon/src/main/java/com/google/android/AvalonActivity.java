package com.google.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.fragments.ClientFragment;
import com.google.android.fragments.RoleSelectionFragment;
import com.google.android.fragments.ServerFragment;
import com.google.android.interfaces.RoleController;


public class AvalonActivity extends Activity implements RoleController {

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
    }

    @Override
    public void onRoleSelected(Role role) {
        Fragment fragment = null;
        if (role == Role.SERVER) {
            fragment = new ServerFragment();
        } else {
            fragment = new ClientFragment();
        }

        // Instantiate and show the new fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
