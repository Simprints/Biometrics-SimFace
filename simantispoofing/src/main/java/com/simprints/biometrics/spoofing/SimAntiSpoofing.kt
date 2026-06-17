package com.simprints.biometrics.spoofing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.biometrics.spoofing.impl.DeepFace
import com.simprints.biometrics.spoofing.impl.OnnxAntiSpoof
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SimAntiSpoofing(
    private val applicationContext: Context,
) {
    private val initLock = ReentrantLock()

    // TODO: Change the implementation here DeepFace/OnnxAntiSpoof
    private val implementation = OnnxAntiSpoof()

    init {
        initLock.withLock {
            implementation.initialize(applicationContext)
        }
    }

    fun calculateSpoofingScore(
        sourceBitmap: Bitmap,
        faceBoundingBox: Rect,
    ): Pair<Bitmap?, Float> = implementation.calculateSpoofingScore(sourceBitmap, faceBoundingBox)

    fun close() = initLock.withLock {
        implementation.close()
    }
}
