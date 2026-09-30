package com.timemaze.game.core;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Renders chambers to PNG files (for eyeballing the pixel art). */
public final class Shot {
    public static void save(Gfx g, int scale, String path) throws Exception {
        BufferedImage img = new BufferedImage(g.w * scale, g.h * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < g.h * scale; y++)
            for (int x = 0; x < g.w * scale; x++) img.setRGB(x, y, g.px[(y / scale) * g.w + x / scale]);
        ImageIO.write(img, "png", new File(path));
    }

    /** Usage: Shot level frameOfSolution out.png */
    public static void main(String[] a) throws Exception {
        int n = Integer.parseInt(a[0]);
        int frames = a.length > 1 ? Integer.parseInt(a[1]) : 0;
        String out = a.length > 2 ? a[2] : "shot.png";
        World w;
        if (frames > 0) {
            w = Bot.runFrames(Levels.get(n), Solutions.get(n), frames);
        } else {
            w = new World(Levels.get(n));
            for (int i = 0; i < 30; i++) w.tick(false, false, false, false);
        }
        Gfx g = new Gfx(320, 176);
        new Renderer().draw(g, w, 0, 0);
        save(g, 3, out);
    }
}
