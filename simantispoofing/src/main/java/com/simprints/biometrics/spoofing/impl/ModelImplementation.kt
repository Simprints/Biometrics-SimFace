package com.simprints.biometrics.spoofing.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect

interface ModelImplementation {
    fun initialize(context: Context)

    fun calculateSpoofingScore(
        sourceBitmap: Bitmap,
        faceBoundingBox: Rect,
    ): Pair<Bitmap?, Float>

    fun close()
}
