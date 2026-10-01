package com.timemaze.game.core;

/** Renders every screen of the game to PNG files for visual review. Usage: GameShots outDir [w h] */
public final class GameShots {
    static Game game;
    static String dir;

    static void frames(int n) {
        for (int i = 0; i < n; i++) {
            game.update();
        }
    }

    static void shot(String name) throws Exception {
        game.render();
        Shot.save(game.gfx, 2, dir + "/" + name + ".png");
    }

    static void tapCenter() {
        game.input.tap(game.vw / 2f, game.vh / 2f);
        game.update();
    }

    public static void main(String[] a) throws Exception {
        dir = a[0];
        int w = a.length > 2 ? Integer.parseInt(a[1]) : 2400, h = a.length > 2 ? Integer.parseInt(a[2]) : 1080;
        Storage.Memory mem = new Storage.Memory();
        mem.putInt("unlocked", 12);
        mem.putInt("best1", 0);
        mem.putInt("best2", 1);
        mem.putInt("best3", 3);
        mem.putInt("started", 1);
        game = new Game(mem, new Audio());
        game.layout(w, h);
        frames(90);
        shot("01_title");
        game.startStory(false);
        frames(80);
        shot("02_story_attic");
        tapCenter(); tapCenter(); frames(60);
        shot("03_story_fall");
        tapCenter(); tapCenter(); tapCenter(); tapCenter(); frames(90);
        shot("04_story_whisper");
        game.screen = Game.S_SELECT;
        frames(10);
        shot("05_select");
        game.startLevel(2);
        frames(20);
        shot("06_intro_card");
        tapCenter();
        game.input.key(Input.K_RIGHT, true);
        frames(40);
        game.input.key(Input.K_RIGHT, false);
        shot("07_play_hint");
        // cutscene in chamber 5
        game.startLevel(5);
        Bot.Cmd[][] sol = Solutions.get(5);
        World w5 = Bot.runFrames(Levels.get(5), sol, 1590);
        game.world = w5;
        game.screen = Game.S_PLAY;
        for (int i = 0; i < 400 && w5.state != World.CUTSCENE; i++) { w5.tick(false, true, false, false); }
        frames(60);
        shot("08_shade_appears");
        tapCenter(); frames(40); tapCenter(); tapCenter(); frames(30);
        shot("09_shade_dialogue");
        for (int i = 0; i < 12; i++) { tapCenter(); frames(5); }
        frames(60);
        shot("10_after_sabotage");
        // chamber 29 reveal
        game.startLevel(29);
        tapCenter();
        frames(80);
        for (int i = 0; i < 9; i++) { tapCenter(); tapCenter(); frames(3); }
        frames(40);
        shot("11_reveal");
        // chase
        game.startLevel(15);
        tapCenter();
        frames(60);
        for (int i = 0; i < 12; i++) { tapCenter(); tapCenter(); frames(3); }
        frames(300);
        shot("12_chase");
        // paradox
        game.startLevel(11);
        Bot.Cmd[][] s11 = Solutions.get(11);
        game.world = Bot.run(Levels.get(11), new Bot.Cmd[][]{s11[0], s11[1], Bot.loop(Bot.walk(4), Bot.act(), Bot.idle(600))}).world;
        game.screen = Game.S_PLAY;
        game.world.stateTimer = 20;
        shot("13_paradox");
        // clear screen
        game.startLevel(3);
        game.world = Bot.run(Levels.get(3), Solutions.get(3)).world;
        game.screen = Game.S_PLAY;
        frames(80);
        shot("14_clear");
        game.screen = Game.S_PAUSE;
        shot("15_pause");
        // ending
        game.startStory(true);
        int[] want = {0, 2, 3, 6, 7, 9, 11};
        int at = 0;
        for (int k : want) {
            while (at < k) { tapCenter(); tapCenter(); at++; }
            frames(k == 11 ? 260 : 70);
            shot("16_ending_" + k);
        }
        game.screen = Game.S_CREDITS;
        game.timer = 0;
        frames(300);
        shot("17_credits");
    }
}
