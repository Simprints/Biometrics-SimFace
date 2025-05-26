package com.simprints.simface.data

import android.graphics.Rect

data class FaceDetection(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val absoluteBoundingBox: Rect,
    val yaw: Float,
    var roll: Float,
    val quality: Float,
    val landmarks: FacialLandmarks?,
)
