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
    fun align_face_with_valid_bounding_box() {
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_flower)
        val boundingBox = Rect(50, 50, 150, 150)

        val croppedBitmap = simFace.faceDetectionProcessor.alignFace(bitmap, boundingBox)

        assertTrue(boundingBox.width() == croppedBitmap.width)
        assertTrue(boundingBox.height() == croppedBitmap.height)
    }

    @Test(expected = IllegalArgumentException::class)
    fun align_face_with_invalid_bounding_box() {
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


}