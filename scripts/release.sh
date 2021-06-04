#!/usr/bin/env bash

# Copyright (C) 2002 - 2021 Devexperts LLC
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

set -e

{ # Make sure script is always fully parsed

source scripts/utils.sh

# (required) Version under which the project release is performed
RELEASE_VERSION=${RELEASE_VERSION:?value expected}
# (optional) If set to "true" release changes are committed and pushed
COMMIT_AND_PUSH="${COMMIT_AND_PUSH}"

RELEASE_TAG="v${RELEASE_VERSION}"

echo "Releasing ${RELEASE_TAG}"

BUILD_DAY=$(date -u '+%Y-%m-%d')
CHANGELOG_FILE="CHANGELOG.md"
UNRELEASED_LINE="## Unreleased"
RELEASE_LINE="## v${RELEASE_VERSION} (${BUILD_DAY})"

./gradlew -q setVersion --newVersion="${RELEASE_VERSION}"
echo "Set release version to ${RELEASE_VERSION}"

echo "Updating changelog with release version"
# Replaces "unreleased" header with release version and date header in changelog
replace_line "${CHANGELOG_FILE}" "${UNRELEASED_LINE}" "${RELEASE_LINE}"

echo "Building and publishing artifacts"
./gradlew --stacktrace --info clean build publish

if [[ "${COMMIT_AND_PUSH}" == "true" ]]; then
    echo "Committing and pushing release changes"
    git add -u
    git commit --allow-empty -m "[BUILD] ${RELEASE_VERSION}"
    git tag "${RELEASE_TAG}"
    git push origin "${RELEASE_TAG}"
else
    echo "Skipped committing and pushing release changes"
fi

NEXT_SNAPSHOT_VERSION=$(./gradlew -q bumpVersion)
echo "Set next snapshot version to ${NEXT_SNAPSHOT_VERSION}"

echo "Updating changelog with unreleased header"
# Adds "unreleased" header before release version and date header in changelog
insert_before_line "${CHANGELOG_FILE}" "${RELEASE_LINE}" "${UNRELEASED_LINE}"

if [[ "${COMMIT_AND_PUSH}" == "true" ]]; then
    echo "Committing and pushing snapshot changes"
    git add -u
    git commit -m "[SNAPSHOT] ${NEXT_SNAPSHOT_VERSION}"
    git push origin HEAD
else
    echo "Skipped committing and pushing snapshot changes"
fi

echo "Done"

}
