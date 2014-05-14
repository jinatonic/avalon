package com.google.android.avalon.rules;

import android.content.Context;

import com.google.android.avalon.model.AvalonMessage;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.PlayerInfo;

import java.util.Set;

/**
 * Created by jinyan on 5/14/14.
 */
public class GameStateController {

    private Context mContext;

    private GameConfiguration mConfig;
    private Set<PlayerInfo> mPlayers;
    private InitialAssignments mAssignments;

    // private for singleton
    private static GameStateController sGameStateController;
    private GameStateController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static GameStateController get(Context context) {
        if (sGameStateController == null) {
            sGameStateController = new GameStateController(context);
        }
        return sGameStateController;
    }

    public void processMessage(AvalonMessage msg) {

    }

}
