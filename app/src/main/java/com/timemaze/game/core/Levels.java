package com.timemaze.game.core;

/** The twenty chambers of the Time Maze. */
public final class Levels {
    private Levels() {}

    public static final int COUNT = 20;

    public static final String[] ZONE_NAMES = {
        "THE CLOCKWORK HALLS", "THE SUNKEN HOURS", "THE FROZEN SECONDS", "THE HEART OF TIME"
    };

    public static Level get(int n) {
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
            .timer('t', 'a', 120)
            .door('D', "a")
            .exit("")
            .intro("A CLOCKWORK BUTTON. PRESS IT AND THE GATE ABOVE OPENS... BUT ONLY FOR TWO SECONDS.")
            .hint("TIMED BUTTONS WORK ONCE PER PRESS. A REMNANT CAN WAIT BEFORE PRESSING IT.")
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
                "S:BECAUSE I KNOW WHERE THE LAST DOOR LEADS.",
                "S:AND I WILL BURY EVERY ROAD THAT TAKES YOU THERE.",
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
                .say("S:I WARNED YOU. TWICE.",
                    "M:I'M NOT SCARED OF YOU!",
                    "S:YOU SHOULD BE. NOT OF ME... OF WHAT COMES AFTER.",
                    "S:IF WORDS CAN'T STOP YOU, THEN I WILL.",
                    "#ACT",
                    "N:THE SHADE IS HUNTING MILO! HE WAKES A FEW SECONDS INTO EVERY LOOP. DON'T LET HIM TOUCH YOU!")
                .chase(17, 5, 180, 0.45f))
            .par(2);
    }

    // =================================================== ZONE 4: HEART OF TIME

    static Level l16() {
        return new Level(16, "INVERSION", 3,
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
            .intro("THE WALLS PULSE LIKE A HEARTBEAT. HERE, THE RULES RUN BACKWARDS.")
            .hint("PLATE A OPENS THE GATE BUT FIRES THE LASER. BE ON THE RIGHT SIDE OF IT.")
            .par(2);
    }

    static Level l17() {
        return new Level(17, "SKYWARD", 3,
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

    static Level l18() {
        return new Level(18, "THE LONG LOOP", 3,
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
            .intro("THREE LOCKS, THREE REMNANTS, ONE PERFECT LOOP. THE HEART IS CLOSE NOW.")
            .par(3);
    }

    static Level l19() {
        return new Level(19, "THE TRUTH", 3,
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

    static Level l20() {
        return new Level(20, "THE HEART OF TIME", 3,
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
