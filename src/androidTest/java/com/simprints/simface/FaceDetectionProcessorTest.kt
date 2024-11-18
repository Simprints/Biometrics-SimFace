import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.biometrics_simface.R
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.SimFaceFacade
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Before
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
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_good_face)

        val face = simFace.faceDetectionProcessor.detectFace(bitmap)[0]

        assertTrue(face.quality > 0.5)
    }

    @Test
    fun bad_image_gets_low_score() = runBlocking {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.royalty_free_bad_face)

        val face = simFace.faceDetectionProcessor.detectFace(bitmap)[0]

        assertTrue(face.quality < 0.5)
    }

}