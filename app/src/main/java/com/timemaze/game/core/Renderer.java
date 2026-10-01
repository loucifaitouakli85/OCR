package com.timemaze.game.core;

/** Draws a {@link World} (the 320x176 room) into a {@link Gfx}. */
public final class Renderer {
    static final int W = World.ROOM_W, H = World.ROOM_H, TILE = World.TILE;

    private World cachedFor;
    private int cachedStamp = -1;
    private final int[] bgCache = new int[W * H];
    private final int[] tileCache = new int[W * H];
    private Theme theme = Theme.ZONES[0];
    private final int[][] gears = new int[3][4]; // cx, cy, radius, teeth
    private int gearCount;

    public static final int REMNANT_TINT = 0xFF6FE8FF;

    // 3x5 digits for small labels
    private static final String[] DIGITS = {
        "####.##.##.####", "..#..#..#..#..#", "###..#####..###", "###..####..####", "#.##.####..#..#",
        "####..###..####", "####..####.####", "###..#..#..#..#", "####.#####.####", "####.####..####"
    };

    public static void digits(Gfx g, int value, int x, int y, int c) {
        String s = Integer.toString(value);
        for (int i = 0; i < s.length(); i++) {
            String d = DIGITS[s.charAt(i) - '0'];
            for (int k = 0; k < 15; k++) if (d.charAt(k) == '#') g.pset(x + i * 4 + k % 3, y + k / 3, c);
        }
    }

