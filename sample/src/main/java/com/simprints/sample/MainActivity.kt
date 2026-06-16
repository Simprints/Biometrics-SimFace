package com.simprints.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simprints.sample.ui.SimFaceCameraViewModel
import com.simprints.sample.ui.SimFaceTestImageViewModel
import com.simprints.sample.ui.models.DemoTab
import com.simprints.sample.ui.models.camera.CameraTarget
import com.simprints.sample.ui.models.camera.SimFaceCameraActions
import com.simprints.sample.ui.models.images.SimFaceTestImageActions
import com.simprints.sample.ui.screens.SimFaceDemoScreen
import com.simprints.sample.ui.theme.SimFaceTesterTheme

class MainActivity : ComponentActivity() {
    private val cameraViewModel: SimFaceCameraViewModel by
        viewModels { SimFaceCameraViewModelFactory(application) }

    private val testImageViewModel: SimFaceTestImageViewModel by
        viewModels { SimFaceTestImageDemoViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val cameraUiState by cameraViewModel.uiState.collectAsStateWithLifecycle()
            val testImageUiState by testImageViewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            var selectedTab by remember { mutableStateOf(DemoTab.CAMERA) }
            val backStack = remember { mutableStateListOf<SimFaceDestination>(SimFaceDestination.Main) }

            LaunchedEffect(cameraViewModel) {
                cameraViewModel.showSnackBarEffect.collect { effect ->
                    snackbarHostState.showSnackbar(effect)
                }
            }

            LaunchedEffect(testImageViewModel) {
                testImageViewModel.showSnackBarEffect.collect { effect ->
                    snackbarHostState.showSnackbar(effect)
                }
            }

            val cameraActions = SimFaceCameraActions(
                onCaptureFace1 = {
                    cameraViewModel.setCameraTarget(CameraTarget.FACE_1)
                    backStack.add(SimFaceDestination.Camera)
                },
                onCaptureFace2 = {
                    cameraViewModel.setCameraTarget(CameraTarget.FACE_2)
                    backStack.add(SimFaceDestination.Camera)
                },
                onCompareCaptured = cameraViewModel::compareCapturedFaces,
            )
            val testImageActions = SimFaceTestImageActions(
                onLoadObama1 = testImageViewModel::loadTestImage1,
                onLoadObama2 = testImageViewModel::loadTestImage2,
                onLoadBush = testImageViewModel::loadTestImage3,
                onLoadLowQuality = testImageViewModel::loadTestImage4,
                onCompareObamaToObama = testImageViewModel::compareObamaWithObama,
                onCompareObamaToBush = testImageViewModel::compareObamaWithBush,
            )
            SimFaceTesterTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    SimFaceDemoScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it },
                        isCameraRoute = backStack.lastOrNull() == SimFaceDestination.Camera,
                        cameraUiState = cameraUiState,
                        testImageUiState = testImageUiState,
                        snackbarHostState = snackbarHostState,
                        onDetectFacesForPreview = cameraViewModel::detectFacesForPreview,
                        onDismissCamera = {
                            if (backStack.size > 1) {
                                backStack.removeLast()
                            }
                        },
                        onImageCaptured = {
                            cameraViewModel.processCapturedBitmap(it)
                            if (backStack.size > 1) {
                                backStack.removeLast()
                            }
                        },
                        cameraActions = cameraActions,
                        testImageActions = testImageActions,
                    )
                }
            }
        }
    }
}
