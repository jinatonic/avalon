package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.avalon.AvalonActivity;
import com.google.android.R;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupServerFragment extends Fragment {

    private TextView mStatusTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.server_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.server_status_text);

        Log.i(AvalonActivity.TAG, "onCreate complete.");
        return v;
    }
}
