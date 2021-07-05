/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.internal

internal typealias PropParser<V> = (value: String) -> V?

internal typealias PropDefaulter<V> = () -> V

internal typealias PropFormatter<V> = (value: V) -> String
