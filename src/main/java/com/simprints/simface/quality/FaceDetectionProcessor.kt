package com.simprints.simface.quality

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.simface.data.FaceDetection
import com.simprints.simface.data.FacialLandmarks

interface FaceDetectionProcessor {
    fun detectFace(
        image: Bitmap,
        onSuccess: (List<FaceDetection>) -> Unit,
        onFailure: (Exception) -> Unit = {},
        onCompleted: () -> Unit = {},
    )

    suspend fun detectFaceBlocking(image: Bitmap): List<FaceDetection>

    fun alignFace(
        bitmap: Bitmap,
        faceBoundingBox: Rect,
    ): Bitmap

    fun warpAlignFace(
        face: FacialLandmarks,
        inputImage: Bitmap,
    ): Bitmap?
}
