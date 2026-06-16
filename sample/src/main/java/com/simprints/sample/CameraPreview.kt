package com.simprints.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.data.FaceDetection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class FaceDetectionResult(
    val faces: List<FaceDetection>,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotation: Int,
)

@Composable
fun CameraPreviewScreen(
    simFace: SimFace,
    onImageCaptured: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var faceDetectionResult by remember { mutableStateOf<FaceDetectionResult?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var previewWidth by remember { mutableStateOf(0f) }
    var previewHeight by remember { mutableStateOf(0f) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) { onDispose { cameraProviderFuture.get()?.unbindAll() } }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    previewView = this
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    post {
                        previewWidth = width.toFloat()
                        previewHeight = height.toFloat()
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                previewWidth = view.width.toFloat()
                previewHeight = view.height.toFloat()
            },
        )

        // Face detection overlay
        faceDetectionResult?.let { result ->
            if (previewWidth > 0 && previewHeight > 0) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    result.faces.forEach { face ->
                        val box = face.absoluteBoundingBox

                        // Calculate scale factors based on preview size vs image size
                        val scaleX = previewWidth / result.imageWidth.toFloat()
                        val scaleY = previewHeight / result.imageHeight.toFloat()

                        // Map coordinates directly
                        val left = box.left * scaleX
                        val top = box.top * scaleY
                        val width = box.width() * scaleX
                        val height = box.height() * scaleY

                        // Draw bounding box
                        val color =
                            when {
                                face.quality >= 0.7 -> Color.Green
                                face.quality >= 0.5 -> Color.Yellow
                                else -> Color.Red
                            }

                        drawRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 4.dp.toPx()),
                        )
                    }
                }
            }
        }

        // UI Overlay
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top info card
            Card(
                colors =
                    CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text =
                            if (faceDetectionResult?.faces?.isNotEmpty() == true) {
                                "✓ Face Detected"
                            } else {
                                "⚠ No Face Detected"
                            },
                        color =
                            if (faceDetectionResult?.faces?.isNotEmpty() == true) {
                                Color.Green
                            } else {
                                Color.Yellow
                            },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )

                    faceDetectionResult?.faces?.firstOrNull()?.let { face ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quality: ${"%.2f".format(face.quality)}",
                            color =
                                when {
                                    face.quality >= 0.7 -> Color.Green
                                    face.quality >= 0.5 -> Color.Yellow
                                    else -> Color.Red
                                },
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "Yaw: ${"%.1f".format(face.yaw)}°",
                            color = Color.White,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "Roll: ${"%.1f".format(face.roll)}°",
                            color = Color.White,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            // Bottom controls
            Box(modifier = Modifier.fillMaxWidth()) {
                // Cancel button on the far left
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    modifier = Modifier.align(Alignment.CenterStart),
                ) { Text("Cancel", color = Color.White) }

                // Camera button dead center
                Button(
                    onClick = {
                        if (!isCapturing && !isProcessing) {
                            isCapturing = true
                            imageCapture?.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        CoroutineScope(Dispatchers.Default).launch {
                                            try {
                                                val bitmap = imageProxyToBitmap(image)
                                                image.close()

                                                // Resize bitmap to 500px width for faster
                                                // processing
                                                val resizedBitmap =
                                                    resizeBitmap(bitmap, 500)

                                                withContext(Dispatchers.Main) {
                                                    onImageCaptured(resizedBitmap)
                                                    // Parent will handle the processing
                                                    // state
                                                }
                                            } catch (e: Exception) {
                                                Log.e(
                                                    "CameraPreview",
                                                    "Image processing failed",
                                                    e,
                                                )
                                                withContext(Dispatchers.Main) {
                                                    isCapturing = false
                                                }
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraPreview", "Capture failed", exception)
                                        isCapturing = false
                                    }
                                },
                            )
                        }
                    },
                    enabled =
                        !isCapturing &&
                            !isProcessing &&
                            faceDetectionResult?.faces?.isNotEmpty() == true,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF20b2d0),
                            disabledContainerColor = Color.Gray,
                        ),
                    modifier = Modifier.size(80.dp).align(Alignment.Center),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isCapturing || isProcessing) "⏳" else "📷",
                            fontSize = 32.sp,
                        )
                    }
                }
            }
        }

        // Processing overlay
        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.8f),
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(64.dp),
                        )
                        Text(
                            text = "Processing image...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val cameraProvider =
            suspendCoroutine<ProcessCameraProvider> { continuation ->
                cameraProviderFuture.addListener(
                    { continuation.resume(cameraProviderFuture.get()) },
                    ContextCompat.getMainExecutor(context),
                )
            }

        cameraProvider.unbindAll()

        val preview = Preview.Builder().build()

        val imageAnalyzer =
            ImageAnalysis
                .Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        imageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

        imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val bitmap = imageProxyToBitmap(imageProxy)
                    val resizedBitmap = resizeBitmap(bitmap, 500)
                    val faces = simFace.detectFaceBlocking(resizedBitmap)

                    withContext(Dispatchers.Main) {
                        faceDetectionResult =
                            FaceDetectionResult(
                                faces = faces,
                                imageWidth = resizedBitmap.width,
                                imageHeight = resizedBitmap.height,
                                rotation = imageProxy.imageInfo.rotationDegrees,
                            )
                    }
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Face detection error", e)
                } finally {
                    imageProxy.close()
                }
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture,
            )

            previewView?.let { preview.setSurfaceProvider(it.surfaceProvider) }
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed", e)
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val planes = image.planes

    var bitmap: Bitmap

    // Check if it's JPEG format (from capture) or YUV format (from analysis)
    if (planes.size == 1) {
        // JPEG format - captured image
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } else {
        // YUV format - analysis frame
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage =
            YuvImage(
                nv21,
                ImageFormat.NV21,
                image.width,
                image.height,
                null,
            )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)

        val imageBytes = out.toByteArray()
        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    // Rotate bitmap to correct orientation
    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())

    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    return bitmap
}

private fun resizeBitmap(
    bitmap: Bitmap,
    maxWidth: Int,
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    if (width <= maxWidth) {
        return bitmap
    }

    val aspectRatio = height.toFloat() / width.toFloat()
    val newWidth = maxWidth
    val newHeight = (newWidth * aspectRatio).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}
