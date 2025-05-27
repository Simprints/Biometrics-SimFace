package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import androidx.test.core.app.*
import com.simprints.biometrics.simface.R
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils.IMAGE_SIZE
import com.simprints.simface.data.FaceDetection
import com.simprints.simface.quality.cropAlignFace
import com.simprints.simface.quality.warpAlignFace
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FaceAlignTest {
    private lateinit var context: Context
    private lateinit var simFace: SimFace

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @Test
    fun crop_image_with_valid_bounding_box() {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_flower)
        val boundingBox = Rect(50, 50, 150, 150)

        val croppedBitmap = cropAlignFace(bitmap, boundingBox)

        assertTrue(boundingBox.width() == croppedBitmap.width)
        assertTrue(boundingBox.height() == croppedBitmap.height)
    }

    @Test(expected = IllegalArgumentException::class)
    fun crop_image_with_invalid_bounding_box() {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_flower)
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
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

        val resultDeferred = CompletableDeferred<List<FaceDetection>>()

        simFace.getFaceDetectionProcessor().detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertTrue(faces.isNotEmpty())
        val face = faces[0]

        val warpedAlignedImage =
            face.landmarks?.let { warpAlignFace(bitmap, it) }

        assertTrue(warpedAlignedImage != null)

        if (warpedAlignedImage != null) {
            assertTrue(warpedAlignedImage.width == IMAGE_SIZE)
        }
        if (warpedAlignedImage != null) {
            assertTrue(warpedAlignedImage.height == IMAGE_SIZE)
        }
    }
}
