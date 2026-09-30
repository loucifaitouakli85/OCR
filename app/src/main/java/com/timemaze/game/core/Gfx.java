package com.timemaze.game.core;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tiny software renderer. Everything in the game is drawn into a low resolution
 * ARGB framebuffer which the platform layer scales up with nearest-neighbour
 * filtering, giving crisp pixel art on any screen.
 */
public final class Gfx {
    public int w, h;
    public int[] px;
    /** Translation applied to every draw call. */
    public int ox, oy;

    public Gfx(int w, int h) {
        resize(w, h);
    }

    public void resize(int nw, int nh) {
        if (px == null || nw != w || nh != h) {
            w = nw;
            h = nh;
            px = new int[w * h];
        }
    }

    public void clear(int c) {
        Arrays.fill(px, c);
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    /** Linear mix of two colours, a in [0,255] is the weight of src. */
    public static int mix(int dst, int src, int a) {
        if (a >= 255) return src;
        if (a <= 0) return dst;
        int ia = 255 - a;
        int r = (((dst >> 16) & 255) * ia + ((src >> 16) & 255) * a) / 255;
        int g = (((dst >> 8) & 255) * ia + ((src >> 8) & 255) * a) / 255;
        int b = ((dst & 255) * ia + (src & 255) * a) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int darken(int c, int amt) {
        return mix(c, 0xFF000000, amt);
    }

    public static int lighten(int c, int amt) {
        return mix(c, 0xFFFFFFFF, amt);
    }

    public void pset(int x, int y, int c) {
        x += ox;
        y += oy;
        if (x < 0 || y < 0 || x >= w || y >= h) return;
        px[y * w + x] = c;
    }

    public void blend(int x, int y, int c, int a) {
        x += ox;
        y += oy;
        if (x < 0 || y < 0 || x >= w || y >= h) return;
        int i = y * w + x;
        px[i] = mix(px[i], c, a);
    }

    public void rect(int x, int y, int rw, int rh, int c) {
        x += ox;
        y += oy;
        int x0 = Math.max(0, x), y0 = Math.max(0, y);
        int x1 = Math.min(w, x + rw), y1 = Math.min(h, y + rh);
        for (int yy = y0; yy < y1; yy++) {
            int row = yy * w;
            for (int xx = x0; xx < x1; xx++) px[row + xx] = c;
        }
    }

    public void blendRect(int x, int y, int rw, int rh, int c, int a) {
        if (a <= 0) return;
        if (a >= 255) {
            rect(x, y, rw, rh, c);
            return;
        }
        x += ox;
        y += oy;
        int x0 = Math.max(0, x), y0 = Math.max(0, y);
        int x1 = Math.min(w, x + rw), y1 = Math.min(h, y + rh);
        for (int yy = y0; yy < y1; yy++) {
            int row = yy * w;
            for (int xx = x0; xx < x1; xx++) px[row + xx] = mix(px[row + xx], c, a);
        }
    }

    /** Draws a 1px outline. */
    public void frame(int x, int y, int rw, int rh, int c) {
        rect(x, y, rw, 1, c);
        rect(x, y + rh - 1, rw, 1, c);
        rect(x, y, 1, rh, c);
        rect(x + rw - 1, y, 1, rh, c);
    }

    public void line(int x0, int y0, int x1, int y1, int c) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            pset(x0, y0, c);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public void circle(int cx, int cy, int r, int c) {
        for (int y = -r; y <= r; y++) {
            int span = (int) Math.sqrt(r * r - y * y + 0.5);
            rect(cx - span, cy + y, span * 2 + 1, 1, c);
        }
    }

    public void blendCircle(int cx, int cy, int r, int c, int a) {
        for (int y = -r; y <= r; y++) {
            int span = (int) Math.sqrt(r * r - y * y + 0.5);
            blendRect(cx - span, cy + y, span * 2 + 1, 1, c, a);
        }
    }

    public void ring(int cx, int cy, int r, int c) {
        int x = r, y = 0, err = 1 - r;
        while (x >= y) {
            pset(cx + x, cy + y, c);
            pset(cx + y, cy + x, c);
            pset(cx - y, cy + x, c);
            pset(cx - x, cy + y, c);
            pset(cx - x, cy - y, c);
            pset(cx - y, cy - x, c);
            pset(cx + y, cy - x, c);
            pset(cx + x, cy - y, c);
            y++;
            if (err < 0) {
                err += 2 * y + 1;
            } else {
                x--;
                err += 2 * (y - x) + 1;
            }
        }
    }

    public void sprite(Sprite s, int x, int y, boolean flip) {
        spriteEx(s, x, y, flip, 0, 0, 255);
    }

    /**
     * Draws a sprite. tintAmt (0..255) mixes every pixel toward tint, alpha
     * (0..255) blends the result over the framebuffer.
     */
    public void spriteEx(Sprite s, int x, int y, boolean flip, int tint, int tintAmt, int alpha) {
        if (alpha <= 0) return;
        x += ox;
        y += oy;
        for (int sy = 0; sy < s.h; sy++) {
            int yy = y + sy;
            if (yy < 0 || yy >= h) continue;
            for (int sx = 0; sx < s.w; sx++) {
                int c = s.px[sy * s.w + (flip ? s.w - 1 - sx : sx)];
                if (c == 0) continue;
                int xx = x + sx;
                if (xx < 0 || xx >= w) continue;
                if (tintAmt > 0) c = mix(c, tint, tintAmt);
                int i = yy * w + xx;
                px[i] = alpha >= 255 ? c : mix(px[i], c, alpha);
            }
        }
    }

    /** Draws a sprite scaled by an integer factor. */
    public void spriteScaled(Sprite s, int x, int y, int scale, boolean flip) {
        for (int sy = 0; sy < s.h; sy++) {
            for (int sx = 0; sx < s.w; sx++) {
                int c = s.px[sy * s.w + (flip ? s.w - 1 - sx : sx)];
                if (c == 0) continue;
                rect(x + sx * scale, y + sy * scale, scale, scale, c);
            }
        }
    }

    // ------------------------------------------------------------------ text

    public int text(String s, int x, int y, int c) {
        return text(s, x, y, c, 1);
    }

    public int text(String s, int x, int y, int c, int scale) {
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                cx = x;
                y += (Font.H + 3) * scale;
                continue;
            }
            Font.Glyph g = Font.get(ch);
            for (int gy = 0; gy < Font.H; gy++) {
                String row = g.rows[gy];
                for (int gx = 0; gx < g.w; gx++) {
                    if (row.charAt(gx) != '.') {
                        if (scale == 1) pset(cx + gx, y + gy, c);
                        else rect(cx + gx * scale, y + gy * scale, scale, scale, c);
                    }
                }
            }
            cx += (g.w + 1) * scale;
        }
        return cx - x;
    }

