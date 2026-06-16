package com.simprints.sample.ui.models

import com.simprints.sample.SimFaceDestination
import com.simprints.sample.ui.models.camera.CameraTarget
import com.simprints.sample.ui.models.camera.SimFaceCameraUiState
import com.simprints.sample.ui.models.images.SimFaceTestImageUiState

data class SimFaceUiState(
    val selectedTab: DemoTab = DemoTab.CAMERA,
    val backStack: List<SimFaceDestination> = listOf(SimFaceDestination.Main),

    val cameraState: SimFaceCameraUiState = SimFaceCameraUiState(),
    val testImageState: SimFaceTestImageUiState = SimFaceTestImageUiState(),
)
