package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.biometrics.simface.R
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.SimFaceFacade
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class FaceDetectionProcessorTest {
    private lateinit var simFace: SimFaceFacade
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val simFaceConfig = SimFaceConfig(context)
        SimFaceFacade.initialize(simFaceConfig)
        simFace = SimFaceFacade.getInstance()
    }

    @Test
    fun normal_image_gets_high_score() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

        val resultDeferred = CompletableDeferred<List<SimFace>>()

        simFace.faceDetectionProcessor.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertTrue(faces.isNotEmpty())
        val face = faces[0]
        assertTrue(face.quality > 0.5)
    }

    @Test
    fun bad_image_gets_low_score() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_bad_face)

        val resultDeferred = CompletableDeferred<List<SimFace>>()

        simFace.faceDetectionProcessor.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertTrue(faces.isNotEmpty())
        val face = faces[0]
        assertTrue(face.quality < 0.5)
    }

    @Test
    fun no_faces_in_flower_image() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_flower)

        val resultDeferred = CompletableDeferred<List<SimFace>>()

        simFace.faceDetectionProcessor.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertTrue(faces.isEmpty())
    }

    @Test
    fun image_with_multiple_faces() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_multiple_faces)

        val resultDeferred = CompletableDeferred<List<SimFace>>()

        simFace.faceDetectionProcessor.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertTrue(faces.size == 5)
    }

    @Test
    fun normal_image_gets_high_score_blocking() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

        val faces = simFace.faceDetectionProcessor.detectFaceBlocking(bitmap)

        assertTrue(faces.isNotEmpty())
        val face = faces[0]
        assertTrue(face.quality > 0.5)
    }

    @Test
    fun bad_image_gets_low_score_blocking() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_bad_face)

        val faces = simFace.faceDetectionProcessor.detectFaceBlocking(bitmap)

        assertTrue(faces.isNotEmpty())
        val face = faces[0]
        assertTrue(face.quality < 0.5)
    }

    @Test
    fun no_faces_in_flower_image_blocking() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_flower)

        val faces = simFace.faceDetectionProcessor.detectFaceBlocking(bitmap)

        assertTrue(faces.isEmpty())
    }

    @Test
    fun image_with_multiple_faces_blocking() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_multiple_faces)

        val faces = simFace.faceDetectionProcessor.detectFaceBlocking(bitmap)

        assertTrue(faces.size == 5)
    }

    @Test
    fun crop_image_with_valid_bounding_box() {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_flower)
        val boundingBox = Rect(50, 50, 150, 150)

        val croppedBitmap = simFace.faceDetectionProcessor.alignFace(bitmap, boundingBox)

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
            bitmap.height + 50
        )

        simFace.faceDetectionProcessor.alignFace(bitmap, boundingBox)
    }

    @Test
    fun align_face_with_valid_bounding_box() = runBlocking {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

        val resultDeferred = CompletableDeferred<List<SimFace>>()

        simFace.faceDetectionProcessor.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertTrue(faces.isNotEmpty())
        val face = faces[0]

        val warpedAlignedImage = face.landmarks?.let { simFace.faceDetectionProcessor.warpAlignFace(it, bitmap) }

////        bitmap gets saved to /storage/emulated/0/Android/data/com.simprints.biometrics.simface.test/files/
//        fun saveBitmapToExternalStorage(context: Context, bitmap: Bitmap, filename: String) {
//            val file = File(context.getExternalFilesDir(null), filename)
//            FileOutputStream(file).use { fos ->
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
//            }
//        }
//        if (warpedAlignedImage != null) {
//            saveBitmapToExternalStorage(context, warpedAlignedImage, "royalty_free_good_face_warp_aligned.png")
//        }

        assertTrue(warpedAlignedImage != null)

        if (warpedAlignedImage != null) {
            assertTrue(warpedAlignedImage.width == 112)
        }
        if (warpedAlignedImage != null) {
            assertTrue(warpedAlignedImage.height == 112)
        }

    }
}