    public void textShadow(String s, int x, int y, int c, int shadow) {
        text(s, x + 1, y + 1, shadow);
        text(s, x, y, c);
    }

    public void textShadow(String s, int x, int y, int c, int shadow, int scale) {
        text(s, x + scale, y + scale, shadow, scale);
        text(s, x, y, c, scale);
    }

    public void textCenter(String s, int cx, int y, int c) {
        text(s, cx - textWidth(s) / 2, y, c);
    }

    public void textCenterShadow(String s, int cx, int y, int c, int shadow) {
        textShadow(s, cx - textWidth(s) / 2, y, c, shadow);
    }

    public void textCenterShadow(String s, int cx, int y, int c, int shadow, int scale) {
        textShadow(s, cx - textWidth(s) * scale / 2, y, c, shadow, scale);
    }

    public static int textWidth(String s) {
        int wdt = 0, best = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                best = Math.max(best, wdt);
                wdt = 0;
                continue;
            }
            wdt += Font.get(ch).w + 1;
        }
        best = Math.max(best, wdt);
        return best > 0 ? best - 1 : 0;
    }

    /** Greedy word wrap into lines no wider than maxW pixels. */
    public static ArrayList<String> wrap(String s, int maxW) {
        ArrayList<String> out = new ArrayList<String>();
        String[] paras = s.split("\n");
        for (String p : paras) {
            String[] words = p.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String cand = line.length() == 0 ? word : line + " " + word;
                if (textWidth(cand) > maxW && line.length() > 0) {
                    out.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(cand);
                }
            }
            out.add(line.toString());
        }
        return out;
    }
}
