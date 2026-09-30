package com.timemaze.game.core;

/** All hand drawn pixel art sprites. Everything faces right; flip for left. */
public final class Sprites {
    private Sprites() {}

    // ------------------------------------------------------------------ Milo
    static final int K = 0xFF22162B; // outline
    static final int HAIR = 0xFF7A4A24, HAIR_L = 0xFFA66A34;
    static final int SKIN = 0xFFF4C7A1, SKIN_D = 0xFFD9A07A, CHEEK = 0xFFEE9C8C;
    static final int SHIRT = 0xFFF2EBDD, SHIRT_D = 0xFFC8BCA6;
    public static final int BLUE = 0xFF2F6BDB, BLUE_D = 0xFF1E4699, BLUE_L = 0xFF5C8FF0;
    static final int BTN = 0xFFF7D24A, SHOE = 0xFF5B3A22;

    private static final String BOY_KEYS = "KHhSsrWwBbLYO";
    private static final int[] BOY_COLS = {K, HAIR, HAIR_L, SKIN, SKIN_D, CHEEK, SHIRT, SHIRT_D, BLUE, BLUE_D, BLUE_L, BTN, SHOE};

    private static final String[] HEAD = {
        "....KKKKK...",
        "...KHHhHHKK.",
        "..KHHhhHHHHK",
        "..KHHHHHHHHK",
        "..KHHHSSSSK.",
        "..KHHSKSSKSK",
        "..KHSSKSSKSK",
        "...KSSSSrSSK",
        "....KKSSSSK.",
    };
    private static final String[] HEAD_BLINK = {
        "....KKKKK...",
        "...KHHhHHKK.",
        "..KHHhhHHHHK",
        "..KHHHHHHHHK",
        "..KHHHSSSSK.",
        "..KHHSSSSSSK",
        "..KHSKKSKKSK",
        "...KSSSSrSSK",
        "....KKSSSSK.",
    };
    private static final String[] BODY = {
        "...KWLBBWK..",
        "..KWKBYBKWK.",
        "..KSKBBBKSK.",
    };
    private static final String[] BODY_UP = {
        ".KSKLBBBKSK.",
        ".KWKBYBBKWK.",
        "..KKBBBBKK..",
    };
    private static final String[] LEGS_STAND = {
        "...KBBBBBK..",
        "...KBbKBbK..",
        "...KBbKBbK..",
        "...KOOKOOOK.",
    };
    private static final String[] LEGS_RUN0 = {
        "...KBBBBBK..",
        "..KBbK.KBbK.",
        ".KBbK...KBK.",
        ".KOK....KOOK",
    };
    private static final String[] LEGS_RUN1 = {
        "...KBBBBBK..",
        "...KBBBBK...",
        "...KBKKbK...",
        "...KOK.KOOK.",
    };
    private static final String[] LEGS_RUN2 = {
        "...KBBBBBK..",
        "..KbBK.KBbK.",
        "..KOK...KBK.",
        "........KOOK",
    };
    private static final String[] LEGS_RUN3 = {
        "...KBBBBBK..",
        "...KBBBBK...",
        "....KbBK....",
        "...KOOOOK...",
    };
    private static final String[] LEGS_JUMP = {
        "...KBBBBBK..",
        "..KBbKKBbK..",
        "..KOOK.KBbK.",
        ".......KOOK.",
    };
    private static final String[] LEGS_FALL = {
        "...KBBBBBK..",
        "...KBbKBbK..",
        "..KBbK.KBbK.",
        "..KOOK.KOOK.",
    };

    private static Sprite boy(String[] head, String[] body, String[] legs) {
        String[] rows = new String[16];
        int i = 0;
        for (String r : head) rows[i++] = r;
        for (String r : body) rows[i++] = r;
        for (String r : legs) rows[i++] = r;
        return Sprite.parse(BOY_KEYS, BOY_COLS, rows);
    }

    public static final Sprite BOY_IDLE = boy(HEAD, BODY, LEGS_STAND);
    public static final Sprite BOY_BLINK = boy(HEAD_BLINK, BODY, LEGS_STAND);
    public static final Sprite[] BOY_RUN = {
        boy(HEAD, BODY, LEGS_RUN0), boy(HEAD, BODY, LEGS_RUN1),
        boy(HEAD, BODY, LEGS_RUN2), boy(HEAD, BODY, LEGS_RUN3)
    };
    public static final Sprite BOY_JUMP = boy(HEAD, BODY_UP, LEGS_JUMP);
    public static final Sprite BOY_FALL = boy(HEAD, BODY_UP, LEGS_FALL);

