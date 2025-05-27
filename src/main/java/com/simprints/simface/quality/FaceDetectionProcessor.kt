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

    /**
     * Crops the image according to the provided bounding box.
     *
     * NOTE: `cropAlignFace` and `warpAlignFace` are mutually exclusive operations.
     *
     * @param bitmap the original image
     * @param faceBoundingBox the bounding box of the face
     */
    fun cropAlignFace(
        bitmap: Bitmap,
        faceBoundingBox: Rect,
    ): Bitmap

    /**
     * Applies a warp transformation to the face image so that position of the landmarks corresponds to locations
     * expected by the face recognition model.
     *
     * In case of failure the whole pipeline returns the unchanged original image.
     *
     * NOTE: `cropAlignFace` and `warpAlignFace` are mutually exclusive operations.
     *
     * @param inputImage the original image
     * @param landmarks the facial landmarks obtained by MLKit
     */
    fun warpAlignFace(
        inputImage: Bitmap,
        landmarks: FacialLandmarks?,
    ): Bitmap
}
