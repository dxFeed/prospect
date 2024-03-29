/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.ext

import com.dxfeed.prospect.PropBuilder
import com.dxfeed.prospect.check

public fun <V : Number> PropBuilder<V>.checkPositive(): PropBuilder<V> =
    check("expected a positive number") { it.toDouble() > 0 }

public fun <V : Number> PropBuilder<V>.checkNotNegative(): PropBuilder<V> =
    check("expected a non-negative number") { it.toDouble() >= 0 }

public fun PropBuilder<Int>.checkInRange(range: IntRange): PropBuilder<Int> =
    check({ "expected value to be in range $range" }) { it in range }

public fun PropBuilder<Long>.checkInRange(range: LongRange): PropBuilder<Long> =
    check({ "expected value to be in range $range" }) { it in range }

public fun <V : CharSequence> PropBuilder<V>.checkMatches(regex: Regex): PropBuilder<V> =
    check({ "expected value to match regex $regex" }) { it.matches(regex) }

@JvmName("checkNotEmptyCollection")
public fun <T, V : Collection<T>> PropBuilder<V>.checkNotEmpty(): PropBuilder<V> =
    check("expected a non-empty collection") { it.isNotEmpty() }
