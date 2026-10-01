package com.timemaze.game.core;

import java.util.ArrayList;

/**
 * Runtime simulation of one chamber: the live boy, his remnants (past loops
 * replayed frame by frame), every mechanism, paradox detection and the Shade.
 *
 * All coordinates are in room pixels (320x176), y grows downward. The
 * simulation runs at a fixed 60 ticks per second.
 */
public final class World {
    public static final int TILE = Level.TILE, COLS = Level.COLS, ROWS = Level.ROWS;
    public static final int ROOM_W = COLS * TILE, ROOM_H = ROWS * TILE;

    // tiles
    public static final int T_EMPTY = 0, T_WALL = 1, T_ONEWAY = 2, T_SPIKE = 3, T_EMITTER = 4, T_CRUMBLE = 5;
    /** Frames a crumbling tile holds after someone first stands on it. */
    public static final int CRUMBLE_FRAMES = 75;

    // body size
    public static final int PW = 8, PH = 15;

    // physics
    public static final float GRAVITY = 0.25f, JUMP_V = 4.5f, MAX_FALL = 5f, RUN = 1.5f;
    static final float ACCEL_GROUND = 0.3f, ACCEL_AIR = 0.2f, FRICTION_GROUND = 0.35f, FRICTION_AIR = 0.08f;
    static final int COYOTE = 5, JUMP_BUFFER = 6;

    // states
    public static final int PLAY = 0, DEAD = 1, PARADOX = 2, CAUGHT = 3, WON = 4, REWIND = 5, CUTSCENE = 6;
    public static final int REWIND_FRAMES = 48, FAIL_FRAMES = 80;

    // --------------------------------------------------------------- types

    public static final class Plate {
        public float x, y, w, h;
        public int col, row;
        public char key;
        public int channel;
        public boolean pressed, broken;
        public int timerFrames, timer; // timed plates only
        public int weight = 1, load;   // heavy plates need weight bodies
    }

    public static final class Lever {
        public float x, y;
        public int col, row;
        public char key;
        public int channel;
        public boolean initial, on, broken;
    }

    public static final class Door {
        public char key;
        public int col, rowTop, rowBottom;
        public Req req;
        public float open;
        public float x, top, fullH;
        public static final float W = 10;

        public float solidH() {
            return fullH * (1f - open);
        }
    }

    public static final class Lift {
        public char key;
        public float x0, y0, x1, y1, x, y, px, py, dx, dy;
        public int width;
        public Req req;
        public static final float H = 6;
    }

    public static final class Laser {
        public char key;
        public int col, row, dir;
        public Req req;
        public boolean on;
        /** Beam rectangle, recomputed every tick. */
        public float bx, by, bw, bh;
    }

    /** Two linked tears in time; stepping into one comes out of the other. */
    public static final class Rift {
        public char key;
        public float ax, ay, bx, by; // top-left of each end's tile
        public Req req;
        public boolean active;
    }

    public static final class Recording {
        public float[] xs = new float[1024], ys = new float[1024];
        public byte[] anim = new byte[1024], flags = new byte[1024];
        public short[] act = new short[1024];
        public int len;

        void add(float x, float y, int a, boolean left, boolean grounded, int action) {
            if (len == xs.length) {
                int n = len * 2;
                xs = java.util.Arrays.copyOf(xs, n);
                ys = java.util.Arrays.copyOf(ys, n);
                anim = java.util.Arrays.copyOf(anim, n);
                flags = java.util.Arrays.copyOf(flags, n);
                act = java.util.Arrays.copyOf(act, n);
            }
            xs[len] = x;
            ys[len] = y;
            anim[len] = (byte) a;
            flags[len] = (byte) ((left ? 1 : 0) | (grounded ? 2 : 0));
            act[len] = (short) action;
            len++;
        }

        public boolean left(int i) {
            return (flags[i] & 1) != 0;
        }

        public boolean grounded(int i) {
            return (flags[i] & 2) != 0;
        }
    }

    public static final class Remnant {
        public final Recording rec;
        public final int id;
        public float x, y, px, py;
        public int anim;
        public boolean left, active;

        Remnant(Recording rec, int id) {
            this.rec = rec;
            this.id = id;
        }
    }

    /** Parsed requirement: AND of (possibly negated) channels. */
    public static final class Req {
        final int[] ch;
        final boolean[] neg;

        Req(String s) {
            ArrayList<Integer> c = new ArrayList<Integer>();
            ArrayList<Boolean> n = new ArrayList<Boolean>();
            boolean negate = false;
            for (int i = 0; i < s.length(); i++) {
                char k = s.charAt(i);
                if (k == '!') {
                    negate = true;
                } else if (k >= 'a' && k <= 'z') {
                    c.add(k - 'a');
                    n.add(negate);
                    negate = false;
                }
            }
            ch = new int[c.size()];
            neg = new boolean[c.size()];
            for (int i = 0; i < ch.length; i++) {
                ch[i] = c.get(i);
                neg[i] = n.get(i);
            }
        }

        boolean eval(boolean[] sig) {
            for (int i = 0; i < ch.length; i++) {
                if (sig[ch[i]] == neg[i]) return false;
            }
            return true;
        }

        public boolean uses(int channel) {
            for (int c : ch) if (c == channel) return true;
            return false;
        }
    }

    public static final class Particle {
        public float x, y, vx, vy;
        public int life, max, color;
        public boolean gravity;
    }

    // --------------------------------------------------------------- state

