#!/usr/bin/env bash
# Installs PulpoModule.xcframework into ./Frameworks.
# The SDK is not public — ask PulpoAR for it, then run:
#
#   ./scripts/install-sdk.sh path/to/PulpoModule.xcframework   # or a folder / .zip that contains it
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$ROOT/Frameworks/PulpoModule.xcframework"
SOURCE="${1:-}"

if [[ -z "$SOURCE" || ! -e "$SOURCE" ]]; then
  echo "Usage: ./scripts/install-sdk.sh path/to/PulpoModule.xcframework" >&2
  echo "The SDK is not public. Ask PulpoAR for the PulpoModule.xcframework files." >&2
  exit 1
fi

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
if [[ "$SOURCE" == *.zip ]]; then
  unzip -q "$SOURCE" -d "$TMP"
  SOURCE="$TMP"
fi

FOUND="$(find "$SOURCE" -type d -name PulpoModule.xcframework -prune | head -n 1)"
if [[ -z "$FOUND" ]]; then
  echo "PulpoModule.xcframework not found in $1" >&2
  exit 1
fi

rm -rf "$DEST"
cp -R "$FOUND" "$DEST"
echo "Installed $DEST"
