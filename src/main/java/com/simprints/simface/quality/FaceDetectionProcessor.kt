package com.simprints.simface.quality

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.simface.core.SimFace

interface FaceDetectionProcessor {
    fun detectFace(
        image: Bitmap,
        onSuccess: (List<SimFace>) -> Unit,
        onFailure: (Exception) -> Unit = {},
        onCompleted: () -> Unit = {}
    )

    suspend fun detectFaceBlocking(image: Bitmap): List<SimFace>

    fun alignFace(bitmap: Bitmap, faceBoundingBox: Rect): Bitmap
}
