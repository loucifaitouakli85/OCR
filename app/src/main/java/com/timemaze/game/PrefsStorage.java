package com.timemaze.game;

import android.content.Context;
import android.content.SharedPreferences;

import com.timemaze.game.core.Storage;

/** Save data kept in SharedPreferences. */
final class PrefsStorage implements Storage {
    private final SharedPreferences prefs;

    PrefsStorage(Context ctx) {
        prefs = ctx.getSharedPreferences("timemaze", Context.MODE_PRIVATE);
    }

    @Override
    public int getInt(String key, int def) {
        return prefs.getInt(key, def);
    }

    @Override
    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }
}
