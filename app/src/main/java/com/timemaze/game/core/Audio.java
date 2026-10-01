package com.timemaze.game.core;

/**
 * Software mixer: procedural chiptune music plus sound effects, rendered to
 * 16-bit mono PCM at {@link #RATE}. The platform layer pulls samples from
 * {@link #render} on its audio thread.
 */
public final class Audio {
    public static final int RATE = Sfx.RATE;

    public static final int SONG_NONE = -1, SONG_TITLE = 0, SONG_ZONE1 = 1, SONG_ZONE2 = 2, SONG_ZONE3 = 3,
        SONG_ZONE4 = 4, SONG_SHADE = 5, SONG_ENDING = 6, SONG_RUST = 7, SONG_ASH = 8;

    private static final int[] ZONE_SONGS = {SONG_ZONE1, SONG_ZONE2, SONG_ZONE3, SONG_RUST, SONG_ASH, SONG_ZONE4};

    /** Music for a zone of the maze (0 = Clockwork Halls ... 5 = Heart of Time). */
    public static int forZone(int zone) {
        return ZONE_SONGS[Math.max(0, Math.min(ZONE_SONGS.length - 1, zone))];
    }

    private final float[][] sfx = Sfx.generateAll();
    private final float[][] voiceData = new float[8][];
    private final int[] voicePos = new int[8];

    public volatile boolean sfxOn = true, musicOn = true;

    // ------------------------------------------------------------ music
    private static final class Song {
        final int bpm;
        final String[] chords;
        final String[] melody;
        final boolean minorDrone;

        Song(int bpm, String chords, String melody, boolean drone) {
            this.bpm = bpm;
            this.chords = chords.split(" ");
            this.melody = melody.trim().split("\\s+");
            this.minorDrone = drone;
        }
    }

    private static final Song[] SONGS = {
        new Song(84, "Am F C E",
            "A4 - C5 - E5 - D5 C5  A4 - - - F4 - A4 -  G4 - C5 - E5 - G5 E5  D5 - C5 - B4 - G#4 -", false),
        new Song(104, "Dm Bb C A",
            "D5 . A4 . F4 . A4 .  D5 . Bb4 . F4 . D5 .  E5 . C5 . G4 . C5 .  C#5 . A4 . E4 . A4 .", false),
        new Song(96, "Em C G D",
            "E5 - B4 - G4 - B4 -  C5 - - E5 D5 - C5 -  B4 - D5 - G5 - D5 -  A4 - F#4 - D5 - - -", false),
        new Song(108, "F#m D A E",
            "F#5 - C#5 - A4 C#5 F#5 -  D5 - A4 - F#4 - A4 -  E5 - C#5 - A4 - C#5 E5  B4 - G#4 - E4 - B4 -", false),
        new Song(120, "Cm Ab Eb G",
            "C5 C5 Eb5 C5 G5 - F5 Eb5  Ab4 - C5 - Eb5 - C5 -  Bb4 Bb4 Eb5 Bb4 G5 - F5 G5  B4 - D5 - F5 - D5 B4", false),
        new Song(70, "Cm Gbm Cm Gbm",
            ". . . . . . . .  . . . . Gb4 - - -  . . . . . . . .  . . . . C5 - B4 -", true),
        new Song(72, "Am F C G",
            "E5 - - D5 C5 - - -  A4 - - - F4 - - -  G4 - C5 - E5 - D5 -  D5 - - - B4 - - -", false),
        new Song(112, "Gm Eb Bb D",
            "G4 . Bb4 . D5 - C5 Bb4  Eb5 - D5 - Bb4 - G4 -  F4 . Bb4 . D5 - F5 D5  F#4 - A4 - D5 - - -", false),
        new Song(92, "Bbm Gb Db F",
            "Bb4 - - Db5 C5 - Bb4 -  Gb4 - - - Db5 - - -  F4 - Ab4 - Db5 - C5 Db5  C5 - - - A4 - - -", false),
    };

    private int song = SONG_NONE;
    private int samplesPerStep; // one step = a 16th note
    private int stepSample, step;
    private float bassPh, arpPh, leadPh, padPh;
    private float bassF, arpF, leadF, padF;
    private float bassEnv, arpEnv, leadEnv, hatEnv;
    private long noise = 0x2545F4914F6CDD1DL;
    private float musicFade = 1f;

    public synchronized void play(int id) {
        if (!sfxOn || id < 0 || id >= sfx.length) return;
        int slot = -1;
        for (int i = 0; i < voiceData.length; i++) {
            if (voiceData[i] == null) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            // steal the voice closest to finishing
            int best = 0;
            for (int i = 1; i < voiceData.length; i++) {
                if (voiceData[i].length - voicePos[i] < voiceData[best].length - voicePos[best]) best = i;
            }
            slot = best;
        }
        voiceData[slot] = sfx[id];
        voicePos[slot] = 0;
    }

    public synchronized void setSong(int id) {
        if (id == song) return;
        song = id;
        step = 0;
        stepSample = 0;
        bassEnv = arpEnv = leadEnv = hatEnv = 0;
        if (id >= 0) samplesPerStep = RATE * 60 / SONGS[id].bpm / 4;
    }

