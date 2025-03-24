package com.simprints.simface.matcher

interface MatchProcessor {
    fun verificationScore(probe: ByteArray, matchAgainst: ByteArray): Double
    fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>
    ): Map<ByteArray, Double>
}
