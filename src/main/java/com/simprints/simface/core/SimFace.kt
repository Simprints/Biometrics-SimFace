package com.simprints.simface.core

import android.graphics.Rect

data class Point2D(val x: Float, val y: Float)

data class FacialLandmarks(
    val eyeLeft: Point2D,
    val eyeRight: Point2D,
    val nose: Point2D,
    val mouthLeft: Point2D,
    val mouthRight: Point2D
)

data class SimFace(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val absoluteBoundingBox: Rect,
    val yaw: Float,
    var roll: Float,
    val quality: Float,
    val landmarks: FacialLandmarks?,
)