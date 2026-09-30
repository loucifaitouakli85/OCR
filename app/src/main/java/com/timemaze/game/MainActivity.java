package com.timemaze.game;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.timemaze.game.core.Audio;
import com.timemaze.game.core.Game;

/** Hosts the game: a single fullscreen, landscape, immersive view. */
public final class MainActivity extends Activity {
    private GameView view;
    private AudioOut audioOut;
    private Game game;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 28) {
            // keep the game away from camera cutouts (LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER)
            try {
                WindowManager.LayoutParams lp = w.getAttributes();
                lp.getClass().getField("layoutInDisplayCutoutMode").setInt(lp, 2);
                w.setAttributes(lp);
            } catch (Exception ignored) {
                // older platform without cutout support
            }
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Audio audio = new Audio();
        game = new Game(new PrefsStorage(this), audio);
        audioOut = new AudioOut(audio);
        view = new GameView(this, game);
        setContentView(view);
        hideSystemUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        audioOut.start();
        view.resume();
    }

    @Override
    protected void onPause() {
        view.pause();
        audioOut.stop();
        game.onPause();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    public void onBackPressed() {
        if (!game.onBack()) super.onBackPressed();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
