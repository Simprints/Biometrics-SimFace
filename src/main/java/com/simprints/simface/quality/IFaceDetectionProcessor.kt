package com.simprints.simface.quality

import android.graphics.Bitmap
import com.simprints.simface.core.SimFace

interface IFaceDetectionProcessor {
    fun detectFace(
        image: Bitmap,
        onSuccess: (List<SimFace>) -> Unit,
        onFailure: (Exception) -> Unit = {},
        onCompleted: () -> Unit = {}
    )

    suspend fun detectFaceBlocking(image: Bitmap): List<SimFace>
}