    /** Animation ids stored in recordings. */
    public static final int A_IDLE = 0, A_BLINK = 1, A_RUN = 2, A_JUMP = 6, A_FALL = 7;

    public static Sprite boyFrame(int anim) {
        switch (anim) {
            case A_BLINK: return BOY_BLINK;
            case A_RUN: case A_RUN + 1: case A_RUN + 2: case A_RUN + 3: return BOY_RUN[anim - A_RUN];
            case A_JUMP: return BOY_JUMP;
            case A_FALL: return BOY_FALL;
            default: return BOY_IDLE;
        }
    }

    // ------------------------------------------------------------ The Shade
    static final int CL_D = 0xFF0E0A16, CL = 0xFF231A36, CL_L = 0xFF3A2C58, VOID = 0xFF040208;
    public static final int EYE = 0xFFFF3B5C;
    private static final String SHADE_KEYS = "DCcXG";
    private static final int[] SHADE_COLS = {CL_D, CL, CL_L, VOID, EYE};

    private static final String[] SHADE_TOP = {
        ".....DDDD.....",
        "....DCcccD....",
        "...DCcCCCcD...",
        "...DCXXXXCCD..",
        "..DCXXXXXXCD..",
        "..DCXGXXGXCD..",
        "..DCXXXXXXCD..",
        "..DCCXXXXCCD..",
        ".DCcCCCCCCcCD.",
        ".DCccCCCCccCD.",
        "DCCCcCCCCcCCCD",
        "DCCDcCCCCcDCCD",
        ".DD.DCCCCD.DD.",
        "....DCCCCD....",
        "...DCCcCCCD...",
        "...DCCcCCCCD..",
        "..DCCCcCCCCD..",
    };
    private static final String[][] SHADE_HEM = {
        {"..DCCCCCCCCCD.", ".D.D.DD.D.D.D."},
        {"..DCCCCCCCCCD.", "..D.D.DD.D.D.."},
        {".DCCCCCCCCCCD.", ".D..D.D.DD..D."},
    };

    public static final Sprite[] SHADE = new Sprite[3];

    static {
        for (int f = 0; f < 3; f++) {
            String[] rows = new String[SHADE_TOP.length + 2];
            System.arraycopy(SHADE_TOP, 0, rows, 0, SHADE_TOP.length);
            rows[SHADE_TOP.length] = SHADE_HEM[f][0];
            rows[SHADE_TOP.length + 1] = SHADE_HEM[f][1];
            SHADE[f] = Sprite.parse(SHADE_KEYS, SHADE_COLS, rows);
        }
    }

    // --------------------------------------------- Old Milo (hood lowered)
    static final int GREY = 0xFFB8B4C0, GREY_D = 0xFF7E7A88, BEARD = 0xFFE4E0E8, OLD_SKIN = 0xFFD9A98A;
    static final int FADED = 0xFF52739F, FADED_D = 0xFF3A5478;
    private static final String OLD_KEYS = "DCcgGSsWBbYK";
    private static final int[] OLD_COLS = {CL_D, CL, CL_L, GREY_D, GREY, OLD_SKIN, SKIN_D, BEARD, FADED, FADED_D, 0xFFC9AE52, K};

    public static final Sprite OLD_MILO = Sprite.parse(OLD_KEYS, OLD_COLS,
        "..............",
        ".....KKKK.....",
        "....KGGgGK....",
        "...KGGGGGgK...",
        "...KGSSSSGK...",
        "...KSKSSKSK...",
        "...KSSSsSSK...",
        "...KWWSSWWK...",
        ".DDKWWWWWWKDD.",
        ".DCcKWWWWKcCD.",
        "DCCcBBWWBBcCCD",
        "DCCDBYBBYBDCCD",
        ".DD.DBBBBD.DD.",
        "....DBBBBD....",
        "...DCBbbBCD...",
        "...DCCcCCCCD..",
        "..DCCCcCCCCD..",
        "..DCCCCCCCCCD.",
        ".D.D.DD.D.D.D.");

    // --------------------------------------------------------- portraits
    private static final String PORT_KEYS = "KHhSsrWBbYDCcXGgEVwO";
    private static final int[] PORT_COLS = {K, HAIR, HAIR_L, SKIN, SKIN_D, CHEEK, SHIRT, BLUE, BLUE_D, BTN,
        CL_D, CL, CL_L, VOID, EYE, GREY, BEARD, GREY_D, 0xFFFFFFFF, OLD_SKIN};

