package com.google.android.avalon;

import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* Created by mikewallstedt on 5/13/14.
*/
public class AssignmentFactory {
    private final GameConfiguration mGameConfiguration;

    public AssignmentFactory(GameConfiguration mGameConfiguration) {
        this.mGameConfiguration = mGameConfiguration;
    }

    public InitialAssignments getAssignments() {
        return null;

    }

    // VisibleForTesting
    List<AvalonRole> getRolesInPlay() {
        int numEvil = mGameConfiguration.numPlayers / 3;
        Collection<AvalonRole> specialEvil = getSpecialEvil();
        if (specialEvil.size() > numEvil) {
            String message = String.format(
                "Too many special evil roles requested. A game with %d players, supports %d " +
                        "evil players. %d were requested.",
                    mGameConfiguration.numPlayers, numEvil, specialEvil.size());
            throw new IllegalStateException(message);
        }

        List<AvalonRole> rolesInPlay = new ArrayList<AvalonRole>();
        rolesInPlay.addAll(specialEvil);
        for (int i = rolesInPlay.size(); i < numEvil; i++) {
            rolesInPlay.add(AvalonRole.EVIL);
        }
        rolesInPlay.addAll(getSpecialGood());
        for (int i = rolesInPlay.size(); i < mGameConfiguration.numPlayers; i++) {
            rolesInPlay.add(AvalonRole.LOYAL);
        }
        return rolesInPlay;
    }

    private Collection<AvalonRole> getSpecialEvil() {
        Set<AvalonRole> result = new HashSet<AvalonRole>();
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

}
