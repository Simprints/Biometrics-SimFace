package com.simprints.simface.data

data class FacialLandmarks(
    val eyeLeft: Point2D,
    val eyeRight: Point2D,
    val nose: Point2D,
    val mouthLeft: Point2D,
    val mouthRight: Point2D,
)
