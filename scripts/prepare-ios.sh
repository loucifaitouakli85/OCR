#!/usr/bin/env bash
# Builds the web version and copies it into the iOS app bundle sources
# (ios/TimeMaze/www). Run this before building the iOS app in Xcode.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
"$ROOT/scripts/build-web.sh"
rm -rf "$ROOT/ios/TimeMaze/www"
cp -R "$ROOT/web/target/webapp" "$ROOT/ios/TimeMaze/www"
# the service worker only matters on https; the app loads files locally
rm -f "$ROOT/ios/TimeMaze/www/sw.js"
echo "Copied the game into ios/TimeMaze/www. Next: cd ios && xcodegen generate && open TimeMaze.xcodeproj"
