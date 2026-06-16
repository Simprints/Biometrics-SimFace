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
import com.simprints.sample.SimFaceDestination
import com.simprints.sample.ui.models.DemoTab
import com.simprints.sample.ui.models.SimFaceUiState

@Composable
fun SimFaceDemoScreen(
    modifier: Modifier = Modifier,
    uiState: SimFaceUiState,
    snackbarHostState: SnackbarHostState,
    onDetectFacesForPreview: suspend (Bitmap) -> List<FaceDetection>,
    onSelectTab: (DemoTab) -> Unit,
    onCaptureFace1: () -> Unit,
    onCaptureFace2: () -> Unit,
    onDismissCamera: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit,
    onLoadObama1: () -> Unit,
    onLoadObama2: () -> Unit,
    onLoadBush: () -> Unit,
    onLoadLowQuality: () -> Unit,
    onCompareCaptured: () -> Unit,
    onCompareObamaToObama: () -> Unit,
    onCompareObamaToBush: () -> Unit,
) {
    val isCameraRoute = uiState.backStack.lastOrNull() == SimFaceDestination.Camera
    BackHandler(enabled = isCameraRoute) { onDismissCamera() }

    if (isCameraRoute) {
        CameraPreviewScreen(
            onDetectFaces = onDetectFacesForPreview,
            isProcessing = uiState.isProcessing,
            onImageCaptured = onImageCaptured,
            onDismiss = onDismissCamera,
        )
        return
    }

    Column(modifier = modifier) {
        when (uiState.selectedTab) {
            DemoTab.CAMERA -> {
                SimFaceCameraDemoScreen(
                    modifier = Modifier.weight(1f),
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onCaptureFace1 = onCaptureFace1,
                    onCaptureFace2 = onCaptureFace2,
                    onCompareCaptured = onCompareCaptured,
                )
            }

            DemoTab.TEST_IMAGES -> {
                SimFaceTestImageDemoScreen(
                    modifier = Modifier.weight(1f),
                    uiState = uiState,
                    onLoadObama1 = onLoadObama1,
                    onLoadObama2 = onLoadObama2,
                    onLoadBush = onLoadBush,
                    onLoadLowQuality = onLoadLowQuality,
                    onCompareObamaToObama = onCompareObamaToObama,
                    onCompareObamaToBush = onCompareObamaToBush,
                )
            }
        }

        NavigationBar {
            NavigationBarItem(
                selected = uiState.selectedTab == DemoTab.CAMERA,
                onClick = { onSelectTab(DemoTab.CAMERA) },
                label = { Text("Camera") },
                icon = { Text("1") },
            )
            NavigationBarItem(
                selected = uiState.selectedTab == DemoTab.TEST_IMAGES,
                onClick = { onSelectTab(DemoTab.TEST_IMAGES) },
                label = { Text("Test Images") },
                icon = { Text("2") },
            )
        }
    }
}
