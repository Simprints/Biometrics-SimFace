package com.simprints.simq.utils

import kotlin.math.exp

object ScoringFunctions {
    /**
     * Ramp scoring function: linear interpolation between min and max.
     *
     * @param x The input value to score
     * @param min Minimum threshold (scores 0.0 below this)
     * @param max Maximum threshold (scores 1.0 above this)
     * @return Score between 0.0 and 1.0
     */
    fun rampScore(
        x: Double,
        min: Double,
        max: Double,
    ): Double = when {
        x < min -> 0.0
        x > max -> 1.0
        else -> (x - min) / (max - min)
    }

    /**
     * Plateau scoring function: optimal range with smooth sigmoid falloff.
     *
     * @param x The input value to score
     * @param centerLow Lower bound of optimal range
     * @param centerHigh Upper bound of optimal range
     * @param edgeLow Lower edge threshold
     * @param edgeHigh Upper edge threshold
     * @param steepness Steepness of sigmoid falloff
     * @return Score between 0.0 and 1.0
     */
    fun plateauScore(
        x: Double,
        centerLow: Double,
        centerHigh: Double,
        edgeLow: Double,
        edgeHigh: Double,
        steepness: Double,
    ): Double = when {
        x in centerLow..centerHigh -> 1.0
        x < centerLow -> 1.0 / (1.0 + exp(-steepness * (x - edgeLow)))
        else -> 1.0 / (1.0 + exp(steepness * (x - edgeHigh)))
    }.coerceIn(0.0, 1.0)
}
