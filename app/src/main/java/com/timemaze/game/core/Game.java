package com.timemaze.game.core;

import java.util.ArrayList;

/**
 * Top level of the game: screens, menus, HUD, touch controls, cutscenes and
 * save data. Platform independent; the host calls {@link #layout},
 * {@link #update} 60 times per second and {@link #render}, then shows
 * {@link #gfx} scaled by {@link #scale}.
 */
public final class Game {
    static final int S_TITLE = 0, S_STORY = 1, S_SELECT = 2, S_INTRO = 3, S_PLAY = 4, S_PAUSE = 5, S_CLEAR = 6,
        S_CREDITS = 7;

    static final int ROOM_W = 320, ROOM_H = 176, HUD_H = 16;
    static final int WHITE = 0xFFFFFFFF, INK = 0xFF120C18, GOLD = 0xFFE8C050, CYAN = 0xFFBFF4FF, GREY = 0xFF9A94A8;

    public final Gfx gfx = new Gfx(464, 200);
    public final Input input = new Input();
    public final Audio audio;
    private final Storage store;
    private final Renderer renderer = new Renderer();

    /** Device pixels per logical pixel. */
    public float scale = 1f;
    public int vw = 464, vh = 200;

    int screen = S_TITLE;
    int timer;
    int menuSel;

    // story
    Story.Beat[] beats;
    int beatIdx, beatTimer, storyChars;
    boolean storyIsEnding;

    // play
    World world;
    int levelNum = 1;
    boolean fast;
    int helpTimer;
    boolean pl, pr, pj, pa;

    // cutscene
    ShadeEvent cut;
    int cutPhase, cutTimer, cutLine, cutChars;
    boolean cutRevealed, cutActed;

    // results
    int clearRemnants, clearFrames, clearBest;
    boolean clearNewBest;

    // layout
    int roomX, roomY;
    boolean sideControls;
    final Btn bLeft = new Btn(), bRight = new Btn(), bJump = new Btn(), bAction = new Btn();
    final Btn bPause = new Btn(), bRestart = new Btn(), bFast = new Btn(), bHelp = new Btn();
    final Btn bBack = new Btn();
    final Btn[] menu = new Btn[6];
    final Btn[] cells = new Btn[Levels.COUNT];

    public boolean quitRequested;

    static final class Btn {
        int x, y, w, h;

        Btn set(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            return this;
        }

        boolean hit(float px, float py) {
            return px >= x - 3 && px < x + w + 3 && py >= y - 3 && py < y + h + 3;
        }
    }

    public Game(Storage store, Audio audio) {
        this.store = store;
        this.audio = audio;
        for (int i = 0; i < menu.length; i++) menu[i] = new Btn();
        for (int i = 0; i < cells.length; i++) cells[i] = new Btn();
        audio.sfxOn = store.getInt("sfx", 1) == 1;
        audio.musicOn = store.getInt("music", 1) == 1;
        audio.setSong(Audio.SONG_TITLE);
        layout(464 * 3, 216 * 3);
    }

    // ================================================================ layout

    /** Chooses the logical resolution for a screen of w x h device pixels. */
    public void layout(int w, int h) {
        float s = Math.min(w / 464f, h / 200f);
        if (s >= 2f) {
            float fl = (float) Math.floor(s);
            if (fl / s >= 0.8f) s = fl;
        }
        if (s <= 0) s = 1;
        scale = s;
        vw = Math.max(320, (int) Math.ceil(w / s));
        vh = Math.max(192, (int) Math.ceil(h / s));
        gfx.resize(vw, vh);
        roomX = (vw - ROOM_W) / 2;
        roomY = (vh - ROOM_H - HUD_H) / 2 + HUD_H;
        int m = roomX;
        sideControls = m >= 70;
        int by = Math.min(vh - 40, roomY + ROOM_H - 36);
        if (sideControls) {
            by = vh - 42;
            int lx = (m - 66) / 2;
            bLeft.set(lx, by, 30, 32);
            bRight.set(lx + 36, by, 30, 32);
            int rx = roomX + ROOM_W + (m - 66) / 2;
            bAction.set(rx, by, 30, 32);
            bJump.set(rx + 36, by, 30, 32);
            bPause.set((m - 28) / 2, roomY - HUD_H, 28, 20);
            int tx = roomX + ROOM_W + (m - 28) / 2;
            bRestart.set(tx, roomY - HUD_H, 28, 20);
            bFast.set(tx, roomY + 10, 28, 20);
            bHelp.set(tx, roomY + 36, 28, 20);
        } else {
            by = vh - 38;
            bLeft.set(4, by, 30, 32);
            bRight.set(38, by, 30, 32);
            bAction.set(vw - 68, by, 30, 32);
            bJump.set(vw - 34, by, 30, 32);
            bPause.set(roomX + 2, roomY - HUD_H + 1, 18, 14);
            bRestart.set(roomX + ROOM_W - 20, roomY - HUD_H + 1, 18, 14);
            bFast.set(roomX + ROOM_W - 40, roomY - HUD_H + 1, 18, 14);
            bHelp.set(roomX + ROOM_W - 60, roomY - HUD_H + 1, 18, 14);
        }
        bBack.set(6, 6, 24, 18);
    }

    // ================================================================ input helpers

    private boolean tapped(Btn b) {
        for (int i = 0; i < input.taps; i++) if (b.hit(input.tapX[i], input.tapY[i])) return true;
        return false;
    }

    private boolean anyTap() {
        return input.taps > 0;
    }

    private boolean confirmKey() {
        return input.pressed(Input.K_ENTER) || input.pressed(Input.K_JUMP) || input.pressed(Input.K_ACTION);
    }

