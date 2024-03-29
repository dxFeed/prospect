#!/usr/bin/env bash

# Copyright (C) 2002 - 2021 Devexperts LLC
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

set -e

replace_line() {
  local TARGET_FILE="$1"
  local LINE_TO_REPLACE="$2"
  local REPLACEMENT="$3"

  local TMP_FILE="$1.tmp"

  awk -v p="${LINE_TO_REPLACE}" -v r="${REPLACEMENT}" \
    '$0==p {print r} $0!=p {print}' \
    "${TARGET_FILE}" > "${TMP_FILE}"

  mv "${TMP_FILE}" "${TARGET_FILE}"
}

insert_before_line() {
  local TARGET_FILE="$1"
  local INSERT_BEFORE_LINE="$2"
  local INSERTION="$3"

  local TMP_FILE="$1.tmp"

  awk -v l="${INSERTION}" -v p="${INSERT_BEFORE_LINE}" \
    '$0==p {print l; printf("\n\n"); print} $0!=p {print}' \
    "${TARGET_FILE}" > "${TMP_FILE}"

  mv "${TMP_FILE}" "${TARGET_FILE}"
}
