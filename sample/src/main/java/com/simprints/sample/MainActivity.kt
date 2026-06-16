package com.simprints.sample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.sample.ui.theme.SimFaceTesterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var simFace: SimFace

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SimFace
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(this))

        setContent {
            SimFaceTesterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimFaceDemo(
                            modifier = Modifier.padding(innerPadding),
                            onProcessImage = { imageRes -> processImage(imageRes) },
                            onProcessBitmap = { bitmap -> processImageFromBitmap(bitmap) },
                            onCompareImages = { result1, result2 ->
                                compareImages(result1, result2)
                            },
                            simFace = simFace,
                            context = this
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        simFace.release()
    }

    private suspend fun processImage(imageRes: Int): FaceResult =
            withContext(Dispatchers.IO) {
                try {
                    // Load bitmap from drawable resources
                    val bitmap: Bitmap = BitmapFactory.decodeResource(resources, imageRes)
                    processImageFromBitmap(bitmap)
                } catch (e: Exception) {
                    FaceResult(
                            bitmap = null,
                            success = false,
                            message = "Error: ${e.message}",
                            faces = emptyList(),
                            embedding = null
                    )
                }
            }

    private suspend fun processImageFromBitmap(bitmap: Bitmap): FaceResult =
            withContext(Dispatchers.IO) {
                try {
                    // Detect faces
                    val faces = simFace.detectFaceBlocking(bitmap)

                    if (faces.isEmpty()) {
                        return@withContext FaceResult(
                                bitmap = bitmap,
                                success = false,
                                message = "No faces detected",
                                faces = emptyList(),
                                embedding = null
                        )
                    }

                    // Get the first face
                    val face = faces[0]

                    // Extract embedding for matching
                    val embedding =
                            try {
                                val alignedFace = face.alignedFaceImage(bitmap)
                                simFace.getEmbedding(alignedFace)
                            } catch (e: Exception) {
                                null
                            }

                    val message = buildString {
                        appendLine("✅ Face detected!")
                        appendLine("Quality Score: ${"%.2f".format(face.quality)}")
                        appendLine("Number of faces: ${faces.size}")
                        appendLine("Bounding Box: ${face.absoluteBoundingBox}")
                        appendLine("Yaw: ${"%.1f".format(face.yaw)}°")
                        appendLine("Roll: ${"%.1f".format(face.roll)}°")

                        if (face.quality >= 0.6) {
                            appendLine("\n🎉 Quality is good!")
                        } else {
                            appendLine("\n⚠️ Quality could be better")
                        }
                    }

                    FaceResult(
                            bitmap = bitmap,
                            success = true,
                            message = message,
                            faces = faces,
                            embedding = embedding
                    )
                } catch (e: Exception) {
                    FaceResult(
                            bitmap = bitmap,
                            success = false,
                            message = "Error: ${e.message}",
                            faces = emptyList(),
                            embedding = null
                    )
                }
            }

    private suspend fun compareImages(result1: FaceResult?, result2: FaceResult?): String =
            withContext(Dispatchers.IO) {
                try {
                    if (result1 == null || result2 == null) {
                        return@withContext "⚠️ Please process both images first"
                    }

                    val embedding1 = result1.embedding
                    val embedding2 = result2.embedding

                    if (embedding1 == null || embedding2 == null) {
                        return@withContext "⚠️ Could not extract embeddings from one or both images"
                    }

                    val score = simFace.verificationScore(embedding1, embedding2)
                    val percentage = score * 100

                    buildString {
                        appendLine("🔍 Face Matching Results")
                        appendLine("━━━━━━━━━━━━━━━━━━━━")
                        appendLine("Match Score: ${"%.2f".format(score)}")
                        appendLine("Match Probability: ${"%.2f".format(percentage)}%")
                        appendLine()

                        when {
                            percentage >= 80 -> appendLine("✅ Strong Match - Likely same person")
                            percentage >= 60 ->
                                    appendLine("⚠️ Moderate Match - Possibly same person")
                            else -> appendLine("❌ No Match - Likely different persons")
                        }
                    }
                } catch (e: Exception) {
                    "❌ Error comparing images: ${e.message}"
                }
            }
}

data class FaceResult(
        val bitmap: Bitmap?,
        val success: Boolean,
        val message: String,
        val faces: List<FaceDetection>,
        val embedding: ByteArray? = null
)

