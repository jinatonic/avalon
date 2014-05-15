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

    private ServerGameStateController mServerGameStateController;

    // UI state variables and pointers
    private TextView mStatusTextView;
    private Spinner mNumPlayers;
    private ListView mSelectPlayers;
    private Button mStartGame;

    private GameConfiguration mGameConfig = new GameConfiguration();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mServerGameStateController = ServerGameStateController.get(getActivity());

        View v = inflater.inflate(R.layout.server_setup_fragment, parent, false);
        mStatusTextView = (TextView) v.findViewById(R.id.server_status_text);

        // TODO restore UI state from mGameConfig (e.g. on rotation)
        setupNumPlayers(v);
        setupSelectPlayers(v);
        setupRoleSelections(v);
        setupStartButton(v);
        update();

        Log.i(AvalonActivity.TAG, "onCreate complete.");
        return v;
    }

    public void update() {
        ServerGameState gameState = mServerGameStateController.getCurrentGameState();
        // TODO
    }

    private void setupNumPlayers(View v) {
        List<NumPlayersChoice> options = new ArrayList<NumPlayersChoice>();
        options.add(new NumPlayersChoice("Num Players", 0));
        for (int i = MIN_PLAYERS; i <= MAX_PLAYERS; i++) {
            options.add(new NumPlayersChoice("" + i, i));
        }

        final ArrayAdapter adapter = new ArrayAdapter(
                getActivity(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNumPlayers = (Spinner) v.findViewById(R.id.num_players_widget);
        mNumPlayers.setAdapter(adapter);

        mNumPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateStartButton();
                mGameConfig.numPlayers = ((NumPlayersChoice)adapter.getItem(i)).value;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mStartGame.setEnabled(false);
                mGameConfig.numPlayers = 0;
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
        // TODO refactor GameConfiguration to store set of AvalonRoles instead of having
        // separate booleans. We could get much better code reuse here.
        ((CheckBox)v.findViewById(R.id.merlin_selected_widget)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        mGameConfig.includeMerlin = checked;
                    }
                }
        );
        ((CheckBox)v.findViewById(R.id.assassin_selected_widget)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        mGameConfig.includeAssassin = checked;
                    }
                }
        );
        ((CheckBox)v.findViewById(R.id.percival_selected_widget)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        mGameConfig.includePercival = checked;
                    }
                });
        ((CheckBox)v.findViewById(R.id.mordred_selected_widget)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        mGameConfig.includeMordred = checked;
                    }
                });
        ((CheckBox)v.findViewById(R.id.mordred_selected_widget)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        mGameConfig.includeMorgana = checked;
                    }
                });
        ((CheckBox)v.findViewById(R.id.oberon_selected_widget)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        mGameConfig.includeOberon = checked;
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

    // TODO get these from the clients.
    private List<PlayerInfo> getPlayers() {
        List<PlayerInfo> result = new ArrayList<PlayerInfo>();
        for (int i = 1; i <= 8; i++) {
            result.add(new PlayerInfo("Player " + i));
        }
        return result;
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
            playerSelected.setChecked(pi.participating);

            playerSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
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
            InitialAssignments assignments = new AssignmentFactory(mGameConfig)
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
