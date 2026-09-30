package com.timemaze.game.core;

/** An immutable ARGB bitmap. A pixel value of 0 is transparent. */
public final class Sprite {
    public final int w, h;
    public final int[] px;

    public Sprite(int w, int h, int[] px) {
        this.w = w;
        this.h = h;
        this.px = px;
    }

    /**
     * Builds a sprite from rows of characters. '.' and ' ' are transparent; any
     * other character is looked up in keys and mapped to the colour with the
     * same index.
     */
    public static Sprite parse(String keys, int[] colors, String... rows) {
        int h = rows.length;
        int w = 0;
        for (String r : rows) w = Math.max(w, r.length());
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            String r = rows[y];
            for (int x = 0; x < r.length(); x++) {
                char c = r.charAt(x);
                if (c == '.' || c == ' ') continue;
                int k = keys.indexOf(c);
                if (k < 0) throw new IllegalArgumentException("Unknown sprite key '" + c + "'");
                px[y * w + x] = colors[k];
            }
        }
        return new Sprite(w, h, px);
    }

    /** Returns a copy with every colour c replaced by the mapping in from/to. */
    public Sprite recolor(int[] from, int[] to) {
        int[] out = px.clone();
        for (int i = 0; i < out.length; i++) {
            for (int k = 0; k < from.length; k++) {
                if (out[i] == from[k]) {
                    out[i] = to[k];
                    break;
                }
            }
        }
        return new Sprite(w, h, out);
    }
}
