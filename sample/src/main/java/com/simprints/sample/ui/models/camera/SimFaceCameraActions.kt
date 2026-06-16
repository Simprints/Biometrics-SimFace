package com.simprints.sample.ui.models.camera

data class SimFaceCameraActions(
    val onCaptureFace1: () -> Unit,
    val onCaptureFace2: () -> Unit,
    val onCompareCaptured: () -> Unit,
)

