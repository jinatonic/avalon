package com.google.android.avalon.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.R;
import com.google.android.avalon.controllers.ServerGameStateController;
import com.google.android.avalon.model.ServerGameState;
import com.google.android.avalon.model.messages.AssassinResponse;
import com.google.android.avalon.model.messages.LadyRequest;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.QuestExecutionResponse;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.QuestProposalResponse;
import com.google.android.avalon.utils.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinyan on 5/12/14.
 */
public class ServerFragment extends Fragment {
    private static final String TAG = "ServerFragment";

    private static final int MAX_ALLOWED_CHARS = 10;
    private static final long OVERLAY_TEXT_DURATION = 3000;

    private static final String JUST_PROPOSED_TEAM_KEY = "just_proposed_team";
    private static final String JUST_RAN_QUEST_KEY = "just_ran_quest";
    private static final String GAME_OVER_KEY = "game_over";
    private static final String SELECTED_PLAYERS_INDEX = "selected_players_index";

    private ServerGameStateController mServerGameStateController;
    private Handler mHandler;

    private TextView mStatusText;
    private View mOverlayContainer;
    private TextView mOverlayText;
    private ImageView[] mQuests;
    private ImageView[] mAttempts;
    private View[] mPlayerContainers;
    private ImageView[] mPlayerImgs;

    // Some UI state variables so we know when to show the overlay
    private boolean justProposedTeam;
    private boolean justRanQuest;
    private boolean gameOver;
    private boolean waitingForUserSelection;

