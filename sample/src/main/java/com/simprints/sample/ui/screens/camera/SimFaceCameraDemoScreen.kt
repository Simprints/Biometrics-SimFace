package com.simprints.sample.ui.screens.camera

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simprints.sample.di.SimFaceCameraViewModelFactory
import com.simprints.sample.ui.composables.CameraCaptureSection
import com.simprints.sample.ui.composables.ComparisonResultCard
import com.simprints.sample.ui.composables.DisplayFaceResult
import kotlinx.coroutines.launch

@Composable
fun SimFaceCameraDemoScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as Application }
    val viewModel = remember(application) {
        ViewModelProvider(
            context as ComponentActivity,
            SimFaceCameraViewModelFactory(application),
        )[SimFaceCameraViewModel::class.java]
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCameraPreview by remember { mutableStateOf(false) }
    val snackbarScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.showSnackBarEffect.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = showCameraPreview) { showCameraPreview = false }

    if (showCameraPreview) {
        CameraPreviewScreen(
            modifier = modifier,
            onDetectFaces = viewModel::detectFacesForPreview,
            isProcessing = uiState.isProcessing,
            onImageCaptured = {
                viewModel.processCapturedBitmap(it)
                showCameraPreview = false
            },
            onDismiss = { showCameraPreview = false },
        )
        return
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            snackbarScope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to capture images")
            }
        }
    }

    fun checkAndRequestCameraPermission(onPermissionGranted: () -> Unit) {
        when {
            hasCameraPermission -> onPermissionGranted()
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(text = "SimFace Camera Demo", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        CameraCaptureSection(
            isBusy = uiState.isProcessing || uiState.isComparing,
            capturedImage1 = uiState.capturedImage1,
            capturedImage2 = uiState.capturedImage2,
            onCaptureFace1 = {
                checkAndRequestCameraPermission {
                    viewModel.setCameraTarget(CameraTarget.FACE_1)
                    showCameraPreview = true
                }
            },
            onCaptureFace2 = {
                checkAndRequestCameraPermission {
                    viewModel.setCameraTarget(CameraTarget.FACE_2)
                    showCameraPreview = true
                }
            },
            onCompareCaptured = viewModel::compareCapturedFaces,
        )

        if (uiState.isProcessing || uiState.isComparing) {
            CircularProgressIndicator()
            Text(
                text = if (uiState.isProcessing) "Processing image..." else "Comparing faces...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ComparisonResultCard(comparisonResult = uiState.comparisonResult)

        uiState.capturedImage1?.let { res ->
            DisplayFaceResult(res, "Captured Face 1", MaterialTheme.colorScheme.tertiary)
        }

        uiState.capturedImage2?.let { res ->
            DisplayFaceResult(res, "Captured Face 2", MaterialTheme.colorScheme.tertiary)
        }
    }
}
