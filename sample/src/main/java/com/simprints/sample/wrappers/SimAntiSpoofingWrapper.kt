package com.simprints.sample.wrappers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.biometrics.spoofing.SimAntiSpoofing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.measureTimedValue

class SimAntiSpoofingWrapper(
    context: Context,
) {
    private val simSpoofing = SimAntiSpoofing(context)

    suspend fun isSpoofed(
        face: FaceDetection,
        sourceBitmap: Bitmap,
    ): Pair<Bitmap?, Float> = withContext(Dispatchers.IO) {
        val result = measureTimedValue {
            simSpoofing.calculateSpoofingScore(sourceBitmap, face.absoluteBoundingBox)
        }
        Log.wtf("!!!!!!!!!", "Timing ms: ${result.duration.inWholeMilliseconds}")

        result.value
    }

    fun release() {
        simSpoofing.close()
    }
}
