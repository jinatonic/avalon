package com.google.android.avalon.controllers;

import android.content.Context;
import android.util.Log;

import com.google.android.avalon.model.messages.AvalonMessage;
import com.google.android.avalon.model.ClientGameState;
import com.google.android.avalon.model.messages.ClientSetupDoneMessage;
import com.google.android.avalon.model.messages.GameOverMessage;
import com.google.android.avalon.model.messages.LadyResponse;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.QuestExecution;
import com.google.android.avalon.model.messages.QuestExecutionResponse;
import com.google.android.avalon.model.messages.QuestProposal;
import com.google.android.avalon.model.messages.QuestProposalResponse;
import com.google.android.avalon.model.messages.RoleAssignment;

/**
 * Created by jinyan on 5/14/14.
 */
public class ClientGameStateController extends GameStateController {

    // Game state
    private ClientGameState mGameState = new ClientGameState();

    // private for singleton
    private static ClientGameStateController sClientGameStateController;
    private ClientGameStateController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static ClientGameStateController get(Context context) {
        if (sClientGameStateController == null) {
            sClientGameStateController = new ClientGameStateController(context);
        }
        return sClientGameStateController;
    }

    public ClientGameState getCurrentGameState() {
        return mGameState;
    }

    public boolean started() {
        return mGameState.started();
    }

    @Override
    public boolean processAvalonMessage(AvalonMessage msg) {
        Log.d(TAG, "processAvalonMessage: " + msg);
        // case through each type and perform appropriate action
        // Always check if the message is expected by the controller first and show warning toast
        // if it's not expected.

        // Most of these messages simply set the appropriate ClientGameState var.
        // The UI will then take the ClientGameState, wait for user response, and then send back
        // the appropriate response (if there is any).

        // PlayerInfo from the client UI, save the info and forward it to the server
        if (msg instanceof PlayerInfo) {
            mGameState.player = (PlayerInfo) msg;
            sendSingleMessage(mGameState.player, msg);
        }

        // RoleAssignment from server
        else if (msg instanceof RoleAssignment) {
            if (mGameState.started()) {
                return showWarningToast(msg);
            }

            mGameState.assignment = (RoleAssignment) msg;
        }

        // ClientSetupDone from UI
        else if (msg instanceof ClientSetupDoneMessage) {
            if (mGameState.started) {
                return showWarningToast(msg);
            }

            mGameState.started = true;
        }

        // GameOverMessage
        else if (msg instanceof GameOverMessage) {
            if (mGameState.gameOver) {
                return showWarningToast(msg);
            }

            mGameState.gameOver = true;
            mGameState.goodWon = ((GameOverMessage) msg).goodWon;
        }

        // LadyResponse
        else if (msg instanceof LadyResponse) {
            mGameState.ladyRsp = (LadyResponse) msg;
        }

        // QuestProposal
        else if (msg instanceof QuestProposal) {
            if (mGameState.proposal != null) {
                return showWarningToast(msg);
            }

            mGameState.proposal = (QuestProposal) msg;
        }

        // QuestProposalResponse, this one is from the UI
        // TODO: do we need fault tolerance? What happens if send fails?
        else if (msg instanceof QuestProposalResponse) {
            mGameState.proposal = null;
            sendSingleMessage(mGameState.player, msg);
        }

        // QuestExecution
        else if (msg instanceof QuestExecution) {
            if (mGameState.execution != null) {
                return showWarningToast(msg);
            }

            mGameState.execution = (QuestExecution) msg;
        }

        // QuestExecutionResponse, this one is from the UI
        // TODO: same here
        else if (msg instanceof QuestExecutionResponse) {
            mGameState.execution = null;
            sendSingleMessage(mGameState.player, msg);
        }

        // Unrecognized or unsupported message for the client.
        else {
            return showWarningToast(msg);
        }

        return true;
    }

}
