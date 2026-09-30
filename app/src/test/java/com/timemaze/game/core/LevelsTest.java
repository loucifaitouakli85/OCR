package com.timemaze.game.core;

import static com.timemaze.game.core.Bot.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LevelsTest {

    @Test
    public void everyChamberIsSolvable() {
        for (int n = 1; n <= Levels.COUNT; n++) {
            Bot.Result r = Bot.run(Levels.get(n), Solutions.get(n));
            assertTrue("Level " + n + " not solved:\n" + r.log, r.won);
        }
    }

    @Test
    public void parMatchesTheKnownSolution() {
        for (int n = 1; n <= Levels.COUNT; n++) {
            Bot.Result r = Bot.run(Levels.get(n), Solutions.get(n));
            assertEquals("Level " + n + " par", Levels.get(n).parRemnants, r.world.remnants.size());
        }
    }

    @Test
    public void shadeAppearsInChambers5_10_15_19() {
        for (int n = 1; n <= Levels.COUNT; n++) {
            boolean shade = n == 5 || n == 10 || n == 15 || n == 19;
            assertEquals("Level " + n, shade, Levels.get(n).isShadeLevel());
        }
        for (int n : new int[]{5, 10, 15, 19}) {
            Bot.Result r = Bot.run(Levels.get(n), Solutions.get(n));
            boolean fired = false;
            for (boolean b : r.world.eventFired) fired |= b;
            assertTrue("Shade never appeared in " + n, fired);
        }
    }

    @Test
    public void echoesCannotBeSkipped() {
        // walking straight to the exit without a remnant must not work
        Bot.Result r = Bot.run(Levels.get(2), new Cmd[][]{loop(walk(7), jumpTo(9), jumpTo(11), walk(16), idle(200))});
        assertFalse(r.won);
    }

    @Test
    public void steppingStoneLedgeIsOutOfReachAlone() {
        Bot.Result r = Bot.run(Levels.get(4), new Cmd[][]{loop(walk(11), jumpTo(13), walk(14), act(), walk(18), walk(17))});
        assertFalse(r.won);
    }

    @Test
    public void timedButtonExpiresBeforeTheBoyArrives() {
        Bot.Result r = Bot.run(Levels.get(8), new Cmd[][]{
            loop(walk(1), walk(10), jumpTo(11), jumpTo(9), jumpTo(11), walk(12), jumpTo(14), walk(18))});
        assertFalse(r.won);
    }

    @Test
    public void remnantWalkingIntoALaserIsAParadox() {
        // remnant 1 turns the laser off only briefly, remnant 2 comes home too late
        Bot.Result r = Bot.run(Levels.get(7), new Cmd[][]{
            loop(walk(3), idle(120), machine()),
            loop(walk(12), idle(300), machine()),
            loop(walk(12), idle(1000))});
        assertFalse(r.won);
        assertTrue(r.log, r.log.contains("HURT") || r.log.contains("OUCH"));
    }

    @Test
    public void changingThePastBlocksARemnant() {
        // the live boy pulls the lever first, so remnant 1's pull closes the gate on remnant 2
        Cmd[][] sol = Solutions.get(11);
        Bot.Result r = Bot.run(Levels.get(11), new Cmd[][]{sol[0], sol[1], loop(walk(4), act(), idle(600))});
        assertFalse(r.won);
        assertTrue(r.log, r.log.contains("BLOCKED"));
    }

    @Test
    public void theShadeCatchesABoyWhoWaits() {
        Bot.Result r = Bot.run(Levels.get(15), new Cmd[][]{loop(idle(2000))});
        assertFalse(r.won);
        assertTrue(r.log, r.log.contains("CAUGHT"));
    }

    @Test
    public void theTruthLimitsTheMachineToTwoTrips() {
        World w = Bot.run(Levels.get(19), Solutions.get(19)).world;
        assertEquals(2, w.maxRemnants);
    }

    @Test
    public void mapsAreWellFormed() {
        for (int n = 1; n <= Levels.COUNT; n++) {
            Level l = Levels.get(n);
            assertEquals(n, l.num);
            assertEquals(n <= 5 ? 0 : n <= 10 ? 1 : n <= 15 ? 2 : 3, l.zone);
            // machine and exit need a free tile above and solid ground below
            assertTrue("machine top " + n, l.grid[l.machineRow - 1][l.machineCol] != '#');
            assertTrue("exit top " + n, l.grid[l.exitRow - 1][l.exitCol] != '#');
            assertTrue("machine floor " + n, l.grid[l.machineRow + 1][l.machineCol] == '#');
            assertTrue("exit floor " + n, l.grid[l.exitRow + 1][l.exitCol] == '#');
            assertTrue("intro " + n, l.intro.length() > 0);
        }
    }
}
