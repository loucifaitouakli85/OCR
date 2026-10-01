#!/usr/bin/env bash
# Builds the browser version (Java -> JavaScript with TeaVM) into web/target/webapp.
# Needs JDK 11+ and Maven. Serve it with any static file server, e.g.:
#   python3 -m http.server -d web/target/webapp 8000
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mvn -B -q -f "$ROOT/web/pom.xml" package
echo "Web build ready in web/target/webapp"
