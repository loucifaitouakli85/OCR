package com.timemaze.game.core;

/** The thirty chambers of the Time Maze. */
public final class Levels {
    private Levels() {}

    public static final int COUNT = 30;

    public static final String[] ZONE_NAMES = {
        "THE CLOCKWORK HALLS", "THE SUNKEN HOURS", "THE FROZEN SECONDS", "THE RUSTED YEARS", "THE ASHEN AGES",
        "THE HEART OF TIME"
    };

    private static final Level[] CACHE = new Level[COUNT + 1];

    /** Returns chamber n (1..COUNT). Levels are never modified, so they are built once. */
    public static Level get(int n) {
        if (n < 1 || n > COUNT) throw new IllegalArgumentException("No level " + n);
        if (CACHE[n] == null) CACHE[n] = build(n);
        return CACHE[n];
    }

    static Level build(int n) {
        switch (n) {
            case 1: return l1();
            case 2: return l2();
            case 3: return l3();
            case 4: return l4();
            case 5: return l5();
            case 6: return l6();
            case 7: return l7();
            case 8: return l8();
            case 9: return l9();
            case 10: return l10();
            case 11: return l11();
            case 12: return l12();
            case 13: return l13();
            case 14: return l14();
            case 15: return l15();
            case 16: return l16();
            case 17: return l17();
            case 18: return l18();
            case 19: return l19();
            case 20: return l20();
            case 21: return l21();
            case 22: return l22();
            case 23: return l23();
            case 24: return l24();
            case 25: return l25();
            case 26: return l26();
            case 27: return l27();
            case 28: return l28();
            case 29: return l29();
            case 30: return l30();
            default: throw new IllegalArgumentException("No level " + n);
        }
    }

    // ================================================== ZONE 1: CLOCKWORK HALLS

    static Level l1() {
        return new Level(1, "AWAKENING", 0,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.........L........#",
            "#........###.......#",
            "#.T....#####....E..#",
            "####################")
            .lever('L', 'a', false)
            .exit("a")
            .intro("MILO WAKES UP ON A COLD STONE FLOOR. SOMEWHERE, A THOUSAND CLOCKS ARE TICKING.")
            .hint("{ } TO MOVE, ^ TO JUMP. CLIMB UP TO THE LEVER.")
            .hint("PRESS @ NEXT TO THE LEVER TO PULL IT.")
            .par(0);
    }

    static Level l2() {
        return new Level(2, "ECHOES", 0,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.........##.......#",
            "#.Ta....####....E..#",
            "####################")
            .plate('a', 'a')
            .exit("a")
            .intro("THE STRANGE BOOTH HUMS. IT CAN SEND MILO BACK TO THE MOMENT HE ARRIVED.")
            .hint("THE DOOR ONLY OPENS WHILE THE PLATE IS HELD. STAND ON IT A WHILE...")
            .hint("...THEN STEP INTO THE TIME MACHINE AND PRESS @ TO TRAVEL BACK.")
            .hint("1>YOUR REMNANT REPEATS WHAT YOU DID. RUN FOR THE DOOR!")
            .par(1);
    }

