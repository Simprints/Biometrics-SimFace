package com.simprints.biometrics.simface.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.biometrics.loadBitmapFromTestResources
import com.simprints.biometrics.simface.Constants
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.data.FaceDetection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FaceAlignTest {
    private lateinit var context: Context
    private lateinit var simFace: SimFace

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @After
    fun cleanup() {
        simFace.release()
    }

    @Test
    fun crop_image_with_valid_bounding_box() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_flower")
        val boundingBox = Rect(50, 50, 150, 150)

        val croppedBitmap = cropAlignFace(bitmap, boundingBox)

        assertThat(croppedBitmap.width).isEqualTo(boundingBox.width())
        assertThat(croppedBitmap.height).isEqualTo(boundingBox.height())
    }

    @Test(expected = IllegalArgumentException::class)
    fun crop_image_with_invalid_bounding_box() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_flower")
        val boundingBox = Rect(
            -50,
            -50,
            bitmap.width + 50,
            bitmap.height + 50,
        )

        cropAlignFace(bitmap, boundingBox)
    }

    @Test
    fun align_face_with_valid_bounding_box() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val resultDeferred = CompletableDeferred<List<FaceDetection>>()

        simFace.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertThat(faces).isNotEmpty()
        val face = faces[0]

        val warpedAlignedImage =
            face.landmarks?.let { warpAlignFace(bitmap, it) }

        assertThat(warpedAlignedImage).isNotNull()

        assertThat(warpedAlignedImage!!.width).isEqualTo(Constants.IMAGE_SIZE)
        assertThat(warpedAlignedImage.height).isEqualTo(Constants.IMAGE_SIZE)
    }
}
