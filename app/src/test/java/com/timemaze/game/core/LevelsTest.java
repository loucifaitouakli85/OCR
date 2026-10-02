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

    private static final int[] SHADE_CHAMBERS = {5, 8, 10, 12, 15, 18, 20, 23, 26, 29};

    @Test
    public void shadeAppearsInTenChambers() {
        for (int n = 1; n <= Levels.COUNT; n++) {
            boolean shade = false;
            for (int k : SHADE_CHAMBERS) shade |= k == n;
            assertEquals("Level " + n, shade, Levels.get(n).isShadeLevel());
        }
        for (int n : SHADE_CHAMBERS) {
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
    public void theButtonInChamber8WorksNormallyUntilTheShadeTampersWithIt() {
        assertEquals(360, new World(Levels.get(8)).plates.get(0).timerFrames);
        // reaching the gate brings the Shade, who shortens the button and restarts the loop
        Cmd[] climb = {walk(10), jumpTo(11), jumpTo(9), jumpTo(11), walk(12), jumpTo(14)};
        Bot.Result r = Bot.run(Levels.get(8), new Cmd[][]{loop(climb), loop(idle(10))});
        assertTrue(r.log, r.log.contains("shade event"));
        assertEquals(100, r.world.plates.get(0).timerFrames);
        // now a boy who presses the button himself is too slow to reach the gate
        r = Bot.run(Levels.get(8), new Cmd[][]{loop(climb),
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
        World w = Bot.run(Levels.get(29), Solutions.get(29)).world;
        assertEquals(2, w.maxRemnants);
    }

    @Test
    public void oneBodyIsNotEnoughForAHeavyPlate() {
        Bot.Result r = Bot.run(Levels.get(16), new Cmd[][]{
            loop(walk(6), idle(900), machine()),
            loop(walk(7), jumpTo(10), jumpTo(12), jumpTo(14), walk(16), idle(300))});
        assertFalse(r.won);
    }

    @Test
    public void aCrackedBridgeDropsABoyWhoLingers() {
        Cmd[][] sol = Solutions.get(18);
        Bot.Result r = Bot.run(Levels.get(18), new Cmd[][]{sol[0], sol[1], loop(walk(9), idle(300))});
        assertFalse(r.won);
    }

    @Test
    public void theShadeRewiresTheExitInChamber20() {
        World w = Bot.run(Levels.get(20), Solutions.get(20)).world;
        boolean[] sig = new boolean[26];
        sig['a' - 'a'] = true;
        assertFalse("the lever alone opens the exit", w.exitReq.eval(sig));
        sig['h' - 'a'] = true;
        assertTrue(w.exitReq.eval(sig));
    }

    @Test
    public void theShadePutsOutTheLightsInChamber23() {
        assertFalse(Levels.get(23).dark);
        assertTrue(Bot.run(Levels.get(23), Solutions.get(23)).world.dark);
    }

    @Test
    public void closingARiftBeforeARemnantUsesItIsAParadox() {
        Level l = new Level(99, "TEST", 4,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.T..L....R....r..E#",
            "####################")
            .lever('L', 'a', false)
            .rift('R', 'r', "!a")
            .exit("")
            .intro("TEST");
        Bot.Result r = Bot.run(l, new Cmd[][]{
            loop(untilTeleport(1), walk(17), untilTeleport(-1), machine()),
            loop(walk(5), act(), idle(400))});
        assertFalse(r.won);
        assertTrue(r.log, r.log.contains("RIFT CLOSED"));
    }

    @Test
    public void mapsAreWellFormed() {
        for (int n = 1; n <= Levels.COUNT; n++) {
            Level l = Levels.get(n);
            assertEquals(n, l.num);
            assertEquals((n - 1) / 5, l.zone);
            // machine and exit need a free tile above and solid ground below
            assertTrue("machine top " + n, l.grid[l.machineRow - 1][l.machineCol] != '#');
            assertTrue("exit top " + n, l.grid[l.exitRow - 1][l.exitCol] != '#');
            assertTrue("machine floor " + n, l.grid[l.machineRow + 1][l.machineCol] == '#');
            assertTrue("exit floor " + n, l.grid[l.exitRow + 1][l.exitCol] == '#');
            assertTrue("intro " + n, l.intro.length() > 0);
        }
    }
}
