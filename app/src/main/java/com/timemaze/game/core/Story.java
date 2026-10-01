package com.timemaze.game.core;

/** The intro and the ending: short scripted beats over hand drawn scenes. */
public final class Story {
    private Story() {}

    public static final int ATTIC = 0, FALL = 1, MAZE = 2, WHISPER = 3, HEART = 4, CRACK = 5, OLD = 6, GONE = 7,
        OUTSIDE = 8, STRANGER = 9, TBC = 10;

    public static final class Beat {
        public final int scene;
        public final String line;

        Beat(int scene, String line) {
            this.scene = scene;
            this.line = line;
        }
    }

    public static final Beat[] INTRO = {
        new Beat(ATTIC, "N:MILO WAS NINE YEARS OLD WHEN HE FOUND THE CLOCK DOOR."),
        new Beat(ATTIC, "N:IT WAS HIDDEN BEHIND GRANDPA'S DUSTY WORKBENCH. IT WAS TICKING... EVEN THOUGH IT HAD NO HANDS."),
        new Beat(FALL, "N:HE OPENED IT. THE FLOOR VANISHED, AND TIME ITSELF SWALLOWED HIM WHOLE."),
        new Beat(MAZE, "N:HE WOKE IN THE TIME MAZE: THIRTY SEALED CHAMBERS, EACH WITH A STRANGE HUMMING MACHINE."),
        new Beat(WHISPER, "N:A VOICE WHISPERED FROM THE WALLS: 'EVERY DOOR OPENS FOR ONE WHO CAN BE IN TWO PLACES AT ONCE.'"),
        new Beat(WHISPER, "N:'REACH THE HEART OF THE MAZE... AND YOU WILL FIND YOUR WAY HOME.'"),
    };

    public static final Beat[] ENDING = {
        new Beat(HEART, "N:THE LAST DOOR SWUNG OPEN, AND MILO STEPPED INTO THE HEART OF THE MAZE."),
        new Beat(HEART, "N:THE HEART OF TIME BEAT ONCE..."),
        new Beat(CRACK, "N:...AND CRACKED."),
        new Beat(OLD, "O:SO IT BEGINS AGAIN. JUST AS IT DID FOR ME."),
        new Beat(OLD, "M:I DIDN'T KNOW! I ONLY WANTED TO GO HOME!"),
        new Beat(OLD, "O:NEITHER DID I. LISTEN TO ME, MILO. THE REMNANTS AREN'T ECHOES. THEY'RE..."),
        new Beat(GONE, "N:HE WAS GONE. ERASED MID-WORD, LIKE A CANDLE IN THE WIND."),
        new Beat(OUTSIDE, "N:BEYOND THE MAZE, THE SKY SPLIT INTO A THOUSAND CLOCK FACES. THE WHOLE WORLD STOOD STILL."),
        new Beat(OUTSIDE, "N:AND FROM EVERY CRACK IN TIME, A REMNANT OF MILO STEPPED OUT."),
        new Beat(STRANGER, "X:AT LAST. THE KEY HAS TURNED."),
        new Beat(STRANGER, "X:THANK YOU, MILO. NOW... THE REAL MAZE BEGINS."),
        new Beat(TBC, ""),
    };

