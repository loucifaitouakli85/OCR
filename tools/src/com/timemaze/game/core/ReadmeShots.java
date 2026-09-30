package com.timemaze.game.core;

/** Screenshots used by the README. Usage: ReadmeShots outDir */
public final class ReadmeShots {
    static Game game;

    static void playShot(int level, int framesBeforeEnd, String out) throws Exception {
        Bot.Result full = Bot.run(Levels.get(level), Solutions.get(level));
        World w = Bot.runFrames(Levels.get(level), Solutions.get(level), full.frames - framesBeforeEnd);
        game.startLevel(level);
        game.world = w;
        game.screen = Game.S_PLAY;
        game.render();
        Shot.save(game.gfx, 2, out);
    }

    public static void main(String[] a) throws Exception {
        String dir = a[0];
        Storage.Memory mem = new Storage.Memory();
        mem.putInt("started", 1);
        game = new Game(mem, new Audio());
        game.layout(2400, 1080);
        for (int i = 0; i < 90; i++) game.update();
        game.render();
        Shot.save(game.gfx, 2, dir + "/screen-title.png");
        playShot(12, 280, dir + "/screen-tower.png");
        playShot(18, 150, dir + "/screen-long-loop.png");
        playShot(7, 330, dir + "/screen-lasers.png");
    }
}
