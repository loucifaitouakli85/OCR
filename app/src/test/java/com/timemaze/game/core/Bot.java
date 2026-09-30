package com.timemaze.game.core;

/**
 * A tiny closed-loop "player" used by the tests to prove that every chamber can
 * be solved with the real physics. Each loop is a list of commands; the loop
 * normally ends by stepping into the time machine.
 */
public final class Bot {
    private Bot() {}

    /** One scripted behaviour. Sets inputs {left,right,jump,action}; returns true when finished. */
    public interface Cmd {
        boolean step(World w, boolean[] in);
    }

    static float cx(World w) {
        return w.x + World.PW / 2f;
    }

    static float stopDist(float vx) {
        float v = Math.abs(vx), d = 0;
        while (v > 0) {
            v -= 0.35f;
            if (v > 0) d += v;
        }
        return d;
    }

    /** Walks until standing still on the centre of the column. */
    public static Cmd walk(final int col) {
        return new Cmd() {
            int n;

            public boolean step(World w, boolean[] in) {
                float d = col * 16 + 8 - cx(w);
                if (++n > 1200) throw new IllegalStateException("walk(" + col + ") stuck at x=" + w.x);
                if (Math.abs(d) < 0.8f && Math.abs(w.vx) < 0.05f && w.onGround) return true;
                if (Math.abs(d) > stopDist(w.vx) + 0.6f) {
                    in[d > 0 ? 1 : 0] = true;
                } else if (Math.abs(d) >= 0.8f && Math.abs(w.vx) < 0.05f) {
                    in[d > 0 ? 1 : 0] = true;
                }
                return false;
            }
        };
    }

    /** Idles for the given number of frames. */
    public static Cmd idle(final int frames) {
        return new Cmd() {
            int n;

            public boolean step(World w, boolean[] in) {
                return ++n >= frames;
            }
        };
    }

    /** Idles until the loop clock reaches frame t. */
    public static Cmd until(final int t) {
        return new Cmd() {
            public boolean step(World w, boolean[] in) {
                return w.t + 1 >= t;
            }
        };
    }

    /** Presses the action button once. */
    public static Cmd act() {
        return new Cmd() {
            int n;

            public boolean step(World w, boolean[] in) {
                if (n++ == 0) {
                    in[3] = true;
                    return false;
                }
                return true;
            }
        };
    }

    /** Holds a direction (-1/1) for a number of frames. */
    public static Cmd hold(final int dir, final int frames) {
        return new Cmd() {
            int n;

            public boolean step(World w, boolean[] in) {
                in[dir < 0 ? 0 : 1] = true;
                return ++n >= frames;
            }
        };
    }

    /** Full jump holding dir (-1, 0, 1) for dirFrames, finishes on landing. */
    public static Cmd jump(final int dir, final int dirFrames) {
        return new Cmd() {
            int n;
            boolean rising = true;

            public boolean step(World w, boolean[] in) {
                if (n > 2 && w.onGround) return true;
                if (++n > 600) throw new IllegalStateException("jump never landed");
                if (n <= 1 || (rising && w.vy < 0)) in[2] = true;
                if (n > 2 && w.vy >= 0) rising = false;
                if (dir != 0 && n <= dirFrames) in[dir < 0 ? 0 : 1] = true;
                return false;
            }
        };
    }

    /** Jumps and steers in the air to land on the centre of col. */
    public static Cmd jumpTo(final int col) {
        return jumpTo(col, 0);
    }

    /** Like jumpTo but only starts steering after delay frames of straight up flight. */
    public static Cmd jumpTo(final int col, final int delay) {
        return new Cmd() {
            int n;
            boolean rising = true;

            public boolean step(World w, boolean[] in) {
                if (n > 2 && w.onGround) return true;
                if (++n > 600) throw new IllegalStateException("jumpTo never landed");
                if (n <= 1 || (rising && w.vy < 0)) in[2] = true;
                if (n > 2 && w.vy >= 0) rising = false;
                if (n <= delay) return false;
                float d = col * 16 + 8 - cx(w);
                float brake = w.vx * w.vx / (2 * 0.2f);
                if (Math.abs(d) > brake + 1f) {
                    in[d > 0 ? 1 : 0] = true;
                } else if (Math.abs(w.vx) > 0.15f) {
                    in[w.vx > 0 ? 0 : 1] = true;
                }
                return false;
            }
        };
    }

