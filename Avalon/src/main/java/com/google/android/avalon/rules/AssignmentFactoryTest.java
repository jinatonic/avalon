package com.google.android.avalon.rules;

import android.test.InstrumentationTestCase;

import com.google.android.avalon.model.messages.PlayerInfo;
import com.google.android.avalon.model.messages.RoleAssignment;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.android.avalon.model.AvalonRole.*;
import static com.google.android.avalon.model.AvalonRole.LOYAL;
import static com.google.android.avalon.model.AvalonRole.MERLIN;
import static com.google.android.avalon.model.AvalonRole.MINION;
import static com.google.android.avalon.model.AvalonRole.MORDRED;

/**
 * Created by mikewallstedt on 5/13/14.
 */
public class AssignmentFactoryTest extends InstrumentationTestCase {

    public void testGetRolesInPlayShouldFillNormalRoles() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 8;
        config.includeMerlin = true;
        config.includeAssassin = true;
        config.includeMordred = true;

        // Expect 3 evil, 5 good. 1 special role in each, the other normal.
        List<AvalonRole> expected = Arrays.asList(
                MORDRED, ASSASSIN, MINION,
                MERLIN, LOYAL, LOYAL, LOYAL, LOYAL);
        assertListsHaveSame(expected, new AssignmentFactory(config).getRolesInPlay());
    }

    public void testGetRolesInPlayWithAllSpecialsIncluded() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 10;
        config.includeMerlin = true;
        config.includePercival = true;
        config.includeAssassin = true;
        config.includeMordred = true;
        config.includeOberon = true;
        config.includeMorgana = true;

        List<AvalonRole> expected = Arrays.asList(
                MORDRED, MORGANA, OBERON, ASSASSIN,
                PERCIVAL, MERLIN, LOYAL, LOYAL, LOYAL, LOYAL);
        assertListsHaveSame(expected, new AssignmentFactory(config).getRolesInPlay());
    }

    public void testGetRolesInPlayForNumPlayersDivBy3() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 6;
        config.includeMerlin = true;

        List<AvalonRole> expected = Arrays.asList(
                MINION, MINION,
                MERLIN, LOYAL, LOYAL, LOYAL);
        assertListsHaveSame(expected, new AssignmentFactory(config).getRolesInPlay());
    }

    public void testGetRolesInPlayShouldFailIFTooManySpecialEvil() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 5;
        config.includeMerlin = true;
        config.includeAssassin = true;
        config.includeMordred = true;
        config.includeOberon = true;

        try {
            new AssignmentFactory(config).getRolesInPlay();
            fail("Exception not thrown");
        } catch (IllegalStateException e) {
            assertEquals(
                    "Too many special evil roles requested. A game with 5 players, supports 2 " +
                            "evil players. 3 were requested.", e.getMessage()
            );
        }
    }

    public void testGetRolesInPlayShouldFailIFTooFewPlayers() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 4;

        try {
            new AssignmentFactory(config).getRolesInPlay();
            fail("Exception not thrown");
        } catch (IllegalStateException e) {
            assertEquals("Too few players. 5 required, only 4 provided.", e.getMessage());
        }
    }

    public void testGetAssignmentsShouldHaveCorrectVisibilityInFullGame() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 10;
        config.includeMerlin = true;
        config.includePercival = true;
        config.includeAssassin = true;
        config.includeMordred = true;
        config.includeOberon = true;
        config.includeMorgana = true;

        InitialAssignments ia = new AssignmentFactory(config).getAssignments(getTestPlayers(10));

        assertEquals(getPlayersForRoles(ia, Arrays.asList(ASSASSIN, OBERON, MORGANA)),
                getAssignmentForRole(ia, MERLIN).seenPlayers);

        assertEquals(getPlayersForRoles(ia, Arrays.asList(MORDRED, MORGANA)),
                getAssignmentForRole(ia, ASSASSIN).seenPlayers);

        assertEquals(getPlayersForRoles(ia, Arrays.asList(ASSASSIN, MORGANA)),
                getAssignmentForRole(ia, MORDRED).seenPlayers);

        assertEquals(getPlayersForRoles(ia, Arrays.asList(ASSASSIN, MORDRED)),
                getAssignmentForRole(ia, MORGANA).seenPlayers);

        assertEquals(getPlayersForRoles(ia, Arrays.asList(MERLIN, MORGANA)),
                getAssignmentForRole(ia, PERCIVAL).seenPlayers);

        assertEquals(Collections.EMPTY_SET, getAssignmentForRole(ia, OBERON).seenPlayers);

        assertEquals(Collections.EMPTY_SET, getAssignmentForRole(ia, LOYAL).seenPlayers);
    }

    public void testGetAssignmentsShouldHaveCorrectVisibilityInSmallGame() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 5;
        config.includeMerlin = true;
        config.includeAssassin = true;

        InitialAssignments ia = new AssignmentFactory(config).getAssignments(getTestPlayers(5));

        assertEquals(getPlayersForRoles(ia, Arrays.asList(ASSASSIN, MINION)),
                getAssignmentForRole(ia, MERLIN).seenPlayers);

        assertEquals(getPlayersForRoles(ia, Arrays.asList(MINION)),
                getAssignmentForRole(ia, ASSASSIN).seenPlayers);

        assertEquals(getPlayersForRoles(ia, Arrays.asList(ASSASSIN)),
                getAssignmentForRole(ia, MINION).seenPlayers);

        assertEquals(Collections.EMPTY_SET, getAssignmentForRole(ia, LOYAL).seenPlayers);
    }

    public void testGetAssignmentsShouldOnlySetKingIfLadyNotEnabled() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 5;
        InitialAssignments ia = new AssignmentFactory(config).getAssignments(getTestPlayers(5));
        assertNotNull(ia.king);
        assertNull(ia.lady);
    }

    public void testGetAssignmentsShouldSetLadyBehindKingIfLadyEnabled() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 5;
        config.enableLadyOfTheLake = true;
        List<PlayerInfo> testPlayers = getTestPlayers(5);
        InitialAssignments ia = new AssignmentFactory(config).getAssignments(testPlayers);
        assertNotNull(ia.king);
        assertNotNull(ia.lady);
        int kingIndex = testPlayers.indexOf(ia.king);
        int ladyIndex = testPlayers.indexOf(ia.lady);
        int expectedLadyIndex = (kingIndex == 0) ? 4 : kingIndex - 1;
        assertEquals(expectedLadyIndex, ladyIndex);
    }

    private List<PlayerInfo> getTestPlayers(int numPlayers) {
        List<PlayerInfo> result = new ArrayList<PlayerInfo>();
        for (int i = 0; i < numPlayers; i++) {
            result.add(new PlayerInfo(UUID.randomUUID(), "Player " + i));
        }
        return result;
    }

    private RoleAssignment getAssignmentForRole(InitialAssignments ia, AvalonRole role) {
        for (RoleAssignment assignment: ia.assignments) {
            if (assignment.role.equals(role)) {
                return assignment;
            }
        }
        return null;
    }

    private Set<PlayerInfo> getPlayersForRoles(
            InitialAssignments ia, Collection<AvalonRole> roles) {
        Set<PlayerInfo> result = new HashSet<PlayerInfo>();
        for (RoleAssignment assignment: ia.assignments) {
            if (roles.contains(assignment.role)) {
                result.add(assignment.player);
            }
        }
        return result;
    }

    // We don't have a "bag" in Java, so make sure that order doesn't break list comparisons.
    private void assertListsHaveSame(List<? extends  Comparable> expected,
                                     List<? extends Comparable> actual) {
        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }
}
