package com.timemaze.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.timemaze.game.core.Game;
import com.timemaze.game.core.Input;

/**
 * Runs the game at a fixed 60 updates per second on the UI thread and draws
 * the low resolution framebuffer scaled up with nearest-neighbour filtering.
 */
final class GameView extends View {
    private static final long STEP_NS = 1_000_000_000L / 60;

    private final Game game;
    private final Paint paint = new Paint();
    private final Rect dst = new Rect();
    private Bitmap bitmap;
    private boolean running;
    private long last;
    private long acc;
    private final float[] xs = new float[10], ys = new float[10];

    GameView(Context ctx, Game game) {
        super(ctx);
        this.game = game;
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        paint.setDither(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    void resume() {
        running = true;
        last = System.nanoTime();
        acc = 0;
        requestFocus();
        postInvalidateOnAnimation();
    }

    void pause() {
        running = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        game.layout(w, h);
        if (bitmap != null) bitmap.recycle();
        bitmap = Bitmap.createBitmap(game.vw, game.vh, Bitmap.Config.ARGB_8888);
        dst.set(0, 0, Math.round(game.vw * game.scale), Math.round(game.vh * game.scale));
        game.render();
        bitmap.setPixels(game.gfx.px, 0, game.vw, 0, 0, game.vw, game.vh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null) return;
        long now = System.nanoTime();
        long dt = now - last;
        last = now;
        if (dt > 100_000_000L) dt = 100_000_000L; // don't spiral after a hiccup
        if (running) acc += dt;
        int steps = 0;
        while (acc >= STEP_NS && steps < 5) {
            game.update();
            acc -= STEP_NS;
            steps++;
        }
        if (steps > 0) {
            game.render();
            bitmap.setPixels(game.gfx.px, 0, game.vw, 0, 0, game.vw, game.vh);
        }
        canvas.drawColor(0xFF000000);
        canvas.drawBitmap(bitmap, null, dst, paint);
        if (running) postInvalidateOnAnimation();
    }

    // ------------------------------------------------------------ touch

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float s = game.scale;
        int action = e.getActionMasked();
        int idx = e.getActionIndex();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            game.input.tap(e.getX(idx) / s, e.getY(idx) / s);
        }
        int n = 0;
        if (action != MotionEvent.ACTION_CANCEL && action != MotionEvent.ACTION_UP) {
            for (int i = 0; i < e.getPointerCount() && n < xs.length; i++) {
                if (action == MotionEvent.ACTION_POINTER_UP && i == idx) continue;
                xs[n] = e.getX(i) / s;
                ys[n] = e.getY(i) / s;
                n++;
            }
        }
        game.input.setTouches(xs, ys, n);
        return true;
    }

    // ------------------------------------------------------------ keys

    private static int map(int code) {
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                return Input.K_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                return Input.K_RIGHT;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                return Input.K_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                return Input.K_DOWN;
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_Z:
            case KeyEvent.KEYCODE_BUTTON_A:
                return Input.K_JUMP;
            case KeyEvent.KEYCODE_X:
            case KeyEvent.KEYCODE_E:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_B:
                return Input.K_ACTION;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_START:
                return Input.K_ENTER;
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return Input.K_BACK;
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_BUTTON_Y:
                return Input.K_RESTART;
            case KeyEvent.KEYCODE_F:
            case KeyEvent.KEYCODE_BUTTON_R1:
                return Input.K_FAST;
            default:
                return -1;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int k = map(keyCode);
        if (k < 0) return super.onKeyDown(keyCode, event);
        game.input.key(k, true);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int k = map(keyCode);
        if (k < 0) return super.onKeyUp(keyCode, event);
        game.input.key(k, false);
        return true;
    }
}
