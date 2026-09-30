#!/usr/bin/env bash
# Builds a signed APK without Gradle, using the Android tools packaged by
# Debian/Ubuntu (apt install aapt apksigner zipalign dalvik-exchange
# android-sdk-platform-23). Handy on machines without the Android SDK.
#
# Output: dist/TimeMaze.apk
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

VERSION_CODE="${VERSION_CODE:-1}"
VERSION_NAME="${VERSION_NAME:-1.0}"
ANDROID_JAR="${ANDROID_JAR:-/usr/lib/android-sdk/platforms/android-23/android.jar}"
DX="${DX:-dalvik-exchange}"
BUILD="$ROOT/build/local"
OUT="$ROOT/dist/TimeMaze.apk"

for tool in aapt zipalign apksigner javac "$DX"; do
  command -v "$tool" >/dev/null || { echo "missing tool: $tool" >&2; exit 1; }
done
[ -f "$ANDROID_JAR" ] || { echo "missing $ANDROID_JAR" >&2; exit 1; }

rm -rf "$BUILD"
mkdir -p "$BUILD/gen" "$BUILD/classes" "$(dirname "$OUT")"

# 1. manifest: Gradle keeps package/version/sdk in build.gradle, so add them here
sed -e 's|<manifest xmlns:android="http://schemas.android.com/apk/res/android">|<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.timemaze.game" android:versionCode="'"$VERSION_CODE"'" android:versionName="'"$VERSION_NAME"'">\n    <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="34" />|' \
  app/src/main/AndroidManifest.xml > "$BUILD/AndroidManifest.xml"

# 2. resources
aapt package -f -m \
  -M "$BUILD/AndroidManifest.xml" \
  -S app/src/main/res \
  -I "$ANDROID_JAR" \
  -J "$BUILD/gen" \
  -F "$BUILD/unsigned.apk"

# 3. java -> class files (Java 8 bytecode, Android APIs only)
find app/src/main/java "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
javac -nowarn -Xlint:-options -source 1.8 -target 1.8 -encoding UTF-8 \
  -bootclasspath "$ANDROID_JAR" \
  -d "$BUILD/classes" @"$BUILD/sources.txt"

# 4. class files -> dex
"$DX" --dex --output="$BUILD/classes.dex" "$BUILD/classes"
(cd "$BUILD" && aapt add unsigned.apk classes.dex >/dev/null)

# 5. align and sign
zipalign -f 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"
apksigner sign \
  --ks keystore/timemaze-debug.jks --ks-key-alias timemaze \
  --ks-pass pass:timemaze --key-pass pass:timemaze \
  --min-sdk-version 21 --v4-signing-enabled false \
  --out "$OUT" "$BUILD/aligned.apk"
apksigner verify --verbose "$OUT" | head -5

echo "Built $OUT ($(du -h "$OUT" | cut -f1))"
