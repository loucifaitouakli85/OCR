package com.timemaze.game.core;

import static com.timemaze.game.core.Bot.*;

/** Known solutions for every chamber, played by {@link Bot}. */
public final class Solutions {
    private Solutions() {}

    public static Cmd[][] get(int n) {
        switch (n) {
            case 1: return new Cmd[][]{
                loop(walk(6), jumpTo(8), jumpTo(10), act(), walk(16)),
            };
            case 2: return new Cmd[][]{
                loop(walk(3), idle(300), machine()),
                loop(walk(7), jumpTo(9), jumpTo(11), walk(16)),
            };
            case 3: return new Cmd[][]{
                loop(walk(1), idle(700), walk(9), jumpTo(10), act()),
                loop(walk(3), jumpTo(1), idle(500), walk(3), walk(9), jumpTo(10), act()),
                loop(walk(14), jumpTo(17), waitFor(w -> w.exitOpen >= 1f), walk(18)),
            };
            case 4: return new Cmd[][]{
                loop(walk(11), idle(400), machine()),
                loop(walk(10), until(200), jumpTo(11), jumpTo(13), walk(14), act(), walk(18), walk(17)),
            };
            case 5: return new Cmd[][]{
                loop(walk(2), idle(1500), machine()),
                loop(walk(12)), // the Shade interrupts here
                loop(walk(2), idle(1800), machine()),
                loop(walk(13), idle(400), machine()),
                loop(walk(12), until(150), jumpTo(13), jumpTo(15), walk(16), act(), walk(13), walk(12)),
            };
            case 6: return new Cmd[][]{
                loop(walk(2), idle(420), machine()),
                loop(walk(7), waitFor(w -> w.lifts.get(0).y <= 64.5f), walk(15), walk(16)),
            };
            case 7: return new Cmd[][]{
                loop(walk(3), idle(900), machine()),
                loop(walk(12), idle(300), machine()),
                loop(walk(17)),
            };
            case 8: return new Cmd[][]{
                loop(walk(10), jumpTo(11), jumpTo(9), jumpTo(11), walk(12), jumpTo(14)), // the Shade shortens the button's window
                loop(until(230), walk(1), walk(3), act()),
                loop(walk(10), jumpTo(11), jumpTo(9), jumpTo(11), walk(12), jumpTo(14), waitFor(w -> w.doors.get(0).open >= 1f), walk(18)),
            };
            case 9: return new Cmd[][]{
                loop(until(50), walk(2), until(300), walk(3), act()),
                loop(walk(7), waitFor(w -> w.lifts.get(0).x >= 175.5f), walk(13), walk(16), act(), walk(11),
                    waitFor(w -> w.lifts.get(0).x <= 112.5f), walk(5)),
            };
            case 10: return new Cmd[][]{
                loop(walk(6), idle(600), machine()),
                loop(walk(8)), // the Shade caves in the low road
                loop(until(60), walk(2), idle(900), machine()),
                loop(walk(7), waitFor(w -> w.lifts.get(0).y <= 64.5f), jumpTo(4), walk(2), idle(250), walk(6), machine()),
                loop(walk(7), waitFor(w -> w.lifts.get(0).y <= 64.5f), walk(10), waitFor(w -> !w.lasers.get(0).on), walk(17), walk(14)),
            };
            case 11: return new Cmd[][]{
                loop(walk(4), act(), until(200), act(), until(450), act(), idle(200), machine()),
                loop(walk(11), until(420), walk(9), waitFor(w -> w.doors.get(0).open >= 1f), machine()),
                loop(walk(12), waitFor(w -> w.doors.get(1).open >= 1f), walk(17)),
            };
            case 12: return new Cmd[][]{
                loop(walk(10), idle(900), machine()),
                loop(walk(9)), // the Shade spikes the floor under the tower
                loop(walk(9), jumpTo(11), walk(18), idle(1100), walk(11), jumpTo(9), machine()),
                loop(walk(9), jumpTo(11), walk(17), until(260), jumpTo(18), idle(500), walk(11), jumpTo(9), machine()),
                loop(walk(9), jumpTo(11), walk(17), until(420), jumpTo(18), jumpTo(16), walk(14), act(), walk(18), walk(17)),
            };
            case 13: return new Cmd[][]{
                loop(walk(4), until(300), jump(0, 0), idle(200), machine()),
                loop(walk(3), until(150), jumpTo(4), until(313), jumpTo(6), walk(8), act(), walk(12), walk(17)),
            };
            case 14: return new Cmd[][]{
                loop(until(40), walk(5), until(330), walk(1), act()),
                loop(walk(3), waitFor(w -> w.lifts.get(0).y <= 96.5f), walk(7), idle(350), walk(2), machine()),
                loop(walk(3), waitFor(w -> w.lifts.get(0).y <= 96.5f), walk(12), waitFor(w -> w.lifts.get(1).y <= 48.5f),
                    walk(14), waitFor(w -> !w.lasers.get(0).on), walk(18)),
            };
            case 15: return new Cmd[][]{
                loop(walk(2), idle(360), machine()),
                loop(walk(6), jumpTo(8), idle(250), jumpTo(5), machine()),
                loop(walk(14), waitFor(w -> w.doors.get(0).open >= 1f), walk(17)),
            };
            case 16: return new Cmd[][]{
                loop(walk(6), idle(900), machine()),
                loop(walk(6), idle(700), machine()),
                loop(walk(7), jumpTo(10), jumpTo(12), jumpTo(14), walk(16)),
            };
            case 18: return new Cmd[][]{
                loop(walk(13), idle(500), walk(17), jump(0, 0), jump(0, 0), walk(18), jump(0, 0), jumpTo(16), walk(1), machine()),
                loop(walk(13)), // the Shade cracks the bridge
                loop(walk(15)),
            };
            case 20: return new Cmd[][]{
                loop(walk(9), idle(800), machine()),
                loop(walk(8), until(150), jumpTo(9), jumpTo(11), walk(12), act(), idle(30)), // the Shade rewires the exit
                loop(walk(9), idle(1000), machine()),
                loop(walk(8), until(120), jumpTo(9), idle(600), walk(7), machine()),
                loop(walk(8), until(250), jumpTo(9), jumpTo(11), walk(12), act(), walk(16), walk(17)),
            };
            case 21: return new Cmd[][]{
                loop(walk(4), idle(1200), machine()),
                loop(untilTeleport(1), walk(13), idle(400), untilTeleport(1), machine()),
                loop(untilTeleport(1), walk(17)),
            };
            case 22: return new Cmd[][]{
                loop(walk(3), idle(1200), machine()),
                loop(untilTeleport(1), walk(10), idle(400), untilTeleport(-1), machine()),
                loop(untilTeleport(1), untilTeleport(1), walk(17)),
            };
            case 23: return new Cmd[][]{
                loop(walk(3), idle(1500), machine()),
                loop(walk(3)), // the Shade puts out the lights
                loop(until(90), walk(5), machine()),
                loop(walk(11), jumpTo(14), waitFor(w -> w.doors.get(0).open >= 1f), walk(18)),
            };
            case 24: return new Cmd[][]{
                loop(walk(9), idle(200), untilTeleport(-1), machine()),
                loop(walk(8), until(64), jumpTo(9), jumpTo(11), walk(12), act(), walk(17)),
            };
            case 26: return new Cmd[][]{
                loop(walk(3), until(160), machine()),
                loop(untilTeleport(-1), walk(18), until(160), untilTeleport(-1), machine()),
                loop(walk(7), waitFor(w -> w.exitOpen >= 1f), jumpTo(9), walk(9)),
            };
            case 27: return new Cmd[][]{
                loop(walk(9), idle(1000), machine()),
                loop(walk(8), until(120), jumpTo(9), idle(700), walk(7), machine()),
                loop(walk(8), until(300), jumpTo(9), jumpTo(11), walk(11), act(), walk(15), walk(18)),
            };
            case 28: return new Cmd[][]{
                loop(walk(5), idle(1500), machine()),
                loop(walk(5), idle(1300), machine()),
                loop(untilTeleport(1), walk(17), idle(200), walk(16), jumpTo(14), machine()),
                loop(untilTeleport(1), walk(15), idle(90), walk(17)),
            };
            case 17: return new Cmd[][]{
                loop(walk(8), until(100), walk(9), until(600), walk(8), waitFor(w -> !w.lasers.get(0).on), machine()),
                loop(walk(8), waitFor(w -> w.doors.get(0).open >= 1f), walk(15), idle(150), walk(12),
                    walk(8), waitFor(w -> !w.lasers.get(0).on), machine()),
                loop(walk(8), waitFor(w -> w.doors.get(0).open >= 1f), walk(18)),
            };
            case 19: return new Cmd[][]{
                loop(walk(7), idle(900), machine()),
                loop(walk(6), until(150), jumpTo(7), until(400), jump(0, 0), idle(150), walk(5), machine()),
                loop(walk(6), until(250), jumpTo(7), until(412), jumpTo(9), walk(10), act(), walk(17)),
            };
            case 25: return new Cmd[][]{
                loop(until(70), walk(12), idle(500), machine()),
                loop(until(90), walk(16), until(270), walk(17), walk(15), machine()),
                loop(walk(3), waitFor(w -> w.lifts.get(0).y <= 48.5f), walk(5), idle(120), walk(2), machine()),
                loop(walk(3), waitFor(w -> w.lifts.get(0).y <= 48.5f), walk(8), waitFor(w -> !w.lasers.get(0).on),
                    walk(14), waitFor(w -> w.doors.get(0).open >= 1f), walk(18)),
            };
            case 29: return new Cmd[][]{
                loop(walk(4), act(), walk(10), until(700), walk(6), machine()),
                loop(until(260), walk(4), act(), until(560), act(), idle(30), machine()),
                loop(walk(9), until(150), jumpTo(10), jumpTo(12), waitFor(w -> w.doors.get(1).open >= 1f), walk(17)),
            };
            case 30: return new Cmd[][]{
                loop(walk(14), idle(1200), machine()),
                loop(walk(13), until(200), jumpTo(14), jumpTo(16), walk(17), idle(700), walk(14), walk(12), machine()),
                loop(until(120), walk(5), idle(500), machine()),
                loop(walk(7), waitFor(w -> w.lifts.get(0).y <= 48.5f), waitFor(w -> !w.lasers.get(0).on), walk(10)),
            };
            default: return null;
        }
    }
}
