package com.timemaze.game.core;

/** Platform independent input: held touches, tap events and keys. */
public final class Input {
    public static final int K_LEFT = 0, K_RIGHT = 1, K_UP = 2, K_DOWN = 3, K_JUMP = 4, K_ACTION = 5, K_BACK = 6,
        K_ENTER = 7, K_RESTART = 8, K_FAST = 9, KEYS = 10;

    public final boolean[] keys = new boolean[KEYS];
    final boolean[] prev = new boolean[KEYS];

    /** Currently held touch points in logical pixels. */
    public final float[] tx = new float[10], ty = new float[10];
    public int touches;

    final float[] tapX = new float[32], tapY = new float[32];
    int taps;

    public void setTouches(float[] xs, float[] ys, int n) {
        touches = Math.min(n, tx.length);
        for (int i = 0; i < touches; i++) {
            tx[i] = xs[i];
            ty[i] = ys[i];
        }
    }

    public void tap(float x, float y) {
        if (taps < tapX.length) {
            tapX[taps] = x;
            tapY[taps] = y;
            taps++;
        }
    }

    public void key(int k, boolean down) {
        if (k >= 0 && k < KEYS) keys[k] = down;
    }

    boolean pressed(int k) {
        return keys[k] && !prev[k];
    }

    /** Called once per update after the game consumed the input. */
    void endFrame() {
        System.arraycopy(keys, 0, prev, 0, KEYS);
        taps = 0;
    }
}