    /** Android back button. Returns false when the app should close. */
    public boolean onBack() {
        switch (screen) {
            case S_TITLE:
                return false;
            case S_PLAY:
                if (world != null && world.state == World.CUTSCENE) return true;
                screen = S_PAUSE;
                menuSel = 0;
                return true;
            case S_PAUSE:
                screen = S_PLAY;
                return true;
            case S_STORY:
                finishStory();
                return true;
            default:
                go(S_TITLE);
                return true;
        }
    }

    /** Called when the app goes to the background. */
    public void onPause() {
        if (screen == S_PLAY && world != null && world.state != World.CUTSCENE) {
            screen = S_PAUSE;
            menuSel = 0;
        }
        save();
    }

    private void go(int s) {
        screen = s;
        timer = 0;
        menuSel = 0;
        if (s == S_TITLE || s == S_SELECT) audio.setSong(Audio.SONG_TITLE);
    }

    private void save() {
        store.putInt("sfx", audio.sfxOn ? 1 : 0);
        store.putInt("music", audio.musicOn ? 1 : 0);
    }

    int unlocked() {
        return Math.max(1, Math.min(Levels.COUNT, store.getInt("unlocked", 1)));
    }

    // ================================================================ update

    public void update() {
        timer++;
        switch (screen) {
            case S_TITLE: updateTitle(); break;
            case S_STORY: updateStory(); break;
            case S_SELECT: updateSelect(); break;
            case S_INTRO: updateIntro(); break;
            case S_PLAY: updatePlay(); break;
            case S_PAUSE: updatePause(); break;
            case S_CLEAR: updateClear(); break;
            case S_CREDITS: updateCredits(); break;
        }
        input.endFrame();
    }

    private void play(int sfx) {
        audio.play(sfx);
    }

    // ---------------------------------------------------------------- title

    private String[] titleItems() {
        return new String[]{
            store.getInt("started", 0) == 1 ? "CONTINUE" : "START",
            "NEW GAME",
            "CHAMBERS",
            "SOUND: " + (audio.sfxOn ? "ON" : "OFF"),
            "MUSIC: " + (audio.musicOn ? "ON" : "OFF"),
        };
    }

    private void updateTitle() {
        String[] items = titleItems();
        int chosen = -1;
        for (int i = 0; i < items.length; i++) if (tapped(menu[i])) chosen = i;
        if (input.pressed(Input.K_UP)) menuSel = (menuSel + items.length - 1) % items.length;
        if (input.pressed(Input.K_DOWN)) menuSel = (menuSel + 1) % items.length;
        if (input.pressed(Input.K_ENTER) || input.pressed(Input.K_ACTION)) chosen = menuSel;
        if (chosen < 0) return;
        menuSel = chosen;
        play(Sfx.SELECT);
        switch (chosen) {
            case 0:
                if (store.getInt("started", 0) == 0) {
                    startStory(false);
                } else {
                    int u = unlocked();
                    if (store.getInt("cleared" + u, 0) == 1 && u == Levels.COUNT) go(S_SELECT);
                    else startLevel(u);
                }
                break;
            case 1:
                startStory(false);
                break;
            case 2:
                go(S_SELECT);
                break;
            case 3:
                audio.sfxOn = !audio.sfxOn;
                save();
                break;
            case 4:
                audio.musicOn = !audio.musicOn;
                save();
                break;
        }
    }

    // ---------------------------------------------------------------- story

    void startStory(boolean ending) {
        storyIsEnding = ending;
        beats = ending ? Story.ENDING : Story.INTRO;
        beatIdx = 0;
        beatTimer = 0;
        storyChars = 0;
        go(S_STORY);
        audio.setSong(ending ? Audio.SONG_ENDING : Audio.SONG_TITLE);
    }

    private void updateStory() {
        beatTimer++;
        Story.Beat b = beats[beatIdx];
        String text = b.line.length() > 2 ? b.line.substring(2) : "";
        if (b.scene == Story.TBC) {
            if (beatTimer > 200 && (anyTap() || confirmKey())) finishStory();
            return;
        }
        bBack.set((vw - 320) / 2 + 280, (vh - 192) / 2 + 4, 36, 14);
        if (tapped(bBack) && !storyIsEnding) {
            finishStory();
            return;
        }
        if (storyChars < text.length()) {
            storyChars++;
            if (storyChars % 3 == 0) play(Sfx.BLIP);
        }
        if (b.scene == Story.CRACK && beatTimer == 1) play(Sfx.SABOTAGE);
        if (b.scene == Story.GONE && beatTimer == 1) play(Sfx.VANISH);
        if (b.scene == Story.STRANGER && beatTimer == 1 && b.line.contains("AT LAST")) play(Sfx.REVEAL);
        if (anyTap() || confirmKey()) {
            if (storyChars < text.length()) {
                storyChars = text.length();
            } else {
                beatIdx++;
                beatTimer = 0;
                storyChars = 0;
                if (beatIdx >= beats.length) finishStory();
            }
        }
    }

    private void finishStory() {
        if (storyIsEnding) {
            store.putInt("finished", 1);
            go(S_CREDITS);
            audio.setSong(Audio.SONG_ENDING);
        } else {
            store.putInt("started", 1);
            startLevel(1);
        }
    }

    // ---------------------------------------------------------------- select

    private void updateSelect() {
        bBack.set(vw / 2 - 150, (vh - 192) / 2 + 2, 24, 16);
        if (tapped(bBack) || input.pressed(Input.K_BACK)) {
            play(Sfx.SELECT);
            go(S_TITLE);
            return;
        }
        int u = unlocked();
        for (int i = 0; i < Levels.COUNT; i++) {
            if (tapped(cells[i])) {
                if (i + 1 <= u) {
                    play(Sfx.SELECT);
                    startLevel(i + 1);
                } else {
                    play(Sfx.DENY);
                }
                return;
            }
        }
        if (input.pressed(Input.K_LEFT)) menuSel = Math.max(0, menuSel - 1);
        if (input.pressed(Input.K_RIGHT)) menuSel = Math.min(u - 1, menuSel + 1);
        if (input.pressed(Input.K_UP)) menuSel = Math.max(0, menuSel - 10);
        if (input.pressed(Input.K_DOWN)) menuSel = Math.min(u - 1, menuSel + 10);
        if (input.pressed(Input.K_ENTER) || input.pressed(Input.K_JUMP)) startLevel(menuSel + 1);
    }