    public static final Sprite PORTRAIT_MILO = Sprite.parse(PORT_KEYS, PORT_COLS,
        "......KKKKKK....",
        "....KKHHhHHHKK..",
        "...KHHHhhHHHHHK.",
        "..KHHHHHhHHHHHHK",
        "..KHHHHHHHHHHHHK",
        "..KHHHSSSSSHHHHK",
        "..KHHSSSSSSSSHK.",
        "..KHSSKwSSSKwSK.",
        "..KSSSKKSSSKKSK.",
        "...KSSSSSSSSSSK.",
        "...KSrSSSssSrSK.",
        "....KSSSKKKSSK..",
        ".....KKSSSSSKK..",
        "...KKWWKKKKKWWK.",
        "..KWWBBWWWWWBBWK",
        "..KWWBBBBYBBBBWK");

    public static final Sprite PORTRAIT_SHADE = Sprite.parse(PORT_KEYS, PORT_COLS,
        "......DDDDD.....",
        "....DDCCcCCDD...",
        "...DCCccccCCCD..",
        "..DCCcCCCCCcCCD.",
        "..DCcCXXXXXCcCD.",
        ".DCCcXXXXXXXCCCD",
        ".DCCXXXXXXXXXCCD",
        ".DCCXXGXXXGXXCCD",
        ".DCCXXGXXXGXXCCD",
        ".DCCXXXXXXXXXCCD",
        ".DCCcXXXXXXXcCCD",
        ".DCCcCXXXXXCcCCD",
        "DCCCccCCCCCccCCD",
        "DCCCcCCCCCCCcCCD",
        "DCCcCCCCCCCCCcCD",
        "DCcCCCCCCCCCCCcD");

    public static final Sprite PORTRAIT_OLD = Sprite.parse(PORT_KEYS, PORT_COLS,
        "......KKKKK.....",
        "....KKgggVgKK...",
        "...KggggVgggVK..",
        "..KgggVggggggVK.",
        "..KgVOOOOOOOgVK.",
        "..KgOOOOOOOOOgK.",
        "..KgOKwOOOKwOgK.",
        "..KOOKKOOOKKOOK.",
        "..KOOOOOsOOOOOK.",
        "..KOEEEOOOEEEOK.",
        "..DKEEEEEEEEEKD.",
        ".DCKEEEEEEEEEKCD",
        "DCCcKEEEEEEEKcCD",
        "DCCcDKEEEEEKDcCD",
        "DCCcBBDKKKDBBcCD",
        "DCcCBBBBYBBBBCcD");

    public static final Sprite PORTRAIT_STRANGER;

    static {
        int RED_D = 0xFF3A0A12, RED = 0xFF7A1424, RED_L = 0xFFB0283A, GOLD = 0xFFFFD34A;
        PORTRAIT_STRANGER = PORTRAIT_SHADE.recolor(new int[]{CL_D, CL, CL_L, EYE}, new int[]{RED_D, RED, RED_L, GOLD});
    }

    public static final Sprite[] STRANGER = new Sprite[3];

    static {
        int RED_D = 0xFF3A0A12, RED = 0xFF7A1424, RED_L = 0xFFB0283A, GOLD = 0xFFFFD34A;
        for (int i = 0; i < 3; i++) {
            STRANGER[i] = SHADE[i].recolor(new int[]{CL_D, CL, CL_L, EYE}, new int[]{RED_D, RED, RED_L, GOLD});
        }
    }

    // --------------------------------------------------------------- misc
    public static final Sprite GEAR_ICON = Sprite.parse("Yy", new int[]{0xFFE0B040, 0xFF9C7420},
        "..Y.Y..",
        ".YYYYY.",
        "YYyyyYY",
        ".Yy.yY.",
        "YYyyyYY",
        ".YYYYY.",
        "..Y.Y..");

    public static final Sprite LOCK_ICON = Sprite.parse("Kkg", new int[]{0xFF6A6F86, 0xFF3A3E52, 0xFFE0B040},
        "..kkk..",
        ".k...k.",
        ".k...k.",
        "KKKKKKK",
        "KKKgKKK",
        "KKKgKKK",
        "KKKKKKK");

    public static final Sprite SHADE_ICON = Sprite.parse("DCG", new int[]{CL_D, CL_L, EYE},
        "..DDD..",
        ".DCCCD.",
        "DCDDDCD",
        "DDGDGDD",
        "DDDDDDD",
        ".DCCCD.",
        "DCCCCCD");
}
