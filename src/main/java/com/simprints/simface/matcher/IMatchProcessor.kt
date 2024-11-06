package com.simprints.simface.matcher

import android.graphics.Bitmap

interface IMatchProcessor {
    fun matchScore(probe: FloatArray, matchAgainst: FloatArray): Float
    fun identificationScore(probe: FloatArray, matchReferences: List<FloatArray>): Map<FloatArray, Float>
}