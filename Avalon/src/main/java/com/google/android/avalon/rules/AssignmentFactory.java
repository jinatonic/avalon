package com.google.android.avalon.rules;

import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
* Created by mikewallstedt on 5/13/14.
*/
public class AssignmentFactory {
    public static final int MIN_PLAYERS = 5;
    public static final int MAX_PLAYERS = 10;

    private final GameConfiguration mGameConfiguration;
    private final int mNumGood;
    private final int mNumEvil;

    public AssignmentFactory(GameConfiguration mGameConfiguration) {
        this.mGameConfiguration = mGameConfiguration;

        mNumEvil = mGameConfiguration.numPlayers % 3 == 0 ?
                mGameConfiguration.numPlayers / 3 : mGameConfiguration.numPlayers / 3 + 1;
        mNumGood = mGameConfiguration.numPlayers - mNumEvil;
    }

    public InitialAssignments getAssignments(List<PlayerInfo> players)
            throws IllegalConfigurationException {
        List<AvalonRole> rolesInPlay = getRolesInPlay();
        if (players.size() != rolesInPlay.size()) {
            throw new IllegalConfigurationException(
                    String.format("Cannot assign %d roles to %d players.",
                            rolesInPlay.size(), players.size()));
        }
        Collections.shuffle(rolesInPlay);

        Map<PlayerInfo, AvalonRole> playerToRole = new HashMap<PlayerInfo, AvalonRole>();
        for (int i = 0; i < players.size(); i++) {
            playerToRole.put(players.get(i), rolesInPlay.get(i));
        }

        Set<RoleAssignment> assignments = new HashSet<RoleAssignment>();
        for (PlayerInfo player: players) {
            PlayerInfo[] seenBy = getSeenBy(playerToRole, player);
            assignments.add(new RoleAssignment(player, playerToRole.get(player), seenBy));
        }

        int kingIndex = new Random().nextInt(players.size());
        PlayerInfo king = players.get(kingIndex);
        PlayerInfo lady = null;
        if (mGameConfiguration.enableLadyOfTheLake) {
            int ladyIndex = kingIndex == 0 ? players.size() - 1 : kingIndex - 1;
            lady = players.get(ladyIndex);
        }

        return new InitialAssignments(assignments, king, lady, mNumGood, mNumEvil);
    }

    // VisibleForTesting
    List<AvalonRole> getRolesInPlay() {
        if (mGameConfiguration.numPlayers < 5) {
            throw new IllegalConfigurationException(
                    String.format("Too few players. 5 required, only %d provided.",
                            mGameConfiguration.numPlayers));
        }
        Collection<AvalonRole> specialEvil = getSpecialEvil();
        if (specialEvil.size() > mNumEvil) {
            String message = String.format(
                "Too many special evil roles requested. A game with %d players, supports %d " +
                        "evil players. %d were requested.",
                    mGameConfiguration.numPlayers, mNumEvil, specialEvil.size());
            throw new IllegalConfigurationException(message);
        }

        List<AvalonRole> rolesInPlay = new ArrayList<AvalonRole>();
        rolesInPlay.addAll(specialEvil);
        for (int i = rolesInPlay.size(); i < mNumEvil; i++) {
            rolesInPlay.add(AvalonRole.MINION);
        }
        rolesInPlay.addAll(getSpecialGood());
        for (int i = rolesInPlay.size(); i < mGameConfiguration.numPlayers; i++) {
            rolesInPlay.add(AvalonRole.LOYAL);
        }
        return rolesInPlay;
    }

    private Collection<AvalonRole> getSpecialEvil() {
        Set<AvalonRole> result = new HashSet<AvalonRole>();
        if (mGameConfiguration.includeAssassin) {
            result.add(AvalonRole.ASSASSIN);
        }
        if (mGameConfiguration.includeMordred) {
            result.add(AvalonRole.MORDRED);
        }
        if (mGameConfiguration.includeMorgana) {
            result.add(AvalonRole.MORGANA);
        }
        if (mGameConfiguration.includeOberon) {
            result.add(AvalonRole.OBERON);
        }
        return result;
    }

    private Collection<AvalonRole> getSpecialGood() {
        Set<AvalonRole> result = new HashSet<AvalonRole>();
        if (mGameConfiguration.includeMerlin) {
            result.add(AvalonRole.MERLIN);
        }
        if (mGameConfiguration.includePercival) {
            result.add(AvalonRole.PERCIVAL);
        }
        return result;
    }

    private PlayerInfo[] getSeenBy(Map<PlayerInfo, AvalonRole> playerToRole, PlayerInfo player) {
        Set<PlayerInfo> seenBy = new HashSet<PlayerInfo>();
        switch(playerToRole.get(player)) {
            case MERLIN:
                for (PlayerInfo p: playerToRole.keySet()) {
                    AvalonRole r = playerToRole.get(p);
                    if (!r.isGood && !r.equals(AvalonRole.MORDRED)) {
                        seenBy.add(p);
                    }
                }
                break;
            case PERCIVAL:
                for (PlayerInfo p: playerToRole.keySet()) {
                    AvalonRole r = playerToRole.get(p);
                    if (r.equals(AvalonRole.ASSASSIN.MERLIN) || r.equals(AvalonRole.MORGANA)) {
                        seenBy.add(p);
                    }
                }
                break;
            case MINION:
            case ASSASSIN:
            case MORDRED:
            case MORGANA:
                for (PlayerInfo p: playerToRole.keySet()) {
                    AvalonRole r = playerToRole.get(p);
                    if (!r.isGood && !r.equals(AvalonRole.OBERON) && p != player) {
                        seenBy.add(p);
                    }
                }
                break;
            // LOYAL and OBERON see nothing.
        }
        PlayerInfo[] result = new PlayerInfo[seenBy.size()];
        return seenBy.toArray(result);
    }
}
