package com.simprints.simq

import android.graphics.Bitmap
import com.simprints.simq.utils.QualityUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QualityUtilsTest {

    @Test
    fun `rampScore returns 0 when below minimum`() {
        val score = QualityUtils.rampScore(x = 10.0, min = 20.0, max = 100.0)
        Assert.assertEquals(0.0, score, 0.001)
    }

    @Test
    fun `rampScore returns 1 when above maximum`() {
        val score = QualityUtils.rampScore(x = 150.0, min = 20.0, max = 100.0)
        Assert.assertEquals(1.0, score, 0.001)
    }

    @Test
    fun `rampScore returns 0_5 at midpoint`() {
        val score = QualityUtils.rampScore(x = 60.0, min = 20.0, max = 100.0)
        Assert.assertEquals(0.5, score, 0.001)
    }

    @Test
    fun `rampScore interpolates linearly`() {
        val score = QualityUtils.rampScore(x = 30.0, min = 20.0, max = 120.0)
        // (30 - 20) / (120 - 20) = 10/100 = 0.1
        Assert.assertEquals(0.1, score, 0.001)
    }

    @Test
    fun `plateauScore returns 1 in optimal range`() {
        val score1 = QualityUtils.plateauScore(
            x = 100.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertEquals(1.0, score1, 0.001)

        val score2 = QualityUtils.plateauScore(
            x = 80.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertEquals(1.0, score2, 0.001)

        val score3 = QualityUtils.plateauScore(
            x = 150.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertEquals(1.0, score3, 0.001)
    }

    @Test
    fun `plateauScore decreases outside optimal range`() {
        // Below center
        val scoreLow = QualityUtils.plateauScore(
            x = 50.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertTrue(scoreLow < 1.0)
        Assert.assertTrue(scoreLow > 0.0)

        // Above center
        val scoreHigh = QualityUtils.plateauScore(
            x = 170.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertTrue(scoreHigh < 1.0)
        Assert.assertTrue(scoreHigh > 0.0)
    }

    @Test
    fun `plateauScore is clamped between 0 and 1`() {
        val scoreVeryLow = QualityUtils.plateauScore(
            x = 0.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertTrue(scoreVeryLow >= 0.0 && scoreVeryLow <= 1.0)

        val scoreVeryHigh = QualityUtils.plateauScore(
            x = 300.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )
        Assert.assertTrue(scoreVeryHigh >= 0.0 && scoreVeryHigh <= 1.0)
    }

    @Test
    fun `plateauScore is symmetric around optimal range`() {
        val scoreLow = QualityUtils.plateauScore(
            x = 50.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )

        val scoreHigh = QualityUtils.plateauScore(
            x = 180.0,
            centerLow = 80.0,
            centerHigh = 150.0,
            edgeLow = 30.0,
            edgeHigh = 190.0,
            steepness = 0.3
        )

        // Should be approximately equal due to symmetry
        Assert.assertEquals(scoreLow, scoreHigh, 0.1)
    }

    @Test
    fun `centerCropBitmap with no displacement crops center`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val cropped = QualityUtils.centerCropBitmap(
            bitmap = bitmap,
            centerCrop = 0.5f,
            horizontalDisplacement = 0.0f,
            verticalDisplacement = 0.0f
        )

        Assert.assertEquals(50, cropped.width)
        Assert.assertEquals(50, cropped.height)
    }

    @Test
    fun `centerCropBitmap with full crop returns same size`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val cropped = QualityUtils.centerCropBitmap(
            bitmap = bitmap,
            centerCrop = 1.0f,
            horizontalDisplacement = 0.0f,
            verticalDisplacement = 0.0f
        )

        Assert.assertEquals(100, cropped.width)
        Assert.assertEquals(100, cropped.height)
    }

    @Test
    fun `resizeBitmap maintains aspect ratio for square bitmap`() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val resized = QualityUtils.resizeBitmap(bitmap, targetArea = 65536.0)

        Assert.assertEquals(256, resized.width)
        Assert.assertEquals(256, resized.height)
    }

    @Test
    fun `resizeBitmap maintains aspect ratio for rectangular bitmap`() {
        val bitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
        val resized = QualityUtils.resizeBitmap(bitmap, targetArea = 65536.0)

        val aspectRatio = resized.width.toDouble() / resized.height.toDouble()
        Assert.assertEquals(2.0, aspectRatio, 0.01)

        val area = resized.width * resized.height
        Assert.assertEquals(65536.0, area.toDouble(), 1000.0)
    }

    @Test
    fun `resizeBitmap scales down large bitmap`() {
        val bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val resized = QualityUtils.resizeBitmap(bitmap, targetArea = 65536.0)

        Assert.assertTrue(resized.width < bitmap.width)
        Assert.assertTrue(resized.height < bitmap.height)
    }

    @Test
    fun `resizeBitmap scales up small bitmap`() {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val resized = QualityUtils.resizeBitmap(bitmap, targetArea = 65536.0)

        Assert.assertTrue(resized.width > bitmap.width)
        Assert.assertTrue(resized.height > bitmap.height)
    }

    @Test
    fun `resizeBitmap handles different target areas`() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)

        val smallResize = QualityUtils.resizeBitmap(bitmap, targetArea = 16384.0)
        val largeResize = QualityUtils.resizeBitmap(bitmap, targetArea = 262144.0)

        Assert.assertTrue(smallResize.width < largeResize.width)
        Assert.assertTrue(smallResize.height < largeResize.height)
    }
}