    // ---------------------------------------------------------------- level

    void startLevel(int n) {
        levelNum = n;
        world = new World(Levels.get(n));
        cut = null;
        fast = false;
        helpTimer = 0;
        store.putInt("started", 1);
        go(S_INTRO);
        audio.setSong(zoneSong());
    }

    private int zoneSong() {
        return Audio.forZone(Levels.get(levelNum).zone);
    }

    private void updateIntro() {
        if (timer > 15 && (anyTap() || confirmKey())) {
            play(Sfx.SELECT);
            screen = S_PLAY;
            timer = 0;
        }
        if (input.pressed(Input.K_BACK)) go(S_SELECT);
    }

    private void updatePlay() {
        World w = world;
        if (w.state == World.CUTSCENE) {
            if (cut == null) startCutscene(w.activeEvent);
            updateCutscene();
            return;
        }
        if (tapped(bPause) || input.pressed(Input.K_BACK)) {
            play(Sfx.SELECT);
            screen = S_PAUSE;
            menuSel = 0;
            return;
        }
        if ((tapped(bRestart) || input.pressed(Input.K_RESTART)) && w.state != World.WON && w.state != World.REWIND) {
            w.resetLoop();
            play(Sfx.REWIND);
        }
        if (tapped(bFast) || input.pressed(Input.K_FAST)) fast = !fast;
        if (tapped(bHelp)) helpTimer = helpTimer > 0 ? 0 : 600;
        if (helpTimer > 0) helpTimer--;

        boolean l = input.keys[Input.K_LEFT], r = input.keys[Input.K_RIGHT];
        boolean j = input.keys[Input.K_JUMP] || input.keys[Input.K_UP];
        boolean a = input.keys[Input.K_ACTION] || input.keys[Input.K_DOWN];
        int splitL = (bLeft.x + bLeft.w + bRight.x) / 2, splitR = (bAction.x + bAction.w + bJump.x) / 2;
        for (int i = 0; i < input.touches; i++) {
            float x = input.tx[i], y = input.ty[i];
            boolean leftZone, rightZone;
            if (sideControls) {
                leftZone = x < roomX && y > roomY + 30;
                rightZone = x >= roomX + ROOM_W && y > roomY + 62;
            } else {
                leftZone = x < bRight.x + bRight.w + 12 && y > bLeft.y - 20;
                rightZone = x > bAction.x - 12 && y > bAction.y - 20;
            }
            if (leftZone) {
                if (x < splitL) l = true;
                else r = true;
            } else if (rightZone) {
                if (x >= splitR) j = true;
                else a = true;
            }
        }
        pl = l;
        pr = r;
        pj = j;
        pa = a;

        int steps = fast && w.state == World.PLAY ? 3 : 1;
        for (int k = 0; k < steps; k++) {
            w.tick(l, r, j, a);
            for (int s = 0; s < w.soundCount; s++) play(w.sounds[s]);
            if (w.state != World.PLAY) break;
        }
        if (w.state == World.CUTSCENE) {
            fast = false;
            startCutscene(w.activeEvent);
        }
        if (w.state == World.WON && w.stateTimer > 45) onCleared();
    }

    private void onCleared() {
        World w = world;
        clearRemnants = w.remnants.size();
        clearFrames = w.totalFrames;
        int prevBest = store.getInt("best" + levelNum, -1);
        clearNewBest = prevBest < 0 || clearRemnants < prevBest;
        if (clearNewBest) store.putInt("best" + levelNum, clearRemnants);
        clearBest = clearNewBest ? clearRemnants : prevBest;
        store.putInt("cleared" + levelNum, 1);
        if (levelNum < Levels.COUNT) store.putInt("unlocked", Math.max(unlocked(), levelNum + 1));
        go(S_CLEAR);
        audio.setSong(zoneSong());
    }

    // ---------------------------------------------------------------- cutscene

    private void startCutscene(ShadeEvent e) {
        if (cut == e) return;
        cut = e;
        cutPhase = 0;
        cutTimer = 0;
        cutLine = 0;
        cutChars = 0;
        cutRevealed = false;
        cutActed = false;
        audio.setSong(Audio.SONG_SHADE);
    }

    private void updateCutscene() {
        cutTimer++;
        world.tick(false, false, false, false);
        for (int s = 0; s < world.soundCount; s++) play(world.sounds[s]);
        String[] lines = cut.lines;
        if (cutPhase == 0) {
            if (cutTimer >= 45 || (cutTimer > 10 && anyTap())) {
                cutPhase = 1;
                cutTimer = 0;
            }
            return;
        }
        if (cutPhase == 1) {
            while (cutLine < lines.length && lines[cutLine].startsWith("#")) {
                if (lines[cutLine].equals("#ACT")) {
                    world.applyEventActions();
                    cutActed = true;
                } else if (lines[cutLine].equals("#REVEAL")) {
                    cutRevealed = true;
                    world.flash = 20;
                    world.shake = 10;
                    play(Sfx.REVEAL);
                }
                cutLine++;
                cutChars = 0;
            }
            if (cutLine >= lines.length) {
                if (!cutActed) {
                    world.applyEventActions();
                    cutActed = true;
                }
                cutPhase = 2;
                cutTimer = 0;
                play(Sfx.VANISH);
                return;
            }
            String text = lines[cutLine].substring(2);
            if (cutChars < text.length()) {
                cutChars++;
                if (cutChars % 3 == 0) play(Sfx.BLIP);
            }
            if (anyTap() || confirmKey()) {
                if (cutChars < text.length()) {
                    cutChars = text.length();
                } else {
                    cutLine++;
                    cutChars = 0;
                }
            }
            return;
        }
        if (cutTimer >= 30) {
            world.finishEvent();
            cut = null;
            audio.setSong(world.chaseOn ? Audio.SONG_SHADE : zoneSong());
        }
    }

