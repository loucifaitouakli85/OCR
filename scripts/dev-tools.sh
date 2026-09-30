#!/usr/bin/env bash
# Runs the desktop-only developer tools on a plain JVM (they use java.awt, which
# Android unit tests don't have, so they live in tools/ instead of app/src/test).
#
#   scripts/dev-tools.sh SolveAll [level]          play the bot solutions and print a report
#   scripts/dev-tools.sh GameShots <outDir>        render every screen to PNG
#   scripts/dev-tools.sh ReadmeShots docs          regenerate the README screenshots
#   scripts/dev-tools.sh IconGen app/src/main/res docs/icon.png   regenerate launcher icons
#
# Needs JUnit on the classpath for the test helpers: set JUNIT_JAR, or the script
# downloads junit 4.13.2 from Maven Central into build/tools-lib.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
[ $# -ge 1 ] || { sed -n '2,10p' "$0"; exit 1; }
TOOL="$1"
shift

LIB="$ROOT/build/tools-lib"
OUT="$ROOT/build/tools-classes"
mkdir -p "$LIB"
JUNIT_JAR="${JUNIT_JAR:-$LIB/junit-4.13.2.jar}"
if [ ! -f "$JUNIT_JAR" ]; then
  curl -sSfL -o "$JUNIT_JAR" https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
fi

rm -rf "$OUT"
mkdir -p "$OUT"
javac -nowarn -encoding UTF-8 -cp "$JUNIT_JAR" -d "$OUT" \
  app/src/main/java/com/timemaze/game/core/*.java \
  app/src/test/java/com/timemaze/game/core/*.java \
  tools/src/com/timemaze/game/core/*.java
java -cp "$OUT:$JUNIT_JAR" "com.timemaze.game.core.$TOOL" "$@"