    static int hash(int a, int b) {
        int h = a * 374761393 + b * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    /** Draws a 320x192 scene at (x0, y0); t is the frame count within the beat sequence. */
    public static void draw(Gfx g, int scene, int x0, int y0, int t) {
        int ox = g.ox, oy = g.oy;
        g.ox += x0;
        g.oy += y0;
        switch (scene) {
            case ATTIC: attic(g, t); break;
            case FALL: fall(g, t); break;
            case MAZE: maze(g, t, false); break;
            case WHISPER: maze(g, t, true); break;
            case HEART: heart(g, t, 0); break;
            case CRACK: heart(g, t, 1); break;
            case OLD: heart(g, t, 2); break;
            case GONE: heart(g, t, 3); break;
            case OUTSIDE: outside(g, t); break;
            case STRANGER: stranger(g, t); break;
            default: g.rect(0, 0, 320, 192, 0xFF000000);
        }
        g.ox = ox;
        g.oy = oy;
    }

    private static void attic(Gfx g, int t) {
        for (int x = 0; x < 320; x += 12) {
            g.rect(x, 0, 12, 192, (x / 12 & 1) == 0 ? 0xFF3A2418 : 0xFF45291B);
            g.rect(x, 0, 1, 192, 0xFF24160E);
        }
        // roof beams
        for (int i = 0; i < 40; i++) {
            g.rect(0, i, 160 - i * 4, 1, 0xFF1E120B);
            g.rect(160 + i * 4, i, 160 - i * 4, 1, 0xFF1E120B);
        }
        // window + moonbeam
        g.rect(34, 50, 30, 30, 0xFF1A2440);
        g.circle(52, 60, 5, 0xFFF0F0D8);
        g.frame(33, 49, 32, 32, 0xFF24160E);
        g.rect(48, 50, 2, 30, 0xFF24160E);
        g.rect(34, 64, 30, 2, 0xFF24160E);
        for (int y = 80; y < 160; y++) g.blendRect(34 + (y - 80), y, 34, 1, 0xFFBFD8FF, 28);
        for (int i = 0; i < 12; i++) {
            int px = 40 + (hash(i, 3) & 63) + (int) (Math.sin((t + i * 40) * 0.02) * 6);
            int py = 90 + ((hash(i, 5) & 63) + t / 3) % 70;
            g.blend(px, py, 0xFFFFF4D0, 140);
        }
        // floor
        g.rect(0, 160, 320, 32, 0xFF5A3A24);
        for (int x = 0; x < 320; x += 24) g.rect(x, 160, 1, 32, 0xFF3A2418);
        g.rect(0, 160, 320, 1, 0xFF7A5234);
        // workbench
        g.rect(236, 122, 70, 6, 0xFF6B4A2E);
        g.rect(236, 122, 70, 1, 0xFF8C6A44);
        g.rect(240, 128, 4, 32, 0xFF4A2E1C);
        g.rect(298, 128, 4, 32, 0xFF4A2E1C);
        g.rect(250, 116, 10, 6, 0xFF8A8FA8);
        g.rect(262, 118, 14, 2, 0xFF6A4020);
        Renderer.gear(g, 290, 114, 7, 8, t * 0.01f, 0xFFB08040, 0xFF3A2418);
        // the clock door
        int cx = 178, cy = 110;
        int pulse = 60 + (int) (50 * Math.sin(t * 0.07));
        g.blendCircle(cx, cy, 36, 0xFFFFD27A, pulse / 3);
        g.circle(cx, cy, 30, 0xFF8C6420);
        g.circle(cx, cy, 27, 0xFFD9A441);
        g.circle(cx, cy, 24, 0xFFF4EEDC);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            g.pset(cx + (int) Math.round(Math.cos(a) * 20), cy + (int) Math.round(Math.sin(a) * 20), 0xFF22162B);
            g.pset(cx + (int) Math.round(Math.cos(a) * 19), cy + (int) Math.round(Math.sin(a) * 19), 0xFF22162B);
        }
        g.circle(cx, cy, 2, 0xFF8C6420);
        g.rect(cx - 30, cy + 26, 60, 34, 0xFF4A2E1C);
        g.rect(cx - 30, cy + 26, 60, 1, 0xFF6B4A2E);
        g.pset(cx + 20, cy + 44, 0xFFFFD27A);
        // Milo
        g.sprite(Sprites.boyFrame((t / 40) % 6 == 5 ? Sprites.A_BLINK : Sprites.A_IDLE), 118, 145, false);
    }

