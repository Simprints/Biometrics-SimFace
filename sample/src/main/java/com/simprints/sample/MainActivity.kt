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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simprints.sample.ui.models.CameraTarget
import com.simprints.sample.ui.models.SimFaceUiEffect
import com.simprints.sample.ui.screens.SimFaceDemoScreen
import com.simprints.sample.ui.theme.SimFaceTesterTheme

class MainActivity : ComponentActivity() {
    private val simFaceDemoViewModel: SimFaceDemoViewModel by
        viewModels { SimFaceDemoViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by simFaceDemoViewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(simFaceDemoViewModel) {
                simFaceDemoViewModel.uiEffects.collect { effect ->
                    when (effect) {
                        is SimFaceUiEffect.ComparisonError -> snackbarHostState.showSnackbar(effect.message)
                        is SimFaceUiEffect.ImageProcessingError -> {
                            val prefix =
                                when (effect.source) {
                                    SimFaceUiEffect.ImageSource.CAPTURE -> "Capture"
                                    SimFaceUiEffect.ImageSource.TEST_IMAGE -> "Image"
                                }
                            snackbarHostState.showSnackbar("$prefix error: ${effect.message}")
                        }
                    }
                }
            }

            SimFaceTesterTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    SimFaceDemoScreen(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState,
                        snackbarHostState = snackbarHostState,
                        onDetectFacesForPreview = simFaceDemoViewModel::detectFacesForPreview,
                        onCaptureFace1 = { simFaceDemoViewModel.openCamera(CameraTarget.FACE_1) },
                        onCaptureFace2 = { simFaceDemoViewModel.openCamera(CameraTarget.FACE_2) },
                        onDismissCamera = simFaceDemoViewModel::dismissCamera,
                        onImageCaptured = simFaceDemoViewModel::processCapturedBitmap,
                        onLoadObama1 = simFaceDemoViewModel::loadTestImage1,
                        onLoadObama2 = simFaceDemoViewModel::loadTestImage2,
                        onLoadBush = simFaceDemoViewModel::loadTestImage3,
                        onLoadLowQuality = simFaceDemoViewModel::loadTestImage4,
                        onCompareCaptured = simFaceDemoViewModel::compareCapturedFaces,
                        onCompareObamaToObama = simFaceDemoViewModel::compareObamaWithObama,
                        onCompareObamaToBush = simFaceDemoViewModel::compareObamaWithBush,
                    )
                }
            }
        }
    }
}