    private List<PlayerInfo> mPlayers;
    private ArrayList<Integer> mSelectedIndices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.server_fragment, parent, false);
        mServerGameStateController = ServerGameStateController.get(getActivity());

        mPlayers = mServerGameStateController.getCurrentGameState().players;
        // Restore game state
        if (savedInstanceState != null) {
            justProposedTeam = savedInstanceState.getBoolean(JUST_PROPOSED_TEAM_KEY);
            justRanQuest = savedInstanceState.getBoolean(JUST_RAN_QUEST_KEY);
            gameOver = savedInstanceState.getBoolean(GAME_OVER_KEY);
            mSelectedIndices = (ArrayList<Integer>) savedInstanceState.getSerializable(
                    SELECTED_PLAYERS_INDEX);
        } else {
            mSelectedIndices = new ArrayList<Integer>(mPlayers.size());
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

        setupNumberDependentViews(inflater, parent, v);

        update();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(JUST_PROPOSED_TEAM_KEY, justProposedTeam);
        outState.putBoolean(JUST_RAN_QUEST_KEY, justRanQuest);
        outState.putBoolean(GAME_OVER_KEY, gameOver);
        outState.putSerializable(SELECTED_PLAYERS_INDEX, mSelectedIndices);
    }

    /**
     * Helper function to set up all the views that are dependent on the number of players.
     */
    private void setupNumberDependentViews(LayoutInflater inflater, ViewGroup parent, View v) {
        mPlayerContainers = new View[mPlayers.size()];
        mPlayerImgs = new ImageView[mPlayers.size()];
        // Player names never change, so we don't need to save it as global
        TextView[] names = new TextView[mPlayers.size()];

        View playerViews;
        switch (mPlayers.size()) {
            case 5:
                playerViews = inflater.inflate(R.layout.player_5_overlay, parent, false);
                mPlayerContainers[0] = playerViews.findViewById(R.id.player1_container);
                mPlayerContainers[1] = playerViews.findViewById(R.id.player2_container);
                mPlayerContainers[2] = playerViews.findViewById(R.id.player3_container);
                mPlayerContainers[3] = playerViews.findViewById(R.id.player4_container);
                mPlayerContainers[4] = playerViews.findViewById(R.id.player5_container);

                mPlayerImgs[0] = (ImageView) playerViews.findViewById(R.id.player1_img);
                mPlayerImgs[1] = (ImageView) playerViews.findViewById(R.id.player2_img);
                mPlayerImgs[2] = (ImageView) playerViews.findViewById(R.id.player3_img);
                mPlayerImgs[3] = (ImageView) playerViews.findViewById(R.id.player4_img);
                mPlayerImgs[4] = (ImageView) playerViews.findViewById(R.id.player5_img);

                names[0] = (TextView) playerViews.findViewById(R.id.player1_name);
                names[1] = (TextView) playerViews.findViewById(R.id.player2_name);
                names[2] = (TextView) playerViews.findViewById(R.id.player3_name);
                names[3] = (TextView) playerViews.findViewById(R.id.player4_name);
                names[4] = (TextView) playerViews.findViewById(R.id.player5_name);

                for (int i = 0; i < mPlayers.size(); i++) {
                    setupViewInfo(i, names[i]);
                }

                // Set up quests
                mQuests[0].setImageResource(R.drawable.campaign_1_player_2);
                mQuests[1].setImageResource(R.drawable.campaign_2_player_3);
                mQuests[2].setImageResource(R.drawable.campaign_3_player_2);
                mQuests[3].setImageResource(R.drawable.campaign_4_player_3);
                mQuests[4].setImageResource(R.drawable.campaign_5_player_3);
                break;
            case 6:
                playerViews = inflater.inflate(R.layout.player_6_overlay, parent, false);
                mPlayerContainers[0] = playerViews.findViewById(R.id.player1_container);
                mPlayerContainers[1] = playerViews.findViewById(R.id.player2_container);
                mPlayerContainers[2] = playerViews.findViewById(R.id.player3_container);
                mPlayerContainers[3] = playerViews.findViewById(R.id.player4_container);
                mPlayerContainers[4] = playerViews.findViewById(R.id.player5_container);
                mPlayerContainers[5] = playerViews.findViewById(R.id.player6_container);

                mPlayerImgs[0] = (ImageView) playerViews.findViewById(R.id.player1_img);
                mPlayerImgs[1] = (ImageView) playerViews.findViewById(R.id.player2_img);
                mPlayerImgs[2] = (ImageView) playerViews.findViewById(R.id.player3_img);
                mPlayerImgs[3] = (ImageView) playerViews.findViewById(R.id.player4_img);
                mPlayerImgs[4] = (ImageView) playerViews.findViewById(R.id.player5_img);
                mPlayerImgs[5] = (ImageView) playerViews.findViewById(R.id.player6_img);

                names[0] = (TextView) playerViews.findViewById(R.id.player1_name);
                names[1] = (TextView) playerViews.findViewById(R.id.player2_name);
                names[2] = (TextView) playerViews.findViewById(R.id.player3_name);
                names[3] = (TextView) playerViews.findViewById(R.id.player4_name);
                names[4] = (TextView) playerViews.findViewById(R.id.player5_name);
                names[5] = (TextView) playerViews.findViewById(R.id.player6_name);

                for (int i = 0; i < mPlayers.size(); i++) {
                    setupViewInfo(i, names[i]);
                }

                // Set up quests
                mQuests[0].setImageResource(R.drawable.campaign_1_player_2);
                mQuests[1].setImageResource(R.drawable.campaign_2_player_3);
                mQuests[2].setImageResource(R.drawable.campaign_3_player_4);
                mQuests[3].setImageResource(R.drawable.campaign_4_player_3);
                mQuests[4].setImageResource(R.drawable.campaign_5_player_4);
                break;
            case 7:
                playerViews = inflater.inflate(R.layout.player_7_overlay, parent, false);
                mPlayerContainers[0] = playerViews.findViewById(R.id.player1_container);
                mPlayerContainers[1] = playerViews.findViewById(R.id.player2_container);
                mPlayerContainers[2] = playerViews.findViewById(R.id.player3_container);
                mPlayerContainers[3] = playerViews.findViewById(R.id.player4_container);
                mPlayerContainers[4] = playerViews.findViewById(R.id.player5_container);
                mPlayerContainers[5] = playerViews.findViewById(R.id.player6_container);
                mPlayerContainers[6] = playerViews.findViewById(R.id.player7_container);

                mPlayerImgs[0] = (ImageView) playerViews.findViewById(R.id.player1_img);
                mPlayerImgs[1] = (ImageView) playerViews.findViewById(R.id.player2_img);
                mPlayerImgs[2] = (ImageView) playerViews.findViewById(R.id.player3_img);
                mPlayerImgs[3] = (ImageView) playerViews.findViewById(R.id.player4_img);
                mPlayerImgs[4] = (ImageView) playerViews.findViewById(R.id.player5_img);
                mPlayerImgs[5] = (ImageView) playerViews.findViewById(R.id.player6_img);
                mPlayerImgs[6] = (ImageView) playerViews.findViewById(R.id.player7_img);

                names[0] = (TextView) playerViews.findViewById(R.id.player1_name);
                names[1] = (TextView) playerViews.findViewById(R.id.player2_name);
                names[2] = (TextView) playerViews.findViewById(R.id.player3_name);
                names[3] = (TextView) playerViews.findViewById(R.id.player4_name);
                names[4] = (TextView) playerViews.findViewById(R.id.player5_name);
                names[5] = (TextView) playerViews.findViewById(R.id.player6_name);
                names[6] = (TextView) playerViews.findViewById(R.id.player7_name);

                for (int i = 0; i < mPlayers.size(); i++) {
                    setupViewInfo(i, names[i]);
                }

                // Set up quests
                mQuests[0].setImageResource(R.drawable.campaign_1_player_2);
                mQuests[1].setImageResource(R.drawable.campaign_2_player_3);
                mQuests[2].setImageResource(R.drawable.campaign_3_player_3);
                mQuests[3].setImageResource(R.drawable.campaign_4_player_4);
                mQuests[4].setImageResource(R.drawable.campaign_5_player_4);
                break;
            default:
                Log.e(TAG, "Number of players is not 5,6,7");
                return;
        }

        // Add the child to the inflated fragment view
        ((ViewGroup) v.findViewById(R.id.server_fragment_container)).addView(playerViews);
    }

    /**
     * Helper function to set the player name and click listeners for a player's view.
     */
    private void setupViewInfo(final int index, TextView playerText) {
        playerText.setText(mPlayers.get(index).name.substring(0, Math.min(MAX_ALLOWED_CHARS,
                mPlayers.get(index).name.length())));
        mPlayerContainers[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waitingForUserSelection) {
                    boolean contains = mSelectedIndices.contains(index);
                    if (contains) {
                        mSelectedIndices.remove(new Integer(index));
                    } else {
                        mSelectedIndices.add(index);
                    }
                    mPlayerContainers[index].setSelected(!contains);

                    checkSelectedState();
                }
            }
        });
    }

    /**
     * Helper function to check if we selected enough players
     */
    private void checkSelectedState() {
        ServerGameState gameState = mServerGameStateController.getCurrentGameState();
        DialogFragment dialog = null;
        if (gameState.waitingForLady) {
            dialog = AcceptDialog.getInstance(DialogContext.Lady,
                    new PlayerInfo[] { mPlayers.get(mSelectedIndices.get(0)) });
        } else if (gameState.waitingForAssassin) {
            dialog = AcceptDialog.getInstance(DialogContext.Assassin,
                    new PlayerInfo[] { mPlayers.get(mSelectedIndices.get(0)) });
        } else if (gameState.needQuestProposal &&
                gameState.getNumPlayersForCurrentQuest() == mSelectedIndices.size()) {
            PlayerInfo[] selected = new PlayerInfo[mSelectedIndices.size()];
            int index = 0;
            for (Integer i : mSelectedIndices) {
                selected[index++] = mPlayers.get(i);
            }
            dialog = AcceptDialog.getInstance(DialogContext.Proposal, selected);
        }

        if (dialog != null) {
            dialog.show(getFragmentManager(), "AcceptDialog");
        }
    }

    /**
     * Callback for accepting the current player selections
     */
    public void onAcceptDialogAccept(DialogContext dialogContext, PlayerInfo[] players) {
        final ServerGameState gameState = mServerGameStateController.getCurrentGameState();
        switch (dialogContext) {
            case Proposal:
                mServerGameStateController.processAvalonMessage(new QuestProposal(players,
                        gameState.currentKing, gameState.currQuestIndex()));

                // TODO: REMOVE ME
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // just always pass for now
                        for (PlayerInfo p : mPlayers) {
                            mServerGameStateController.processAvalonMessage(new QuestProposalResponse(p, true, gameState.currQuestIndex()));
                        }
                        update();
                    }
                }, 5000);
                break;
            case Lady:
                mServerGameStateController.processAvalonMessage(new LadyRequest(players[0]));
                break;
            case Assassin:
                mServerGameStateController.processAvalonMessage(new AssassinResponse(players[0]));
                break;
        }
        clearSelected();
        update();
    }

    /**
     * Callback for cancelling the current player selections. It de-selects everything.
     */
    public void onAcceptDialogCancel() {
        clearSelected();
    }

    private void clearSelected() {
        for (int i = 0; i < mPlayers.size(); i++) {
            mPlayerContainers[i].setSelected(false);
        }
        mSelectedIndices.clear();
    }

    /**
     * Update the UI based on the current game state.
     */
    public void update() {
        final ServerGameState gameState = mServerGameStateController.getCurrentGameState();

        // Special case game over
        if (gameState.gameOver) {
            if (!gameOver) {
                String overlay = (gameState.goodWon) ? "Good won!" : "Evil won!";
                showOverlay(overlay);
                mStatusText.setText(overlay);
                gameOver = true;
            }
            return;
        }

        // Update the status text
        if (gameState.waitingForLady) {
            mStatusText.setText(gameState.currentLady.name + ": " +
                    getString(R.string.server_lady_required));
            waitingForUserSelection = true;
        } else if (gameState.needQuestProposal) {
            mStatusText.setText(gameState.currentKing.name + ": " +
                    getString(R.string.server_proposal_required));
            waitingForUserSelection = true;
        } else if (gameState.lastQuestProposal != null || gameState.lastQuestExecution != null) {
            mStatusText.setText(getString(R.string.server_player_response_required));
            waitingForUserSelection = false;
        } else if (gameState.waitingForAssassin) {
            mStatusText.setText(getString(R.string.server_assassin_required));
            waitingForUserSelection = true;
        } else {
            waitingForUserSelection = false;
        }

        // check UI states
        if (gameState.lastQuestProposal != null) {
            justProposedTeam = true;
        } else if (justProposedTeam) {
            // If we just proposed the team, let's check if it passed and show corresponding overlay
            String overlay = (gameState.lastQuestExecution != null) ? "Proposal passed." :
                    "Proposal failed.";
            showOverlay(overlay);
            // Also display player's votes accordingly
            for (int i = 0; i < mPlayers.size(); i++) {
                boolean passed = gameState.lastQuestProposalResponses.get(mPlayers.get(i)).approve;
                int img = passed? R.drawable.approve : R.drawable.reject;
                if (mPlayers.get(i).equals(gameState.currentKing)) {
                    AnimationUtils.switchImageLong(mPlayerImgs[i], img, R.drawable.king);
                } else {
                    AnimationUtils.switchImageLong(mPlayerImgs[i], img, R.drawable.check);  // TODO
                }
            }

            justProposedTeam = false;

            // TODO REMOVE ME
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (PlayerInfo p : gameState.players) {
                        mServerGameStateController.processAvalonMessage(new QuestExecutionResponse(p, true, gameState.currQuestIndex()));
                    }
                    update();
                }
            }, 7000);
        } else {
            // Display normal player states
            for (int i = 0; i < mPlayers.size(); i++) {
                if (mPlayers.get(i).equals(gameState.currentKing)) {
                    AnimationUtils.setImage(mPlayerImgs[i], R.drawable.king, false);
                } else {
                    AnimationUtils.setImage(mPlayerImgs[i], R.drawable.check, false);  // TODO
                }
            }
        }

        if (gameState.lastQuestExecution != null) {
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
                AnimationUtils.setImage(mQuests[i], R.drawable.passed, false);
            } else {
                AnimationUtils.setImage(mQuests[i], R.drawable.failed, false);
            }
        }

        // <= because currNumAttempts is 0-based
        int i = 0;
        for (; i < gameState.currentNumAttempts; i++) {
            // We use the storePrev flag to restore the image once we are done with this round.
            AnimationUtils.setImage(mAttempts[i], R.drawable.failed, true);
        }
        for (; i < 5; i++) {
            AnimationUtils.restorePrev(mAttempts[i]);
        }
    }

    /**
     * Overlay should be used to notify the mSelectedPlayers of important updates, such as quest passing.
     */
    private void showOverlay(String text) {
        mOverlayText.setText(text);
        AnimationUtils.fadeIn(mOverlayContainer);
        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                AnimationUtils.fadeOut(mOverlayContainer);
            }
        }, OVERLAY_TEXT_DURATION);
    }

    public enum DialogContext { Proposal, Lady, Assassin }

    public static class AcceptDialog extends DialogFragment {
        private static final String EXTRA_DIALOG_CONTEXT = "extra_dialog_context";
        private static final String EXTRA_PLAYER_SELECTION = "extra_player_selection";

        private DialogContext mDialogContext;
        private PlayerInfo[] mSelected;
        private AcceptDialogCallback mCallback;

        public static AcceptDialog getInstance(DialogContext dContext, PlayerInfo[] selected) {
            Bundle args = new Bundle(2);
            args.putSerializable(EXTRA_DIALOG_CONTEXT, dContext);
            args.putSerializable(EXTRA_PLAYER_SELECTION, selected);

            AcceptDialog dialog = new AcceptDialog();
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mDialogContext = (DialogContext) getArguments().getSerializable(EXTRA_DIALOG_CONTEXT);
            mSelected = (PlayerInfo[]) getArguments().getSerializable(EXTRA_PLAYER_SELECTION);
            mCallback = (AcceptDialogCallback) getActivity();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            TextView tv = new TextView(getActivity());
            tv.setText(getContentString());
            tv.setTextSize(20f);
            tv.setGravity(Gravity.CENTER);
            Dialog d = new AlertDialog.Builder(getActivity())
                    .setView(tv)
                    .setTitle("Confirm your choice")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCallback.onAcceptDialogAccept(mDialogContext, mSelected);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCallback.onAcceptDialogCancel();
                        }
                    })
                    .setCancelable(false)
                    .create();
            d.setCanceledOnTouchOutside(false);
            return d;
        }

        public String getContentString() {
            StringBuffer buf = new StringBuffer();
            buf.append("Please confirm your selection for ");
            switch (mDialogContext) {
                case Proposal:
                    buf.append("the quest proposal: ");
                    for (int i = 0; i < mSelected.length; i++) {
                        buf.append(mSelected[i].name);
                        if (i < mSelected.length - 1) {
                            buf.append(", ");
                        }
                    }
                    break;
                case Lady:
                    buf.append("lady target: ").append(mSelected[0].name);
                    break;
                case Assassin:
                    buf.append("who you think is Merlin: ").append(mSelected[0].name);
                    break;
            }
            buf.append(".");
            return buf.toString();
        }
    }

    public interface AcceptDialogCallback {
        public void onAcceptDialogAccept(DialogContext dialogContext, PlayerInfo[] players);
        public void onAcceptDialogCancel();
    }
}
