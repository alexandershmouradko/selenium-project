#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

rm -rf dist
mkdir -p dist

for module in cf-*-utils; do
  mkdir -p "dist/$module"
  cp "$module"/build/libs/*.jar "dist/$module/"
done

(
  cd dist
  find . -type f -name '*.jar' -print0 | sort -z | xargs -0 sha256sum > SHA256SUMS
)

echo "Distribution artifacts collected under $ROOT_DIR/dist"
