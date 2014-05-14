package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.R;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jinyan on 5/12/14.
 */
public class ServerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.server_fragment, parent, false);
        return v;
    }


}
