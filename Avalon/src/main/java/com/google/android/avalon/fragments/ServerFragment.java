package com.google.android.avalon.fragments;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.controllers.ServerGameStateController;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.utils.AnimationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jinyan on 5/12/14.
 */
public class ServerFragment extends Fragment {
    private static final String TAG = "ServerFragment";

    private static final int MAX_ALLOWED_CHARS = 10;

    private static final String JUST_PROPOSED_TEAM_KEY = "just_proposed_team";
    private static final String JUST_RAN_QUEST_KEY = "just_ran_quest";
    private static final String GAME_OVER_KEY = "game_over";
    private static final String SELECTED_PLAYER_MAP_KEY = "selected_player_map";

    private static final long OVERLAY_TEXT_DURATION = 5000;

    private ServerGameStateController mServerGameStateController;
    private Handler mHandler;

    private TextView mStatusText;
    private View mOverlayContainer;
    private TextView mOverlayText;
    private ImageView[] mQuests;
    private ImageView[] mAttempts;

    // Some UI state variables so we know when to show the overlay
    private boolean justProposedTeam;
    private boolean justRanQuest;
    private boolean gameOver;

    private HashMap<PlayerInfo, Boolean> mSelectedPlayers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.server_fragment, parent, false);
        mServerGameStateController = ServerGameStateController.get(getActivity());

        // Restore game state
        if (savedInstanceState != null) {
            justProposedTeam = savedInstanceState.getBoolean(JUST_PROPOSED_TEAM_KEY);
            justRanQuest = savedInstanceState.getBoolean(JUST_RAN_QUEST_KEY);
            gameOver = savedInstanceState.getBoolean(GAME_OVER_KEY);
            mSelectedPlayers = (HashMap<PlayerInfo, Boolean>) savedInstanceState.
                    getSerializable(SELECTED_PLAYER_MAP_KEY);
        } else {
            mSelectedPlayers = new HashMap<PlayerInfo, Boolean>();
            for (PlayerInfo p : mServerGameStateController.getCurrentGameState().players) {
                mSelectedPlayers.put(p, false);
            }
        }

        // This should force horizontal view and disable rotation
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mStatusText = (TextView) v.findViewById(R.id.server_status_text);
        mOverlayContainer = v.findViewById(R.id.server_overlay_container);
        mOverlayText = (TextView) v.findViewById(R.id.server_overlay_text);
        mQuests = new ImageView[] {
                (ImageView) v.findViewById(R.id.server_quest_1),
                (ImageView) v.findViewById(R.id.server_quest_2),
                (ImageView) v.findViewById(R.id.server_quest_3),
                (ImageView) v.findViewById(R.id.server_quest_4),
                (ImageView) v.findViewById(R.id.server_quest_5)
        };
        mAttempts = new ImageView[] {
                (ImageView) v.findViewById(R.id.server_attempts_1),
                (ImageView) v.findViewById(R.id.server_attempts_2),
                (ImageView) v.findViewById(R.id.server_attempts_3),
                (ImageView) v.findViewById(R.id.server_attempts_4),
                (ImageView) v.findViewById(R.id.server_attempts_5)
        };

        mHandler = new Handler();

        View playerViews = organizePlayerViews(inflater, parent);
        if (playerViews != null) {
            ((FrameLayout) v.findViewById(R.id.server_fragment_container)).addView(playerViews);
        } else {
            Log.e(TAG, "organizePlayerViews returned null");
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(JUST_PROPOSED_TEAM_KEY, justProposedTeam);
        outState.putBoolean(JUST_RAN_QUEST_KEY, justRanQuest);
        outState.putBoolean(GAME_OVER_KEY, gameOver);
        outState.putSerializable(SELECTED_PLAYER_MAP_KEY, mSelectedPlayers);
    }

    /**
     * Helper function to set up the overlay player views.
     */
    private View organizePlayerViews(LayoutInflater inflater, ViewGroup parent) {
        List<PlayerInfo> players = new ArrayList<PlayerInfo>(mSelectedPlayers.keySet());

        View v = null;
        switch (mSelectedPlayers.size()) {
            case 5:
                v = inflater.inflate(R.layout.player_5_overlay, parent, false);
                View container1 = v.findViewById(R.id.player1_container);
                View container2 = v.findViewById(R.id.player2_container);
                View container3 = v.findViewById(R.id.player3_container);
                View container4 = v.findViewById(R.id.player4_container);
                View container5 = v.findViewById(R.id.player5_container);

                TextView player1 = (TextView) v.findViewById(R.id.player1_name);
                TextView player2 = (TextView) v.findViewById(R.id.player2_name);
                TextView player3 = (TextView) v.findViewById(R.id.player3_name);
                TextView player4 = (TextView) v.findViewById(R.id.player4_name);
                TextView player5 = (TextView) v.findViewById(R.id.player5_name);

                setupViewInfo(container1, player1, players.get(0));
                setupViewInfo(container2, player2, players.get(1));
                setupViewInfo(container3, player3, players.get(2));
                setupViewInfo(container4, player4, players.get(3));
                setupViewInfo(container5, player5, players.get(4));
                break;
            case 6:
                v = inflater.inflate(R.layout.player_6_overlay, parent, false);
                break;
            case 7:
                v = inflater.inflate(R.layout.player_7_overlay, parent, false);
                break;
            default:
                break;
        }

        return v;
    }

    /**
     * Helper function to set the player name and click listeners for a player's view.
     */
    private void setupViewInfo(final View container, TextView playerText, final PlayerInfo player) {
        playerText.setText(player.name.substring(0, Math.min(MAX_ALLOWED_CHARS,
                player.name.length())));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean contains = mSelectedPlayers.get(player);
                container.setSelected(!contains);
                mSelectedPlayers.put(player, !contains);
            }
        });
    }

    /**
     * Update the UI based on the current game state.
     */
    public void update() {
        ServerGameState gameState = mServerGameStateController.getCurrentGameState();

        // Update the status text
        if (gameState.waitingForLady) {
            mStatusText.setText(getString(R.string.server_lady_required));
        } else if (gameState.needQuestProposal) {
            mStatusText.setText(getString(R.string.server_proposal_required));
        } else if (gameState.lastQuestProposal != null || gameState.lastQuestExecution != null) {
            mStatusText.setText(getString(R.string.server_player_response_required));
        } else if (gameState.waitingForAssassin) {
            mStatusText.setText(getString(R.string.server_assassin_required));
        }

        // check UI state
        if (gameState.lastQuestProposal != null) {
            justProposedTeam = true;
        } else if (justProposedTeam) {
            // If we just proposed the team, let's check if it passed and show corresponding overlay
            String overlay = (gameState.lastQuestExecution != null) ? "Proposal passed." :
                    "Proposal failed.";
            showOverlay(overlay);
            justProposedTeam = false;
        } else if (gameState.gameOver && !gameOver) {
            String overlay = (gameState.goodWon) ? "Good won!" : "Evil won!";
            showOverlay(overlay);
            gameOver = true;
        } else if (gameState.lastQuestExecution != null) {
            justRanQuest = true;
        } else if (justRanQuest) {
            String overlay = (gameState.quests.get(gameState.quests.size() - 1)) ?
                    "Campaign passed." : "Campaign failed.";
            showOverlay(overlay);
            justRanQuest = false;
        }

        // Refresh the game board
        for (int i = 0; i < gameState.quests.size(); i++) {
            boolean passed = gameState.quests.get(i);
            if (passed) {
                // TODO: Set the appropriate image for mQuests[i]
            } else {

            }
        }

        // <= because currNumAttempts is 0-based
        for (int i = 0; i <= gameState.currentNumAttempts; i++) {
            // TODO: set the appropriate image for mAttempts[i];
        }
        for (int i = gameState.currentNumAttempts + 1; i < 5; i++) {
        }
    }

    /**
     * Overlay should be used to notify the mSelectedPlayers of important updates, such as quest passing.
     */
    private void showOverlay(String text) {
        mOverlayText.setText(text);
        mOverlayContainer.setVisibility(View.VISIBLE);
        AnimationUtils.fadeIn(mOverlayContainer);
        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                AnimationUtils.fadeOut(mOverlayContainer);
                mOverlayText.setVisibility(View.GONE);
            }
        }, OVERLAY_TEXT_DURATION);
    }
}
