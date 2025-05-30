package com.simprints.biometrics.simface.matcher

internal interface MatchProcessor {
    fun verificationScore(
        probe: ByteArray,
        matchAgainst: ByteArray,
    ): Double

    fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>,
    ): List<Pair<ByteArray, Double>>
}