    // ---------------------------------------------------------------- pause & clear

    private static final String[] PAUSE_ITEMS = {"RESUME", "RESTART LOOP", "ERASE REMNANTS", "CHAMBERS", "MAIN MENU"};

    private void updatePause() {
        int chosen = -1;
        for (int i = 0; i < PAUSE_ITEMS.length; i++) if (tapped(menu[i])) chosen = i;
        if (input.pressed(Input.K_UP)) menuSel = (menuSel + PAUSE_ITEMS.length - 1) % PAUSE_ITEMS.length;
        if (input.pressed(Input.K_DOWN)) menuSel = (menuSel + 1) % PAUSE_ITEMS.length;
        if (input.pressed(Input.K_ENTER) || input.pressed(Input.K_ACTION)) chosen = menuSel;
        if (input.pressed(Input.K_BACK)) chosen = 0;
        if (chosen < 0) return;
        play(Sfx.SELECT);
        switch (chosen) {
            case 0:
                screen = S_PLAY;
                break;
            case 1:
                world.resetLoop();
                screen = S_PLAY;
                break;
            case 2:
                world.resetLevel();
                screen = S_PLAY;
                break;
            case 3:
                go(S_SELECT);
                menuSel = levelNum - 1;
                break;
            case 4:
                go(S_TITLE);
                break;
        }
    }

    private void updateClear() {
        String[] items = clearItems();
        int chosen = -1;
        for (int i = 0; i < items.length; i++) if (tapped(menu[i])) chosen = i;
        if (input.pressed(Input.K_LEFT) || input.pressed(Input.K_UP)) menuSel = (menuSel + items.length - 1) % items.length;
        if (input.pressed(Input.K_RIGHT) || input.pressed(Input.K_DOWN)) menuSel = (menuSel + 1) % items.length;
        if (timer > 30 && (input.pressed(Input.K_ENTER) || input.pressed(Input.K_ACTION) || input.pressed(Input.K_JUMP))) chosen = menuSel;
        if (chosen < 0 || timer < 20) return;
        play(Sfx.SELECT);
        if (chosen == 0) {
            if (levelNum == Levels.COUNT) startStory(true);
            else startLevel(levelNum + 1);
        } else if (chosen == 1) {
            startLevel(levelNum);
        } else {
            go(S_SELECT);
            menuSel = levelNum - 1;
        }
    }

    private String[] clearItems() {
        return new String[]{levelNum == Levels.COUNT ? "CONTINUE" : "NEXT", "RETRY", "CHAMBERS"};
    }

    private void updateCredits() {
        if (timer > 120 && (anyTap() || confirmKey())) go(S_TITLE);
    }

    // ================================================================ render

    public void render() {
        Gfx g = gfx;
        g.clear(0xFF07050A);
        switch (screen) {
            case S_TITLE: drawTitle(g); break;
            case S_STORY: drawStory(g); break;
            case S_SELECT: drawSelect(g); break;
            case S_INTRO: drawPlay(g); drawIntroCard(g); break;
            case S_PLAY: drawPlay(g); break;
            case S_PAUSE: drawPlay(g); drawPause(g); break;
            case S_CLEAR: drawPlay(g); drawClear(g); break;
            case S_CREDITS: drawCredits(g); break;
        }
    }

    private void backdrop(Gfx g, Theme th) {
        g.rect(0, 0, vw, vh, th.bg);
        for (int y = 0; y < vh; y += 12) g.rect(0, y + 11, vw, 1, th.bg2);
        for (int y = 0; y < vh; y += 12)
            for (int x = ((y / 12) & 1) * 12; x < vw; x += 24) g.rect(x + 23, y, 1, 11, th.bg2);
        Renderer.gear(g, vw / 2 - 150, vh / 2 - 30, 46, 12, timer * 0.004f, th.gear, th.bg);
        Renderer.gear(g, vw / 2 + 150, vh / 2 + 40, 36, 10, -timer * 0.005f, th.gear, th.bg);
        Renderer.gear(g, vw / 2 + 110, vh / 2 - 70, 20, 8, timer * 0.009f, th.gear, th.bg);
    }

    private void panel(Gfx g, int x, int y, int w, int h) {
        g.blendRect(x, y, w, h, 0xFF0A0612, 225);
        g.frame(x, y, w, h, 0xFF4A3A66);
        g.frame(x + 1, y + 1, w - 2, h - 2, 0xFF1E1630);
        g.rect(x + 2, y, w - 4, 1, 0xFF6A5A8A);
    }

    private void button(Gfx g, Btn b, String label, boolean selected) {
        int bg = selected ? 0xFF3A2C58 : 0xFF1E1630;
        g.rect(b.x, b.y, b.w, b.h, bg);
        g.frame(b.x, b.y, b.w, b.h, selected ? GOLD : 0xFF4A3A66);
        g.textCenter(label, b.x + b.w / 2, b.y + (b.h - 7) / 2, selected ? WHITE : 0xFFD8D0E8);
    }

    // ---------------------------------------------------------------- title

