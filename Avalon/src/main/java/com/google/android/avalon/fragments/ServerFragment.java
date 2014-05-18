package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.R;
import com.google.android.avalon.controllers.ServerGameStateController;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.ServerGameState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jinyan on 5/12/14.
 */
public class ServerFragment extends Fragment {

    private ServerGameStateController mServerGameStateController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mServerGameStateController = ServerGameStateController.get(getActivity());
        View v = inflater.inflate(R.layout.server_fragment, parent, false);
        return v;
    }


    public void update() {
        ServerGameState gameState = mServerGameStateController.getCurrentGameState();
        // TODO complete the update function
    }

}