    static int hash(int a, int b, int c) {
        int h = a * 374761393 + b * 668265263 + c * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    // ------------------------------------------------------------ caches

    private int tileStamp(World w) {
        int s = 17;
        for (int r = 0; r < World.ROWS; r++)
            for (int c = 0; c < World.COLS; c++) s = s * 31 + w.tiles[r][c];
        return s;
    }

    private void buildCaches(World w) {
        theme = Theme.ZONES[w.level.zone];
        Theme t = theme;
        int seed = w.level.num;
        // background masonry
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int c = t.bg;
                int course = y / 12;
                int off = (course & 1) * 12;
                boolean mortar = y % 12 == 11 || (x + off) % 24 == 23;
                if (mortar) c = t.bg2;
                else if ((hash(x / 24 + (course & 1) * 97, course, seed) & 7) == 0) c = Gfx.mix(c, t.bg2, 90);
                if ((hash(x, y, seed) & 127) == 0) c = t.bg2;
                bgCache[y * W + x] = c;
            }
        }
        // soft vignette near the top
        for (int y = 0; y < 40; y++)
            for (int x = 0; x < W; x++) bgCache[y * W + x] = Gfx.mix(bgCache[y * W + x], 0xFF000000, (40 - y) * 2);
        // gears, spread out so they never overlap
        int placed = 0;
        for (int k = 0; k < 60 && placed < 3; k++) {
            int h = hash(seed, k, 99);
            int r = 16 + (h & 15) + (placed == 0 ? 10 : 0);
            int cx = 24 + ((h >>> 4) & 0xFFFF) % 272, cy = 24 + ((h >>> 12) & 0xFFFF) % 128;
            boolean ok = true;
            for (int j = 0; j < placed; j++) {
                int dx = cx - gears[j][0], dy = cy - gears[j][1], min = r + gears[j][2] + 6;
                if (dx * dx + dy * dy < min * min) ok = false;
            }
            if (!ok) continue;
            gears[placed][0] = cx;
            gears[placed][1] = cy;
            gears[placed][2] = r;
            gears[placed][3] = 8 + ((h >>> 20) & 7);
            placed++;
        }
        gearCount = placed;
        // tiles
        java.util.Arrays.fill(tileCache, 0);
        Gfx tg = new Gfx(1, 1);
        tg.w = W;
        tg.h = H;
        tg.px = tileCache;
        for (int r = 0; r < World.ROWS; r++)
            for (int c = 0; c < World.COLS; c++) drawTile(tg, w, c, r);
        cachedFor = w;
        cachedStamp = tileStamp(w);
    }

    private boolean isWall(World w, int c, int r) {
        if (c < 0 || c >= World.COLS || r < 0 || r >= World.ROWS) return true;
        int t = w.tiles[r][c];
        return t == World.T_WALL || t == World.T_EMITTER;
    }

    private void drawTile(Gfx g, World w, int c, int r) {
        int t = w.tiles[r][c];
        int x = c * TILE, y = r * TILE;
        Theme th = theme;
        if (t == World.T_WALL) {
            g.rect(x, y, TILE, TILE, th.wall);
            for (int k = 0; k < 2; k++) {
                int by = y + k * 8;
                int joint = ((r + k + c) & 1) == 0 ? 5 : 12;
                g.rect(x, by + 7, TILE, 1, th.wallD);
                g.rect(x + joint, by, 1, 7, th.wallD);
                g.rect(x, by, TILE, 1, Gfx.mix(th.wall, th.wallL, 150));
                g.rect(x + joint + 1, by, 1, 7, Gfx.mix(th.wall, th.wallL, 120));
            }
            for (int i = 0; i < 5; i++) {
                int h = hash(c, r, i + 7);
                int px = x + (h & 15), py = y + ((h >>> 4) & 15);
                g.pset(px, py, (h & 256) != 0 ? th.wallD : th.wallL);
            }
            boolean up = isWall(w, c, r - 1), down = isWall(w, c, r + 1);
            boolean lf = isWall(w, c - 1, r), rt = isWall(w, c + 1, r);
            if (!lf) g.rect(x, y, 1, TILE, th.edge);
            if (!rt) g.rect(x + TILE - 1, y, 1, TILE, th.edge);
            if (!down) g.rect(x, y + TILE - 1, TILE, 1, th.edge);
            if (!up) {
                g.rect(x, y, TILE, 1, th.edge);
                g.rect(x, y + 1, TILE, 2, th.lip);
                g.rect(x, y + 3, TILE, 1, th.lipD);
                for (int i = 0; i < TILE; i++) {
                    int h = hash(c * 16 + i, r, 3);
                    if (th.lipStyle == 1 && (h & 3) == 0) g.rect(x + i, y + 3, 1, 1 + ((h >>> 3) & 3), th.lip);
                    else if (th.lipStyle == 2 && (h & 7) == 0) g.rect(x + i, y + 3, 1, 2 + ((h >>> 3) & 3), th.lipD);
                    else if (th.lipStyle == 3 && (i & 3) == 1) g.pset(x + i, y + 2, 0xFFFFF0B0);
                    else if (th.lipStyle == 4 && (h & 7) == 0) g.rect(x + i, y + 3, 1, 1 + ((h >>> 3) & 3), 0xFFB0602A);
                    else if (th.lipStyle == 5 && (h & 7) == 0) g.pset(x + i, y + 1, (h & 64) != 0 ? 0xFFFFB060 : 0xFFFF7040);
                    else if (th.lipStyle == 0 && (h & 7) == 0) g.pset(x + i, y + 1, th.wallL);
                }
                if (!lf) g.rect(x, y, 1, 4, th.edge);
                if (!rt) g.rect(x + TILE - 1, y, 1, 4, th.edge);
            }
        } else if (t == World.T_ONEWAY) {
            g.rect(x, y, TILE, 4, th.edge);
            g.rect(x, y, TILE, 3, th.lipD);
            g.rect(x, y, TILE, 1, th.lip);
            g.rect(x + 3, y + 4, 2, 3, th.wallD);
            g.rect(x + 11, y + 4, 2, 3, th.wallD);
        } else if (t == World.T_SPIKE) {
            int base = 0xFF8A8FA8, hi = 0xFFD8DCEA, dk = 0xFF4A4E62;
            g.rect(x + 1, y + 14, 14, 2, dk);
            for (int s = 0; s < 3; s++) {
                int sx = x + 2 + s * 4;
                for (int k = 0; k < 6; k++) {
                    int half = k / 2;
                    g.rect(sx + 2 - half, y + 9 + k, 1 + half * 2, 1, base);
                    g.pset(sx + 2 - half, y + 9 + k, hi);
                }
                g.pset(sx + 2, y + 8, hi);
            }
        } else if (t == World.T_EMITTER) {
            g.rect(x + 1, y + 1, 14, 14, 0xFF3A3E52);
            g.frame(x + 1, y + 1, 14, 14, 0xFF1A1C28);
            g.rect(x + 3, y + 3, 10, 10, 0xFF5A5F78);
            g.rect(x + 4, y + 4, 8, 1, 0xFF8A8FA8);
        }
    }

    // ------------------------------------------------------------ frame

    /** Draws the room with its top-left corner at (ox, oy). */
    public void draw(Gfx g, World w, int ox, int oy) {
        if (cachedFor != w || cachedStamp != tileStamp(w)) buildCaches(w);
        Theme th = theme;
        int shakeX = 0, shakeY = 0;
        if (w.shake > 0) {
            shakeX = (hash(w.frameCounter, 1, 2) % 3);
            shakeY = (hash(w.frameCounter, 3, 4) % 2);
        }
        int bx = ox + shakeX, by = oy + shakeY;
        // background
        for (int y = 0; y < H; y++) {
            int gy = by + y;
            if (gy < 0 || gy >= g.h) continue;
            int x0 = Math.max(0, -bx), x1 = Math.min(W, g.w - bx);
            if (x1 > x0) System.arraycopy(bgCache, y * W + x0, g.px, gy * g.w + bx + x0, x1 - x0);
        }
        g.ox = bx;
        g.oy = by;
        float spin = w.frameCounter * 0.004f;
        for (int i = 0; i < gearCount; i++) {
            float dir = (i & 1) == 0 ? 1 : -1;
            gear(g, gears[i][0], gears[i][1], gears[i][2], gears[i][3], spin * dir * 30f / gears[i][2], th.gear, th.bg);
        }
        drawLiftTracks(g, w, th);
        // tiles
        for (int y = 0; y < H; y++) {
            int gy = by + y;
            if (gy < 0 || gy >= g.h) continue;
            int row = y * W, dst = gy * g.w + bx;
            for (int x = 0; x < W; x++) {
                int c = tileCache[row + x];
                if (c != 0 && bx + x >= 0 && bx + x < g.w) g.px[dst + x] = c;
            }
        }
        drawCrumble(g, w, th);
        drawMachine(g, w, th);
        drawExit(g, w, th);
        for (World.Rift rf : w.rifts) drawRift(g, w, rf);
        for (World.Plate p : w.plates) drawPlate(g, w, p);
        for (World.Lever l : w.levers) drawLever(g, w, l);
        for (World.Door d : w.doors) drawDoor(g, w, d);
        for (World.Lift l : w.lifts) drawLift(g, w, l);
        for (World.Laser z : w.lasers) drawLaser(g, w, z);
        drawBodies(g, w);
        drawShade(g, w);
        for (World.Particle p : w.particles) {
            int a = 255 * p.life / Math.max(1, p.max);
            g.blend((int) p.x, (int) p.y, p.color, a);
        }
        if (w.dark) drawDarkness(g, w);
        drawStateFx(g, w);
        g.ox = 0;
        g.oy = 0;
    }

    public static void gear(Gfx g, int cx, int cy, int R, int teeth, float ang, int col, int bg) {
        int hole = R / 3;
        for (int y = -R; y <= R; y++) {
            for (int x = -R; x <= R; x++) {
                int d2 = x * x + y * y;
                if (d2 > R * R) continue;
                float r = (float) Math.sqrt(d2);
                float a = (float) Math.atan2(y, x) + ang;
                boolean on;
                if (r > R - 4) {
                    float f = a * teeth / (float) (2 * Math.PI);
                    f -= (float) Math.floor(f);
                    on = f < 0.5f;
                } else if (r < hole) {
                    on = r > hole - 3;
                } else if (r > R * 0.72f || r < hole + 3) {
                    on = true;
                } else {
                    float f = a * 5 / (float) (2 * Math.PI);
                    f -= (float) Math.floor(f);
                    on = f < 0.18f;
                }
                if (on) g.pset(cx + x, cy + y, (r > R - 5 && r < R - 3) ? Gfx.mix(col, bg, 80) : col);
            }
        }
    }

    private void drawLiftTracks(Gfx g, World w, Theme th) {
        for (World.Lift l : w.lifts) {
            int x0 = (int) l.x0 + l.width * TILE / 2, x1 = (int) l.x1 + l.width * TILE / 2;
            int y0 = (int) l.y0 + 3, y1 = (int) l.y1 + 3;
            int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
            for (int i = 0; i <= steps; i += 3) {
                int x = x0 + (x1 - x0) * i / Math.max(1, steps);
                int y = y0 + (y1 - y0) * i / Math.max(1, steps);
                g.pset(x, y, Gfx.mix(th.bg, th.accent, 90));
            }
        }
    }

    private void drawMachine(Gfx g, World w, Theme th) {
        drawMachineAt(g, (int) w.machineX, (int) w.machineY, w.frameCounter, w.machineDepleted(),
            w.maxRemnants < 9 ? Math.min(w.maxRemnants, 9) : 0, w.remnants.size());
    }

    /** The time machine booth (16x32). cap > 0 shows that many charge lights. */
    public static void drawMachineAt(Gfx g, int x, int y, int f, boolean dead, int cap, int used) {
        int brass = 0xFFD9A441, brassD = 0xFF8C6420, brassL = 0xFFFFD27A;
        int metal = 0xFF5A5F78, metalD = 0xFF2E3142;
        int glow = dead ? 0xFF7A4A5A : 0xFF4FD8FF;
        // base
        g.rect(x, y + 28, 16, 4, metalD);
        g.rect(x + 1, y + 28, 14, 1, metal);
        g.rect(x + 3, y + 30, 2, 1, dead ? 0xFFFF4040 : 0xFF40FF80);
        g.rect(x + 11, y + 30, 2, 1, (f / 20 & 1) == 0 ? brassL : brassD);
        // glass tube
        int pulse = 60 + (int) (40 * Math.sin(f * 0.08));
        g.blendRect(x + 2, y + 8, 12, 20, glow, dead ? 40 : pulse);
        for (int i = 0; i < 4; i++) {
            int py = y + 27 - ((f / 2 + i * 6) % 20);
            int px = x + 4 + (hash(i, (f / 2 + i * 6) / 20, 5) & 7);
            if (!dead) g.pset(px, py, 0xFFE0FAFF);
        }
        g.blendRect(x + 3, y + 9, 1, 17, 0xFFFFFFFF, 90);
        // pillars
        g.rect(x, y + 6, 2, 22, brassD);
        g.rect(x + 14, y + 6, 2, 22, brassD);
        g.rect(x, y + 6, 1, 22, brass);
        g.rect(x + 14, y + 6, 1, 22, brass);
        // dome and clock
        g.rect(x + 2, y + 2, 12, 6, brass);
        g.rect(x + 4, y, 8, 2, brass);
        g.rect(x + 1, y + 6, 14, 2, brassD);
        g.rect(x + 3, y + 1, 10, 1, brassL);
        g.circle(x + 8, y + 4, 3, 0xFFF4EEDC);
        double a = -f * 0.06;
        g.pset(x + 8, y + 4, 0xFF22162B);
        g.pset(x + 8 + (int) Math.round(Math.cos(a) * 2), y + 4 + (int) Math.round(Math.sin(a) * 2), 0xFF22162B);
        g.pset(x + 8, y + 2, 0xFF22162B);
        // charge lights when the machine is limited
        for (int i = 0; i < cap; i++) g.pset(x + 3 + i * 2, y + 29, i < used ? 0xFF3A3A48 : 0xFF6FE8FF);
    }

    private void drawExit(Gfx g, World w, Theme th) {
        int x = (int) w.exitX, y = (int) w.exitY;
        int f = w.frameCounter;
        float o = w.exitOpen;
        int stone = th.wallL, stoneD = th.wallD;
        // portal interior
        for (int yy = 4; yy < 32; yy++) {
            for (int xx = 2; xx < 14; xx++) {
                int c;
                if (o > 0) {
                    double d = Math.sqrt((xx - 7.5) * (xx - 7.5) + (yy - 18) * (yy - 18) * 0.35);
                    int band = (int) (d * 1.3 - f * 0.15) & 3;
                    int[] pal = {0xFFFFFFFF, 0xFFBFF4FF, th.accent, 0xFF6A4AC8};
                    c = Gfx.mix(0xFF0A0610, pal[band], (int) (230 * o));
                } else {
                    c = 0xFF0A0610;
                }
                g.pset(x + xx, y + yy, c);
            }
        }
        // bars retract upward
        int barH = (int) (28 * (1f - o));
        for (int b = 0; b < 3; b++) {
            int bx = x + 3 + b * 4;
            g.rect(bx, y + 4, 2, barH, 0xFF6A6F86);
            g.rect(bx, y + 4, 1, barH, 0xFFA8ACC0);
            if (barH > 2) g.rect(bx, y + 4 + barH - 2, 2, 2, 0xFF3A3E52);
        }
        if (barH > 0) {
            g.rect(x + 2, y + 12, 12, 1, 0xFF3A3E52);
            g.rect(x + 2, y + 22, 12, 1, 0xFF3A3E52);
        }
        // arch
        g.rect(x, y + 2, 2, 30, stone);
        g.rect(x + 14, y + 2, 2, 30, stone);
        g.rect(x + 1, y, 14, 4, stone);
        g.rect(x + 1, y + 3, 14, 1, stoneD);
        g.rect(x, y + 2, 1, 30, stoneD);
        g.rect(x + 15, y + 2, 1, 30, stoneD);
        g.rect(x + 2, y, 12, 1, Gfx.lighten(stone, 60));
        // requirement lights
        reqLights(g, w.exitReq, x + 8, y + 1, w.sig);
    }

    /** Small coloured studs showing which channels a mechanism listens to. */
    private void reqLights(Gfx g, World.Req req, int cx, int y, boolean[] sig) {
        int n = req.ch.length;
        int x0 = cx - n * 2 + 1;
        for (int i = 0; i < n; i++) {
            int col = Theme.channel(req.ch[i]);
            boolean met = sig[req.ch[i]] != req.neg[i];
            int x = x0 + i * 4;
            if (req.neg[i]) {
                g.frame(x - 1, y, 3, 3, met ? col : Gfx.darken(col, 150));
            } else {
                g.rect(x - 1, y, 3, 3, met ? col : Gfx.darken(col, 160));
                if (met) g.pset(x - 1, y, Gfx.lighten(col, 120));
            }
        }
    }

    private void drawPlate(Gfx g, World w, World.Plate p) {
        if (w.plateFallen(p)) return;
        int x = (int) p.x, y = (int) p.y;
        int col = Theme.channel(p.channel);
        g.rect(x - 1, y + 1, 14, 2, 0xFF2E3142);
        if (p.weight > 1 && !p.broken) {
            // heavy plate: iron rim and one pip per body it needs
            g.rect(x - 2, y, 16, 3, 0xFF4A4E62);
            g.rect(x - 2, y, 16, 1, 0xFF8A8FA8);
            for (int i = 0; i < p.weight; i++) {
                int px = x + 6 - p.weight * 2 + i * 4;
                g.rect(px, y - 5, 3, 3, 0xFF1A1C28);
                g.rect(px, y - 5, 2, 2, i < p.load ? col : Gfx.darken(col, 150));
            }
        }
        if (p.broken) {
            g.rect(x, y + 1, 12, 1, 0xFF5A5A62);
            g.pset(x + 3, y, 0xFF5A5A62);
            g.pset(x + 8, y, 0xFF5A5A62);
            if ((w.frameCounter / 7 + p.col) % 9 == 0) g.pset(x + 2 + (w.frameCounter % 8), y - 2, 0xFFFFD27A);
            return;
        }
        boolean lit = p.pressed || p.timer > 0;
        if (p.pressed) {
            g.rect(x, y + 1, 12, 1, col);
        } else {
            g.rect(x, y - 1, 12, 2, lit ? col : Gfx.darken(col, 110));
            g.rect(x + 1, y - 1, 10, 1, lit ? Gfx.lighten(col, 110) : Gfx.darken(col, 60));
        }
        if (p.timerFrames > 0) {
            // little clock plus remaining time bar
            int cx = x + 6, cy = y - 7;
            g.circle(cx, cy, 3, 0xFF2E3142);
            g.ring(cx, cy, 3, col);
            double a = p.timer > 0 ? -Math.PI / 2 + 2 * Math.PI * (1 - (double) p.timer / p.timerFrames) : -Math.PI / 2;
            g.line(cx, cy, cx + (int) Math.round(Math.cos(a) * 2), cy + (int) Math.round(Math.sin(a) * 2), 0xFFFFFFFF);
            if (p.timer > 0) {
                int bw = 12 * p.timer / p.timerFrames;
                g.rect(x, y - 2, bw, 1, Gfx.lighten(col, 80));
            }
        }
    }

    private void drawCrumble(Gfx g, World w, Theme th) {
        for (int r = 0; r < World.ROWS; r++) {
            for (int c = 0; c < World.COLS; c++) {
                if (w.tiles[r][c] != World.T_CRUMBLE) continue;
                int st = w.crumble[r][c];
                if (st < 0) continue;
                int x = c * TILE, y = r * TILE;
                if (st > 0) {
                    x += (hash(w.frameCounter / 2, c, r) & 2) - 1;
                    y += st < 15 ? (w.frameCounter & 1) : 0;
                }
                int base = Gfx.mix(th.wall, 0xFF8A7A6A, 70);
                g.rect(x, y, TILE, TILE, base);
                g.rect(x, y, TILE, 2, th.lip);
                g.rect(x, y + TILE - 1, TILE, 1, th.edge);
                g.rect(x, y, 1, TILE, th.edge);
                g.rect(x + TILE - 1, y, 1, TILE, th.edge);
                // cracks widen as it gives way
                int dk = th.edge;
                g.line(x + 3, y + 2, x + 6, y + 8, dk);
                g.line(x + 6, y + 8, x + 4, y + 14, dk);
                g.line(x + 6, y + 8, x + 12, y + 10, dk);
                g.line(x + 11, y + 2, x + 9, y + 6, dk);
                if (st > 0 && st < 30) {
                    g.line(x + 12, y + 10, x + 14, y + 15, dk);
                    g.line(x + 2, y + 11, x + 5, y + 9, dk);
                }
                for (int i = 0; i < 4; i++) {
                    int h = hash(c, r, i + 40);
                    g.pset(x + 1 + (h & 13), y + 3 + ((h >>> 4) & 11), th.wallL);
                }
            }
        }
    }

    private void drawRift(Gfx g, World w, World.Rift rf) {
        int col = rf.req.ch.length > 0 ? Theme.channel(rf.req.ch[0]) : 0xFFB98CFF;
        for (int end = 0; end < 2; end++) {
            int x = (int) (end == 0 ? rf.ax : rf.bx), y = (int) (end == 0 ? rf.ay : rf.by);
            int cx = x + 8, cy = y + 8;
            if (rf.active) {
                for (int ring = 7; ring >= 1; ring--) {
                    int band = (ring + w.frameCounter / 4 + end * 2) & 3;
                    int[] pal = {0xFF2A1048, 0xFF6A3AC8, 0xFFB98CFF, 0xFFE8D8FF};
                    for (int yy = -ring - 1; yy <= ring + 1; yy++) {
                        int span = (int) Math.round(Math.sqrt(Math.max(0, (ring + 1) * (ring + 1) - yy * yy)) * 0.6);
                        g.rect(cx - span, cy + yy, span * 2 + 1, 1, pal[band]);
                    }
                }
                g.blendRect(cx - 1, cy - 7, 2, 14, 0xFFFFFFFF, 120);
            } else {
                for (int yy = -8; yy <= 8; yy += 2) {
                    int span = (int) Math.round(Math.sqrt(64 - yy * yy) * 0.6);
                    g.pset(cx - span, cy + yy, 0xFF4A3A66);
                    g.pset(cx + span, cy + yy, 0xFF4A3A66);
                }
            }
            reqLights(g, rf.req, cx, y - 4, w.sig);
        }
    }

    private int[] lightBuf = new int[W * H];

    private void light(int cx, int cy, int r, int strength) {
        int r2 = r * r;
        for (int y = Math.max(0, cy - r); y < Math.min(H, cy + r); y++) {
            int dy = y - cy;
            for (int x = Math.max(0, cx - r); x < Math.min(W, cx + r); x++) {
                int dx = x - cx;
                int d2 = dx * dx + dy * dy;
                if (d2 >= r2) continue;
                int v = strength * (r2 - d2) / r2;
                int i = y * W + x;
                if (v > lightBuf[i]) lightBuf[i] = v;
            }
        }
    }

    /** Lights out: everything fades to black except around the boy, his remnants and the machines. */
    private void drawDarkness(Gfx g, World w) {
        java.util.Arrays.fill(lightBuf, 0);
        if (w.state != World.DEAD) light((int) w.x + 4, (int) w.y + 7, 52, 300);
        for (World.Remnant r : w.remnants) if (r.active) light((int) r.x + 4, (int) r.y + 7, 34, 260);
        light((int) w.machineX + 8, (int) w.machineY + 16, 26, 230);
        if (w.exitOpen > 0) light((int) w.exitX + 8, (int) w.exitY + 18, 24, 220);
        for (World.Rift rf : w.rifts) {
            if (!rf.active) continue;
            light((int) rf.ax + 8, (int) rf.ay + 8, 18, 200);
            light((int) rf.bx + 8, (int) rf.by + 8, 18, 200);
        }
        for (World.Laser z : w.lasers) if (z.on) light(z.col * TILE + 8, z.row * TILE + 8, 14, 200);
        for (int y = 0; y < H; y++) {
            int gy = g.oy + y;
            if (gy < 0 || gy >= g.h) continue;
            for (int x = 0; x < W; x++) {
                int gx = g.ox + x;
                if (gx < 0 || gx >= g.w) continue;
                int l = Math.min(255, lightBuf[y * W + x]);
                int a = 236 - l;
                if (a <= 0) continue;
                a = Math.min(236, (a + 20) / 40 * 40); // banded, like old lamplight
                int i = gy * g.w + gx;
                g.px[i] = Gfx.mix(g.px[i], 0xFF030206, a);
            }
        }
    }

    private void drawLever(Gfx g, World w, World.Lever l) {
        int x = (int) l.x, y = (int) l.y;
        int col = Theme.channel(l.channel);
        g.rect(x + 4, y + 12, 8, 4, 0xFF3A3E52);
        g.rect(x + 5, y + 12, 6, 1, 0xFF6A6F86);
        if (l.broken) {
            g.line(x + 8, y + 12, x + 10, y + 10, 0xFF8A8FA8);
            g.pset(x + 3, y + 15, 0xFF8A8FA8);
            g.rect(x + 12, y + 14, 2, 2, Gfx.darken(col, 120));
            return;
        }
        int tipX = l.on ? x + 12 : x + 4, tipY = y + 5;
        g.line(x + 8, y + 12, tipX, tipY, 0xFFA8ACC0);
        g.rect(tipX - 1, tipY - 1, 3, 3, l.on ? col : Gfx.darken(col, 100));
        g.pset(tipX - 1, tipY - 1, Gfx.lighten(col, 120));
        g.pset(x + 8, y + 14, l.on ? col : Gfx.darken(col, 140));
    }

    private void drawDoor(Gfx g, World w, World.Door d) {
        int x = (int) d.x, top = (int) d.top;
        float sh = d.solidH();
        int h = (int) Math.ceil(sh);
        int metal = 0xFF6A6F86, metalD = 0xFF3A3E52, metalL = 0xFFA8ACC0;
        // housing
        g.rect(x - 2, top, 14, 3, metalD);
        g.rect(x - 2, top, 14, 1, metal);
        if (h > 0) {
            g.rect(x, top, 10, h, metalD);
            for (int b = 0; b < 3; b++) {
                g.rect(x + 1 + b * 3, top, 2, h, metal);
                g.rect(x + 1 + b * 3, top, 1, h, metalL);
            }
            int col = d.req.ch.length > 0 ? Theme.channel(d.req.ch[0]) : 0xFFFFFFFF;
            for (int yy = top + 6; yy < top + h - 2; yy += 10) g.rect(x, yy, 10, 2, Gfx.darken(col, 40));
            // teeth at the bottom
            for (int k = 0; k < 5; k++) g.pset(x + k * 2, top + h, metalD);
        }
        reqLights(g, d.req, x + 5, top + 3 + (h > 5 ? 1 : 0), w.sig);
    }

    private void drawLift(Gfx g, World w, World.Lift l) {
        int x = Math.round(l.x), y = Math.round(l.y), ww = l.width * TILE;
        int col = l.req.ch.length > 0 ? Theme.channel(l.req.ch[0]) : 0xFFFFFFFF;
        g.rect(x, y, ww, 6, 0xFF3A3E52);
        g.rect(x, y, ww, 2, 0xFFA8ACC0);
        g.rect(x, y + 2, ww, 1, 0xFF6A6F86);
        g.rect(x + 2, y + 3, ww - 4, 1, Gfx.darken(col, 30));
        g.rect(x, y + 5, ww, 1, 0xFF1A1C28);
        reqLights(g, l.req, x + ww / 2, y + 7, w.sig);
    }

    private void drawLaser(Gfx g, World w, World.Laser z) {
        int ex = z.col * TILE, ey = z.row * TILE;
        int col = z.req.ch.length > 0 ? Theme.channel(z.req.ch[0]) : 0xFFFF4040;
        // lens facing the beam
        int lx = ex + 6, ly = ey + 6;
        if (z.dir == Level.DIR_LEFT) lx = ex + 1;
        if (z.dir == Level.DIR_RIGHT) lx = ex + 11;
        if (z.dir == Level.DIR_UP) ly = ey + 1;
        if (z.dir == Level.DIR_DOWN) ly = ey + 11;
        g.rect(lx, ly, 4, 4, z.on ? 0xFFFF6060 : 0xFF5A2A2A);
        g.pset(lx, ly, z.on ? 0xFFFFD0D0 : 0xFF7A4A4A);
        reqLights(g, z.req, ex + 8, ey + (z.dir == Level.DIR_DOWN ? 2 : 11), w.sig);
        if (z.bw <= 0 || z.bh <= 0) return;
        int bx = (int) z.bx, by = (int) z.by, bw = (int) z.bw, bh = (int) z.bh;
        if (!z.on) {
            // faint dotted path so players know where it fires
            if (bw > bh) for (int i = 0; i < bw; i += 4) g.blend(bx + i, by + 2, 0xFFFF4040, 50);
            else for (int i = 0; i < bh; i += 4) g.blend(bx + 2, by + i, 0xFFFF4040, 50);
            return;
        }
        boolean flick = (w.frameCounter & 2) == 0;
        if (bw > bh) {
            g.blendRect(bx, by, bw, bh, 0xFFFF2040, 90);
            g.rect(bx, by + 1, bw, 2, flick ? 0xFFFF8090 : 0xFFFFC0C8);
            g.rect(bx, by + 2, bw, 1, 0xFFFFFFFF);
        } else {
            g.blendRect(bx, by, bw, bh, 0xFFFF2040, 90);
            g.rect(bx + 1, by, 2, bh, flick ? 0xFFFF8090 : 0xFFFFC0C8);
            g.rect(bx + 2, by, 1, bh, 0xFFFFFFFF);
        }
    }

    private void drawBodies(Gfx g, World w) {
        boolean rewinding = w.state == World.REWIND;
        float k = rewinding ? 1f - (float) w.stateTimer / World.REWIND_FRAMES : 1f;
        for (int i = 0; i < w.remnants.size(); i++) {
            World.Remnant r = w.remnants.get(i);
            float rx, ry;
            int anim;
            boolean left;
            if (rewinding) {
                int t = Math.min(r.rec.len - 1, (int) (w.rewindFrom * k));
                rx = r.rec.xs[t];
                ry = r.rec.ys[t];
                anim = r.rec.anim[t];
                left = r.rec.left(t);
            } else {
                if (!r.active) continue;
                rx = r.x;
                ry = r.y;
                anim = r.anim;
                left = r.left;
            }
            int sx = Math.round(rx) - 2, sy = Math.round(ry) - 1;
            boolean bad = w.state == World.PARADOX && w.paradoxRemnant == i;
            int tint = bad ? 0xFFFF3030 : REMNANT_TINT;
            int alpha = bad ? ((w.frameCounter / 4 & 1) == 0 ? 255 : 120) : 175 + ((hash(w.frameCounter / 3, i, 1) & 31));
            Sprite s = Sprites.boyFrame(anim);
            g.spriteEx(s, sx, sy, left, tint, 150, alpha);
            // scanline shimmer
            int line = (w.frameCounter / 2 + i * 5) % 20;
            if (line < 16) g.blendRect(sx, sy + line, 12, 1, tint, 60);
            digits(g, i + 1, sx + 5, sy - 7, bad ? 0xFFFF6060 : 0xFFBFF4FF);
        }
        if (w.state == World.DEAD) return;
        int sx = Math.round(w.x) - 2, sy = Math.round(w.y) - 1;
        if (rewinding) return; // the loop that just ended is already drawn as the newest remnant
        g.sprite(Sprites.boyFrame(w.anim), sx, sy, w.left);
        if (w.state == World.PLAY) {
            boolean prompt = w.nearLever() || (w.inMachine() && w.onGround);
            if (prompt && (w.frameCounter / 20 & 1) == 0) g.text("@", sx + 2, sy - 10, 0xFFFFFFFF);
        }
    }

    private void drawShade(Gfx g, World w) {
        if (w.state == World.CUTSCENE && w.activeEvent != null) {
            // the Game draws the shade during cutscenes via drawShadeFigure
            return;
        }
        if (w.chaseOn) {
            int sx = Math.round(w.chaseX), sy = Math.round(w.chaseY);
            int alpha = w.chaseAwake() ? 255 : 90 + (w.t * 3 % 60);
            g.spriteEx(Sprites.SHADE[(w.frameCounter / 8) % 3], sx, sy, w.x < w.chaseX, 0, 0, alpha);
        }
    }

    /** Shade figure with a materialise effect; appear in [0,1]. */
    public static void drawShadeFigure(Gfx g, Sprite s, int x, int y, boolean flip, float appear, int frame) {
        if (appear >= 1f) {
            g.sprite(s, x, y, flip);
            return;
        }
        for (int row = 0; row < s.h; row++) {
            float rowK = appear * 1.6f - (float) (s.h - row) / s.h * 0.6f;
            if (rowK <= 0) continue;
            int off = (int) ((1f - Math.min(1f, rowK)) * 12 * ((hash(row, frame / 2, 7) & 1) == 0 ? 1 : -1));
            for (int col = 0; col < s.w; col++) {
                int c = s.px[row * s.w + (flip ? s.w - 1 - col : col)];
                if (c == 0) continue;
                g.blend(x + col + off, y + row, c, (int) (255 * Math.min(1f, rowK)));
            }
        }
    }

    private void drawStateFx(Gfx g, World w) {
        int st = w.state;
        if (st == World.REWIND) {
            int a = 80 + (int) (40 * Math.sin(w.stateTimer * 0.3));
            g.blendRect(0, 0, W, H, 0xFF2040A0, a);
            for (int y = (w.frameCounter & 1); y < H; y += 2) g.blendRect(0, y, W, 1, 0xFF000000, 40);
            int band = (w.stateTimer * 7) % H;
            g.blendRect(0, band, W, 3, 0xFFFFFFFF, 60);
            g.textCenterShadow("{{ REWINDING", W / 2, 20, 0xFFBFF4FF, 0xFF102040);
        } else if (st == World.PARADOX) {
            for (int i = 0; i < 6; i++) {
                int y = (hash(w.stateTimer / 3, i, 11) & 0x7FFF) % H;
                int sh = (hash(i, w.stateTimer, 12) % 6);
                int hgt = 2 + (hash(i, w.stateTimer / 3, 13) & 7);
                for (int yy = y; yy < Math.min(H, y + hgt); yy++) {
                    int gy = g.oy + yy;
                    if (gy < 0 || gy >= g.h) continue;
                    int row = gy * g.w;
                    int x0 = Math.max(0, g.ox), x1 = Math.min(g.w, g.ox + W);
                    if (sh > 0) for (int x = x1 - 1; x >= x0 + sh; x--) g.px[row + x] = g.px[row + x - sh];
                    else if (sh < 0) for (int x = x0; x < x1 + sh; x++) g.px[row + x] = g.px[row + x - sh];
                }
            }
            g.blendRect(0, 0, W, H, 0xFFFF0040, 40 + (w.stateTimer % 8) * 6);
        } else if (st == World.DEAD || st == World.CAUGHT) {
            g.blendRect(0, 0, W, H, st == World.CAUGHT ? 0xFF100018 : 0xFF400010, Math.min(150, w.stateTimer * 4));
        } else if (st == World.WON) {
            g.blendRect(0, 0, W, H, 0xFFFFFFFF, Math.min(255, w.stateTimer * 6));
        }
        if (w.flash > 0) g.blendRect(0, 0, W, H, 0xFFFFFFFF, w.flash * 18);
    }
}
