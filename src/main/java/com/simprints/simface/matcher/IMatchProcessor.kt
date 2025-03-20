package com.simprints.simface.matcher

interface IMatchProcessor {
    fun verificationScore(probe: ByteArray, matchAgainst: ByteArray): Float
    fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>
    ): Map<ByteArray, Float>
}