    /** Walks into the time machine and travels back. */
    public static Cmd[] machine(World w) {
        return new Cmd[]{walk(w.level.machineCol), act()};
    }

    public static Cmd machine() {
        return new Cmd() {
            Cmd walk, act;

            public boolean step(World w, boolean[] in) {
                if (walk == null) walk = walk(w.level.machineCol);
                if (act == null) {
                    if (walk.step(w, in)) act = act();
                    return false;
                }
                return act.step(w, in);
            }
        };
    }

    /** Idles until the predicate holds. */
    public interface Cond {
        boolean ok(World w);
    }

    public static Cmd waitFor(final Cond c) {
        return new Cmd() {
            int n;

            public boolean step(World w, boolean[] in) {
                if (++n > 6000) throw new IllegalStateException("waitFor timed out");
                return c.ok(w);
            }
        };
    }

    public static Cmd[] loop(Cmd... cmds) {
        return cmds;
    }

    public static final class Result {
        public boolean won;
        public String log = "";
        public int frames;
        public World world;
    }

    /** Plays the scripted loops. The last loop must reach the exit. */
    public static Result run(Level level, Cmd[][] loops) {
        return run(level, loops, Integer.MAX_VALUE);
    }

    /** Plays the solution but stops after maxFrames, returning the world at that moment. */
    public static World runFrames(Level level, Cmd[][] loops, int maxFrames) {
        return run(level, loops, maxFrames).world;
    }

    public static Result run(Level level, Cmd[][] loops, int maxFrames) {
        Result res = new Result();
        World w = new World(level);
        res.world = w;
        StringBuilder log = new StringBuilder();
        int frames = 0;
        outer:
        for (int li = 0; li < loops.length; li++) {
            Cmd[] cmds = loops[li];
            int ci = 0;
            int grace = 0;
            while (true) {
                if (frames >= maxFrames) break outer;
                if (w.state == World.CUTSCENE) {
                    boolean restart = w.activeEvent.restartLoop;
                    w.applyEventActions();
                    w.finishEvent();
                    log.append("  [loop ").append(li + 1).append("] shade event at t=").append(w.t).append(restart ? " (loop restarted)\n" : "\n");
                    if (restart) continue outer;
                    continue;
                }
                if (w.state == World.WON) {
                    res.won = true;
                    break outer;
                }
                if (w.state == World.DEAD || w.state == World.PARADOX || w.state == World.CAUGHT) {
                    log.append("  [loop ").append(li + 1).append("] FAILED at t=").append(w.t).append(": ").append(w.failReason).append('\n');
                    break outer;
                }
                if (w.state == World.REWIND) {
                    while (w.state == World.REWIND) {
                        w.tick(false, false, false, false);
                        frames++;
                    }
                    log.append("  [loop ").append(li + 1).append("] travelled back, remnants=").append(w.remnants.size()).append('\n');
                    continue outer;
                }
                if (ci >= cmds.length && li == loops.length - 1 && grace < 300) {
                    // last loop: give the exit a moment to finish opening
                    grace++;
                    w.tick(false, false, false, false);
                    frames++;
                    continue;
                }
                if (ci >= cmds.length) {
                    log.append("  [loop ").append(li + 1).append("] script ended without travelling back or winning (t=").append(w.t).append(")\n");
                    break outer;
                }
                boolean[] in = new boolean[4];
                boolean done;
                try {
                    done = cmds[ci].step(w, in);
                } catch (IllegalStateException e) {
                    log.append("  [loop ").append(li + 1).append("] cmd ").append(ci).append(" error: ").append(e.getMessage())
                        .append(" (x=").append(w.x).append(" y=").append(w.y).append(" t=").append(w.t).append(")\n");
                    break outer;
                }
                if (done) {
                    ci++;
                    continue;
                }
                w.tick(in[0], in[1], in[2], in[3]);
                frames++;
                if (frames > 60 * 60 * 10) {
                    log.append("  timeout\n");
                    break outer;
                }
            }
        }
        // allow the win state to register if the final command walked into the door
        res.frames = frames;
        res.log = log.toString();
        return res;
    }
}
