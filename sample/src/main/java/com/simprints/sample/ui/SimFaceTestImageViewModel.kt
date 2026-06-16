package com.simprints.sample.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.sample.R
import com.simprints.sample.ui.models.FaceResult
import com.simprints.sample.ui.models.images.SimFaceTestImageUiState
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

class SimFaceTestImageViewModel(
    private val repository: SimFaceWrapper,
    private val imageLoader: SampleImageLoader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SimFaceTestImageUiState())
    val uiState: StateFlow<SimFaceTestImageUiState> = _uiState.asStateFlow()

    private val _showSnackBar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val showSnackBarEffect: SharedFlow<String> = _showSnackBar

    fun loadTestImage1() = loadTestImage(TestImageDemoSlot.OBAMA_1, R.drawable.obama1)

    fun loadTestImage2() = loadTestImage(TestImageDemoSlot.OBAMA_2, R.drawable.obama2)

    fun loadTestImage3() = loadTestImage(TestImageDemoSlot.BUSH, R.drawable.bush)

    fun loadTestImage4() = loadTestImage(TestImageDemoSlot.LOW_QUALITY, R.drawable.low_quality)

    fun compareObamaWithObama() {
        compareTestImages(uiState.value.result1, uiState.value.result2)
    }

    fun compareObamaWithBush() {
        compareTestImages(uiState.value.result1, uiState.value.result3)
    }

    private fun loadTestImage(
        slot: TestImageDemoSlot,
        imageRes: Int,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, comparisonResult = null) }
            val result = processImage(imageRes)
            _uiState.update {
                when (slot) {
                    TestImageDemoSlot.OBAMA_1 -> it.copy(result1 = result, isProcessing = false)
                    TestImageDemoSlot.OBAMA_2 -> it.copy(result2 = result, isProcessing = false)
                    TestImageDemoSlot.BUSH -> it.copy(result3 = result, isProcessing = false)
                    TestImageDemoSlot.LOW_QUALITY -> it.copy(result4 = result, isProcessing = false)
                }
            }
            if (!result.success) {
                _showSnackBar.tryEmit("Image error: ${result.message}")
            }
        }
    }

    private fun compareTestImages(
        result1: FaceResult?,
        result2: FaceResult?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isComparing = true) }
            val comparisonResult = compareImages(result1, result2)
            _uiState.update { it.copy(comparisonResult = comparisonResult, isComparing = false) }
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

private enum class TestImageDemoSlot {
    OBAMA_1,
    OBAMA_2,
    BUSH,
    LOW_QUALITY,
}