    public final Level level;
    public final int[][] tiles = new int[ROWS][COLS];
    public final ArrayList<Plate> plates = new ArrayList<Plate>();
    public final ArrayList<Lever> levers = new ArrayList<Lever>();
    public final ArrayList<Door> doors = new ArrayList<Door>();
    public final ArrayList<Lift> lifts = new ArrayList<Lift>();
    public final ArrayList<Laser> lasers = new ArrayList<Laser>();
    public final ArrayList<Rift> rifts = new ArrayList<Rift>();
    /** Crumbling tiles: 0 intact, > 0 frames left before collapse, -1 collapsed. */
    public final int[][] crumble = new int[ROWS][COLS];
    public final boolean[] sig = new boolean[26];
    public Req exitReq;
    public boolean dark;
    boolean riftArmed = true;
    public float exitOpen;
    public final float machineX, machineY, exitX, exitY;

    // live boy
    public float x, y, vx, vy;
    public boolean onGround, left;
    public int anim, runTick, idleTick;
    int standOn = -1; // 0..999 lift, 1000+ remnant
    int coyote, jumpBuffer;
    boolean prevJump, prevAction;
    Recording rec;

    public final ArrayList<Remnant> remnants = new ArrayList<Remnant>();
    public int t;
    public int state = PLAY, stateTimer;
    public int rewindFrom;
    public int paradoxRemnant = -1;
    public String failReason = "";
    public int maxRemnants;
    public int loops, deaths, paradoxes, totalFrames;

    // shade
    public final boolean[] eventFired;
    public ShadeEvent activeEvent;
    public boolean chaseOn;
    public float chaseX, chaseY, chaseSpeed;
    int chaseCol, chaseRow, chaseDelay;

    // feedback for the renderer / audio
    public final ArrayList<Particle> particles = new ArrayList<Particle>();
    public final int[] sounds = new int[32];
    public int soundCount;
    public String toast = "";
    public int toastTimer;
    public int shake;
    public int flash;
    public int frameCounter;

