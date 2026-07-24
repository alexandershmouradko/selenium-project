#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./gradlew clean frameworkCheck build sourcesJar javadocJar publishToMavenLocal "$@"
./gradlew -p examples/consumer-smoke clean test "$@"

echo "Framework and standalone consumer validation completed successfully."
