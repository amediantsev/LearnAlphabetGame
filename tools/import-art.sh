#!/usr/bin/env bash
# Converts art/<lang>/*.png (from ChatGPT) into small WebP files the app ships.
# Usage: tools/import-art.sh uk
set -euo pipefail

lang="${1:?usage: tools/import-art.sh <language code, e.g. uk>}"
root="$(cd "$(dirname "$0")/.." && pwd)"
src="$root/art/$lang"
dst="$root/app/src/main/assets/images/$lang"
data_name="$lang"
if [ "$lang" = "uk" ]; then data_name="Ukrainian"; fi
names_file="$root/app/src/main/java/app/learnalphabet/alphabet/$data_name.kt"

command -v cwebp >/dev/null || { echo "cwebp not found: brew install webp" >&2; exit 1; }
mkdir -p "$dst"
shopt -s nullglob nocaseglob
files=("$src"/*.png)
if [ ${#files[@]} -eq 0 ]; then
  echo "No PNG files in art/$lang/ — nothing to do."
  exit 0
fi

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

for f in "${files[@]}"; do
  base="$(basename "$f")"
  base="${base%.*}"
  if [ -f "$names_file" ] && ! grep -q "\"$base\"" "$names_file"; then
    echo "WARNING: $base.png does not match any picture name in $(basename "$names_file") — skipped" >&2
    continue
  fi
  sips -Z 512 -s format png "$f" --out "$tmp/$base.png" >/dev/null
  cwebp -quiet -q 82 "$tmp/$base.png" -o "$dst/$base.webp"
  echo "ok  $base.webp ($(( $(stat -f%z "$dst/$base.webp") / 1024 )) KB)"
done
