package com.timemaze.game.core;

/** Sound effect ids and their procedural synthesis. */
public final class Sfx {
    private Sfx() {}

    public static final int JUMP = 0, LAND = 1, PLATE_ON = 2, PLATE_OFF = 3, LEVER = 4, DOOR = 5, EXIT_OPEN = 6,
        REWIND = 7, PARADOX = 8, DEATH = 9, WIN = 10, SHADE = 11, SABOTAGE = 12, VANISH = 13, TICK = 14,
        DENY = 15, BLIP = 16, SELECT = 17, REVEAL = 18, COUNT = 19;

    public static final int RATE = 22050;

    /** Generates the sample data for every effect. */
    public static float[][] generateAll() {
        float[][] out = new float[COUNT][];
        out[JUMP] = sweep(0.13f, 280, 620, 0.25f, 0.35f, false);
        out[LAND] = noise(0.06f, 0.35f, 0.15f);
        out[PLATE_ON] = concat(tone(0.05f, 660, 0.5f, 0.3f), tone(0.07f, 990, 0.5f, 0.3f));
        out[PLATE_OFF] = concat(tone(0.05f, 740, 0.5f, 0.25f), tone(0.07f, 440, 0.5f, 0.25f));
        out[LEVER] = concat(noise(0.02f, 0.4f, 0.9f), tone(0.08f, 520, 0.25f, 0.3f));
        out[DOOR] = mixIn(sweep(0.3f, 110, 70, 0.5f, 0.25f, false), noise(0.3f, 0.12f, 0.1f));
        out[EXIT_OPEN] = arp(new float[]{523, 659, 784, 1047}, 0.07f, 0.28f);
        out[REWIND] = wobble(0.75f, 1400, 180, 0.3f);
        out[PARADOX] = mixIn(detuned(0.6f, 150, 158, 0.3f), noise(0.6f, 0.15f, 0.5f));
        out[DEATH] = sweep(0.45f, 620, 90, 0.5f, 0.35f, false);
        out[WIN] = arp(new float[]{523, 659, 784, 1047, 1319, 1568}, 0.08f, 0.3f);
        out[SHADE] = drone(1.2f, 55, 0.4f);
        out[SABOTAGE] = mixIn(noise(0.6f, 0.45f, 0.25f), sweep(0.6f, 200, 40, 0.5f, 0.3f, false));
        out[VANISH] = sweep(0.18f, 900, 1800, 0.125f, 0.18f, false);
        out[TICK] = noise(0.015f, 0.25f, 1f);
        out[DENY] = detuned(0.2f, 110, 116, 0.3f);
        out[BLIP] = tone(0.018f, 880, 0.5f, 0.12f);
        out[SELECT] = concat(tone(0.04f, 784, 0.5f, 0.25f), tone(0.06f, 1175, 0.5f, 0.25f));
        out[REVEAL] = mixIn(drone(1.6f, 41, 0.35f), wobble(1.6f, 300, 900, 0.12f));
        return out;
    }

    static float[] tone(float secs, float freq, float duty, float vol) {
        int n = (int) (secs * RATE);
        float[] b = new float[n];
        float ph = 0;
        for (int i = 0; i < n; i++) {
            ph += freq / RATE;
            ph -= (int) ph;
            float env = 1f - (float) i / n;
            b[i] = (ph < duty ? 1 : -1) * vol * env;
        }
        return b;
    }

    static float[] sweep(float secs, float f0, float f1, float duty, float vol, boolean tri) {
        int n = (int) (secs * RATE);
        float[] b = new float[n];
        float ph = 0;
        for (int i = 0; i < n; i++) {
            float k = (float) i / n;
            float f = f0 + (f1 - f0) * k;
            ph += f / RATE;
            ph -= (int) ph;
            float env = (1f - k) * Math.min(1f, i / 80f);
            float v = tri ? (ph < 0.5f ? ph * 4 - 1 : 3 - ph * 4) : (ph < duty ? 1 : -1);
            b[i] = v * vol * env;
        }
        return b;
    }

    static float[] noise(float secs, float vol, float bright) {
        int n = (int) (secs * RATE);
        float[] b = new float[n];
        long seed = 12345;
        float lp = 0;
        for (int i = 0; i < n; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            float r = ((seed >>> 33) & 0xFFFF) / 32768f - 1f;
            lp += (r - lp) * bright;
            b[i] = lp * vol * (1f - (float) i / n);
        }
        return b;
    }

    static float[] arp(float[] notes, float step, float vol) {
        float[] out = new float[0];
        for (float f : notes) out = concat(out, tone(step, f, 0.5f, vol));
        return out;
    }

    static float[] wobble(float secs, float f0, float f1, float vol) {
        int n = (int) (secs * RATE);
        float[] b = new float[n];
        float ph = 0;
        for (int i = 0; i < n; i++) {
            float k = (float) i / n;
            float f = f0 + (f1 - f0) * k;
            f *= 1f + 0.08f * (float) Math.sin(i * 2 * Math.PI * 18 / RATE);
            ph += f / RATE;
            ph -= (int) ph;
            float v = ph < 0.5f ? ph * 4 - 1 : 3 - ph * 4;
            float env = Math.min(1f, i / 400f) * (1f - k * 0.7f);
            b[i] = v * vol * env;
        }
        return b;
    }

    static float[] detuned(float secs, float fa, float fb, float vol) {
        int n = (int) (secs * RATE);
        float[] b = new float[n];
        float pa = 0, pb = 0;
        for (int i = 0; i < n; i++) {
            pa += fa / RATE;
            pa -= (int) pa;
            pb += fb / RATE;
            pb -= (int) pb;
            float env = 1f - (float) i / n;
            b[i] = ((pa < 0.5f ? 1 : -1) + (pb < 0.3f ? 1 : -1)) * 0.5f * vol * env;
        }
        return b;
    }

    static float[] drone(float secs, float f, float vol) {
        int n = (int) (secs * RATE);
        float[] b = new float[n];
        float p1 = 0, p2 = 0;
        for (int i = 0; i < n; i++) {
            p1 += f / RATE;
            p1 -= (int) p1;
            p2 += f * 1.414f / RATE;
            p2 -= (int) p2;
            float k = (float) i / n;
            float env = (float) Math.sin(Math.PI * k);
            float v = (p1 < 0.5f ? p1 * 4 - 1 : 3 - p1 * 4) + 0.6f * (p2 < 0.5f ? 1 : -1) * 0.5f;
            b[i] = v * vol * env * 0.7f;
        }
        return b;
    }

    static float[] concat(float[] a, float[] b) {
        float[] o = new float[a.length + b.length];
        System.arraycopy(a, 0, o, 0, a.length);
        System.arraycopy(b, 0, o, a.length, b.length);
        return o;
    }

    static float[] mixIn(float[] a, float[] b) {
        float[] o = new float[Math.max(a.length, b.length)];
        for (int i = 0; i < o.length; i++) {
            float v = 0;
            if (i < a.length) v += a[i];
            if (i < b.length) v += b[i];
            o[i] = v;
        }
        return o;
    }
}
