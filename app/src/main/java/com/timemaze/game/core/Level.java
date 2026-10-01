package com.timemaze.game.core;

import java.util.ArrayList;

/**
 * Static description of one chamber of the maze. The layout is an ASCII grid of
 * {@link #COLS} x {@link #ROWS} tiles:
 *
 * <pre>
 *   #  wall            .  empty           =  one way ledge
 *   ^  spikes          T  time machine    E  exit door
 *   x  crumbling floor (gives way a moment after someone steps on it)
 * </pre>
 *
 * Any other character is an object placeholder declared with the builder
 * methods (plates, levers, doors, lifts, lasers...). Sources drive named
 * channels 'a'..'z'; consumers take a requirement such as "ab" (a AND b) or
 * "!c" (NOT c). An empty requirement is always true.
 */
public final class Level {
    public static final int COLS = 20, ROWS = 11, TILE = 16;

    public static final int PLATE = 0, LEVER = 1, TIMER = 2, DOOR = 3, LIFT = 4, LASER = 5, RIFT = 6;
    public static final int DIR_LEFT = 0, DIR_RIGHT = 1, DIR_UP = 2, DIR_DOWN = 3;

    /** An object declaration. */
    public static final class Obj {
        public int type;
        public char key;
        public int col, row;
        /** Doors: list of tile rows occupied (top to bottom). */
        public int rowTop, rowBottom;
        public char channel;
        public String req = "";
        public boolean initial;
        public int frames;
        public int endCol, endRow, width;
        public int dir;
        /** Plates: how many bodies must stand on it. */
        public int weight = 1;
    }

    public final int num;
    public final String name;
    public final int zone;
    public final char[][] grid = new char[ROWS][COLS];
    public final ArrayList<Obj> objs = new ArrayList<Obj>();
    public final ArrayList<String> hints = new ArrayList<String>();
    public final ArrayList<ShadeEvent> events = new ArrayList<ShadeEvent>();
    public String intro = "";
    public String exitReq = "";
    public int maxRemnants = 9;
    /** Lights out: only the boy, his remnants and the machines glow. */
    public boolean dark;
    public int machineCol = -1, machineRow = -1, exitCol = -1, exitRow = -1;
    /** Fewest remnants the designer needed (shown after clearing). */
    public int parRemnants = -1;

    public Level(int num, String name, int zone, String... rows) {
        this.num = num;
        this.name = name;
        this.zone = zone;
        if (rows.length != ROWS) throw new IllegalArgumentException("Level " + num + ": need " + ROWS + " rows");
        for (int r = 0; r < ROWS; r++) {
            if (rows[r].length() != COLS)
                throw new IllegalArgumentException("Level " + num + " row " + r + ": need " + COLS + " cols, got " + rows[r].length());
            for (int c = 0; c < COLS; c++) {
                char ch = rows[r].charAt(c);
                grid[r][c] = ch;
                if (ch == 'T') {
                    machineCol = c;
                    machineRow = r;
                } else if (ch == 'E') {
                    exitCol = c;
                    exitRow = r;
                }
            }
        }
        if (machineCol < 0 || exitCol < 0) throw new IllegalArgumentException("Level " + num + ": needs T and E");
    }

    private int[] find(char key) {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] == key) return new int[]{c, r};
        throw new IllegalArgumentException("Level " + num + ": key '" + key + "' not in map");
    }

    private Obj add(int type, char key) {
        Obj o = new Obj();
        o.type = type;
        o.key = key;
        int[] p = find(key);
        o.col = p[0];
        o.row = p[1];
        objs.add(o);
        return o;
    }

    public Level intro(String s) {
        intro = s;
        return this;
    }

    public Level hint(String s) {
        hints.add(s);
        return this;
    }

    public Level exit(String req) {
        exitReq = req;
        return this;
    }

    public Level par(int remnants) {
        parRemnants = remnants;
        return this;
    }

    public Level maxRemnants(int n) {
        maxRemnants = n;
        return this;
    }

    /** Pressure plate: channel is active while anybody stands on it. */
    public Level plate(char key, char channel) {
        Obj o = add(PLATE, key);
        o.channel = channel;
        return this;
    }

    /** Heavy plate: only active while at least two bodies stand on it (a tower counts). */
    public Level heavy(char key, char channel) {
        Obj o = add(PLATE, key);
        o.channel = channel;
        o.weight = 2;
        return this;
    }

    /** A pair of rifts: stepping into one comes out of the other, while req holds. */
    public Level rift(char a, char b, String req) {
        Obj o = add(RIFT, a);
        int[] e = find(b);
        o.endCol = e[0];
        o.endRow = e[1];
        o.req = req;
        return this;
    }

    public Level dark() {
        dark = true;
        return this;
    }

    /** Timed plate: stays active for the given number of frames after release. */
    public Level timer(char key, char channel, int frames) {
        Obj o = add(TIMER, key);
        o.channel = channel;
        o.frames = frames;
        return this;
    }

    /** Lever toggled with the action button. */
    public Level lever(char key, char channel, boolean initial) {
        Obj o = add(LEVER, key);
        o.channel = channel;
        o.initial = initial;
        return this;
    }

    /** Vertical gate made of all the cells marked with key; open while req holds. */
    public Level door(char key, String req) {
        Obj o = add(DOOR, key);
        o.req = req;
        o.rowTop = o.row;
        int r = o.row;
        while (r + 1 < ROWS && grid[r + 1][o.col] == key) r++;
        o.rowBottom = r;
        return this;
    }

    /** Moving ledge: rests at start, travels to end while req holds. */
    public Level lift(char start, char end, int width, String req) {
        Obj o = add(LIFT, start);
        int[] e = find(end);
        o.endCol = e[0];
        o.endRow = e[1];
        o.width = width;
        o.req = req;
        return this;
    }

    /** Laser emitter firing in dir; the beam is deadly while req holds. */
    public Level laser(char key, int dir, String req) {
        Obj o = add(LASER, key);
        o.dir = dir;
        o.req = req;
        return this;
    }

    public Level event(ShadeEvent e) {
        events.add(e);
        return this;
    }

    public boolean isShadeLevel() {
        return !events.isEmpty();
    }
}