    private static void fall(Gfx g, int t) {
        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 320; x++) {
                double dx = x - 160, dy = y - 96;
                double r = Math.sqrt(dx * dx + dy * dy);
                double a = Math.atan2(dy, dx);
                int band = (int) (a * 3 / Math.PI + r * 0.08 - t * 0.12) & 3;
                int[] pal = {0xFF140A2A, 0xFF22124A, 0xFF301A66, 0xFF1A2A6A};
                int c = pal[band];
                if (r < 30) c = Gfx.mix(c, 0xFFBFF4FF, (int) ((30 - r) * 6));
                g.pset(x, y, c);
            }
        }
        for (int i = 0; i < 40; i++) {
            double a = hash(i, 7) * 0.001;
            double d = ((hash(i, 9) & 255) + t * 2) % 260;
            int px = 160 + (int) (Math.cos(a) * d), py = 96 + (int) (Math.sin(a) * d * 0.7);
            g.pset(px, py, 0xFFFFFFFF);
        }
        for (int i = 0; i < 6; i++) {
            double a = i * 1.05 + t * 0.01;
            double d = 40 + ((t * 1.3 + i * 30) % 120);
            int n = (hash(i, t / 30) & 7) + 1;
            Renderer.digits(g, n, 160 + (int) (Math.cos(a) * d), 96 + (int) (Math.sin(a) * d * 0.6), 0xFF6FE8FF);
        }
        int bob = (int) (Math.sin(t * 0.1) * 3);
        g.spriteScaled(Sprites.BOY_FALL, 148, 80 + bob, 2, (t / 20 & 1) == 0);
    }

    private static void maze(Gfx g, int t, boolean whisper) {
        Theme th = Theme.ZONES[0];
        g.rect(0, 0, 320, 192, th.bg);
        for (int y = 0; y < 192; y += 12) g.rect(0, y + 11, 320, 1, th.bg2);
        Renderer.gear(g, 70, 70, 40, 12, t * 0.004f, th.gear, th.bg);
        Renderer.gear(g, 250, 60, 26, 9, -t * 0.006f, th.gear, th.bg);
        // stone floor
        for (int x = 0; x < 320; x += 16) {
            g.rect(x, 160, 16, 32, th.wall);
            g.rect(x, 160, 16, 3, th.lip);
            g.rect(x + ((x / 16 & 1) == 0 ? 5 : 12), 167, 1, 25, th.wallD);
            g.rect(x, 175, 16, 1, th.wallD);
        }
        Renderer.drawMachineAt(g, 200, 128, t, false, 0, 0);
        // Milo waking up
        Sprite s = t < 120 ? Sprites.BOY_FALL : Sprites.boyFrame((t / 50) % 5 == 4 ? Sprites.A_BLINK : Sprites.A_IDLE);
        g.sprite(s, 150, 144, false);
        if (whisper) {
            g.blendRect(0, 0, 320, 192, 0xFF000000, 150);
            int a = 60 + (int) (60 * Math.sin(t * 0.05));
            g.blendRect(262, 40, 3, 2, 0xFFFFD34A, a);
            g.blendRect(272, 40, 3, 2, 0xFFFFD34A, a);
        }
    }

    private static void heart(Gfx g, int t, int phase) {
        g.rect(0, 0, 320, 192, 0xFF12081C);
        int cx = 160, cy = 78;
        // rotating rays
        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 320; x++) {
                double a = Math.atan2(y - cy, x - cx) + t * 0.004;
                int k = (int) Math.floor(a * 12 / Math.PI) & 1;
                if (k == 0) g.blend(x, y, 0xFF5A2A7A, 70);
            }
        }
        int shake = phase == 1 ? (hash(t, 1) % 3) : 0;
        cx += shake;
        int pulse = (int) (6 * Math.sin(t * (phase == 0 ? 0.08 : 0.25)));
        g.blendCircle(cx, cy, 52 + pulse, 0xFFFF7AD8, 50);
        g.circle(cx, cy, 44, 0xFF8C6420);
        g.circle(cx, cy, 41, 0xFFE8C050);
        g.circle(cx, cy, 37, 0xFF3A1650);
        for (int r = 34; r > 0; r -= 2) g.circle(cx, cy, r, Gfx.mix(0xFF6A2A9A, 0xFFFFFFFF, (34 - r) * 7));
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            g.rect(cx + (int) (Math.cos(a) * 39) - 1, cy + (int) (Math.sin(a) * 39) - 1, 2, 2, 0xFF22162B);
        }
        double h1 = t * (phase == 0 ? 0.02 : -0.3), h2 = t * (phase == 0 ? 0.002 : -0.05);
        g.line(cx, cy, cx + (int) (Math.cos(h1) * 26), cy + (int) (Math.sin(h1) * 26), 0xFF22162B);
        g.line(cx, cy, cx + (int) (Math.cos(h2) * 18), cy + (int) (Math.sin(h2) * 18), 0xFF22162B);
        if (phase >= 1) {
            // cracks
            int[][] cracks = {{0, -40, 8, -20, -4, -6, 6, 10, 0, 40}, {-38, 6, -18, 2, -8, 14}, {38, -8, 20, -2, 10, 12}};
            for (int[] c : cracks) {
                for (int i = 0; i + 3 < c.length; i += 2) g.line(cx + c[i], cy + c[i + 1], cx + c[i + 2], cy + c[i + 3], 0xFFFFFFFF);
            }
            if (phase == 1 && (t / 6) % 5 == 0) g.blendRect(0, 0, 320, 192, 0xFFFFFFFF, 120);
        }
        // floor
        g.rect(0, 160, 320, 32, 0xFF3E2456);
        g.rect(0, 160, 320, 2, 0xFFE8C050);
        g.sprite(Sprites.boyFrame(Sprites.A_IDLE), 140, 145, false);
        if (phase == 2) {
            int alpha = 160 + (int) (80 * Math.sin(t * 0.3));
            if ((hash(t / 3, 4) & 7) == 0) alpha = 60;
            g.spriteEx(Sprites.OLD_MILO, 170, 142, true, 0, 0, alpha);
        } else if (phase == 3) {
            for (int i = 0; i < 30; i++) {
                int life = (t * 2 + i * 17) % 90;
                g.blend(176 + (hash(i, 1) & 15) - 4, 160 - life, 0xFFB8B4C0, 200 - life * 2);
            }
        }
    }

    private static void outside(Gfx g, int t) {
        for (int y = 0; y < 192; y++) g.rect(0, y, 320, 1, Gfx.mix(0xFF0A0A24, 0xFF3A1A4A, y));
        // clock faces filling the sky
        for (int i = 0; i < 14; i++) {
            int h = hash(i, 21);
            int cx = (h & 0x1FF) % 320, cy = 12 + ((h >>> 9) & 0x7F) % 90, r = 6 + ((h >>> 16) & 15);
            g.blendCircle(cx, cy, r, 0xFFE8E0C8, 60);
            g.ring(cx, cy, r, 0xFFE8C050);
            double a = (h & 63) * 0.1;
            g.line(cx, cy, cx + (int) (Math.cos(a) * (r - 2)), cy + (int) (Math.sin(a) * (r - 2)), 0xFFE8C050);
        }
        // cracks in the sky
        int x = 40, y = 0;
        for (int i = 0; i < 12; i++) {
            int nx = x + 10 + (hash(i, 31) & 15), ny = y + 6 + (hash(i, 32) & 7);
            g.line(x, y, nx, ny, 0xFFFFFFFF);
            if (i % 3 == 1) g.line(nx, ny, nx + 8, ny - 6, 0xFFBFF4FF);
            x = nx;
            y = ny;
        }
        // town silhouette
        int[] roofs = {0, 110, 30, 96, 58, 104, 90, 84, 120, 100, 150, 92, 196, 104, 230, 88, 262, 98, 296, 90, 320, 100};
        for (int i = 0; i + 2 < roofs.length; i += 2) {
            int x0 = roofs[i], x1 = roofs[i + 2], top = roofs[i + 1];
            g.rect(x0, top, x1 - x0, 160 - top, 0xFF08060E);
            for (int k = 0; k < 3; k++) {
                int wx = x0 + 6 + k * 9;
                if (wx + 3 < x1 && (hash(i, k) & 3) != 0) g.rect(wx, top + 8 + (k & 1) * 12, 3, 4, 0xFFE8C050);
            }
        }
        // church tower with a clock
        g.rect(172, 50, 20, 60, 0xFF08060E);
        for (int k = 0; k < 10; k++) g.rect(182 - k, 40 + k, 1 + k * 2, 1, 0xFF08060E);
        g.circle(182, 66, 6, 0xFFE8E0C8);
        g.line(182, 66, 182, 61, 0xFF08060E);
        g.line(182, 66, 186, 68, 0xFF08060E);
        // street and remnants
        g.rect(0, 160, 320, 32, 0xFF14101E);
        g.rect(0, 160, 320, 1, 0xFF3A2C58);
        for (int i = 0; i < 9; i++) {
            int rx = 12 + i * 34 + (hash(i, 41) & 7);
            if (Math.abs(rx - 152) < 16) continue;
            int alpha = 100 + (hash(i, t / 8) & 63);
            g.spriteEx(Sprites.BOY_IDLE, rx, 145, (i & 1) == 0, Renderer.REMNANT_TINT, 150, alpha);
        }
        g.sprite(Sprites.BOY_IDLE, 152, 145, false);
    }

    private static void stranger(Gfx g, int t) {
        for (int y = 0; y < 192; y++) g.rect(0, y, 320, 1, Gfx.mix(0xFF1A0610, 0xFF4A0E1E, y));
        g.blendCircle(230, 70, 44, 0xFFFFE8D0, 200);
        g.blendCircle(230, 70, 52, 0xFFFFE8D0, 40);
        // rooftop
        g.rect(0, 150, 320, 42, 0xFF08040A);
        for (int k = 0; k < 40; k++) g.rect(120 + k, 150 - k / 2, 200 - k * 2, 1, 0xFF08040A);
        g.rect(250, 104, 10, 30, 0xFF08040A);
        // the stranger
        int sx = 190, sy = 112;
        g.spriteScaled(Sprites.STRANGER[(t / 10) % 3], sx, sy, 2, true);
        int glow = 150 + (int) (100 * Math.sin(t * 0.1));
        g.blendRect(sx + 8, sy + 10, 4, 2, 0xFFFFD34A, glow);
        g.blendRect(sx + 16, sy + 10, 4, 2, 0xFFFFD34A, glow);
        // tiny Milo remnants far below
        for (int i = 0; i < 6; i++) g.spriteEx(Sprites.BOY_IDLE, 20 + i * 22, 172, false, Renderer.REMNANT_TINT, 160, 70);
    }
}
