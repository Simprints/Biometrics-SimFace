package com.simprints.sample.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.sample.ui.models.DemoTab
import com.simprints.sample.ui.models.camera.SimFaceCameraActions
import com.simprints.sample.ui.models.camera.SimFaceCameraUiState
import com.simprints.sample.ui.models.images.SimFaceTestImageActions
import com.simprints.sample.ui.models.images.SimFaceTestImageUiState

@Composable
fun SimFaceDemoScreen(
    modifier: Modifier = Modifier,
    selectedTab: DemoTab,
    isCameraRoute: Boolean,
    cameraUiState: SimFaceCameraUiState,
    testImageUiState: SimFaceTestImageUiState,
    snackbarHostState: SnackbarHostState,
    onDetectFacesForPreview: suspend (Bitmap) -> List<FaceDetection>,
    onSelectTab: (DemoTab) -> Unit,
    onDismissCamera: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit,
    cameraActions: SimFaceCameraActions,
    testImageActions: SimFaceTestImageActions,
) {
    BackHandler(enabled = isCameraRoute) { onDismissCamera() }

    if (isCameraRoute) {
        CameraPreviewScreen(
            onDetectFaces = onDetectFacesForPreview,
            isProcessing = cameraUiState.isProcessing,
            onImageCaptured = onImageCaptured,
            onDismiss = onDismissCamera,
        )
        return
    }

    Column(modifier = modifier) {
        when (selectedTab) {
            DemoTab.CAMERA -> {
                SimFaceCameraDemoScreen(
                    modifier = Modifier.weight(1f),
                    uiState = cameraUiState,
                    actions = cameraActions,
                    snackbarHostState = snackbarHostState,
                )
            }

            DemoTab.TEST_IMAGES -> {
                SimFaceTestImageDemoScreen(
                    modifier = Modifier.weight(1f),
                    uiState = testImageUiState,
                    actions = testImageActions,
                )
            }
        }

        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == DemoTab.CAMERA,
                onClick = { onSelectTab(DemoTab.CAMERA) },
                label = { Text("Camera") },
                icon = { Text("1") },
            )
            NavigationBarItem(
                selected = selectedTab == DemoTab.TEST_IMAGES,
                onClick = { onSelectTab(DemoTab.TEST_IMAGES) },
                label = { Text("Test Images") },
                icon = { Text("2") },
            )
        }
    }
}
