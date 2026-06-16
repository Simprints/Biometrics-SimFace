package com.simprints.sample

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.sample.ui.models.DemoTab
import com.simprints.sample.ui.models.FaceResult
import com.simprints.sample.ui.models.SimFaceUiState
import com.simprints.sample.ui.models.camera.CameraTarget
import com.simprints.sample.wrappers.SampleImageLoader
import com.simprints.sample.wrappers.SimFaceWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SimFaceDemoViewModel(
    private val repository: SimFaceWrapper,
    private val imageLoader: SampleImageLoader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SimFaceUiState())
    val uiState: StateFlow<SimFaceUiState> = _uiState.asStateFlow()

    private val _showSnackBar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val showSnackBarEffect: SharedFlow<String> = _showSnackBar

    fun openCamera(target: CameraTarget) {
        _uiState.update {
            it.copy(
                cameraState = it.cameraState.copy(cameraTarget = target),
                backStack = it.backStack + SimFaceDestination.Camera,
            )
        }
    }

    fun dismissCamera() {
        _uiState.update {
            if (it.backStack.size <= 1) {
                it
            } else {
                it.copy(backStack = it.backStack.dropLast(1))
            }
        }
    }

    fun selectTab(tab: DemoTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun processCapturedBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(cameraState = it.cameraState.copy(isProcessing = true)) }
            val result = processImageFromBitmap(bitmap)
            _uiState.update {
                when (it.cameraState.cameraTarget) {
                    CameraTarget.FACE_1 ->
                        it.copy(
                            cameraState =
                                it.cameraState.copy(
                                    capturedImage1 = result,
                                    isProcessing = false,
                                ),
                        )
                    CameraTarget.FACE_2 ->
                        it.copy(
                            cameraState =
                                it.cameraState.copy(
                                    capturedImage2 = result,
                                    isProcessing = false,
                                ),
                        )
                }
            }
            dismissCamera()
            if (!result.success) {
                _showSnackBar.tryEmit("Capture error: ${result.message}")
            }
        }
    }

    fun loadTestImage1() = loadTestImage(TestImageSlot.OBAMA_1, R.drawable.obama1)

    fun loadTestImage2() = loadTestImage(TestImageSlot.OBAMA_2, R.drawable.obama2)

    fun loadTestImage3() = loadTestImage(TestImageSlot.BUSH, R.drawable.bush)

    fun loadTestImage4() = loadTestImage(TestImageSlot.LOW_QUALITY, R.drawable.low_quality)

    fun compareCapturedFaces() {
        compareCameraFaces(uiState.value.cameraState.capturedImage1, uiState.value.cameraState.capturedImage2)
    }

    fun compareObamaWithObama() {
        compareTestImages(uiState.value.testImageState.result1, uiState.value.testImageState.result2)
    }

    fun compareObamaWithBush() {
        compareTestImages(uiState.value.testImageState.result1, uiState.value.testImageState.result3)
    }

    suspend fun detectFacesForPreview(bitmap: Bitmap): List<FaceDetection> = repository.detectFaces(bitmap)

    private fun loadTestImage(
        slot: TestImageSlot,
        imageRes: Int,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    testImageState =
                        it.testImageState.copy(
                            isProcessing = true,
                            comparisonResult = null,
                        ),
                )
            }
            val result = processImage(imageRes)
            _uiState.update {
                when (slot) {
                    TestImageSlot.OBAMA_1 ->
                        it.copy(
                            testImageState =
                                it.testImageState.copy(
                                    result1 = result,
                                    isProcessing = false,
                                ),
                        )
                    TestImageSlot.OBAMA_2 ->
                        it.copy(
                            testImageState =
                                it.testImageState.copy(
                                    result2 = result,
                                    isProcessing = false,
                                ),
                        )
                    TestImageSlot.BUSH ->
                        it.copy(
                            testImageState =
                                it.testImageState.copy(
                                    result3 = result,
                                    isProcessing = false,
                                ),
                        )
                    TestImageSlot.LOW_QUALITY ->
                        it.copy(
                            testImageState =
                                it.testImageState.copy(
                                    result4 = result,
                                    isProcessing = false,
                                ),
                        )
                }
            }
            if (!result.success) {
                _showSnackBar.tryEmit("Image error: ${result.message}")
            }
        }
    }

    private fun compareCameraFaces(
        result1: FaceResult?,
        result2: FaceResult?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(cameraState = it.cameraState.copy(isComparing = true)) }
            val comparisonResult = compareImages(result1, result2)
            _uiState.update {
                it.copy(
                    cameraState = it.cameraState.copy(
                        comparisonResult = comparisonResult,
                        isComparing = false,
                    ),
                )
            }
            if (comparisonResult.startsWith("⚠") || comparisonResult.startsWith("❌")) {
                _showSnackBar.tryEmit(comparisonResult)
            }
        }
    }

    private fun compareTestImages(
        result1: FaceResult?,
        result2: FaceResult?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(testImageState = it.testImageState.copy(isComparing = true)) }
            val comparisonResult = compareImages(result1, result2)
            _uiState.update {
                it.copy(
                    testImageState = it.testImageState.copy(
                        comparisonResult = comparisonResult,
                        isComparing = false,
                    ),
                )
            }
            if (comparisonResult.startsWith("⚠") || comparisonResult.startsWith("❌")) {
                _showSnackBar.tryEmit(comparisonResult)
            }
        }
    }

    private suspend fun processImage(imageRes: Int): FaceResult = withContext(ioDispatcher) {
        try {
            val bitmap = imageLoader.load(imageRes) ?: return@withContext FaceResult(
                bitmap = null,
                success = false,
                message = "Could not decode test image",
                faces = emptyList(),
                embedding = null,
            )
            processImageFromBitmap(bitmap)
        } catch (e: Exception) {
            FaceResult(
                bitmap = null,
                success = false,
                message = "Error: ${e.message}",
                faces = emptyList(),
                embedding = null,
            )
        }
    }

    private suspend fun processImageFromBitmap(bitmap: Bitmap): FaceResult = withContext(ioDispatcher) {
        try {
            val faces = repository.detectFaces(bitmap)
            if (faces.isEmpty()) {
                return@withContext FaceResult(
                    bitmap = bitmap,
                    success = false,
                    message = "No faces detected",
                    faces = emptyList(),
                    embedding = null,
                )
            }

            val face = faces[0]
            val embedding = try {
                repository.getEmbedding(face, bitmap)
            } catch (_: Exception) {
                null
            }

            val message = buildString {
                appendLine("✅ Face detected!")
                appendLine("Quality Score: ${"%.2f".format(Locale.US, face.quality)}")
                appendLine("Number of faces: ${faces.size}")
                appendLine("Bounding Box: ${face.absoluteBoundingBox}")
                appendLine("Yaw: ${"%.1f".format(Locale.US, face.yaw)}°")
                appendLine("Roll: ${"%.1f".format(Locale.US, face.roll)}°")

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
                embedding = embedding,
            )
        } catch (e: Exception) {
            FaceResult(
                bitmap = bitmap,
                success = false,
                message = "Error: ${e.message}",
                faces = emptyList(),
                embedding = null,
            )
        }
    }

    private suspend fun compareImages(
        result1: FaceResult?,
        result2: FaceResult?,
    ): String = withContext(ioDispatcher) {
        try {
            if (result1 == null || result2 == null) {
                return@withContext "⚠️ Please process both images first"
            }

            val embedding1 = result1.embedding
            val embedding2 = result2.embedding

            if (embedding1 == null || embedding2 == null) {
                return@withContext "⚠️ Could not extract embeddings from one or both images"
            }

            val score = repository.verificationScore(embedding1, embedding2)
            val percentage = score * 100

            buildString {
                appendLine("🔍 Face Matching Results")
                appendLine("━━━━━━━━━━━━━━━━━━━━")
                appendLine("Match Score: ${"%.2f".format(Locale.US, score)}")
                appendLine("Match Probability: ${"%.2f".format(Locale.US, percentage)}%")
                appendLine()

                when {
                    percentage >= 80 -> appendLine("✅ Strong Match - Likely same person")
                    percentage >= 60 -> appendLine("⚠️ Moderate Match - Possibly same person")
                    else -> appendLine("❌ No Match - Likely different persons")
                }
            }
        } catch (e: Exception) {
            "❌ Error comparing images: ${e.message}"
        }
    }

    override fun onCleared() {
        repository.release()
        super.onCleared()
    }
}

private enum class TestImageSlot {
    OBAMA_1,
    OBAMA_2,
    BUSH,
    LOW_QUALITY,
}
