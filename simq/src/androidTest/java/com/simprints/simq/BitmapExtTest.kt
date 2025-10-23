package com.simprints.simq

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.simq.utils.centerCrop
import com.simprints.simq.utils.resizeToArea
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapExtTest {
    @Test
    fun centerCropBitmapWithNoDisplacementCropsCenter() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val cropped =
            bitmap.centerCrop(
                centerCrop = 0.5f,
                horizontalDisplacement = 0.0f,
                verticalDisplacement = 0.0f,
            )

        assertThat(cropped.width).isEqualTo(50)
        assertThat(cropped.height).isEqualTo(50)
    }

    @Test
    fun centerCropBitmapWithFullCropReturnsSameSize() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val cropped =
            bitmap.centerCrop(
                centerCrop = 1.0f,
                horizontalDisplacement = 0.0f,
                verticalDisplacement = 0.0f,
            )

        assertThat(cropped.width).isEqualTo(100)
        assertThat(cropped.height).isEqualTo(100)
    }

    @Test
    fun resizeBitmapMaintainsAspectRatioForSquareBitmap() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val resized = bitmap.resizeToArea(targetArea = 65536.0)

        assertThat(resized.width).isEqualTo(256)
        assertThat(resized.height).isEqualTo(256)
    }

    @Test
    fun resizeBitmapMaintainsAspectRatioForRectangularBitmap() {
        val bitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
        val resized = bitmap.resizeToArea(targetArea = 65536.0)

        val aspectRatio = resized.width.toDouble() / resized.height.toDouble()
        assertThat(aspectRatio).isWithin(0.01).of(2.0)

        val area = resized.width * resized.height
        assertThat(area.toDouble()).isWithin(1000.0).of(65536.0)
    }

    @Test
    fun resizeBitmapScalesDownLargeBitmap() {
        val bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val resized = bitmap.resizeToArea(targetArea = 65536.0)

        assertThat(resized.width).isLessThan(bitmap.width)
        assertThat(resized.height).isLessThan(bitmap.height)
    }

    @Test
    fun resizeBitmapScalesUpSmallBitmap() {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val resized = bitmap.resizeToArea(targetArea = 65536.0)

        assertThat(resized.width).isGreaterThan(bitmap.width)
        assertThat(resized.height).isGreaterThan(bitmap.height)
    }

    @Test
    fun resizeBitmapHandlesDifferentTargetAreas() {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)

        val smallResize = bitmap.resizeToArea(targetArea = 16384.0)
        val largeResize = bitmap.resizeToArea(targetArea = 262144.0)

        assertThat(smallResize.width).isLessThan(largeResize.width)
        assertThat(smallResize.height).isLessThan(largeResize.height)
    }
}
