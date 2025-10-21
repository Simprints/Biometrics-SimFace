package com.simprints.simq

import android.graphics.Bitmap
import com.simprints.simq.shadows.ShadowCore
import com.simprints.simq.shadows.ShadowCvType
import com.simprints.simq.shadows.ShadowImgproc
import com.simprints.simq.shadows.ShadowMat
import com.simprints.simq.shadows.ShadowMatOfDouble
import com.simprints.simq.shadows.ShadowOpenCVLoader
import com.simprints.simq.shadows.ShadowOpenCVUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.opencv.android.OpenCVLoader as AndroidOpenCVLoader

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [
    ShadowOpenCVLoader::class,
    ShadowOpenCVUtils::class,
    ShadowMat::class,
    ShadowMatOfDouble::class,
    ShadowImgproc::class,
    ShadowCore::class,
    ShadowCvType::class
])

class SimQTest {
    
    private fun createTestBitmap(width: Int = 256, height: Int = 256): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
    
    @Test
    fun `calculateFaceQuality returns value between 0 and 1`() {
        val bitmap = createTestBitmap()
        val quality = SimQ.calculateFaceQuality(bitmap)
        
        assertTrue(quality >= 0.0f)
        assertTrue(quality <= 1.0f)
    }
    
    @Test
    fun `calculateFaceQuality with perfect alignment returns higher score`() {
        val bitmap = createTestBitmap()
        
        val perfectScore = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 0.0,
            yaw = 0.0,
            roll = 0.0
        )
        
        val poorAlignmentScore = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 20.0,
            yaw = 20.0,
            roll = 20.0
        )
        
        assertTrue(perfectScore >= poorAlignmentScore)
    }
    
    @Test
    fun `calculateFaceQuality with extreme angles returns low score`() {
        val bitmap = createTestBitmap()
        
        val quality = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 30.0,  // Exceeds maxIndividualAngle
            yaw = 0.0,
            roll = 0.0
        )
        
        assertTrue(quality < 0.5f)
    }
    
    @Test
    fun `calculateFaceQuality with eye openness includes it in calculation`() {
        val bitmap = createTestBitmap()
        
        // Test with eyes open
        val openEyesScore = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            leftEyeOpenness = 1.0,
            rightEyeOpenness = 1.0,
            weights = QualityWeights(
                alignment = 0.3,
                blur = 0.2,
                brightness = 0.2,
                contrast = 0.1,
                eyeOpenness = 0.2
            )
        )
        
        // Test with eyes closed
        val closedEyesScore = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            leftEyeOpenness = 0.0,
            rightEyeOpenness = 0.0,
            weights = QualityWeights(
                alignment = 0.3,
                blur = 0.2,
                brightness = 0.2,
                contrast = 0.1,
                eyeOpenness = 0.2
            )
        )
        
        // Open eyes should score higher when eye weight is significant
        assertTrue(openEyesScore >= closedEyesScore)
    }
    
    @Test
    fun `calculateFaceQuality without eye openness ignores eye weight`() {
        val bitmap = createTestBitmap()
        
        val qualityNoEyes = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            leftEyeOpenness = null,
            rightEyeOpenness = null,
            weights = QualityWeights(eyeOpenness = 0.2)
        )
        
        // Should still return valid score
        assertTrue(qualityNoEyes >= 0.0f)
        assertTrue(qualityNoEyes <= 1.0f)
    }

    @Test
    fun `calculateFaceQuality with only left eye openness provided`() {
        val bitmap = createTestBitmap()

        // Only one eye value - should not use eye openness
        val quality = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            leftEyeOpenness = 1.0,
            rightEyeOpenness = null,
            weights = QualityWeights(eyeOpenness = 0.2)
        )

        assertTrue(quality >= 0.0f && quality <= 1.0f)
    }
    
    @Test
    fun `calculateFaceQuality with custom weights affects result`() {
        val bitmap = createTestBitmap()
        
        // Weight alignment heavily
        val alignmentWeighted = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 20.0,
            yaw = 20.0,
            roll = 20.0,
            weights = QualityWeights(
                alignment = 0.9,
                blur = 0.025,
                brightness = 0.025,
                contrast = 0.025,
                eyeOpenness = 0.025
            )
        )
        
        // Weight other factors
        val otherWeighted = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 20.0,
            yaw = 20.0,
            roll = 20.0,
            weights = QualityWeights(
                alignment = 0.025,
                blur = 0.325,
                brightness = 0.325,
                contrast = 0.325,
                eyeOpenness = 0.0
            )
        )
        
        // With poor alignment, heavily weighting alignment should give lower score
        assertTrue(alignmentWeighted < otherWeighted)
    }
    
    @Test
    fun `calculateFaceQuality with custom parameters affects thresholds`() {
        val bitmap = createTestBitmap()
        
        // Strict parameters
        val strictQuality = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 15.0,
            yaw = 15.0,
            roll = 15.0,
            parameters = QualityParameters(
                maxAlignmentAngle = 10.0,
                maxIndividualAngle = 20.0
            )
        )
        
        // Lenient parameters
        val lenientQuality = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 15.0,
            yaw = 15.0,
            roll = 15.0,
            parameters = QualityParameters(
                maxAlignmentAngle = 30.0,
                maxIndividualAngle = 40.0
            )
        )
        
        // Same angles should score better with lenient parameters
        assertTrue(lenientQuality >= strictQuality)
    }

    
    @Test
    fun `calculateFaceQuality handles different bitmap sizes`() {
        val smallBitmap = createTestBitmap(64, 64)
        val mediumBitmap = createTestBitmap(256, 256)
        val largeBitmap = createTestBitmap(1024, 1024)
        
        val smallQuality = SimQ.calculateFaceQuality(smallBitmap)
        val mediumQuality = SimQ.calculateFaceQuality(mediumBitmap)
        val largeQuality = SimQ.calculateFaceQuality(largeBitmap)
        
        // All should return valid scores
        assertTrue(smallQuality >= 0.0f && smallQuality <= 1.0f)
        assertTrue(mediumQuality >= 0.0f && mediumQuality <= 1.0f)
        assertTrue(largeQuality >= 0.0f && largeQuality <= 1.0f)
    }
    
    @Test
    fun `calculateFaceQuality with default parameters returns reasonable score`() {
        val bitmap = createTestBitmap()
        
        val quality = SimQ.calculateFaceQuality(bitmap)

        assertTrue(quality >= 0.0f)
        assertTrue(quality <= 1.0f)
    }
    
    @Test
    fun `calculateFaceQuality is consistent with same inputs`() {
        val bitmap = createTestBitmap()
        
        val quality1 = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 10.0,
            yaw = 5.0,
            roll = -3.0
        )
        
        val quality2 = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 10.0,
            yaw = 5.0,
            roll = -3.0
        )
        
        assertEquals(quality1, quality2, 0.001f)
    }
    

    
    @Test
    fun `calculateFaceQuality with zero weights returns zero or handled gracefully`() {
        val bitmap = createTestBitmap()
        
        val quality = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            weights = QualityWeights(
                alignment = 0.0,
                blur = 0.0,
                brightness = 0.0,
                contrast = 0.0,
                eyeOpenness = 0.0
            )
        )
        
        assertTrue(quality >= 0.0f && quality <= 1.0f)
    }
}