    public synchronized int currentSong() {
        return song;
    }

    public synchronized void render(short[] out, int n) {
        boolean music = musicOn && song >= 0;
        Song s = music ? SONGS[song] : null;
        for (int i = 0; i < n; i++) {
            float v = 0;
            if (s != null) {
                if (stepSample == 0) onStep(s);
                if (++stepSample >= samplesPerStep) {
                    stepSample = 0;
                    step++;
                }
                v += musicSample(s) * 0.55f * musicFade;
            }
            for (int k = 0; k < voiceData.length; k++) {
                float[] d = voiceData[k];
                if (d == null) continue;
                v += d[voicePos[k]] * 0.8f;
                if (++voicePos[k] >= d.length) voiceData[k] = null;
            }
            // soft clip
            if (v > 1f) v = 1f;
            else if (v < -1f) v = -1f;
            out[i] = (short) (v * 26000);
        }
    }

    private void onStep(Song s) {
        int bar = (step / 16) % s.chords.length;
        int inBar = step % 16;
        int[] chord = chordNotes(s.chords[bar]);
        // bass on eighths, root with octave bounce
        if (inBar % 2 == 0) {
            int oct = (inBar / 2) % 4 == 2 ? 12 : 0;
            bassF = midiToFreq(chord[0] - 24 + oct);
            bassEnv = s.minorDrone ? 0.9f : 1f;
        }
        // arpeggio on sixteenths
        if (!s.minorDrone) {
            int[] pattern = {0, 1, 2, 3, 2, 1};
            arpF = midiToFreq(chord[pattern[step % pattern.length]] + 12);
            arpEnv = 0.55f;
        } else if (inBar == 0) {
            padF = midiToFreq(chord[0] - 12);
        }
        // melody tokens are eighth notes
        if (inBar % 2 == 0) {
            int idx = (step / 2) % s.melody.length;
            String tok = s.melody[idx];
            if (tok.equals(".")) {
                leadEnv = 0;
            } else if (!tok.equals("-")) {
                leadF = noteFreq(tok);
                leadEnv = 1f;
            }
        }
        // clock tick hi-hat
        if (inBar % 4 == 2 || (s.bpm >= 110 && inBar % 2 == 1)) hatEnv = inBar % 4 == 2 ? 0.8f : 0.35f;
    }

    private float musicSample(Song s) {
        float dt = 1f / RATE;
        bassPh += bassF * dt;
        bassPh -= (int) bassPh;
        arpPh += arpF * dt;
        arpPh -= (int) arpPh;
        leadPh += leadF * dt;
        leadPh -= (int) leadPh;
        padPh += padF * dt;
        padPh -= (int) padPh;
        float tri = bassPh < 0.5f ? bassPh * 4 - 1 : 3 - bassPh * 4;
        float v = tri * bassEnv * 0.45f;
        v += (arpPh < 0.125f ? 1 : -1) * arpEnv * 0.08f;
        float lead = (leadPh < 0.5f ? 1 : -1) * 0.5f + (leadPh < 0.25f ? 0.3f : -0.1f);
        float vib = 1f;
        v += lead * leadEnv * 0.16f * vib;
        if (s.minorDrone) {
            float pad = padPh < 0.5f ? padPh * 4 - 1 : 3 - padPh * 4;
            v += pad * 0.25f;
        }
        noise ^= noise << 13;
        noise ^= noise >>> 7;
        noise ^= noise << 17;
        float nz = ((noise >>> 40) & 0xFFFF) / 32768f - 1f;
        v += nz * hatEnv * 0.07f;
        // envelopes
        bassEnv *= 0.99985f;
        arpEnv *= 0.9993f;
        leadEnv *= 0.99992f;
        hatEnv *= 0.994f;
        return v;
    }

    // ------------------------------------------------------------ notes

    static float midiToFreq(int m) {
        return (float) (440.0 * Math.pow(2, (m - 69) / 12.0));
    }

    static int noteMidi(String n) {
        int i = 0;
        char c = n.charAt(i++);
        int base;
        switch (c) {
            case 'C': base = 0; break;
            case 'D': base = 2; break;
            case 'E': base = 4; break;
            case 'F': base = 5; break;
            case 'G': base = 7; break;
            case 'A': base = 9; break;
            default: base = 11;
        }
        if (n.charAt(i) == '#') {
            base++;
            i++;
        } else if (n.charAt(i) == 'b') {
            base--;
            i++;
        }
        int oct = Integer.parseInt(n.substring(i));
        return (oct + 1) * 12 + base;
    }

    static float noteFreq(String n) {
        return midiToFreq(noteMidi(n));
    }

    /** Root (octave 4), third, fifth and octave of a chord like "F#m" or "Bb". */
    static int[] chordNotes(String c) {
        boolean minor = c.endsWith("m");
        String root = minor ? c.substring(0, c.length() - 1) : c;
        int r = noteMidi(root + "4");
        return new int[]{r, r + (minor ? 3 : 4), r + 7, r + 12};
    }
}