    public World(Level level) {
        this.level = level;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = level.grid[r][c];
                tiles[r][c] = tileFor(ch);
            }
        }
        for (Level.Obj o : level.objs) {
            switch (o.type) {
                case Level.PLATE:
                case Level.TIMER: {
                    Plate p = new Plate();
                    p.col = o.col;
                    p.row = o.row;
                    p.key = o.key;
                    p.x = o.col * TILE + 2;
                    p.y = o.row * TILE + 13;
                    p.w = 12;
                    p.h = 3;
                    p.channel = o.channel - 'a';
                    p.timerFrames = o.type == Level.TIMER ? o.frames : 0;
                    p.weight = o.weight;
                    plates.add(p);
                    break;
                }
                case Level.LEVER: {
                    Lever l = new Lever();
                    l.col = o.col;
                    l.row = o.row;
                    l.key = o.key;
                    l.x = o.col * TILE;
                    l.y = o.row * TILE;
                    l.channel = o.channel - 'a';
                    l.initial = o.initial;
                    levers.add(l);
                    break;
                }
                case Level.DOOR: {
                    Door d = new Door();
                    d.key = o.key;
                    d.col = o.col;
                    d.rowTop = o.rowTop;
                    d.rowBottom = o.rowBottom;
                    d.req = new Req(o.req);
                    d.x = o.col * TILE + 3;
                    d.top = o.rowTop * TILE;
                    d.fullH = (o.rowBottom - o.rowTop + 1) * TILE;
                    doors.add(d);
                    break;
                }
                case Level.LIFT: {
                    Lift l = new Lift();
                    l.key = o.key;
                    l.x0 = o.col * TILE;
                    // the marked tiles are where a rider stands, so the deck is at their bottom edge
                    l.y0 = (o.row + 1) * TILE;
                    l.x1 = o.endCol * TILE;
                    l.y1 = (o.endRow + 1) * TILE;
                    l.width = o.width;
                    l.req = new Req(o.req);
                    lifts.add(l);
                    break;
                }
                case Level.LASER: {
                    Laser z = new Laser();
                    z.key = o.key;
                    z.col = o.col;
                    z.row = o.row;
                    z.dir = o.dir;
                    z.req = new Req(o.req);
                    tiles[o.row][o.col] = T_EMITTER;
                    lasers.add(z);
                    break;
                }
                case Level.RIFT: {
                    Rift rf = new Rift();
                    rf.key = o.key;
                    rf.ax = o.col * TILE;
                    rf.ay = o.row * TILE;
                    rf.bx = o.endCol * TILE;
                    rf.by = o.endRow * TILE;
                    rf.req = new Req(o.req);
                    rifts.add(rf);
                    break;
                }
            }
        }
        exitReq = new Req(level.exitReq);
        machineX = level.machineCol * TILE;
        machineY = (level.machineRow - 1) * TILE;
        exitX = level.exitCol * TILE;
        exitY = (level.exitRow - 1) * TILE;
        maxRemnants = level.maxRemnants;
        dark = level.dark;
        eventFired = new boolean[level.events.size()];
        resetLoop();
    }

    static int tileFor(char ch) {
        switch (ch) {
            case '#': return T_WALL;
            case '=': return T_ONEWAY;
            case '^': return T_SPIKE;
            case 'x': return T_CRUMBLE;
            default: return T_EMPTY;
        }
    }

    // ------------------------------------------------------------ loops

    /** Rewinds time to the start of the loop, keeping every remnant. */
    public void resetLoop() {
        t = 0;
        for (Plate p : plates) {
            p.pressed = false;
            p.timer = 0;
            p.load = 0;
        }
        for (Lever l : levers) l.on = l.initial;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) crumble[r][c] = 0;
        riftArmed = true;
        for (Lift l : lifts) {
            l.x = l.px = l.x0;
            l.y = l.py = l.y0;
            l.dx = l.dy = 0;
        }
        x = machineX + (TILE - PW) / 2f;
        y = machineY + 2 * TILE - PH;
        vx = vy = 0;
        onGround = true;
        left = false;
        standOn = -1;
        coyote = 0;
        jumpBuffer = 0;
        prevJump = true;
        prevAction = true;
        anim = Sprites.A_IDLE;
        rec = new Recording();
        rec.add(x, y, anim, left, true, -1);
        for (Remnant r : remnants) {
            r.active = true;
            r.x = r.px = r.rec.xs[0];
            r.y = r.py = r.rec.ys[0];
            r.anim = r.rec.anim[0];
            r.left = r.rec.left(0);
        }
        computeSignals();
        for (Door d : doors) d.open = d.req.eval(sig) ? 1f : 0f;
        exitOpen = exitReq.eval(sig) ? 1f : 0f;
        updateLasers();
        updateRifts();
        chaseX = chaseCol * TILE + 1;
        chaseY = (chaseRow - 1) * TILE;
        paradoxRemnant = -1;
        state = PLAY;
        stateTimer = 0;
    }

    /** Erases every remnant and restarts the chamber. Shade damage remains. */
    public void resetLevel() {
        remnants.clear();
        resetLoop();
    }

    public boolean machineDepleted() {
        return remnants.size() >= maxRemnants;
    }

    // ------------------------------------------------------------ tick

    /** Advances the simulation by one frame with the given held buttons. */
    public void tick(boolean inLeft, boolean inRight, boolean inJump, boolean inAction) {
        frameCounter++;
        soundCount = 0;
        updateParticles();
        if (toastTimer > 0) toastTimer--;
        if (shake > 0) shake--;
        if (flash > 0) flash--;

        switch (state) {
            case PLAY:
                break;
            case REWIND:
                if (++stateTimer >= REWIND_FRAMES) resetLoop();
                prevJump = inJump;
                prevAction = inAction;
                return;
            case DEAD:
            case PARADOX:
            case CAUGHT:
                if (++stateTimer >= FAIL_FRAMES) resetLoop();
                prevJump = inJump;
                prevAction = inAction;
                return;
            default:
                stateTimer++;
                prevJump = inJump;
                prevAction = inAction;
                return;
        }

        t++;
        totalFrames++;
        boolean jumpPressed = inJump && !prevJump;
        boolean actionPressed = inAction && !prevAction;
        prevJump = inJump;
        prevAction = inAction;

        // 1. signals from where everybody stood last frame
        computeSignals();
        updateCrumble();
        // 2. mechanisms
        updateDoors();
        updateLifts();
        updateLasers();
        updateRifts();
        updateExit();
        // 3. remnants replay the past
        for (int i = 0; i < remnants.size(); i++) {
            Remnant r = remnants.get(i);
            r.px = r.x;
            r.py = r.y;
            if (!r.active) continue;
            if (t < r.rec.len) {
                r.x = r.rec.xs[t];
                r.y = r.rec.ys[t];
                r.anim = r.rec.anim[t];
                r.left = r.rec.left(t);
                int a = r.rec.act[t];
                if (a >= 0 && a < levers.size()) toggleLever(levers.get(a));
            } else {
                r.active = false;
                burst(r.x + PW / 2f, r.y + PH / 2f, 0xFF7FE8FF, 14);
                sound(Sfx.VANISH);
            }
        }
        // 4. the live boy
        int action = updatePlayer(inLeft, inRight, inJump, jumpPressed, actionPressed);
        rec.add(x, y, anim, left, onGround, action);
        if (state == REWIND) {
            remnants.add(new Remnant(rec, remnants.size()));
            return;
        }
        // 5. consequences
        checkParadoxes();
        if (state != PLAY) return;
        checkHazards();
        if (state != PLAY) return;
        checkExit();
        if (state != PLAY) return;
        updateChase();
        if (state != PLAY) return;
        checkEvents();
    }

    // ------------------------------------------------------------ signals

    void computeSignals() {
        boolean[] s = sig;
        for (int i = 0; i < 26; i++) s[i] = false;
        for (Plate p : plates) {
            if (p.broken) {
                p.pressed = false;
                continue;
            }
            boolean was = p.pressed;
            if (plateFallen(p)) {
                p.pressed = false;
                p.load = 0;
                continue;
            }
            p.load = bodiesOn(p);
            p.pressed = p.load >= p.weight;
            if (p.pressed && !was) sound(Sfx.PLATE_ON);
            if (!p.pressed && was && p.timerFrames == 0) sound(Sfx.PLATE_OFF);
            if (p.timerFrames > 0) {
                // a timed button starts its countdown when pressed; standing on it doesn't extend it
                if (p.pressed && !was) p.timer = p.timerFrames;
                else if (p.timer > 0) {
                    p.timer--;
                    if (p.timer == 0) sound(Sfx.PLATE_OFF);
                    else if (p.timer % 30 == 0) sound(Sfx.TICK);
                }
                if (p.timer > 0) s[p.channel] = true;
            } else if (p.pressed) {
                s[p.channel] = true;
            }
        }
        for (Lever l : levers) if (l.on && !l.broken) s[l.channel] = true;
    }

    /** Number of bodies pressing a plate. Heavy plates also feel a tower standing on it. */
    int bodiesOn(Plate p) {
        float px0 = p.x, py0 = p.y - 1, ph = p.h + 1;
        if (p.weight > 1) {
            py0 -= 2 * PH + 2;
            ph += 2 * PH + 2;
        }
        int n = 0;
        if (overlap(x, y, PW, PH, px0, py0, p.w, ph)) n++;
        for (Remnant r : remnants) {
            if (r.active && overlap(r.x, r.y, PW, PH, px0, py0, p.w, ph)) n++;
        }
        return n;
    }

    /** A plate resting on a collapsed crumbling tile has fallen with it. */
    boolean plateFallen(Plate p) {
        int r = p.row + 1;
        return r < ROWS && tiles[r][p.col] == T_CRUMBLE && crumble[r][p.col] < 0;
    }

    void updateCrumble() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (tiles[r][c] != T_CRUMBLE) continue;
                int st = crumble[r][c];
                if (st > 0) {
                    if (--st == 0) {
                        st = -1;
                        sound(Sfx.LAND);
                        burst(c * TILE + 8, r * TILE + 4, 0xFF9A8A7A, 10);
                    } else if (st % 15 == 0) {
                        sound(Sfx.TICK);
                    }
                    crumble[r][c] = st;
                } else if (st == 0 && anyBodyStandingOn(c * TILE, r * TILE)) {
                    crumble[r][c] = CRUMBLE_FRAMES;
                }
            }
        }
    }

    boolean anyBodyStandingOn(float tx, float top) {
        if (x + PW > tx && x < tx + TILE && Math.abs(y + PH - top) <= 1.5f) return true;
        for (Remnant r : remnants) {
            if (r.active && r.x + PW > tx && r.x < tx + TILE && Math.abs(r.y + PH - top) <= 1.5f) return true;
        }
        return false;
    }

    void updateRifts() {
        for (Rift rf : rifts) rf.active = rf.req.eval(sig);
    }

    void updateDoors() {
        for (Door d : doors) {
            boolean want = d.req.eval(sig);
            if (want) {
                if (d.open < 1f) {
                    if (d.open == 0f) sound(Sfx.DOOR);
                    d.open = Math.min(1f, d.open + 0.1f);
                }
            } else if (d.open > 0f) {
                float next = Math.max(0f, d.open - 0.1f);
                float solid = d.fullH * (1f - next);
                if (!anyBodyIn(d.x, d.top, Door.W, solid)) {
                    if (d.open == 1f) sound(Sfx.DOOR);
                    d.open = next;
                }
            }
        }
    }

    boolean anyBodyIn(float rx, float ry, float rw, float rh) {
        if (overlap(x, y, PW, PH, rx, ry, rw, rh)) return true;
        for (Remnant r : remnants) if (r.active && overlap(r.x, r.y, PW, PH, rx, ry, rw, rh)) return true;
        return false;
    }

    void updateLifts() {
        for (int i = 0; i < lifts.size(); i++) {
            Lift l = lifts.get(i);
            l.px = l.x;
            l.py = l.y;
            boolean on = l.req.eval(sig);
            float tx = on ? l.x1 : l.x0, ty = on ? l.y1 : l.y0;
            float dx = clampStep(tx - l.x, 1f), dy = clampStep(ty - l.y, 1f);
            if (dy < 0 && riderBlocked(l, i, dx, dy)) {
                // never squash a rider into the ceiling
                dx = 0;
                dy = 0;
            }
            l.x += dx;
            l.y += dy;
            l.dx = dx;
            l.dy = dy;
        }
    }

    boolean riderBlocked(Lift l, int idx, float dx, float dy) {
        if (standOn == idx && solidOverlap(x + dx, y + dy, PW, PH)) return true;
        for (Remnant r : remnants) {
            if (!r.active) continue;
            if (r.x + PW > l.x && r.x < l.x + l.width * TILE && Math.abs(r.y + PH - l.y) <= 0.5f
                && solidOverlap(r.x + dx, r.y + dy, PW, PH)) return true;
        }
        return false;
    }

    static float clampStep(float d, float max) {
        if (d > max) return max;
        if (d < -max) return -max;
        return d;
    }

    void updateLasers() {
        for (Laser z : lasers) {
            z.on = z.req.eval(sig);
            float cx = z.col * TILE + TILE / 2f, cy = z.row * TILE + TILE / 2f;
            int dc = z.dir == Level.DIR_LEFT ? -1 : z.dir == Level.DIR_RIGHT ? 1 : 0;
            int dr = z.dir == Level.DIR_UP ? -1 : z.dir == Level.DIR_DOWN ? 1 : 0;
            int c = z.col + dc, r = z.row + dr;
            while (c >= 0 && c < COLS && r >= 0 && r < ROWS && !solidTile(c, r)) {
                c += dc;
                r += dr;
            }
            // beam spans from the emitter edge to the blocking tile edge
            if (dc != 0) {
                float x0 = dc > 0 ? z.col * TILE + TILE : (c + 1) * TILE;
                float x1 = dc > 0 ? c * TILE : z.col * TILE;
                // doors can block the beam
                for (Door d : doors) {
                    if (d.solidH() <= 0.5f) continue;
                    if (cy < d.top || cy > d.top + d.solidH()) continue;
                    if (dc > 0 && d.x >= x0 && d.x < x1) x1 = d.x;
                    if (dc < 0 && d.x + Door.W <= x1 && d.x + Door.W > x0) x0 = d.x + Door.W;
                }
                z.bx = x0;
                z.bw = Math.max(0, x1 - x0);
                z.by = cy - 2;
                z.bh = 4;
            } else {
                float y0 = dr > 0 ? z.row * TILE + TILE : (r + 1) * TILE;
                float y1 = dr > 0 ? r * TILE : z.row * TILE;
                z.bx = cx - 2;
                z.bw = 4;
                z.by = y0;
                z.bh = Math.max(0, y1 - y0);
            }
        }
    }

    void updateExit() {
        boolean want = exitReq.eval(sig);
        if (want && exitOpen < 1f) {
            if (exitOpen == 0f) sound(Sfx.EXIT_OPEN);
            exitOpen = Math.min(1f, exitOpen + 0.05f);
        } else if (!want && exitOpen > 0f) {
            exitOpen = Math.max(0f, exitOpen - 0.05f);
        }
    }

    void toggleLever(Lever l) {
        if (l.broken) return;
        l.on = !l.on;
        sound(Sfx.LEVER);
    }

    // ------------------------------------------------------------ player

    /** Returns the index of a toggled lever, or -1. */
    int updatePlayer(boolean inLeft, boolean inRight, boolean inJump, boolean jumpPressed, boolean actionPressed) {
        float bottomBefore = y + PH;
        // carried by what we stand on
        if (standOn >= 1000) {
            int i = standOn - 1000;
            if (i < remnants.size() && remnants.get(i).active) {
                Remnant r = remnants.get(i);
                moveX(r.x - r.px);
                y += r.y - r.py;
            }
        } else if (standOn >= 0) {
            Lift l = lifts.get(standOn);
            moveX(l.dx);
            y += l.dy;
        }

        int dir = (inRight ? 1 : 0) - (inLeft ? 1 : 0);
        if (dir != 0) left = dir < 0;
        float target = dir * RUN;
        float accel = dir != 0 ? (onGround ? ACCEL_GROUND : ACCEL_AIR) : (onGround ? FRICTION_GROUND : FRICTION_AIR);
        if (vx < target) vx = Math.min(target, vx + accel);
        else if (vx > target) vx = Math.max(target, vx - accel);

        if (onGround) coyote = COYOTE;
        else if (coyote > 0) coyote--;
        if (jumpPressed) jumpBuffer = JUMP_BUFFER;
        else if (jumpBuffer > 0) jumpBuffer--;
        if (jumpBuffer > 0 && coyote > 0) {
            vy = -JUMP_V;
            jumpBuffer = 0;
            coyote = 0;
            onGround = false;
            standOn = -1;
            sound(Sfx.JUMP);
        }
        if (!inJump && vy < -1.5f) vy += 0.3f; // short hop when released early
        vy = Math.min(MAX_FALL, vy + GRAVITY);

        moveX(vx);
        boolean wasGround = onGround;
        moveY(vy, bottomBefore);
        if (onGround && !wasGround) {
            dust(x + PW / 2f, y + PH);
            sound(Sfx.LAND);
        }
        travelRifts();

        // animation
        if (onGround) {
            if (Math.abs(vx) > 0.2f) {
                runTick++;
                anim = Sprites.A_RUN + (runTick / 6) % 4;
                idleTick = 0;
            } else {
                runTick = 0;
                idleTick++;
                anim = (idleTick % 200) > 190 ? Sprites.A_BLINK : Sprites.A_IDLE;
            }
        } else {
            anim = vy < 0 ? Sprites.A_JUMP : Sprites.A_FALL;
        }

        // interactions
        int toggled = -1;
        if (actionPressed) {
            Lever near = null;
            int idx = -1;
            for (int i = 0; i < levers.size(); i++) {
                Lever l = levers.get(i);
                if (overlap(x, y, PW, PH, l.x - 4, l.y, TILE + 8, TILE)) {
                    near = l;
                    idx = i;
                    break;
                }
            }
            if (near != null) {
                if (near.broken) {
                    showToast("IT'S BROKEN...");
                } else {
                    toggleLever(near);
                    toggled = idx;
                }
            } else if (inMachine()) {
                if (!onGround) {
                    // must be standing
                } else if (machineDepleted()) {
                    showToast("THE MACHINE IS OUT OF POWER");
                    sound(Sfx.DENY);
                } else {
                    timeTravel();
                }
            }
        }
        return toggled;
    }

    /** Stepping into an open rift brings the boy out of its twin. */
    void travelRifts() {
        float cx = x + PW / 2f, cy = y + PH / 2f;
        boolean inside = false;
        for (Rift rf : rifts) {
            boolean inA = cx >= rf.ax + 2 && cx <= rf.ax + TILE - 2 && cy >= rf.ay && cy <= rf.ay + TILE;
            boolean inB = cx >= rf.bx + 2 && cx <= rf.bx + TILE - 2 && cy >= rf.by && cy <= rf.by + TILE;
            if (!inA && !inB) continue;
            inside = true;
            if (!riftArmed || !rf.active) continue;
            float tx = inA ? rf.bx : rf.ax, ty = inA ? rf.by : rf.ay;
            burst(cx, cy, 0xFFB98CFF, 12);
            x = tx + (TILE - PW) / 2f;
            y = ty + TILE - PH;
            vy = Math.min(vy, 0f);
            standOn = -1;
            onGround = false;
            riftArmed = false;
            sound(Sfx.VANISH);
            burst(x + PW / 2f, y + PH / 2f, 0xFFB98CFF, 12);
            return;
        }
        if (!inside) riftArmed = true;
    }

    /** True when a jump from (fx, fy) to (tx, ty) is a trip through an open rift. */
    boolean riftTrip(float fx, float fy, float tx, float ty) {
        for (Rift rf : rifts) {
            if (!rf.active) continue;
            if (near(fx, fy, rf.ax, rf.ay) && near(tx, ty, rf.bx, rf.by)) return true;
            if (near(fx, fy, rf.bx, rf.by) && near(tx, ty, rf.ax, rf.ay)) return true;
        }
        return false;
    }

    static boolean near(float bx, float by, float tileX, float tileY) {
        float cx = bx + PW / 2f, cy = by + PH / 2f;
        return cx >= tileX - 6 && cx <= tileX + TILE + 6 && cy >= tileY - 10 && cy <= tileY + TILE + 10;
    }

    public boolean inMachine() {
        float cx = x + PW / 2f;
        return cx >= machineX + 1 && cx <= machineX + TILE - 1 && y + PH > machineY + TILE && y + PH <= machineY + 2 * TILE + 0.5f;
    }

    public boolean nearLever() {
        for (Lever l : levers) if (overlap(x, y, PW, PH, l.x - 4, l.y, TILE + 8, TILE)) return true;
        return false;
    }

    void timeTravel() {
        // final frame of this loop is recorded by the caller
        state = REWIND;
        stateTimer = 0;
        rewindFrom = t;
        loops++;
        sound(Sfx.REWIND);
        flash = 12;
    }

    void moveX(float dx) {
        if (dx == 0) return;
        float nx = x + dx;
        if (dx > 0) {
            float lim = nx;
            boolean hit = false;
            int c0 = (int) Math.floor(nx / TILE), c1 = (int) Math.floor((nx + PW - 0.001f) / TILE);
            int r0 = (int) Math.floor(y / TILE), r1 = (int) Math.floor((y + PH - 0.001f) / TILE);
            for (int r = r0; r <= r1; r++)
                for (int c = c0; c <= c1; c++)
                    if (solidTile(c, r) && c * TILE < lim + PW) {
                        lim = Math.min(lim, c * TILE - PW);
                        hit = true;
                    }
            for (Door d : doors) {
                float sh = d.solidH();
                if (sh > 0.5f && overlap(nx, y, PW, PH, d.x, d.top, Door.W, sh) && x + PW <= d.x + 0.01f) {
                    lim = Math.min(lim, d.x - PW);
                    hit = true;
                }
            }
            if (hit) {
                nx = Math.max(x, lim);
                vx = 0;
            }
        } else {
            float lim = nx;
            boolean hit = false;
            int c0 = (int) Math.floor(nx / TILE), c1 = (int) Math.floor((nx + PW - 0.001f) / TILE);
            int r0 = (int) Math.floor(y / TILE), r1 = (int) Math.floor((y + PH - 0.001f) / TILE);
            for (int r = r0; r <= r1; r++)
                for (int c = c0; c <= c1; c++)
                    if (solidTile(c, r) && (c + 1) * TILE > lim) {
                        lim = Math.max(lim, (c + 1) * TILE);
                        hit = true;
                    }
            for (Door d : doors) {
                float sh = d.solidH();
                if (sh > 0.5f && overlap(nx, y, PW, PH, d.x, d.top, Door.W, sh) && x >= d.x + Door.W - 0.01f) {
                    lim = Math.max(lim, d.x + Door.W);
                    hit = true;
                }
            }
            if (hit) {
                nx = Math.min(x, lim);
                vx = 0;
            }
        }
        x = nx;
    }

    void moveY(float dy, float bottomBefore) {
        float ny = y + dy;
        onGround = false;
        int newStand = -1;
        if (dy > 0) {
            float best = Float.MAX_VALUE;
            float oldBottom = y + PH;
            float newBottom = ny + PH;
            int c0 = (int) Math.floor((x + 0.001f) / TILE), c1 = (int) Math.floor((x + PW - 0.001f) / TILE);
            int r0 = (int) Math.floor((oldBottom - 0.01f) / TILE), r1 = (int) Math.floor((newBottom - 0.001f) / TILE);
            for (int r = Math.max(0, r0); r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    int tt = tileAt(c, r);
                    float top = r * TILE;
                    if (solidTile(c, r)) {
                        if (newBottom > top && oldBottom <= top + 0.01f) {
                            if (top < best) {
                                best = top;
                                newStand = -1;
                            }
                        }
                    } else if (tt == T_ONEWAY) {
                        if (newBottom >= top && oldBottom <= top + 0.01f && top < best) {
                            best = top;
                            newStand = -1;
                        }
                    }
                }
            }
            for (Door d : doors) {
                float sh = d.solidH();
                if (sh > 0.5f && x + PW > d.x && x < d.x + Door.W && oldBottom <= d.top + 0.01f && newBottom > d.top && d.top < best) {
                    best = d.top;
                    newStand = -1;
                }
            }
            for (int i = 0; i < lifts.size(); i++) {
                Lift l = lifts.get(i);
                // ties with the floor go to the lift so a resting lift carries its rider away
                if (x + PW > l.x && x < l.x + l.width * TILE && bottomBefore <= l.py + 0.5f && newBottom >= l.y && l.y <= best) {
                    best = l.y;
                    newStand = i;
                }
            }
            for (int i = 0; i < remnants.size(); i++) {
                Remnant r = remnants.get(i);
                if (!r.active) continue;
                if (x + PW > r.x + 1 && x < r.x + PW - 1 && bottomBefore <= r.py + 0.5f && newBottom >= r.y && r.y <= best) {
                    best = r.y;
                    newStand = 1000 + i;
                }
            }
            if (best != Float.MAX_VALUE) {
                ny = best - PH;
                vy = 0;
                onGround = true;
            }
        } else if (dy < 0) {
            float top = ny;
            float lim = -Float.MAX_VALUE;
            boolean hit = false;
            int c0 = (int) Math.floor((x + 0.001f) / TILE), c1 = (int) Math.floor((x + PW - 0.001f) / TILE);
            int r0 = (int) Math.floor(top / TILE), r1 = (int) Math.floor((y + 0.01f) / TILE);
            for (int r = r0; r <= r1; r++)
                for (int c = c0; c <= c1; c++)
                    if (solidTile(c, r) && (r + 1) * TILE > top && (r + 1) * TILE <= y + 0.01f) {
                        lim = Math.max(lim, (r + 1) * TILE);
                        hit = true;
                    }
            for (Door d : doors) {
                float sh = d.solidH();
                if (sh > 0.5f && x + PW > d.x && x < d.x + Door.W && d.top + sh > top && d.top + sh <= y + 0.01f) {
                    lim = Math.max(lim, d.top + sh);
                    hit = true;
                }
            }
            if (hit) {
                ny = lim;
                vy = 0;
            }
        }
        y = ny;
        standOn = newStand;
    }

    boolean solidTile(int c, int r) {
        int t = tileAt(c, r);
        return t == T_WALL || t == T_EMITTER || (t == T_CRUMBLE && crumble[r][c] >= 0);
    }

    int tileAt(int c, int r) {
        if (c < 0 || c >= COLS || r < 0) return T_WALL;
        if (r >= ROWS) return T_EMPTY;
        return tiles[r][c];
    }

    boolean solidOverlap(float bx, float by, float bw, float bh) {
        int c0 = (int) Math.floor(bx / TILE), c1 = (int) Math.floor((bx + bw - 0.001f) / TILE);
        int r0 = (int) Math.floor(by / TILE), r1 = (int) Math.floor((by + bh - 0.001f) / TILE);
        for (int r = r0; r <= r1; r++)
            for (int c = c0; c <= c1; c++)
                if (solidTile(c, r)) return true;
        for (Door d : doors) {
            float sh = d.solidH();
            if (sh > 0.5f && overlap(bx, by, bw, bh, d.x, d.top, Door.W, sh)) return true;
        }
        return false;
    }

    static boolean overlap(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    // ------------------------------------------------------------ rules

    void checkParadoxes() {
        for (int i = 0; i < remnants.size(); i++) {
            Remnant r = remnants.get(i);
            if (!r.active) continue;
            String why = null;
            if (solidOverlap(r.x + 1, r.y + 1, PW - 2, PH - 2)) {
                why = "REMNANT " + (i + 1) + " WAS BLOCKED";
            } else if (r.rec.grounded(t) && !supported(r, i)) {
                why = "REMNANT " + (i + 1) + " LOST ITS FOOTING";
            } else if (Math.abs(r.x - r.px) + Math.abs(r.y - r.py) > 24 && t > 1 && !riftTrip(r.px, r.py, r.x, r.y)) {
                why = "REMNANT " + (i + 1) + " FOUND THE RIFT CLOSED";
            } else if (hazardAt(r.x, r.y)) {
                why = "REMNANT " + (i + 1) + " WAS HURT";
            }
            if (why != null) {
                paradoxRemnant = i;
                fail(PARADOX, why);
                paradoxes++;
                sound(Sfx.PARADOX);
                shake = 20;
                return;
            }
        }
    }

    boolean supported(Remnant rm, int idx) {
        float bottom = rm.y + PH;
        float x0 = rm.x, x1 = rm.x + PW;
        int c0 = (int) Math.floor((x0 + 0.001f) / TILE), c1 = (int) Math.floor((x1 - 0.001f) / TILE);
        int r = (int) Math.floor((bottom + 0.5f) / TILE);
        for (int c = c0; c <= c1; c++) {
            int tt = tileAt(c, r);
            if ((solidTile(c, r) || tt == T_ONEWAY) && Math.abs(bottom - r * TILE) <= 1.5f) return true;
        }
        for (Door d : doors) {
            if (d.solidH() > 0.5f && x1 > d.x && x0 < d.x + Door.W && Math.abs(bottom - d.top) <= 1.5f) return true;
        }
        for (Lift l : lifts) {
            if (x1 > l.x && x0 < l.x + l.width * TILE && Math.abs(bottom - l.y) <= 1.5f) return true;
        }
        for (int j = 0; j < idx; j++) {
            Remnant o = remnants.get(j);
            if (o.active && x1 > o.x + 1 && x0 < o.x + PW - 1 && Math.abs(bottom - o.y) <= 1.5f) return true;
        }
        return false;
    }

    boolean hazardAt(float bx, float by) {
        int c0 = (int) Math.floor(bx / TILE), c1 = (int) Math.floor((bx + PW - 0.001f) / TILE);
        int r0 = (int) Math.floor(by / TILE), r1 = (int) Math.floor((by + PH - 0.001f) / TILE);
        for (int r = r0; r <= r1; r++)
            for (int c = c0; c <= c1; c++)
                if (tileAt(c, r) == T_SPIKE && overlap(bx, by, PW, PH, c * TILE + 2, r * TILE + 9, TILE - 4, 7)) return true;
        for (Laser z : lasers) {
            if (z.on && z.bw > 0 && z.bh > 0 && overlap(bx + 1, by + 1, PW - 2, PH - 2, z.bx, z.by, z.bw, z.bh)) return true;
        }
        return false;
    }

    void checkHazards() {
        if (hazardAt(x, y)) {
            deaths++;
            fail(DEAD, "OUCH!");
            burst(x + PW / 2f, y + PH / 2f, Sprites.BLUE, 20);
            sound(Sfx.DEATH);
            shake = 12;
        } else if (y > ROOM_H + 8) {
            deaths++;
            fail(DEAD, "LOST IN THE VOID");
            sound(Sfx.DEATH);
        }
    }

    void checkExit() {
        if (exitOpen < 1f || !onGround) return;
        float cx = x + PW / 2f;
        if (cx > exitX + 3 && cx < exitX + TILE - 3 && y + PH > exitY + TILE && y + PH <= exitY + 2 * TILE + 0.5f) {
            state = WON;
            stateTimer = 0;
            sound(Sfx.WIN);
        }
    }

    void fail(int st, String why) {
        state = st;
        stateTimer = 0;
        failReason = why;
    }

    void showToast(String s) {
        toast = s;
        toastTimer = 120;
    }

    // ------------------------------------------------------------ shade

    void checkEvents() {
        ArrayList<ShadeEvent> ev = level.events;
        for (int i = 0; i < ev.size(); i++) {
            if (eventFired[i]) continue;
            ShadeEvent e = ev.get(i);
            boolean go = false;
            switch (e.trigger) {
                case ShadeEvent.ON_START:
                    go = true;
                    break;
                case ShadeEvent.ON_LOOP:
                    go = remnants.size() >= e.loop && t >= 2;
                    break;
                case ShadeEvent.ON_SIGNAL:
                    go = sig[e.signal - 'a'] && remnants.size() >= e.minRemnants;
                    break;
                case ShadeEvent.ON_ENTER: {
                    int c = (int) ((x + PW / 2f) / TILE), r = (int) ((y + PH / 2f) / TILE);
                    go = c >= e.rc0 && c <= e.rc1 && r >= e.rr0 && r <= e.rr1;
                    break;
                }
            }
            if (go) {
                eventFired[i] = true;
                if (e.group >= 0) {
                    for (int j = 0; j < ev.size(); j++) if (ev.get(j).group == e.group) eventFired[j] = true;
                }
                activeEvent = e;
                state = CUTSCENE;
                stateTimer = 0;
                sound(Sfx.SHADE);
                return;
            }
        }
    }

    /** Performs the sabotage of the active event (called by the cutscene). */
    public void applyEventActions() {
        ShadeEvent e = activeEvent;
        if (e == null) return;
        for (ShadeEvent.Action a : e.actions) {
            switch (a.type) {
                case ShadeEvent.A_BREAK:
                    for (Plate p : plates) {
                        if (p.key == a.key) {
                            p.broken = true;
                            p.pressed = false;
                            burst(p.x + 6, p.y, 0xFFFFB040, 24);
                        }
                    }
                    for (Lever l : levers) {
                        if (l.key == a.key) {
                            l.broken = true;
                            l.on = false;
                            burst(l.x + 8, l.y + 8, 0xFFFFB040, 24);
                        }
                    }
                    break;
                case ShadeEvent.A_TILE:
                    tiles[a.row][a.col] = tileFor(a.tile);
                    burst(a.col * TILE + 8, a.row * TILE + 8, 0xFF9A8CB8, 16);
                    break;
                case ShadeEvent.A_CHASE:
                    chaseOn = true;
                    chaseCol = a.col;
                    chaseRow = a.row;
                    chaseDelay = a.n;
                    chaseSpeed = a.speed;
                    chaseX = chaseCol * TILE + 1;
                    chaseY = (chaseRow - 1) * TILE;
                    break;
                case ShadeEvent.A_REWIRE:
                    rewire(a.key, a.req);
                    break;
                case ShadeEvent.A_TIMER:
                    for (Plate p : plates) if (p.key == a.key) p.timerFrames = a.n;
                    break;
                case ShadeEvent.A_DARK:
                    dark = true;
                    break;
                case ShadeEvent.A_LIMIT:
                    maxRemnants = Math.min(maxRemnants, remnants.size() + a.n);
                    break;
            }
        }
        computeSignals();
        shake = 16;
        flash = 10;
        sound(Sfx.SABOTAGE);
    }

    void rewire(char key, String req) {
        Req r = new Req(req);
        if (key == 'E') exitReq = r;
        for (Door d : doors) if (d.key == key) d.req = r;
        for (Lift l : lifts) if (l.key == key) l.req = r;
        for (Laser z : lasers) if (z.key == key) z.req = r;
        for (Rift rf : rifts) if (rf.key == key) rf.req = r;
    }

    /** Ends the cutscene and resumes play. */
    public void finishEvent() {
        ShadeEvent e = activeEvent;
        activeEvent = null;
        if (e != null && e.clearRemnants) remnants.clear();
        if (e != null && e.restartLoop) {
            resetLoop();
        } else {
            state = PLAY;
        }
    }

    void updateChase() {
        if (!chaseOn) return;
        if (t < chaseDelay) return;
        float tx = x + PW / 2f, ty = y + PH / 2f;
        float cx = chaseX + 6, cy = chaseY + 10;
        float dx = tx - cx, dy = ty - cy;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d < 9f) {
            fail(CAUGHT, "THE SHADE CAUGHT YOU");
            sound(Sfx.DEATH);
            shake = 14;
            return;
        }
        chaseX += dx / d * chaseSpeed;
        chaseY += dy / d * chaseSpeed;
        if ((frameCounter & 3) == 0) {
            Particle p = new Particle();
            p.x = chaseX + 3 + (frameCounter * 7 % 8);
            p.y = chaseY + 18;
            p.vy = 0.2f;
            p.life = p.max = 30;
            p.color = 0xFF3A2C58;
            particles.add(p);
        }
    }

    public boolean chaseAwake() {
        return chaseOn && t >= chaseDelay;
    }

    // ------------------------------------------------------------ fx

    void sound(int id) {
        if (soundCount < sounds.length) sounds[soundCount++] = id;
    }

    void dust(float cx, float by) {
        for (int i = 0; i < 4; i++) {
            Particle p = new Particle();
            p.x = cx;
            p.y = by - 1;
            p.vx = (i - 1.5f) * 0.35f;
            p.vy = -0.25f;
            p.life = p.max = 14;
            p.color = 0xFFB8AC9A;
            particles.add(p);
        }
    }

    public void burst(float cx, float cy, int color, int n) {
        for (int i = 0; i < n; i++) {
            double a = (i * 2.399963) + frameCounter;
            float sp = 0.6f + (i % 5) * 0.35f;
            Particle p = new Particle();
            p.x = cx;
            p.y = cy;
            p.vx = (float) Math.cos(a) * sp;
            p.vy = (float) Math.sin(a) * sp - 0.6f;
            p.gravity = true;
            p.life = p.max = 30 + (i % 4) * 8;
            p.color = color;
            particles.add(p);
        }
    }

    void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.x += p.vx;
            p.y += p.vy;
            if (p.gravity) p.vy += 0.08f;
            if (--p.life <= 0) particles.remove(i);
        }
    }

    public int remnantsUsed() {
        return remnants.size();
    }
}
