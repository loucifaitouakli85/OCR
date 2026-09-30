package com.timemaze.game.core;

/** Command line runner: plays every known solution and prints a report. */
public final class SolveAll {
    public static void main(String[] args) {
        int from = 1, to = Levels.COUNT;
        if (args.length > 0) from = to = Integer.parseInt(args[0]);
        int ok = 0, total = 0;
        for (int n = from; n <= to; n++) {
            Bot.Cmd[][] sol = Solutions.get(n);
            if (sol == null) {
                System.out.println("Level " + n + ": no solution yet");
                continue;
            }
            total++;
            Bot.Result r = Bot.run(Levels.get(n), sol);
            System.out.println("Level " + n + " " + Levels.get(n).name + ": " + (r.won ? "SOLVED" : "NOT SOLVED")
                + " in " + r.frames + " frames, remnants=" + r.world.remnants.size());
            if (!r.won || args.length > 0) System.out.print(r.log);
            if (r.won) ok++;
        }
        System.out.println(ok + "/" + total + " solved");
        if (ok != total) System.exit(1);
    }
}