@Composable
fun SimFaceDemo(
        modifier: Modifier = Modifier,
        onProcessImage: suspend (Int) -> FaceResult,
        onProcessBitmap: suspend (Bitmap) -> FaceResult,
        onCompareImages: suspend (FaceResult?, FaceResult?) -> String,
        simFace: SimFace,
        context: ComponentActivity
) {
    var result1 by remember { mutableStateOf<FaceResult?>(null) }
    var result2 by remember { mutableStateOf<FaceResult?>(null) }
    var result3 by remember { mutableStateOf<FaceResult?>(null) }
    var result4 by remember { mutableStateOf<FaceResult?>(null) }
    var capturedImage1 by remember { mutableStateOf<FaceResult?>(null) }
    var capturedImage2 by remember { mutableStateOf<FaceResult?>(null) }
    var comparisonResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var isComparing by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var cameraTarget by remember { mutableStateOf(1) } // 1 for image1, 2 for image2
    var hasCameraPermission by remember {
        mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
        )
    }
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher
    val permissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasCameraPermission = isGranted
                if (isGranted) {
                    showCamera = true
                } else {
                    Toast.makeText(
                                    context,
                                    "Camera permission is required to capture images",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            }

    // Helper function to check and request permission
    fun checkAndRequestCameraPermission(target: Int) {
        cameraTarget = target
        when {
            hasCameraPermission -> {
                showCamera = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    if (showCamera) {
        CameraPreviewScreen(
                simFace = simFace,
                isProcessing = isProcessing,
                onImageCaptured = { bitmap ->
                    isProcessing = true
                    coroutineScope.launch {
                        val result = onProcessBitmap(bitmap)
                        if (cameraTarget == 1) {
                            capturedImage1 = result
                        } else {
                            capturedImage2 = result
                        }
                        isProcessing = false
                        showCamera = false
                    }
                },
                onDismiss = { showCamera = false }
        )
        return // Don't show main UI when camera is active
    }

    Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "SimFace Test App", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Camera Capture Section
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
        ) {
            Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                        text = "📸 Camera Capture & Compare",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                            onClick = { checkAndRequestCameraPermission(1) },
                            enabled = !isProcessing && !isComparing,
                            modifier = Modifier.weight(1f)
                    ) { Text("Capture Face 1") }

                    Button(
                            onClick = { checkAndRequestCameraPermission(2) },
                            enabled = !isProcessing && !isComparing,
                            modifier = Modifier.weight(1f)
                    ) { Text("Capture Face 2") }
                }

                Button(
                        onClick = {
                            isComparing = true
                            coroutineScope.launch {
                                comparisonResult = onCompareImages(capturedImage1, capturedImage2)
                                isComparing = false
                            }
                        },
                        enabled =
                                !isProcessing &&
                                        !isComparing &&
                                        capturedImage1 != null &&
                                        capturedImage2 != null,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                ),
                        modifier = Modifier.fillMaxWidth()
                ) { Text("Compare Captured Faces") }
            }
        }

        // Test Images Section
        Text(text = "Test Images", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // Buttons to test different images
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                    onClick = {
                        isProcessing = true
                        comparisonResult = null
                        coroutineScope.launch {
                            result1 = onProcessImage(R.drawable.obama1)
                            isProcessing = false
                        }
                    },
                    enabled = !isProcessing && !isComparing
            ) { Text("Load Obama 1") }

            Button(
                    onClick = {
                        isProcessing = true
                        comparisonResult = null
                        coroutineScope.launch {
                            result2 = onProcessImage(R.drawable.obama2)
                            isProcessing = false
                        }
                    },
                    enabled = !isProcessing && !isComparing
            ) { Text("Load Obama 2") }
        }

        // Comparison buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                    onClick = {
                        isProcessing = true
                        comparisonResult = null
                        coroutineScope.launch {
                            result3 = onProcessImage(R.drawable.bush)
                            isProcessing = false
                        }
                    },
                    enabled = !isProcessing && !isComparing
            ) { Text("Load Bush 1") }

            Button(
                    onClick = {
                        isProcessing = true
                        comparisonResult = null
                        coroutineScope.launch {
                            result4 = onProcessImage(R.drawable.low_quality)
                            isProcessing = false
                        }
                    },
                    enabled = !isProcessing && !isComparing
            ) { Text("Load Low Quality") }
        }

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                    onClick = {
                        isComparing = true
                        coroutineScope.launch {
                            comparisonResult = onCompareImages(result1, result2)
                            isComparing = false
                        }
                    },
                    enabled = !isProcessing && !isComparing && result1 != null && result2 != null,
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Compare Obama with Obama") }

            Button(
                    onClick = {
                        isComparing = true
                        coroutineScope.launch {
                            comparisonResult = onCompareImages(result1, result3)
                            isComparing = false
                        }
                    },
                    enabled = !isProcessing && !isComparing && result1 != null && result3 != null,
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Compare Obama with Bush") }
        }

        if (isProcessing || isComparing) {
            CircularProgressIndicator()
            Text(
                    text = if (isProcessing) "Processing image..." else "Comparing faces...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Display comparison results
        comparisonResult?.let { comparison ->
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = comparison, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Display captured images
        capturedImage1?.let { res ->
            DisplayFaceResult(res, "Captured Face 1", MaterialTheme.colorScheme.tertiary)
        }

        capturedImage2?.let { res ->
            DisplayFaceResult(res, "Captured Face 2", MaterialTheme.colorScheme.tertiary)
        }

        // Display test image results
        result1?.let { res -> DisplayFaceResult(res, "Obama 1", MaterialTheme.colorScheme.primary) }

        result2?.let { res ->
            DisplayFaceResult(res, "Obama 2", MaterialTheme.colorScheme.secondary)
        }

        result3?.let { res -> DisplayFaceResult(res, "Bush 1", MaterialTheme.colorScheme.tertiary) }

        result4?.let { res ->
            DisplayFaceResult(res, "Low Quality", MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun DisplayFaceResult(
        result: FaceResult,
        title: String,
        titleColor: androidx.compose.ui.graphics.Color
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)

            result.bitmap?.let { bitmap ->
                Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Processed face $title",
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                )
            }

            Text(
                    text = result.message,
                    fontSize = 14.sp,
                    fontWeight = if (result.success) FontWeight.Normal else FontWeight.Bold,
                    color =
                            if (result.success) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.error
            )
        }
    }
}
