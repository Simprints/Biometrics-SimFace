package com.simprints.simface.quality

import android.graphics.Bitmap
import com.simprints.simface.data.FaceDetection

interface FaceDetectionProcessor {
    fun detectFace(
        image: Bitmap,
        onSuccess: (List<FaceDetection>) -> Unit,
        onFailure: (Exception) -> Unit = {},
        onCompleted: () -> Unit = {},
    )

    suspend fun detectFaceBlocking(image: Bitmap): List<FaceDetection>

}
