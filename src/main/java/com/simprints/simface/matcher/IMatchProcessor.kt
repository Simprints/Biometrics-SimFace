package com.simprints.simface.matcher

import android.graphics.Bitmap

interface IMatchProcessor {
    fun verificationScore(probe: ByteArray, matchAgainst: ByteArray): Float
    fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>
    ): Map<ByteArray, Float>
}