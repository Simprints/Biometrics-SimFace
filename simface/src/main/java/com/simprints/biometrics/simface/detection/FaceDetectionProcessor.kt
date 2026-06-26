package com.simprints.biometrics.simface.detection

import android.graphics.Bitmap
import com.simprints.biometrics.simface.data.FaceDetection

internal interface FaceDetectionProcessor {
    fun detectFace(
        image: Bitmap,
        onSuccess: (List<FaceDetection>) -> Unit,
        onFailure: (Exception) -> Unit = {},
        onCompleted: () -> Unit = {},
    )

    suspend fun detectFaceBlocking(image: Bitmap): List<FaceDetection>
}
