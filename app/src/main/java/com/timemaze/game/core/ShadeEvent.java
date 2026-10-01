package com.timemaze.game.core;

import java.util.ArrayList;

/**
 * A scripted appearance of the Shade. He lives outside of time, so whatever he
 * breaks stays broken no matter how often the boy rewinds.
 *
 * Dialogue lines are prefixed with the speaker: "S:" the Shade, "M:" Milo,
 * "O:" old Milo (unmasked), "X:" the stranger, "N:" narration. Two commands can
 * appear among the lines: "#ACT" performs the sabotage at that point and
 * "#REVEAL" drops the hood.
 */
public final class ShadeEvent {
    public static final int ON_START = 0, ON_LOOP = 1, ON_SIGNAL = 2, ON_ENTER = 3;

    public static final int A_BREAK = 0, A_TILE = 1, A_CHASE = 2, A_LIMIT = 3, A_REWIRE = 4, A_TIMER = 5, A_DARK = 6;

    public static final class Action {
        public int type;
        public char key;
        public int col, row;
        public char tile;
        public int n;
        public float speed;
        public String req;
    }

    public int trigger = ON_START;
    public int loop;
    public char signal;
    public int minRemnants;
    public int rc0, rr0, rc1, rr1;
    public int col = 10, row = 5;
    public String[] lines = new String[0];
    public final ArrayList<Action> actions = new ArrayList<Action>();
    public boolean restartLoop, clearRemnants;
    /** Events sharing a group >= 0 are alternatives: once one fires, the others never do. */
    public int group = -1;

    public ShadeEvent onStart() {
        trigger = ON_START;
        return this;
    }

    /** Fires when the given loop (0 = first) starts. */
    public ShadeEvent onLoop(int n) {
        trigger = ON_LOOP;
        loop = n;
        return this;
    }

    /** Fires the first time channel c turns on while at least minRemnants exist. */
    public ShadeEvent onSignal(char c, int minRemnants) {
        trigger = ON_SIGNAL;
        signal = c;
        this.minRemnants = minRemnants;
        return this;
    }

    /** Fires when the live boy enters the tile rectangle (inclusive). */
    public ShadeEvent onEnter(int c0, int r0, int c1, int r1) {
        trigger = ON_ENTER;
        rc0 = c0;
        rr0 = r0;
        rc1 = c1;
        rr1 = r1;
        return this;
    }

    /** Where the Shade materialises (tile whose bottom he stands on). */
    public ShadeEvent at(int c, int r) {
        col = c;
        row = r;
        return this;
    }

    public ShadeEvent say(String... l) {
        lines = l;
        return this;
    }

    private Action act(int type) {
        Action a = new Action();
        a.type = type;
        actions.add(a);
        return a;
    }

    /** Breaks the plate or lever placed at map key k. */
    public ShadeEvent breakObj(char k) {
        act(A_BREAK).key = k;
        return this;
    }

    public ShadeEvent setTile(int c, int r, char tile) {
        Action a = act(A_TILE);
        a.col = c;
        a.row = r;
        a.tile = tile;
        return this;
    }

    /** From now on the Shade hunts the live boy every loop. */
    public ShadeEvent chase(int c, int r, int delayFrames, float speed) {
        Action a = act(A_CHASE);
        a.col = c;
        a.row = r;
        a.n = delayFrames;
        a.speed = speed;
        return this;
    }

    /** Limits the machine to n more trips. */
    public ShadeEvent limit(int n) {
        act(A_LIMIT).n = n;
        return this;
    }

    /** Changes what a gate, lift, laser, rift or the exit ('E') listens to. */
    public ShadeEvent rewire(char key, String req) {
        Action a = act(A_REWIRE);
        a.key = key;
        a.req = req;
        return this;
    }

    /** Changes how long the timed button at key stays active. */
    public ShadeEvent timer(char key, int frames) {
        Action a = act(A_TIMER);
        a.key = key;
        a.n = frames;
        return this;
    }

    /** Puts out the lights for the rest of the chamber. */
    public ShadeEvent darken() {
        act(A_DARK);
        return this;
    }

    public ShadeEvent group(int g) {
        group = g;
        return this;
    }

    public ShadeEvent restartLoop() {
        restartLoop = true;
        return this;
    }

    public ShadeEvent clearRemnants() {
        clearRemnants = true;
        restartLoop = true;
        return this;
    }
}
