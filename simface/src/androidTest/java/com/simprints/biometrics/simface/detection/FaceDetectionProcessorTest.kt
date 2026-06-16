package com.simprints.biometrics.simface.detection

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.assertThat
import com.simprints.biometrics.loadBitmapFromTestResources
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
class FaceDetectionProcessorTest {
    private lateinit var simFace: SimFace
    private lateinit var context: Context

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
    fun normal_image_gets_high_score() = runTest {
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
        assertThat(face.quality).isGreaterThan(0.5f)
    }

    @Test
    fun bad_image_gets_low_score() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_bad_face")
        val resultDeferred = CompletableDeferred<List<FaceDetection>>()

        simFace.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertThat(faces).isNotEmpty()
        val face = faces[0]
        assertThat(face.quality).isLessThan(0.5f)
    }

    @Test
    fun no_faces_in_flower_image() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_flower")
        val resultDeferred = CompletableDeferred<List<FaceDetection>>()

        simFace.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertThat(faces).isEmpty()
    }

    @Test
    fun image_with_multiple_faces() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_multiple_faces")
        val resultDeferred = CompletableDeferred<List<FaceDetection>>()

        simFace.detectFace(bitmap, onSuccess = { faces ->
            resultDeferred.complete(faces)
        }, onFailure = { error ->
            resultDeferred.completeExceptionally(error)
        })

        val faces = resultDeferred.await()
        assertThat(faces).hasSize(5)
    }

    @Test
    fun normal_image_gets_high_score_blocking() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val faces = simFace.detectFaceBlocking(bitmap)

        assertThat(faces).isNotEmpty()
        val face = faces[0]
        assertThat(face.quality).isGreaterThan(0.5f)
    }

    @Test
    fun bad_image_gets_low_score_blocking() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_bad_face")
        val faces = simFace.detectFaceBlocking(bitmap)

        assertThat(faces).isNotEmpty()
        val face = faces[0]
        assertThat(face.quality).isLessThan(0.5f)
    }

    @Test
    fun no_faces_in_flower_image_blocking() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_flower")
        val faces = simFace.detectFaceBlocking(bitmap)

        assertThat(faces).isEmpty()
    }

    @Test
    fun image_with_multiple_faces_blocking() = runTest {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_multiple_faces")
        val faces = simFace.detectFaceBlocking(bitmap)

        assertThat(faces).hasSize(5)
    }
}
