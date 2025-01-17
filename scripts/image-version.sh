#!/bin/bash

source scripts/image_vars.src

# Pins image versions to the latest release version
# if the image is not already pinned.
pinImageVersions(){
  echo "# $(date) -- $LAST_RELEASE_VERSION"
  for PREFIX in ${VAR_PREFIXES_ARR[@]}; do
    local IMG_VAR="${PREFIX}_VER"
    # If not (automatically or manually) pinned, then pin to latest release version
    if ! grep -q -w "${IMG_VAR}" scripts/image_versions.src; then
      local IMG_VER=$(getVarValue "${PREFIX}" _VER)
      >&2 echo "Pinning ${IMG_VAR}=\"$IMG_VER\""
      echo "${IMG_VAR}=\"$IMG_VER\""
    fi
  done
}

# Unpins auto-pinned image versions
unpinImageVersion(){
  local PREFIX=$1
  local IMG_VAR="${PREFIX}_VER"
  >&2 echo "Unpinning ${IMG_VAR}"
  sed "/^${IMG_VAR}=/d" scripts/image_versions.src
}

# Returns only versions that have been automatically pinned (by this script)
autoPinnedImages(){
  for PREFIX in ${VAR_PREFIXES_ARR[@]}; do
    if grep -q "^${PREFIX}_VER=" scripts/image_versions.src; then
      echo "${PREFIX}"
    fi
  done
}

# Returns only versions that have been automatically pinned (by this script)
# and have changed
changedAutoPinnedImages(){
  for PREFIX in $(autoPinnedImages); do
    >&2 echo "Found pinned image: ${PREFIX}"
    local IMG_DIFFS=$(comparePinnedImages)
    >&2 echo "$IMG_DIFFS" | jq
    if [ "$IMG_DIFFS" = "  Error" ]; then
      return 4
    elif ! isImageSame "$IMG_DIFFS"; then
      echo "${PREFIX}"
    fi
  done
}

# Returns JSON of image differences between locally created image
# and the pinned image version (which has a release tag)
comparePinnedImages(){
  local IMG_VER=$(getVarValue "${PREFIX}" _VER)
  # Release versions are tagged on non-dev images only (see secrel.yml) so no image `dev_` image prefix is needed
  local IMG_NAME=$(getVarValue "${PREFIX}" _IMG)
  local GHCR_PATH="ghcr.io/department-of-veterans-affairs/abd-vro-internal/${IMG_NAME}"
  local GRADLE_IMG_NAME=$(getVarValue "${PREFIX}" _GRADLE_IMG)
  >&2 echo "  Comparing local ${GRADLE_IMG_NAME} vs GHCR's ${IMG_NAME}:$IMG_VER"
  container-diff diff --type=history --type=size --json \
    "daemon://${GRADLE_IMG_NAME}" \
    "remote://${GHCR_PATH}:${IMG_VER}" || echo "  Error"
}

# Using container-diff, every tiny difference is detected.
# There are docker-build-time differences (e.g., installing packages and file timestamps)
# that cause two images that are practically the same to be different.
# This function tries to account for some trivial differences but
# an image size difference cannot be ignored.
isImageSame(){
  local IMG_DIFFS=$1
  local SIZE_DIFF_LEN=$(echo "${IMG_DIFFS}" | jq '.[] | select(.DiffType == "Size") | .Diff | length')
  # The 'grep -v " is used to ignore difference in the file identifier
  local HIST_DIFF_LEN=$(echo "${IMG_DIFFS}" | jq '.[] | select(.DiffType == "History")' | \
    grep -v "/bin/sh -c #(nop) COPY file:.* in fat.jar" | \
    jq '.Diff.Adds + .Diff.Dels | length')

  if [ "$SIZE_DIFF_LEN" = 0 ] && [ "$HIST_DIFF_LEN" = 0 ]; then
    >&2 echo "  Same"
    return 0
  else
    >&2 echo "  Different"
    return 1
  fi
}

## The functions above perform only read operations.
## File modifications are done below.

case "$1" in
  pin) pinImageVersions >> scripts/image_versions.src
    ;;
  unpinIfDiff)
    CHANGED_PINNED_IMAGES=$(changedAutoPinnedImages)
    if [ "$?" = 4 ]; then
      >&2 echo "Error comparing images, probably due to missing image.\
      Retry after secrel.yml workflow publishes release versions."
      exit 44
    fi
    for PREFIX in $CHANGED_PINNED_IMAGES; do
      unpinImageVersion "${PREFIX}" > unpinned_versions.src && \
        mv unpinned_versions.src scripts/image_versions.src
    done
    ;;
  "") echo "Usage:
  To pin versions of unpinned images:
    $0 pin
  To unpin versions of pinned images that have changed:
    $0 unpinIfDiff
"
    ;;
esac
