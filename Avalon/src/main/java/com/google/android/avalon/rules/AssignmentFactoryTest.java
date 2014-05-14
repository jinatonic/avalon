package com.google.android.avalon.rules;

import android.test.InstrumentationTestCase;

import com.google.android.avalon.rules.AssignmentFactory;
import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.PlayerName;

import java.util.Arrays;
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

    public void setup() {
        PlayerName alice = new PlayerName(UUID.randomUUID(), "Alice");
        PlayerName bob = new PlayerName(UUID.randomUUID(), "Bob");
        PlayerName carl = new PlayerName(UUID.randomUUID(), "Carl");
        PlayerName don = new PlayerName(UUID.randomUUID(), "Don");
        PlayerName eve = new PlayerName(UUID.randomUUID(), "Eve");
        PlayerName fred = new PlayerName(UUID.randomUUID(), "Fred");
        PlayerName greg = new PlayerName(UUID.randomUUID(), "Greg");
        PlayerName han = new PlayerName(UUID.randomUUID(), "han");
    }

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
        assertRoleListsEqual(expected, new AssignmentFactory(config).getRolesInPlay());
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
        assertRoleListsEqual(expected, new AssignmentFactory(config).getRolesInPlay());
    }

    public void testGetRolesInPlayForNumPlayersDivBy3() {
        GameConfiguration config = new GameConfiguration();
        config.numPlayers = 6;
        config.includeMerlin = true;

        List<AvalonRole> expected = Arrays.asList(
                MINION, MINION,
                MERLIN, LOYAL, LOYAL, LOYAL);
        assertRoleListsEqual(expected, new AssignmentFactory(config).getRolesInPlay());
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

    // We don't have a "bag" in Java, so make sure that order doesn't break list comparisons.
    private void assertRoleListsEqual(List<AvalonRole> expected, List<AvalonRole> actual) {
        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

}
