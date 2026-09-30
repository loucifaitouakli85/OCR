package com.timemaze.game;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.timemaze.game.core.Audio;

/** Streams the software mixer to an AudioTrack on its own thread. */
final class AudioOut implements Runnable {
    private static final int CHUNK = 512;

    private final Audio audio;
    private volatile boolean running;
    private Thread thread;
    private AudioTrack track;

    AudioOut(Audio audio) {
        this.audio = audio;
    }

    @SuppressWarnings("deprecation")
    synchronized void start() {
        if (running) return;
        try {
            int min = AudioTrack.getMinBufferSize(Audio.RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int size = Math.max(min, CHUNK * 2 * 4);
            track = new AudioTrack(AudioManager.STREAM_MUSIC, Audio.RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, size, AudioTrack.MODE_STREAM);
            track.play();
        } catch (Exception e) {
            track = null;
            return; // play silently if audio is unavailable
        }
        running = true;
        thread = new Thread(this, "TimeMazeAudio");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    synchronized void stop() {
        running = false;
        if (thread != null) {
            try {
                thread.join(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
        if (track != null) {
            try {
                track.pause();
                track.flush();
                track.release();
            } catch (Exception ignored) {
                // already released
            }
            track = null;
        }
    }

    @Override
    public void run() {
        short[] buf = new short[CHUNK];
        AudioTrack t = track;
        while (running && t != null) {
            audio.render(buf, CHUNK);
            if (t.write(buf, 0, CHUNK) < 0) break;
        }
    }
}
