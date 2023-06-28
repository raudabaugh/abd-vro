#!/bin/bash

source scripts/image_vars.src

pinImageVersions(){
  echo "# $(date)"
  for PREFIX in ${VAR_PREFIXES_ARR[@]}; do
    local IMG_VER=$(getVarValue "${PREFIX}" _VER)
    local IMG_VAR="${PREFIX}_VER"
    if ! grep -q "^${IMG_VAR}=" scripts/image_versions.src; then
      >&2 echo "Pinning ${IMG_VAR}=\"$IMG_VER\""
      echo "${IMG_VAR}=\"$IMG_VER\""
    fi
  done
}
unpinImageVersion(){
  local PREFIX=$1
  local IMG_VAR="${PREFIX}_VER"
  >&2 echo "Unpinning ${IMG_VAR}"
  sed "/^${IMG_VAR}=/d" scripts/image_versions.src
}
pinnedImages(){
  for PREFIX in ${VAR_PREFIXES_ARR[@]}; do
    local IMG_VAR="${PREFIX}_VER"
    if grep -q "^${IMG_VAR}=" scripts/image_versions.src; then
      echo "${PREFIX}"
    fi
  done
}
comparePinnedImages(){
  local IMG_VER=$(getVarValue "${PREFIX}" _VER)
  local IMG_NAME=$(getVarValue "${PREFIX}" _IMG)
  >&2 echo "  Comparing ${IMG_NAME}: $1 vs $IMG_VER"
  container-diff diff --type=history --type=size --json \
    "daemon://ghcr.io/department-of-veterans-affairs/abd-vro-internal/${IMG_NAME}:${1}" \
    "daemon://ghcr.io/department-of-veterans-affairs/abd-vro-internal/${IMG_NAME}:${IMG_VER}"
}
isImageSame(){
  local IMG_DIFFS=$1
  local SIZE_DIFF_LEN=$(echo "${IMG_DIFFS}" | jq '.[] | select(.DiffType == "Size") | .Diff | length')
  local HIST_DIFF_LEN=$(echo "${IMG_DIFFS}" | jq '.[] | select(.DiffType == "History") | .Diff.Adds + .Diff.Dels | length')

  # >&2 echo "  $SIZE_DIFF_LEN $HIST_DIFF_LEN"
  if [ "$SIZE_DIFF_LEN" = 0 ] && [ "$HIST_DIFF_LEN" = 0 ]; then
    >&2 echo "  Same"
    return 0
  else
    >&2 echo "  Different"
    return 1
  fi
}
changedPinnedImages(){
  local CURR_VERSION=$1
  for PREFIX in $(pinnedImages); do
    >&2 echo "Found pinned image: ${PREFIX}"
    local IMG_DIFFS=$(comparePinnedImages "${CURR_VERSION}")
    if ! isImageSame "$IMG_DIFFS"; then
      echo "${PREFIX}"
    fi
  done
}

## The functions above perform only read operations.
## File modifications are done below.

case "$1" in
  pin) pinImageVersions >> scripts/image_versions.src
    ;;
  unpinIfDiff)
    CURR_VERSION=$2
    for PREFIX in $(changedPinnedImages "${CURR_VERSION}"); do
      unpinImageVersion "${PREFIX}" > unpinned_versions.src && \
        mv unpinned_versions.src scripts/image_versions.src
    done
    ;;
  "") echo "Usage:
  To pin versions of unpinned images:
    $0 pin
  To unpin versions of pinned images that have changed:
    $0 unpinIfDiff <image_tag>
"
    ;;
esac