    private void drawTitle(Gfx g) {
        backdrop(g, Theme.ZONES[0]);
        int cx = vw / 2, top = (vh - 192) / 2;
        // title
        String title = "TIME MAZE";
        int tw = Gfx.textWidth(title) * 4;
        int tx = cx - tw / 2, ty = top + 18;
        for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++) g.text(title, tx + dx * 2, ty + dy * 2 + 2, INK, 4);
        g.text(title, tx, ty + 3, 0xFF8C6420, 4);
        g.text(title, tx, ty, GOLD, 4);
        g.textCenterShadow("THE BOY WHO WALKED THROUGH TIME", cx, ty + 36, CYAN, INK);
        // Milo, the machine and his flickering remnants
        int mx = cx - 200, my = top + 116;
        Renderer.drawMachineAt(g, mx, my + 16 - 32 + 16, timer, false, 0, 0);
        for (int i = 0; i < 3; i++) {
            int phase = (timer + i * 70) % 210;
            int alpha = phase < 30 ? phase * 5 : phase > 180 ? (210 - phase) * 5 : 150;
            g.spriteEx(Sprites.BOY_IDLE, mx + 22 + i * 16, my + 17, false, Renderer.REMNANT_TINT, 150, alpha);
        }
        g.sprite(Sprites.boyFrame((timer / 40) % 7 == 6 ? Sprites.A_BLINK : Sprites.A_IDLE), mx + 70, my + 17, false);
        g.rect(mx - 10, my + 33, 110, 2, 0xFF8A6A44);
        // the Shade watching from the dark
        int sa = 60 + (int) (50 * Math.sin(timer * 0.03));
        g.spriteEx(Sprites.SHADE[(timer / 10) % 3], cx + 140, top + 120, true, 0, 0, sa);
        // menu
        String[] items = titleItems();
        int w = 110, h = 15, y0 = top + 76;
        for (int i = 0; i < items.length; i++) {
            menu[i].set(cx - w / 2, y0 + i * 19, w, h);
            button(g, menu[i], items[i], i == menuSel);
        }
        g.textCenter("CHAPTER ONE", cx, vh - 12, 0xFF5A4E70);
    }

    // ---------------------------------------------------------------- story

    private void drawStory(Gfx g) {
        Story.Beat b = beats[beatIdx];
        int sx = (vw - 320) / 2, sy = (vh - 192) / 2;
        int t = beatTimer + beatIdx * 1000;
        if (b.scene == Story.TBC) {
            g.rect(0, 0, vw, vh, 0xFF000000);
            String s1 = "TO BE CONTINUED...";
            int n = Math.min(s1.length(), beatTimer / 6);
            g.textCenterShadow(s1.substring(0, n), vw / 2, vh / 2 - 30, WHITE, 0xFF303040, 2);
            if (beatTimer > 150) {
                int a = Math.min(255, (beatTimer - 150) * 4);
                g.textCenter("TIME MAZE II", vw / 2, vh / 2 + 4, Gfx.mix(0xFF000000, GOLD, a));
                g.textCenter("THE UNRAVELING", vw / 2, vh / 2 + 16, Gfx.mix(0xFF000000, 0xFFFF7AD8, a));
            }
            if (beatTimer > 200 && (beatTimer / 30 & 1) == 0) g.textCenter("TAP", vw / 2, vh - 20, GREY);
            return;
        }
        // scenes are lifted a little so the text box never hides the characters
        int lift = 16;
        Story.draw(g, b.scene, sx, sy - lift, t);
        // letterbox the rest of the screen
        g.rect(0, 0, sx, vh, 0xFF000000);
        g.rect(sx + 320, 0, vw - sx - 320, vh, 0xFF000000);
        g.rect(0, 0, vw, Math.max(0, sy - lift), 0xFF000000);
        g.rect(0, sy + 192 - lift, vw, vh - sy - 192 + lift, 0xFF000000);
        if (beatTimer < 20 && (beatIdx == 0 || beats[beatIdx - 1].scene != b.scene)) {
            g.blendRect(0, 0, vw, vh, 0xFF000000, 255 - beatTimer * 12);
        }
        dialogBox(g, sx + 6, sy + 192 - 46, 308, 44, b.line, storyChars);
        if (!storyIsEnding) {
            g.blendRect(bBack.x, bBack.y, bBack.w, bBack.h, 0xFF000000, 150);
            g.textCenter("SKIP", bBack.x + bBack.w / 2, bBack.y + 4, GREY);
        }
    }

    /** Dialogue box with portrait; line is "S:TEXT" etc. chars limits the typewriter. */
    private void dialogBox(Gfx g, int x, int y, int w, int h, String line, int chars) {
        char who = line.length() > 1 ? line.charAt(0) : 'N';
        String text = line.length() > 2 ? line.substring(2) : "";
        panel(g, x, y, w, h);
        Sprite portrait = null;
        String name = null;
        int nameCol = WHITE;
        switch (who) {
            case 'S': portrait = Sprites.PORTRAIT_SHADE; name = "THE SHADE"; nameCol = Sprites.EYE; break;
            case 'M': portrait = Sprites.PORTRAIT_MILO; name = "MILO"; nameCol = 0xFF6FA8FF; break;
            case 'O': portrait = Sprites.PORTRAIT_OLD; name = "OLD MILO"; nameCol = 0xFFD8D4E0; break;
            case 'X': portrait = Sprites.PORTRAIT_STRANGER; name = "???"; nameCol = 0xFFFFD34A; break;
        }
        int tx = x + 6;
        int tw = w - 12;
        int ty = y + 5;
        if (portrait != null) {
            g.rect(x + 4, y + 4, 36, 36, 0xFF1E1630);
            g.spriteScaled(portrait, x + 6, y + 6, 2, false);
            g.frame(x + 4, y + 4, 36, 36, 0xFF4A3A66);
            tx = x + 46;
            tw = w - 52;
            g.text(name, tx, ty, nameCol);
            ty += 11;
        }
        int color = who == 'N' ? 0xFFD8D0E8 : WHITE;
        ArrayList<String> lines = Gfx.wrap(text, tw);
        int left = chars;
        for (String l : lines) {
            if (left <= 0) break;
            String part = l.length() <= left ? l : l.substring(0, left);
            g.text(part, tx, ty, color);
            left -= l.length() + 1;
            ty += 9;
        }
        if (chars >= text.length() && (timer / 20 & 1) == 0) g.text(">", x + w - 10, y + h - 10, GOLD);
    }

    // ---------------------------------------------------------------- select

    private void drawSelect(Gfx g) {
        int zones = Levels.ZONE_NAMES.length;
        backdrop(g, Theme.ZONES[Math.min(zones - 1, (unlocked() - 1) / 5)]);
        int cx = vw / 2, top = (vh - 192) / 2;
        g.textCenterShadow("CHOOSE A CHAMBER", cx, top + 6, GOLD, INK);
        bBack.set(cx - 150, top + 2, 24, 16);
        button(g, bBack, "<", false);
        int u = unlocked();
        // two zones per row: 5 cells each
        int cw = 28, ch = 22, gap = 3, zoneGap = 12;
        int zoneW = 5 * cw + 4 * gap;
        int x0 = cx - (2 * zoneW + zoneGap) / 2;
        for (int z = 0; z < zones; z++) {
            int zx = x0 + (z % 2) * (zoneW + zoneGap);
            int y = top + 24 + (z / 2) * 50;
            Theme th = Theme.ZONES[z];
            g.textCenter(Levels.ZONE_NAMES[z], zx + zoneW / 2, y, th.accent);
            for (int i = 0; i < 5; i++) {
                int n = z * 5 + i + 1;
                Btn b = cells[n - 1].set(zx + i * (cw + gap), y + 10, cw, ch);
                boolean open = n <= u;
                boolean sel = menuSel == n - 1;
                g.rect(b.x, b.y, b.w, b.h, open ? th.wallD : 0xFF14101C);
                g.rect(b.x, b.y, b.w, 2, open ? th.lip : 0xFF2A2436);
                g.frame(b.x, b.y, b.w, b.h, sel ? WHITE : (open ? th.edge : 0xFF2A2436));
                if (open) {
                    String num = (n < 10 ? "0" : "") + n;
                    g.textCenterShadow(num, b.x + b.w / 2 - 1, b.y + 8, WHITE, INK);
                    int best = store.getInt("best" + n, -1);
                    if (best >= 0) {
                        boolean star = best <= Levels.get(n).parRemnants;
                        g.text(star ? "*" : "+", b.x + 2, b.y + ch - 8, star ? GOLD : CYAN);
                    }
                    if (Levels.get(n).isShadeLevel()) g.sprite(Sprites.SHADE_ICON, b.x + b.w - 8, b.y + 2, false);
                } else {
                    g.sprite(Sprites.LOCK_ICON, b.x + b.w / 2 - 3, b.y + 8, false);
                }
            }
        }
        g.textCenter("* CLEARED AT PAR   + CLEARED", cx, top + 190 - 8, GREY);
    }

    // ---------------------------------------------------------------- play

    private void drawPlay(Gfx g) {
        World w = world;
        Theme th = Theme.ZONES[w.level.zone];
        // frame around the room
        g.rect(0, 0, vw, vh, Gfx.darken(th.bg, 120));
        renderer.draw(g, w, roomX, roomY);
        if (cut != null) drawCutsceneFigure(g);
        drawHud(g, th);
        if (screen == S_PLAY || screen == S_PAUSE) drawControls(g);
        drawHints(g, w);
        drawFailText(g, w);
        if (w.toastTimer > 0 && w.state == World.PLAY) {
            int tw = Gfx.textWidth(w.toast) + 12;
            g.blendRect(roomX + 160 - tw / 2, roomY + 70, tw, 14, 0xFF000000, 180);
            g.textCenter(w.toast, roomX + 160, roomY + 74, WHITE);
        }
        if (cut != null && cutPhase == 1 && cutLine < cut.lines.length && !cut.lines[cutLine].startsWith("#")) {
            String line = cut.lines[cutLine];
            if (cutRevealed && line.startsWith("S:")) line = "O:" + line.substring(2);
            // characters stand on the floor, so talk happens at the top of the room
            dialogBox(g, roomX + 6, roomY + 4, ROOM_W - 12, 46, line, cutChars);
        }
    }

    private void drawCutsceneFigure(Gfx g) {
        float appear;
        if (cutPhase == 0) appear = Math.min(1f, cutTimer / 45f);
        else if (cutPhase == 2) appear = Math.max(0f, 1f - cutTimer / 30f);
        else appear = 1f;
        Sprite s = cutRevealed ? Sprites.OLD_MILO : Sprites.SHADE[(timer / 10) % 3];
        int x = roomX + cut.col * 16 + 1, y = roomY + (cut.row + 1) * 16 - s.h;
        boolean flip = world.x < cut.col * 16;
        if (cutPhase == 1 && !cutRevealed) y += (int) Math.round(Math.sin(timer * 0.08) * 1.5);
        Renderer.drawShadeFigure(g, s, x, y, flip, appear, timer);
        if (!cutRevealed && appear >= 1f) {
            g.blend(x + (flip ? 8 : 5), y + 5, Sprites.EYE, 120);
        }
    }

    private void drawHud(Gfx g, Theme th) {
        World w = world;
        int y = roomY - HUD_H;
        g.rect(roomX, y, ROOM_W, HUD_H, 0xFF0A0612);
        g.rect(roomX, y + HUD_H - 1, ROOM_W, 1, th.edge);
        int x = roomX + (sideControls ? 4 : 24);
        String num = (levelNum < 10 ? "0" : "") + levelNum;
        g.rect(x, y + 3, 15, 10, th.accent);
        g.text(num, x + 2, y + 5, INK);
        g.text(w.level.name, x + 19, y + 5, WHITE);
        // right side: remnants and loop clock
        int rx = roomX + ROOM_W - (sideControls ? 6 : 66);
        int secs = w.t / 60;
        String clock = "~" + secs / 60 + ":" + (secs % 60 < 10 ? "0" : "") + secs % 60;
        rx -= Gfx.textWidth(clock);
        g.text(clock, rx, y + 5, CYAN);
        String rem = "x" + w.remnants.size() + (w.maxRemnants < 9 ? "/" + w.maxRemnants : "");
        rx -= Gfx.textWidth(rem) + 8;
        g.text(rem, rx, y + 5, w.machineDepleted() ? 0xFFFF7070 : CYAN);
        g.spriteEx(Sprites.BOY_IDLE, rx - 13, y + 1, false, Renderer.REMNANT_TINT, 150, 220);
        if (w.chaseOn) {
            g.sprite(Sprites.SHADE_ICON, rx - 26, y + 4, false);
        }
    }

    private void iconButton(Gfx g, Btn b, int kind, boolean on) {
        g.blendRect(b.x, b.y, b.w, b.h, on ? 0xFF3A2C58 : 0xFF000000, on ? 230 : 140);
        g.frame(b.x, b.y, b.w, b.h, on ? GOLD : 0xFF4A3A66);
        int cx = b.x + b.w / 2, cy = b.y + b.h / 2;
        int c = on ? WHITE : 0xFFD8D0E8;
        switch (kind) {
            case 0: // pause
                g.rect(cx - 3, cy - 3, 2, 7, c);
                g.rect(cx + 1, cy - 3, 2, 7, c);
                break;
            case 1: // restart loop
                g.ring(cx, cy, 4, c);
                g.rect(cx + 2, cy - 5, 4, 3, on ? 0xFF3A2C58 : 0xFF0A0612);
                g.rect(cx + 3, cy - 5, 3, 1, c);
                g.rect(cx + 5, cy - 5, 1, 3, c);
                break;
            case 2: // fast forward
                for (int k = 0; k < 4; k++) {
                    g.rect(cx - 5 + k, cy - 3 + k, 1, 7 - k * 2, c);
                    g.rect(cx + k, cy - 3 + k, 1, 7 - k * 2, c);
                }
                break;
            case 3:
                g.textCenter("?", cx, cy - 3, c);
                break;
        }
    }

    private void padButton(Gfx g, Btn b, char icon, boolean down) {
        int a = down ? 200 : 110;
        g.blendRect(b.x + 1, b.y + 1, b.w - 2, b.h - 2, down ? 0xFF4A3A70 : 0xFF141020, a);
        g.frame(b.x, b.y, b.w, b.h, down ? GOLD : 0xFF5A4A7A);
        g.rect(b.x + 1, b.y + b.h - 2, b.w - 2, 1, 0xFF0A0612);
        String s = String.valueOf(icon);
        g.text(s, b.x + (b.w - Gfx.textWidth(s)) / 2, b.y + (b.h - 7) / 2 + (down ? 1 : 0), down ? WHITE : 0xFFC8C0D8);
    }

    private void drawControls(Gfx g) {
        iconButton(g, bPause, 0, false);
        iconButton(g, bRestart, 1, false);
        iconButton(g, bFast, 2, fast);
        if (!world.level.hints.isEmpty()) iconButton(g, bHelp, 3, helpTimer > 0);
        padButton(g, bLeft, '{', pl);
        padButton(g, bRight, '}', pr);
        padButton(g, bJump, '^', pj);
        padButton(g, bAction, '@', pa);
        if (sideControls) {
            g.textCenter("JUMP", bJump.x + bJump.w / 2, bJump.y - 9, 0xFF5A4E70);
            g.textCenter("USE", bAction.x + bAction.w / 2, bAction.y - 9, 0xFF5A4E70);
        }
    }

    private void drawHints(Gfx g, World w) {
        ArrayList<String> hs = w.level.hints;
        if (hs.isEmpty() || w.state == World.CUTSCENE || screen != S_PLAY) return;
        int loop = w.remnants.size();
        // hints may start with "N>" meaning "from loop N on"
        int group = 0;
        for (String h : hs) {
            int min = hintLoop(h);
            if (min <= loop) group = Math.max(group, min);
        }
        ArrayList<String> show = new ArrayList<String>();
        for (String h : hs) if (hintLoop(h) == group) show.add(hintText(h));
        int period = 300;
        int idx;
        if (helpTimer > 0) {
            idx = ((600 - helpTimer) / period) % show.size();
        } else {
            if (w.t >= show.size() * period) return;
            idx = w.t / period;
        }
        String text = show.get(idx);
        ArrayList<String> lines = Gfx.wrap(text, ROOM_W - 24);
        int h = lines.size() * 9 + 6;
        g.blendRect(roomX + 6, roomY + 4, ROOM_W - 12, h, 0xFF000000, 170);
        g.frame(roomX + 6, roomY + 4, ROOM_W - 12, h, 0xFF4A3A66);
        int y = roomY + 8;
        for (String l : lines) {
            g.textCenter(l, roomX + ROOM_W / 2, y, 0xFFFFF0C0);
            y += 9;
        }
    }

    static int hintLoop(String h) {
        return h.length() > 2 && h.charAt(1) == '>' && Character.isDigit(h.charAt(0)) ? h.charAt(0) - '0' : 0;
    }

    static String hintText(String h) {
        return hintLoop(h) > 0 || (h.length() > 2 && h.charAt(1) == '>') ? h.substring(2) : h;
    }

    private void drawFailText(Gfx g, World w) {
        String big = null;
        int col = WHITE;
        switch (w.state) {
            case World.PARADOX: big = "PARADOX!"; col = 0xFFFF6080; break;
            case World.DEAD: big = "OUCH!"; col = 0xFFFF9080; break;
            case World.CAUGHT: big = "CAUGHT!"; col = 0xFFB080FF; break;
        }
        if (big == null) return;
        int cy = roomY + 60;
        int jitter = w.state == World.PARADOX ? (w.stateTimer / 2 % 3) - 1 : 0;
        g.textCenterShadow(big, roomX + 160 + jitter, cy, col, INK, 3);
        g.textCenterShadow(w.failReason, roomX + 160, cy + 28, WHITE, INK);
        if (w.state == World.PARADOX) g.textCenterShadow("A PAST SELF COULDN'T FOLLOW ITS RECORDING", roomX + 160, cy + 40, GREY, INK);
        g.textCenterShadow("REWINDING THIS LOOP...", roomX + 160, cy + 54, CYAN, INK);
    }

    private void drawIntroCard(Gfx g) {
        g.blendRect(0, 0, vw, vh, 0xFF000000, 170);
        Level l = world.level;
        int cx = vw / 2, top = (vh - 192) / 2;
        int pw = 280, ph = 128, px = cx - pw / 2, py = top + 32;
        panel(g, px, py, pw, ph);
        Theme th = Theme.ZONES[l.zone];
        g.textCenter(Levels.ZONE_NAMES[l.zone], cx, py + 8, th.accent);
        g.textCenterShadow("CHAMBER " + (l.num < 10 ? "0" : "") + l.num, cx, py + 22, WHITE, INK, 2);
        g.textCenterShadow(l.name, cx, py + 42, GOLD, INK);
        ArrayList<String> lines = Gfx.wrap(l.intro, pw - 24);
        int y = py + 60;
        for (String s : lines) {
            g.textCenter(s, cx, y, 0xFFD8D0E8);
            y += 10;
        }
        if (l.maxRemnants < 9) g.textCenter("THE MACHINE HOLDS ONLY " + l.maxRemnants + " TRIPS", cx, py + ph - 26, 0xFFFF9080);
        if ((timer / 25 & 1) == 0) g.textCenter("TAP TO BEGIN", cx, py + ph - 13, CYAN);
    }

    private void drawPause(Gfx g) {
        g.blendRect(0, 0, vw, vh, 0xFF000000, 150);
        int cx = vw / 2, top = (vh - 192) / 2;
        panel(g, cx - 70, top + 30, 140, 132);
        g.textCenterShadow("PAUSED", cx, top + 38, GOLD, INK);
        for (int i = 0; i < PAUSE_ITEMS.length; i++) {
            menu[i].set(cx - 58, top + 54 + i * 21, 116, 16);
            button(g, menu[i], PAUSE_ITEMS[i], i == menuSel);
        }
    }

    private void drawClear(Gfx g) {
        int a = Math.min(200, timer * 8);
        g.blendRect(0, 0, vw, vh, 0xFF000000, a);
        int cx = vw / 2, top = (vh - 192) / 2;
        panel(g, cx - 110, top + 26, 220, 140);
        g.textCenterShadow("CHAMBER CLEARED", cx, top + 36, GOLD, INK, 2);
        Level l = world.level;
        g.textCenter(l.name, cx, top + 56, WHITE);
        int secs = clearFrames / 60;
        g.textCenter("REMNANTS USED: " + clearRemnants + "   (PAR " + l.parRemnants + ")", cx, top + 72, CYAN);
        g.textCenter("TIME IN THE MAZE: " + secs / 60 + ":" + (secs % 60 < 10 ? "0" : "") + secs % 60, cx, top + 84, CYAN);
        if (clearRemnants <= l.parRemnants) g.textCenter("* PERFECT LOOP *", cx, top + 98, GOLD);
        else if (clearNewBest) g.textCenter("NEW BEST!", cx, top + 98, GOLD);
        String[] items = clearItems();
        int bw = 62;
        for (int i = 0; i < items.length; i++) {
            menu[i].set(cx - (bw * 3 + 12) / 2 + i * (bw + 6), top + 136, bw, 18);
            button(g, menu[i], items[i], i == menuSel);
        }
    }

    private static final String[] CREDITS = {
        "TIME MAZE",
        "",
        "A GAME ABOUT BEING",
        "IN TWO PLACES AT ONCE",
        "",
        "DESIGN, CODE, PIXELS AND MUSIC",
        "BUILT WITH CLAUDE CODE",
        "",
        "INSPIRED BY CHRONOTRON AND BRAID",
        "",
        "MILO WILL RETURN IN",
        "TIME MAZE II: THE UNRAVELING",
        "",
        "THANK YOU FOR PLAYING!",
    };

    private void drawCredits(Gfx g) {
        backdrop(g, Theme.ZONES[Theme.ZONES.length - 1]);
        g.blendRect(0, 0, vw, vh, 0xFF000000, 120);
        int y = Math.max(vh - timer / 2, 16);
        int cx = vw / 2;
        for (int i = 0; i < CREDITS.length; i++) {
            int yy = y + i * 14;
            if (yy < -10 || yy > vh) continue;
            g.textCenterShadow(CREDITS[i], cx, yy, i == 0 ? GOLD : WHITE, INK, i == 0 ? 2 : 1);
        }
        for (int i = 0; i < 5; i++) {
            int phase = (timer + i * 50) % 250;
            int alpha = phase < 40 ? phase * 4 : phase > 200 ? (250 - phase) * 3 : 160;
            g.spriteEx(Sprites.BOY_IDLE, cx - 60 + i * 24, vh - 24, false, Renderer.REMNANT_TINT, 150, alpha);
        }
        if (timer > 120 && (timer / 30 & 1) == 0) g.textCenter("TAP TO RETURN", cx, vh - 40, GREY);
    }
}
