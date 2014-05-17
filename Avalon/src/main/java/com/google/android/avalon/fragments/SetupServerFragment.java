package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.avalon.AvalonActivity;
import com.google.android.R;
import com.google.android.avalon.controllers.ServerGameStateController;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.rules.AssignmentFactory;
import com.google.android.avalon.rules.IllegalConfigurationException;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.avalon.rules.AssignmentFactory.MAX_PLAYERS;
import static com.google.android.avalon.rules.AssignmentFactory.MIN_PLAYERS;

/**
 * Created by jinyan on 5/12/14.
 */
public class SetupServerFragment extends Fragment {

    // UI state variables and pointers
    private Spinner mNumPlayers;
    private GridView mSelectPlayers;
    private Button mStartGame;
    private PlayerAdapter mPlayerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.server_setup_fragment, parent, false);
        setupNumPlayers(v);
        setupSelectPlayers(v);
        setupRoleSelections(v);
        setupStartButton(v);

        return v;
    }

    public void update() {
        // This is the only data that should be affected by the clients.
        mPlayerAdapter.notifyDataSetChanged();
    }

    private void setupNumPlayers(View v) {
        // Populate options.
        List<NumPlayersChoice> options = new ArrayList<NumPlayersChoice>();
        options.add(new NumPlayersChoice("Num Players", 0));
        for (int i = MIN_PLAYERS; i <= MAX_PLAYERS; i++) {
            options.add(new NumPlayersChoice("" + i, i));
        }
        final ArrayAdapter<NumPlayersChoice> adapter = new ArrayAdapter<NumPlayersChoice>(
                getActivity(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNumPlayers = (Spinner) v.findViewById(R.id.num_players_widget);
        mNumPlayers.setAdapter(adapter);

        // Set state from model.
        for (int i = 0; i < adapter.getCount(); i++) {
            if (getConfig().numPlayers == adapter.getItem(i).value) {
                mNumPlayers.setSelection(i);
            }
        }

        // Set logic to respond to user selections.
        mNumPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateStartButton();
                getConfig().numPlayers = adapter.getItem(i).value;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mStartGame.setEnabled(false);
                getConfig().numPlayers = 0;
            }
        });
    }

    private class NumPlayersChoice {
        String label;
        int value;

        private NumPlayersChoice(String label, int value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void setupSelectPlayers(View v) {
        mSelectPlayers = (GridView) v.findViewById(R.id.select_players_widget);
        mPlayerAdapter = new PlayerAdapter();
        mSelectPlayers.setAdapter(mPlayerAdapter);
    }

    private class PlayerAdapter extends ArrayAdapter<PlayerInfo> {

        public PlayerAdapter() {
            super(getActivity(), 0, getPlayers());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.select_player, null);
                ((CheckBox)convertView).setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            PlayerInfo playerInfo = getItem(position);
                            playerInfo.participating = b;
                        }
                    });
            }
            PlayerInfo player = getItem(position);
            ((CheckBox)convertView).setChecked(player.participating);
            ((CheckBox)convertView).setText(player.name);
            return convertView;
        }
    }

    // TODO add element for lady of the lake.
    private void setupRoleSelections(View v) {
        setupRoleSelection(v, AvalonRole.MERLIN, R.id.merlin_selected_widget);
        setupRoleSelection(v, AvalonRole.PERCIVAL, R.id.percival_selected_widget);
        setupRoleSelection(v, AvalonRole.ASSASSIN, R.id.assassin_selected_widget);
        setupRoleSelection(v, AvalonRole.MORDRED, R.id.mordred_selected_widget);
        setupRoleSelection(v, AvalonRole.MORGANA, R.id.morgana_selected_widget);
        setupRoleSelection(v, AvalonRole.OBERON, R.id.oberon_selected_widget);
    }

    private void setupRoleSelection(View v, final AvalonRole role, int resourceId) {
        CheckBox checkBox = (CheckBox) v.findViewById(resourceId);
        checkBox.setChecked(getConfig().specialRoles.contains(role));
        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked) {
                            getConfig().specialRoles.add(role);
                        } else {
                            getConfig().specialRoles.remove(role);
                        }
                    }
                }
        );
    }

    private void setupStartButton(View v) {
        mStartGame = (Button) v.findViewById(R.id.start_game_widget);
        updateStartButton();
        mStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }

    private GameConfiguration getConfig() {
        return ServerGameStateController.get(getActivity()).getConfig();
    }

    private List<PlayerInfo> getPlayers() {
        return ServerGameStateController.get(getActivity()).getCurrentGameState().players;
    }

    private void updateStartButton() {
        mStartGame.setEnabled(readyToStart());
    }

    private boolean readyToStart() {
       NumPlayersChoice choice = (NumPlayersChoice) mNumPlayers.getSelectedItem();
       return choice.value >= MIN_PLAYERS && getParticipatingPlayers().size() == choice.value;
    }

    private List<PlayerInfo> getParticipatingPlayers() {
        List<PlayerInfo> result = new ArrayList<PlayerInfo>();
        for (PlayerInfo pi : getPlayers()) {
            if (pi.participating) {
                result.add(pi);
            }
        }
        return result;
    }

    private void startGame() {
        try {
            InitialAssignments assignments = new AssignmentFactory(getConfig())
                    .getAssignments(getParticipatingPlayers());

            // TODO remove this (of course)
            StringBuilder sb = new StringBuilder();
            for (RoleAssignment ra : assignments.assignments) {
                sb.append(ra.player.name + " is " + ra.role + "\n");
            }
            sb.append(assignments.king.name + " is king.");
            Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_LONG).show();
        } catch (IllegalConfigurationException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