    static Level l3() {
        return new Level(3, "TWO PLACES AT ONCE", 0,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#b.................#",
            "###.......T........#",
            "#a........#....^^.E#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .exit("ab")
            .intro("TWO PLATES. ONE BOY. OR... MAYBE NOT JUST ONE.")
            .hint("THE DOOR NEEDS BOTH PLATES. EVERY TRIP BACK ADDS ANOTHER REMNANT.")
            .hint("MIND THE SPIKES!")
            .par(2);
    }

    static Level l4() {
        return new Level(4, "STEPPING STONE", 0,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.............L....#",
            "#...........######.#",
            "#..................#",
            "#.T..............E.#",
            "####################")
            .lever('L', 'a', false)
            .exit("a")
            .intro("THE LEVER IS OUT OF REACH. IF ONLY SOMEONE COULD GIVE MILO A BOOST.")
            .hint("WAIT UNDER THE LEDGE, THEN GO BACK IN TIME.")
            .hint("1>JUMP ON YOUR REMNANT'S HEAD TO REACH THE LEDGE.")
            .par(1);
    }

    static Level l5() {
        return new Level(5, "THE SHADE", 0,
            "####################",
            "#.........#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........#.....L..#",
            "#.........D...######",
            "#.........D........#",
            "#.b..T..a.D.E......#",
            "####################")
            .plate('b', 'b')
            .plate('a', 'a')
            .lever('L', 'a', false)
            .door('D', "b")
            .exit("a")
            .intro("SOMETHING IS WATCHING FROM THE DARK. MILO FEELS IT IN HIS BONES.")
            .hint("THE GATE NEEDS PLATE B. THE EXIT NEEDS PLATE A... OR THAT LEVER.")
            .event(shade5(new ShadeEvent().onSignal('a', 1).at(8, 9)))
            .event(shade5(new ShadeEvent().onEnter(11, 1, 18, 9).at(9, 9)))
            .par(2);
    }

    static ShadeEvent shade5(ShadeEvent e) {
        return e.group(0)
            .say("S:...",
                "M:WHO... WHO ARE YOU?",
                "S:YOU SHOULDN'T BE HERE, BOY. TURN BACK.",
                "S:EVERY DOOR YOU OPEN BRINGS THE END A LITTLE CLOSER.",
                "#ACT",
                "N:THE SHADE SMASHED PLATE A AND SCATTERED MILO'S REMNANTS.",
                "M:HE BROKE THE EASY WAY OUT... BUT THAT LEVER UP THERE STILL WORKS.")
            .breakObj('a').clearRemnants();
    }

    // =================================================== ZONE 2: SUNKEN HOURS

    static Level l6() {
        return new Level(6, "RISING TIDE", 1,
            "####################",
            "#..................#",
            "#..................#",
            "#......p...........#",
            "#.......#######.####",
            "#.......#..........#",
            "#.......#..........#",
            "#.......#..........#",
            "#.......#..........#",
            "#.a..T.P#.......E..#",
            "####################")
            .plate('a', 'a')
            .lift('P', 'p', 1, "a")
            .exit("")
            .intro("THE WALLS HERE ARE DAMP AND GREEN. OLD LIFTS STILL CREAK IN THEIR SHAFTS.")
            .hint("LIFTS MOVE WHILE THEIR PLATE IS HELD. THIS ONE LEADS UP TO THE HIGH ROAD.")
            .par(1);
    }

    static Level l7() {
        return new Level(7, "LASER HALL", 1,
            "####################",
            "#.......Z....Y.....#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.Ta........b....E.#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .laser('Z', Level.DIR_DOWN, "!a")
            .laser('Y', Level.DIR_DOWN, "!b")
            .exit("")
            .intro("RED LIGHT HUMS ACROSS THE HALL. THE BEAMS SWITCH OFF WHILE THEIR PLATES ARE HELD.")
            .hint("REMNANTS ARE HURT BY LASERS TOO. A HURT REMNANT BREAKS TIME: A PARADOX!")
            .par(2);
    }

    static Level l8() {
        return new Level(8, "SPLIT SECOND", 1,
            "####################",
            "#...............D..#",
            "#...............D.E#",
            "#.............######",
            "#..........==......#",
            "#..................#",
            "#.......==.........#",
            "#..................#",
            "###........==......#",
            "#t.T...............#",
            "####################")
            .timer('t', 'a', 360)
            .door('D', "a")
            .exit("")
            .intro("A CLOCKWORK BUTTON. PRESS IT AND THE GATE ABOVE OPENS FOR A FEW SECONDS.")
            .hint("TIMED BUTTONS WORK ONCE PER PRESS. A REMNANT CAN WAIT BEFORE PRESSING IT.")
            // the button works normally until Milo reaches the gate; then the Shade shortens it
            .event(new ShadeEvent().onEnter(14, 1, 15, 2).at(17, 2)
                .say("S:CLEVER BOY. YOU THINK TIME BENDS FOR YOU.",
                    "M:YOU AGAIN?",
                    "S:LET ME SHOW YOU HOW SHORT A SECOND CAN BE.",
                    "#ACT",
                    "N:THE SHADE TAMPERED WITH THE BUTTON. NOW THE GATE STAYS OPEN FOR BARELY A MOMENT.",
                    "M:THEN MY ECHO WILL HAVE TO PRESS IT AT EXACTLY THE RIGHT TIME.")
                .timer('t', 100).restartLoop())
            .par(1);
    }

    static Level l9() {
        return new Level(9, "THE BRIDGE", 1,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..............==..#",
            "#..................#",
            "#..................#",
            "#.aT.E.P...p....L..#",
            "#######^^^^^^#######")
            .plate('a', 'a')
            .lift('P', 'p', 2, "a")
            .lever('L', 'b', false)
            .exit("b")
            .intro("A FLOATING BRIDGE SPANS A PIT OF SPIKES. THE LEVER THAT OPENS THE EXIT IS ON THE FAR SIDE.")
            .hint("THE BRIDGE CROSSES WHILE PLATE A IS HELD, AND DRIFTS BACK WHEN IT IS RELEASED.")
            .par(1);
    }

    static Level l10() {
        return new Level(10, "THE SHADE RETURNS", 1,
            "####################",
            "#..........Y.......#",
            "#..................#",
            "#.c....p...........#",
            "######..#########..#",
            "#.......#..........#",
            "#.......#..........#",
            "#.......#..........#",
            "#.......D..........#",
            "#.b.T.aPD.....E....#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .plate('c', 'c')
            .door('D', "a")
            .lift('P', 'p', 1, "b")
            .laser('Y', Level.DIR_DOWN, "!c")
            .exit("")
            .intro("THE AIR TURNS COLD. MILO KNOWS THAT FEELING NOW. HE IS NOT ALONE.")
            .event(shade10(new ShadeEvent().onSignal('a', 1).at(9, 9)))
            .event(shade10(new ShadeEvent().onLoop(2).at(9, 9)))
            .par(2);
    }

    static ShadeEvent shade10(ShadeEvent e) {
        return e.group(0)
            .say("S:STILL RUNNING THROUGH THESE HALLS?",
                "M:YOU AGAIN! WHY ARE YOU DOING THIS?",
                "S:THAT IS NOT FOR YOU TO KNOW, BOY... NOT YET.",
                "S:BUT I WILL BURY EVERY ROAD YOU TRY TO TAKE.",
                "#ACT",
                "N:THE CEILING CAVED IN. THE LOW ROAD IS GONE, AND SO ARE MILO'S REMNANTS.",
                "M:THEN I'LL TAKE THE HIGH ROAD.")
            .setTile(10, 5, '#').setTile(10, 6, '#').setTile(10, 7, '#').setTile(10, 8, '#').setTile(10, 9, '#')
            .clearRemnants();
    }

    // ================================================= ZONE 3: FROZEN SECONDS

    static Level l11() {
        return new Level(11, "TOGGLE", 2,
            "####################",
            "#.......#....#.....#",
            "#.......#....#.....#",
            "#.......#....#.....#",
            "#.......#....#.....#",
            "#.......#....#.....#",
            "#.......#....#.....#",
            "#.......D....F.....#",
            "#.......D....F.....#",
            "#.T.L...D..b.F...E.#",
            "####################")
            .lever('L', 'a', false)
            .plate('b', 'b')
            .door('D', "a")
            .door('F', "!a")
            .exit("b")
            .intro("FROST CREEPS OVER THE GEARS. ONE LEVER, TWO GATES: WHEN ONE OPENS, THE OTHER SHUTS.")
            .hint("A REMNANT CAN PULL A LEVER MORE THAN ONCE. TIMING IS EVERYTHING.")
            .par(2);
    }

    static Level l12() {
        return new Level(12, "TOWER OF ME", 2,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.............L....#",
            "#..........#######.#",
            "#..................#",
            "#..................#",
            "#.T..............E.#",
            "####################")
            .lever('L', 'a', false)
            .exit("a")
            .intro("THE LEDGE IS FOUR BLOCKS HIGH. ONE REMNANT WON'T BE ENOUGH.")
            .hint("REMNANTS CAN STAND ON EACH OTHER. THE ONE AT THE BOTTOM MUST STAY THE LONGEST!")
            .event(new ShadeEvent().onLoop(1).at(10, 9)
                .say("S:A TOWER BUILT OUT OF YOURSELF. HOW TOUCHING.",
                    "S:BUT EVERY TOWER FALLS, BOY.",
                    "#ACT",
                    "N:THE SHADE DROVE SPIKES INTO THE FLOOR WHERE THE TOWER STOOD, AND SCATTERED MILO'S REMNANTS.",
                    "M:THEN I'LL BUILD IT ON THE OTHER SIDE OF THE LEDGE.")
                .setTile(10, 9, '^').clearRemnants())
            .par(2);
    }

    static Level l13() {
        return new Level(13, "LEAP OF FAITH", 2,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.......L..........#",
            "#....#######.......#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.T..............E.#",
            "####################")
            .lever('L', 'a', false)
            .exit("a")
            .maxRemnants(2)
            .intro("THE MACHINE HERE IS WEAK: TWO TRIPS AT MOST. THE LEDGE IS FIVE BLOCKS UP.")
            .hint("STAND ON A REMNANT AS IT JUMPS... AND LEAP WHEN IT REACHES THE TOP!")
            .par(1);
    }

    static Level l14() {
        return new Level(14, "COUNTERWEIGHT", 2,
            "####################",
            "#..............Z...#",
            "#...........q.....E#",
            "############.#######",
            "#..................#",
            "#..p...c....Q......#",
            "##..################",
            "#..................#",
            "#..................#",
            "#T.P.a.............#",
            "####################")
            .plate('a', 'a')
            .plate('c', 'c')
            .lift('P', 'p', 1, "a")
            .lift('Q', 'q', 1, "!a")
            .laser('Z', Level.DIR_DOWN, "!c")
            .exit("")
            .intro("TWO LIFTS ON ONE CHAIN. WHEN THE LEFT ONE RISES, THE RIGHT ONE SINKS.")
            .hint("LIFT B GOES UP WHEN PLATE A IS RELEASED. LET GO AT THE RIGHT MOMENT.")
            .par(2);
    }

    static Level l15() {
        return new Level(15, "THE HUNT", 2,
            "####################",
            "#..............#...#",
            "#..............#...#",
            "#..............#...#",
            "#..............#...#",
            "#..............#...#",
            "#..............#...#",
            "#.......b......#...#",
            "#......###.....D...#",
            "#.a..T.........D.E.#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .door('D', "ab")
            .exit("")
            .intro("THE FROST IS CRACKING. SOMEWHERE BEHIND THE GATE, SOMETHING IS WAITING FOR MILO.")
            .event(new ShadeEvent().onStart().at(17, 9)
                .say("S:I HAVE WARNED YOU AGAIN AND AGAIN.",
                    "M:I'M NOT SCARED OF YOU!",
                    "S:YOU SHOULD BE. NOT OF ME... OF WHAT COMES AFTER.",
                    "S:IF WORDS CAN'T STOP YOU, THEN I WILL.",
                    "#ACT",
                    "N:THE SHADE IS HUNTING MILO! HE WAKES A FEW SECONDS INTO EVERY LOOP. DON'T LET HIM TOUCH YOU!")
                .chase(17, 5, 180, 0.45f))
            .par(2);
    }

    // ================================================== ZONE 4: THE RUSTED YEARS

    static Level l16() {
        return new Level(16, "DEAD WEIGHT", 3,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#............##....#",
            "#.T...H.....###.E..#",
            "########^^##########")
            .heavy('H', 'a')
            .exit("a")
            .intro("RUST EATS THE WALLS HERE. AN IRON PLATE SITS IN THE FLOOR, FAR TOO HEAVY FOR ONE BOY.")
            .hint("HEAVY PLATES NEED TWO BODIES. TWO REMNANTS... OR A TOWER OF THEM.")
            .par(2);
    }

    static Level l17() {
        return new Level(17, "INVERSION", 3,
            "####################",
            "#......Z..#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........#........#",
            "#.........D........#",
            "#.........D........#",
            "#.T......aD....b..E#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .door('D', "a")
            .laser('Z', Level.DIR_DOWN, "a")
            .exit("b")
            .intro("THE RUSTED GEARS HERE TURN THE WRONG WAY. SO DO THE RULES.")
            .hint("PLATE A OPENS THE GATE BUT FIRES THE LASER. BE ON THE RIGHT SIDE OF IT.")
            .par(2);
    }

    static Level l18() {
        return new Level(18, "THE FLOOR GIVES WAY", 3,
            "####################",
            "#..................#",
            "#..................#",
            "#.################.#",
            "#.................=#",
            "#..................#",
            "#................==#",
            "#..................#",
            "#................==#",
            "#.T..........b.E...#",
            "####################")
            .plate('b', 'b')
            .exit("b")
            .intro("AN OLD STONE BRIDGE CROSSES THE VOID. A PASSAGE ABOVE LEADS BACK, BUT ONLY ONE WAY.")
            .hint("THE WAY BACK FROM THE FAR SIDE IS UP THE LEDGES AND ALONG THE TOP.")
            .event(new ShadeEvent().onLoop(1).at(9, 9)
                .say("S:YOU WALK THESE BRIDGES AS IF THEY WERE BUILT FOR YOU.",
                    "S:NOTHING IN THIS MAZE WAS BUILT FOR YOU, BOY.",
                    "#ACT",
                    "N:THE SHADE CRACKED THE BRIDGE. IT WILL ONLY HOLD FOR A MOMENT AFTER SOMEONE STEPS ON IT.",
                    "M:THEN MY ECHO AND I CROSS TOGETHER.")
                .setTile(7, 10, 'x').setTile(8, 10, 'x').setTile(9, 10, 'x').setTile(10, 10, 'x').setTile(11, 10, 'x')
                .restartLoop())
            .par(1);
    }

    static Level l19() {
        return new Level(19, "SKYWARD", 3,
            "####################",
            "#..................#",
            "#..................#",
            "#.........L........#",
            "#.......########...#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#.T..............E.#",
            "####################")
            .lever('L', 'a', false)
            .exit("a")
            .maxRemnants(2)
            .intro("SIX BLOCKS UP, AND ONLY TWO TRIPS IN THE MACHINE. A TOWER ALONE WON'T DO IT.")
            .hint("A TOWER... THAT JUMPS?")
            .par(2);
    }

    static Level l20() {
        return new Level(20, "RUST AND RUIN", 3,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#...........L......#",
            "#.........#####....#",
            "#..................#",
            "#.T......H.......E.#",
            "####################")
            .lever('L', 'a', false)
            .heavy('H', 'h')
            .exit("a")
            .intro("A LEVER ON A LEDGE, AN IRON PLATE BENEATH IT. THE AIR SMELLS OF RUST... AND OF SOMEONE WAITING.")
            .event(new ShadeEvent().onSignal('a', 1).at(15, 9)
                .say("S:A LEVER. HOW QUAINT.",
                    "M:NOT YOU AGAIN!",
                    "S:LET ME TEACH YOU SOMETHING ABOUT LOCKS.",
                    "#ACT",
                    "N:THE SHADE REWIRED THE EXIT AND SCATTERED MILO'S REMNANTS. NOW THE EXIT NEEDS THE LEVER AND THE HEAVY PLATE TOGETHER.",
                    "M:HE KEEPS CHANGING THE RULES. BUT EVERY RULE HAS AN ANSWER.")
                .rewire('E', "ah").clearRemnants().restartLoop())
            .par(2);
    }

    // ================================================== ZONE 5: THE ASHEN AGES

    static Level l21() {
        return new Level(21, "RIFTS", 4,
            "####################",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#.T.a....R..#b.r.E.#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .rift('R', 'r', "a")
            .exit("b")
            .intro("ASH DRIFTS THROUGH THE AIR. TEARS IN TIME FLICKER BETWEEN THE WALLS.")
            .hint("STEP INTO A RIFT TO COME OUT OF ITS TWIN. THIS ONE ONLY OPENS WHILE PLATE A IS HELD.")
            .hint("REMNANTS TRAVEL THROUGH RIFTS TOO, AND THEY NEED THEM OPEN TO COME HOME.")
            .par(2);
    }

    static Level l22() {
        return new Level(22, "RIFT RELAY", 4,
            "####################",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#.....#......#.....#",
            "#T.aR.#.r.bQ.#.q.E.#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .rift('R', 'r', "a")
            .rift('Q', 'q', "b")
            .exit("")
            .intro("THREE SEALED ROOMS AND TWO RIFTS. EACH ECHO OPENS THE WAY FOR THE NEXT.")
            .par(2);
    }

    static Level l23() {
        return new Level(23, "LIGHTS OUT", 4,
            "####################",
            "#.........Z.....#..#",
            "#...............#..#",
            "#...............#..#",
            "#...............#..#",
            "#...............#..#",
            "#...............#..#",
            "#...............D..#",
            "#...............D..#",
            "#T.a.b..........D.E#",
            "############^^######")
            .plate('a', 'a')
            .timer('b', 'b', 120)
            .laser('Z', Level.DIR_DOWN, "!a")
            .door('D', "b")
            .exit("")
            .intro("A LONG HALL OF LIGHT AND LOCKED GATES. FOR NOW, THE LAMPS STILL BURN.")
            .event(new ShadeEvent().onLoop(1).at(8, 9)
                .say("S:YOU SEE TOO MUCH, BOY.",
                    "S:LET'S FIND OUT HOW BRAVE YOU ARE IN THE DARK.",
                    "#ACT",
                    "N:THE SHADE SNUFFED OUT EVERY LAMP. ONLY MILO, HIS REMNANTS AND THE MACHINES STILL GLOW.",
                    "M:THEN MY ECHOES WILL LIGHT THE WAY.")
                .darken().restartLoop())
            .par(2);
    }

    static Level l24() {
        return new Level(24, "SINKING STONE", 4,
            "####################",
            "#..................#",
            "#..................#",
            "#...........L....E.#",
            "#.........##########",
            "#..................#",
            "#Q.T...............#",
            "#########x##########",
            "#..................#",
            "#....q.............#",
            "####################")
            .lever('L', 'a', false)
            .rift('Q', 'q', "")
            .exit("a")
            .intro("THE ONLY PLACE TO STAND BELOW THE LEDGE IS CRACKED. WHOEVER STANDS THERE WON'T STAND FOR LONG.")
            .hint("CRACKED FLOORS GIVE WAY A MOMENT AFTER SOMEONE STEPS ON THEM. BE QUICK!")
            .par(1);
    }

    static Level l25() {
        return new Level(25, "THE LONG LOOP", 4,
            "####################",
            "#........Z.....G...#",
            "#..p.c.........G..E#",
            "#...################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..P....T...a....b.#",
            "####################")
            .plate('a', 'a')
            .timer('b', 'b', 120)
            .plate('c', 'c')
            .lift('P', 'p', 1, "a")
            .laser('Z', Level.DIR_DOWN, "!c")
            .door('G', "b")
            .exit("")
            .intro("THREE LOCKS, THREE REMNANTS, ONE PERFECT LOOP. THE ASH FALLS LIKE SNOW HERE.")
            .par(3);
    }

    // ================================================= ZONE 6: THE HEART OF TIME

    static Level l26() {
        return new Level(26, "THE HUNT RETURNS", 5,
            "####################",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#........E.........#",
            "#.......####.......#",
            "#R.a..T.......r...b#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .rift('R', 'r', "")
            .exit("ab")
            .intro("THE HEART OF TIME BEATS SOMEWHERE ABOVE. BETWEEN ITS BEATS, SOMETHING IS HUNTING.")
            .event(new ShadeEvent().onStart().at(9, 7)
                .say("S:YOU ARE SO CLOSE NOW. TOO CLOSE.",
                    "M:YOU COULDN'T CATCH ME LAST TIME!",
                    "S:LAST TIME I WAS HOLDING BACK.",
                    "#ACT",
                    "N:THE SHADE IS HUNTING AGAIN, AND HE IS FASTER. THE RIFT MIGHT SHAKE HIM OFF.")
                .chase(9, 3, 210, 0.6f))
            .par(2);
    }

    static Level l27() {
        return new Level(27, "HEAVY HEART", 5,
            "####################",
            "#...............#..#",
            "#...............#..#",
            "#...............#..#",
            "#...............#..#",
            "#..........L....#..#",
            "#.........#####.#..#",
            "#...............D..#",
            "#...............D..#",
            "#.T......H......D.E#",
            "####################")
            .lever('L', 'a', false)
            .heavy('H', 'h')
            .door('D', "h")
            .exit("a")
            .intro("ONE TOWER, TWO JOBS. THE MAZE IS TESTING EVERYTHING MILO HAS LEARNED.")
            .par(2);
    }

    static Level l28() {
        return new Level(28, "ALL AT ONCE", 5,
            "####################",
            "#...........#......#",
            "#...........#......#",
            "#...........#.r..c.#",
            "#...........###x####",
            "#...........#...Z..#",
            "#...........#......#",
            "#...........#......#",
            "#...........#......#",
            "#.T..H...R..#....E.#",
            "####################")
            .heavy('H', 'h')
            .plate('c', 'c')
            .rift('R', 'r', "h")
            .laser('Z', Level.DIR_DOWN, "!c")
            .exit("")
            .intro("IRON, RIFTS, CRUMBLING STONE AND LIGHT THAT BURNS. EVERYTHING AT ONCE.")
            .par(3);
    }

    static Level l29() {
        return new Level(29, "THE TRUTH", 5,
            "####################",
            "#......#......#....#",
            "#......#......#....#",
            "#......#......#....#",
            "#......#......#....#",
            "#......#......F....#",
            "#......#......F..E.#",
            "#......D...#########",
            "#......D...........#",
            "#.T.L..D...........#",
            "####################")
            .lever('L', 'a', false)
            .door('D', "a")
            .door('F', "!a")
            .exit("")
            .intro("THE SHADE IS WAITING. THIS TIME, HE ISN'T HIDING.")
            .event(new ShadeEvent().onStart().at(5, 9)
                .say("S:SO. YOU MADE IT THIS FAR.",
                    "M:WHAT DO YOU WANT FROM ME? EVERY TIME I GET CLOSE, YOU...",
                    "S:I WANT YOU TO STOP, MILO.",
                    "M:...HOW DO YOU KNOW MY NAME?",
                    "#REVEAL",
                    "O:BECAUSE IT WAS MY NAME TOO.",
                    "M:YOU'RE... ME? BUT YOU'RE SO OLD...",
                    "O:SIXTY YEARS OLDER. I WALKED THESE HALLS ONCE, JUST LIKE YOU.",
                    "O:THE MAZE ISN'T A PRISON. IT'S A LOCK... AND WE ARE ITS KEY.",
                    "O:WHEN I OPENED THE LAST DOOR, TIME BROKE. CITIES FROZE MID-BREATH. PEOPLE SHATTERED INTO ECHOES.",
                    "O:I SPENT MY WHOLE LIFE FINDING A WAY BACK HERE. TO STOP YOU. TO STOP ME.",
                    "M:BUT THE MAZE WON'T LET ME OUT ANY OTHER WAY!",
                    "O:I KNOW. I'M SORRY.",
                    "#ACT",
                    "N:OLD MILO TORE THE CORE OUT OF THE TIME MACHINE. IT HAS ONLY TWO TRIPS LEFT.",
                    "O:IF YOU STILL CHOOSE TO GO ON... THEN BE CLEVER. I NEVER WAS.")
                .limit(2))
            .hint("ONE LEVER, TWO GATES, TWO TRIPS. PLAN EVERY PULL.")
            .par(2);
    }

    static Level l30() {
        return new Level(30, "THE HEART OF TIME", 5,
            "####################",
            "#.......Z..........#",
            "#......p..E........#",
            "#.......#####......#",
            "#..........#.......#",
            "#..........#.......#",
            "#..........#.....b.#",
            "#..........#...#####",
            "#..........H.......#",
            "#.T..a.P...H.......#",
            "####################")
            .plate('a', 'a')
            .plate('b', 'b')
            .lift('P', 'p', 1, "a")
            .door('H', "!a")
            .laser('Z', Level.DIR_DOWN, "!b")
            .exit("")
            .intro("THE LAST CHAMBER. BEYOND THAT DOOR BEATS THE HEART OF TIME... AND THE WAY HOME.")
            .hint("THE LIFT PLATE ALSO SEALS THE LOWER GATE. MIND THE ORDER OF YOUR LOOPS.")
            .par(3);
    }
}
