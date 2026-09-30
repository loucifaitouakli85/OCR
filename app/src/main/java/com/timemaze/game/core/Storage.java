package com.timemaze.game.core;

import java.util.HashMap;

/** Persistent key/value storage provided by the platform. */
public interface Storage {
    int getInt(String key, int def);

    void putInt(String key, int value);

    /** Volatile implementation used by tests and desktop tools. */
    final class Memory implements Storage {
        private final HashMap<String, Integer> map = new HashMap<String, Integer>();

        public int getInt(String key, int def) {
            Integer v = map.get(key);
            return v == null ? def : v;
        }

        public void putInt(String key, int value) {
            map.put(key, value);
        }
    }
}
