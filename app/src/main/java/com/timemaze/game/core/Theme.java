package com.timemaze.game.core;

/** Colour palette of one zone of the maze. */
public final class Theme {
    public final int bg, bg2, gear, wall, wallL, wallD, edge, lip, lipD, accent;
    public final int lipStyle; // 0 stone, 1 moss, 2 snow, 3 gold

    Theme(int bg, int bg2, int gear, int wall, int wallL, int wallD, int edge, int lip, int lipD, int accent, int lipStyle) {
        this.bg = bg;
        this.bg2 = bg2;
        this.gear = gear;
        this.wall = wall;
        this.wallL = wallL;
        this.wallD = wallD;
        this.edge = edge;
        this.lip = lip;
        this.lipD = lipD;
        this.accent = accent;
        this.lipStyle = lipStyle;
    }

    public static final Theme[] ZONES = {
        new Theme(0xFF1B1422, 0xFF241A2C, 0xFF30243A, 0xFF6B4E3A, 0xFF8C6A4E, 0xFF4A3528, 0xFF24180F, 0xFFB48C5C, 0xFF8A6A44, 0xFFE0A040, 0),
        new Theme(0xFF0D1B20, 0xFF13262C, 0xFF1B343A, 0xFF3E6A66, 0xFF5A8C84, 0xFF2A4A48, 0xFF10201F, 0xFF74C45C, 0xFF4A8A3C, 0xFF5AE0C0, 1),
        new Theme(0xFF0D1327, 0xFF141E38, 0xFF1D2B4C, 0xFF5A74A8, 0xFF7E9ACC, 0xFF3C5080, 0xFF18223F, 0xFFEAF4FF, 0xFFB8CCE8, 0xFF9AD8FF, 2),
        new Theme(0xFF190D23, 0xFF241338, 0xFF321E4C, 0xFF5E3A7A, 0xFF7E56A0, 0xFF3E2456, 0xFF1C0C2A, 0xFFE8C050, 0xFFA8842C, 0xFFFF7AD8, 3),
    };

    /** Colours of the signal channels a..h (then repeating). */
    public static final int[] CHANNEL = {
        0xFFFF5A5A, 0xFF5AE06A, 0xFF5AA8FF, 0xFFFFD84A, 0xFFE26AFF, 0xFFFF9A3A, 0xFF4AE8E8, 0xFFF0F0F0
    };

    public static int channel(int c) {
        return CHANNEL[c % CHANNEL.length];
    }
}
