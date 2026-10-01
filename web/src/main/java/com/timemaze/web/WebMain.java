package com.timemaze.web;

import com.timemaze.game.core.Audio;
import com.timemaze.game.core.Game;
import com.timemaze.game.core.Input;
import com.timemaze.game.core.Storage;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSByRef;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Browser host for the shared game core, compiled to JavaScript by TeaVM.
 * The DOM side (canvas, touch, keys, gamepads, Web Audio) lives in host.js;
 * this class wires it to {@link Game} and runs the fixed 60 Hz update loop.
 */
public final class WebMain {
    private static final double STEP_MS = 1000.0 / 60.0;

    private static Game game;
    private static Audio audio;
    private static final short[] audioBuf = new short[4096];
    /** Shared with host.js: [count, x0, y0, x1, y1, ...] in device pixels. */
    private static final float[] touches = new float[1 + 2 * 10];
    private static final float[] xs = new float[10], ys = new float[10];
    private static double last = -1, acc;

    private WebMain() {}

    @JSFunctor
    interface FrameFn extends JSObject {
        void call(double timestamp);
    }

    @JSFunctor
    interface TapFn extends JSObject {
        void call(double x, double y);
    }

    @JSFunctor
    interface KeyFn extends JSObject {
        void call(String code, boolean down);
    }

    @JSFunctor
    interface FillFn extends JSObject {
        void call(int samples);
    }

    @JSFunctor
    interface ResizeFn extends JSObject {
        void call(int width, int height);
    }

    @JSFunctor
    interface PauseFn extends JSObject {
        void call();
    }

    @JSBody(params = {"frame", "tap", "key", "fill", "resize", "pause", "touches", "audio", "rate"},
        script = "TimeMazeHost.init(frame, tap, key, fill, resize, pause, touches, audio, rate);")
    private static native void init(FrameFn frame, TapFn tap, KeyFn key, FillFn fill, ResizeFn resize, PauseFn pause,
        @JSByRef float[] touches, @JSByRef short[] audio, int rate);

    @JSBody(params = {"px", "w", "h", "scale"}, script = "TimeMazeHost.present(px, w, h, scale);")
    private static native void present(@JSByRef int[] px, int w, int h, double scale);

    @JSBody(params = {"key"}, script = "return TimeMazeHost.load(key);")
    private static native int load(String key);

    @JSBody(params = {"key", "value"}, script = "TimeMazeHost.save(key, value);")
    private static native void save(String key, int value);

    /** Save data in localStorage. */
    private static final class WebStorage implements Storage {
        @Override
        public int getInt(String key, int def) {
            int v = load(key);
            return v == Integer.MIN_VALUE ? def : v;
        }

        @Override
        public void putInt(String key, int value) {
            save(key, value);
        }
    }

    public static void main(String[] args) {
        audio = new Audio();
        game = new Game(new WebStorage(), audio);
        init(WebMain::frame, WebMain::tap, WebMain::key, WebMain::fill, WebMain::resize, WebMain::pause,
            touches, audioBuf, Audio.RATE);
    }

    private static void resize(int width, int height) {
        game.layout(Math.max(1, width), Math.max(1, height));
        game.render();
        present(game.gfx.px, game.vw, game.vh, game.scale);
    }

    private static void frame(double ts) {
        if (last < 0) last = ts;
        double dt = ts - last;
        last = ts;
        if (dt > 100) dt = 100; // don't spiral after the tab was hidden
        if (dt > 0) acc += dt;
        int n = Math.min(10, (int) touches[0]);
        float s = game.scale;
        for (int i = 0; i < n; i++) {
            xs[i] = touches[1 + i * 2] / s;
            ys[i] = touches[2 + i * 2] / s;
        }
        game.input.setTouches(xs, ys, n);
        int steps = 0;
        while (acc >= STEP_MS && steps < 5) {
            game.update();
            acc -= STEP_MS;
            steps++;
        }
        if (steps == 5) acc = 0;
        if (steps > 0) {
            game.render();
            present(game.gfx.px, game.vw, game.vh, game.scale);
        }
    }

    private static void tap(double x, double y) {
        game.input.tap((float) (x / game.scale), (float) (y / game.scale));
    }

    private static void key(String code, boolean down) {
        if ("Escape".equals(code) || "Backspace".equals(code) || "GamepadStart".equals(code)) {
            if (down) game.onBack();
            return;
        }
        int k = map(code);
        if (k >= 0) game.input.key(k, down);
    }

    private static int map(String code) {
        switch (code) {
            case "ArrowLeft": case "KeyA": case "GamepadLeft": return Input.K_LEFT;
            case "ArrowRight": case "KeyD": case "GamepadRight": return Input.K_RIGHT;
            case "ArrowUp": case "KeyW": case "GamepadUp": return Input.K_UP;
            case "ArrowDown": case "KeyS": case "GamepadDown": return Input.K_DOWN;
            case "Space": case "KeyZ": case "GamepadA": return Input.K_JUMP;
            case "KeyX": case "KeyE": case "GamepadX": case "GamepadB": return Input.K_ACTION;
            case "Enter": case "NumpadEnter": return Input.K_ENTER;
            case "KeyR": case "GamepadY": return Input.K_RESTART;
            case "KeyF": case "GamepadR1": return Input.K_FAST;
            default: return -1;
        }
    }

    private static void fill(int samples) {
        audio.render(audioBuf, Math.min(samples, audioBuf.length));
    }

    private static void pause() {
        game.onPause();
    }
}
