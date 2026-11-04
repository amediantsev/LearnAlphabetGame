#!/usr/bin/env bash
# Prints the version for a build as "<versionName> <versionCode>".
# Releases are numbered v0.1.0, v0.2.0, v0.3.0...: one per merge into main, never replaced.
# versionCode is the middle number, so every release is newer than the one before it
# (Android refuses to install an older versionCode over a newer one).
#
#   tools/next-version.sh          -> next release, e.g. "0.4.0 4"
#                                     (or the existing one if HEAD already has a release tag)
#   tools/next-version.sh pr 12    -> PR build, e.g. "0.4.0-pr12 4"
# Needs the tags: git fetch --tags (CI checks out with fetch-depth: 0).
set -euo pipefail

numbers() { git tag -l 'v0.*.0' | sed -nE 's/^v0\.([0-9]+)\.0$/\1/p'; }

if [ "${1:-}" != "pr" ]; then
  # A re-run on a commit that already has a release reuses its number instead of making a new one.
  here="$(git tag --points-at HEAD -l 'v0.*.0' | sed -nE 's/^v0\.([0-9]+)\.0$/\1/p' | sort -n | tail -1)"
  if [ -n "$here" ]; then
    echo "0.$here.0 $here"
    exit 0
  fi
fi

last="$(numbers | sort -n | tail -1)"
n=$(( ${last:-0} + 1 ))
if [ "${1:-}" = "pr" ]; then
  echo "0.$n.0-pr${2:?usage: tools/next-version.sh pr <number>} $n"
else
  echo "0.$n.0 $n"
fi
