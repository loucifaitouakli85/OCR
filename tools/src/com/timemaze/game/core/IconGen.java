package com.timemaze.game.core;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Generates the launcher icons from pixel art. Usage: IconGen resDir */
public final class IconGen {
    static final int BG = 0xFF241338;

    /** 30x30 icon art: a clock face holding Milo and his remnant. */
    static Gfx art() {
        Gfx g = new Gfx(30, 30);
        g.clear(0);
        g.circle(15, 15, 14, 0xFF8C6420);
        g.circle(15, 15, 13, 0xFFE8C050);
        g.circle(15, 15, 11, 0xFF2A1640);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            g.pset(15 + (int) Math.round(Math.cos(a) * 10), 15 + (int) Math.round(Math.sin(a) * 10), 0xFFE8C050);
        }
        g.spriteEx(Sprites.PORTRAIT_MILO, 4, 6, false, Renderer.REMNANT_TINT, 170, 200);
        g.sprite(Sprites.PORTRAIT_MILO, 8, 9, false);
        // keep the portrait inside the round face
        for (int y = 0; y < 30; y++)
            for (int x = 0; x < 30; x++) {
                double d = Math.hypot(x - 15, y - 15);
                if (d > 11.5 && d <= 14.6 && g.px[y * 30 + x] != 0) {
                    int c = g.px[y * 30 + x];
                    if (c != 0xFF8C6420 && c != 0xFFE8C050) g.px[y * 30 + x] = d > 13.5 ? 0xFF8C6420 : 0xFFE8C050;
                }
                if (d > 14.6) g.px[y * 30 + x] = 0;
            }
        return g;
    }

    static void write(int size, int k, boolean legacy, File out) throws Exception {
        write(size, k, legacy ? 1 : 0, out);
    }

    /** mode 0: transparent background, 1: rounded square, 2: full-bleed opaque square. */
    static void write(int size, int k, int mode, File out) throws Exception {
        boolean legacy = mode == 1;
        Gfx a = art();
        BufferedImage img = new BufferedImage(size, size, mode == 2 ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        int off = (size - 30 * k) / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int c = mode == 2 ? BG : 0;
                if (legacy) {
                    // rounded square background
                    int r = size / 6;
                    int cx = Math.min(x, size - 1 - x), cy = Math.min(y, size - 1 - y);
                    boolean inside = cx >= r || cy >= r || Math.hypot(r - cx, r - cy) <= r;
                    if (inside) c = BG;
                }
                int ax = (x - off) / k, ay = (y - off) / k;
                if (x >= off && y >= off && ax < 30 && ay < 30) {
                    int p = a.px[ay * 30 + ax];
                    if (p != 0) c = p;
                }
                img.setRGB(x, y, c);
            }
        }
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
    }

    public static void main(String[] args) throws Exception {
        File res = new File(args[0]);
        String[] dens = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
        int[] legacy = {48, 72, 96, 144, 192};
        int[] legacyK = {1, 2, 3, 4, 6};
        int[] fg = {108, 162, 216, 324, 432};
        int[] fgK = {2, 3, 4, 6, 8};
        for (int i = 0; i < dens.length; i++) {
            write(legacy[i], legacyK[i], true, new File(res, "mipmap-" + dens[i] + "/ic_launcher.png"));
            write(fg[i], fgK[i], false, new File(res, "mipmap-" + dens[i] + "/ic_launcher_foreground.png"));
        }
        // a large preview for the README
        if (args.length > 1) write(256, 8, true, new File(args[1]));
        // web (PWA) and iOS icons
        if (args.length > 2) {
            File web = new File(args[2]);
            write(192, 6, 1, new File(web, "icon-192.png"));
            write(512, 17, 1, new File(web, "icon-512.png"));
            write(512, 12, 2, new File(web, "icon-maskable-512.png"));
            write(180, 5, 2, new File(web, "apple-touch-icon.png"));
        }
        if (args.length > 3) write(1024, 28, 2, new File(args[3]));
    }
}
