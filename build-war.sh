#!/usr/bin/env bash
# Build the deployable WAR entirely inside a container — no JDK, no Maven, and
# no ~/.m2 on the host. Uses the `build` stage of the same multi-stage
# Dockerfile the image/deployment use, then streams the artifact out.
#
# The WAR lands **owned by you**, not root: podman writes to this script's
# stdout (a file your shell created via the `>` redirect), rather than creating
# the file itself. That matters here because podman runs rootful, so a
# `podman cp` would drop a root-owned file into your tree.
#
#   ./build-war.sh   ->   target/task-tracker.war   (owned by the invoking user)
set -euo pipefail
cd "$(dirname "$0")"

out=target/task-tracker.war

echo ">> building WAR inside a container (Dockerfile 'build' stage)..."
iid=$(podman build -q --target build .)

mkdir -p target
# Remove any previous (possibly root-owned) artifact so the redirect below can
# create a fresh file we own. The dir is ours, so this succeeds regardless of
# who owned the old file.
rm -f "$out"

# Stream the WAR to our stdout; the shell's '>' created $out as us, so the
# resulting file is user-owned even though podman itself runs as root.
podman run --rm "$iid" cat /src/target/task-tracker.war > "$out"

echo ">> wrote $out ($(id -un)-owned)"
