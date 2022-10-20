package io.hyleo.api

import io.hyleo.api.probability.Weight

open class Balanceable<T>(
    val weight: Weight,
    val occurrences: Map<T, Float>,
)