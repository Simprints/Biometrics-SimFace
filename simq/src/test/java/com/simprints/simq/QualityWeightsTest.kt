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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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

class QualityWeightsTest {

    private fun createTestBitmap(width: Int = 256, height: Int = 256): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
    
    @Test
    fun `default weights sum to 1_0`() {
        val weights = QualityWeights.DEFAULT
        val sum = weights.alignment + weights.blur + weights.brightness + 
                  weights.contrast + weights.eyeOpenness
        assertEquals(1.0, sum, 0.001)
    }
    
    @Test
    fun `custom weights can be created`() {
        val customWeights = QualityWeights(
            alignment = 0.25,
            blur = 0.25,
            brightness = 0.25,
            contrast = 0.15,
            eyeOpenness = 0.10
        )
        
        assertEquals(0.25, customWeights.alignment, 0.001)
        assertEquals(0.25, customWeights.blur, 0.001)
        assertEquals(0.25, customWeights.brightness, 0.001)
        assertEquals(0.15, customWeights.contrast, 0.001)
        assertEquals(0.10, customWeights.eyeOpenness, 0.001)
    }

    @Test
    fun `calculateFaceQuality with all weights maximizing one metric`() {
        val bitmap = createTestBitmap()

        // Only alignment weight
        val alignmentOnly = SimQ.calculateFaceQuality(
            bitmap = bitmap,
            pitch = 0.0,
            yaw = 0.0,
            roll = 0.0,
            weights = QualityWeights(
                alignment = 1.0,
                blur = 0.0,
                brightness = 0.0,
                contrast = 0.0,
                eyeOpenness = 0.0
            )
        )

        assertEquals(alignmentOnly, 1f)
    }
}
