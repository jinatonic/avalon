package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private ListView mSelectPlayers;
    private Button mStartGame;

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
        // TODO force UI update (model changed)
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
        mSelectPlayers = (ListView) v.findViewById(R.id.select_players_widget);
        mSelectPlayers.setAdapter(new PlayerAdapter(getPlayers()));
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

    private class PlayerAdapter extends ArrayAdapter<PlayerInfo> {
        public PlayerAdapter(List<PlayerInfo> players) {
            super(getActivity(), 0, players);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.select_player, null);
            }

            final PlayerInfo pi = getItem(position);

            ((TextView)convertView.findViewById(R.id.player_name_widget)).setText(pi.name);
            CheckBox playerSelected = (CheckBox) convertView.findViewById(R.id.player_selected_widget);
            Log.i("MWALL DEBUG", "Player " + pi.name + " is participating? " + pi.participating);
            playerSelected.setChecked(pi.participating);

            playerSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    Log.i("MWALL DEBUG", "SET player " + pi.name + " to participating " + checked);
                    Log.i("MWALL DEBUG", "BUTTON TEXT: " + compoundButton.getText() + " PI NAME: " + pi.name);
                    compoundButton.setText(pi.name);
                    pi.participating = checked;
                    updateStartButton();
                }
            });

            return convertView;
        }
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
        PlayerAdapter playerAdapter = (PlayerAdapter) mSelectPlayers.getAdapter();
        for (int i = 0; i < playerAdapter.getCount(); i++) {
            if (playerAdapter.getItem(i).participating) {
                result.add(playerAdapter.getItem(i));
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
