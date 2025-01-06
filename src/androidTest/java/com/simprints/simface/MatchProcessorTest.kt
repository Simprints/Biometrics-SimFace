import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.SimFaceFacade
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerificationTest {
    private lateinit var simFace: SimFaceFacade

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val simFaceConfig = SimFaceConfig(context)
        SimFaceFacade.initialize(simFaceConfig)
        simFace = SimFaceFacade.getInstance()
    }

    @Test
    fun score_between_identical_vectors_should_be_one() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f, 0.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f, 0.0f))

        val distance = simFace.matchProcessor.verificationScore(array1, array2)

        assertEquals(1.0f, distance, 0.0001f)
    }

    @Test
    fun score_between_orthogonal_vectors_should_be_one_half() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(0.0f, 1.0f))

        val distance = simFace.matchProcessor.verificationScore(array1, array2)

        assertEquals(0.5f, distance, 0.0001f)
    }

    @Test
    fun score_between_opposite_vectors_should_be_zero() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(-1.0f, 0.0f))

        val distance = simFace.matchProcessor.verificationScore(array1, array2)

        assertEquals(0.0f, distance, 0.0001f)
    }

    @Test
    fun score_between_arbitrary_vectors_should_be_between_zero_and_one() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 2.0f, 3.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(4.0f, 5.0f, 6.0f))

        val distance = simFace.matchProcessor.verificationScore(array1, array2)

        assertTrue(distance > 0.0f && distance < 1.0f)
    }
}

@RunWith(AndroidJUnit4::class)
class IdentificationTest {
    private lateinit var simFace: SimFaceFacade

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val simFaceConfig = SimFaceConfig(context)
        SimFaceFacade.initialize(simFaceConfig)
        simFace = SimFaceFacade.getInstance()
    }

    @Test
    fun score_map_should_be_ordered_by_distance() {
        val referenceArray = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val arrayList = listOf(
            Utils.floatArrayToByteArray(floatArrayOf(-1.0f, 0.0f)),    // opposite to referenceArray
            Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f)),    // identical to referenceArray
            Utils.floatArrayToByteArray(floatArrayOf(0.0f, 1.0f)),    // orthogonal to referenceArray
            Utils.floatArrayToByteArray(floatArrayOf(0.707f, 0.707f)) // 45 degrees to referenceArray
        )

        val sortedMap = simFace.matchProcessor.identificationScore(referenceArray, arrayList)
        val sortedDistances = sortedMap.values.toList()

        // Closest match (identical vector) should have a score of 1
        assertEquals(1.0f, sortedDistances[0], 0.0001f)

        // 45-degree vector (second closest) should have a score of around 0.85355
        assertEquals(0.85355f, sortedDistances[1], 0.0001f)

        // Orthogonal vector (further away) should have a score of 0.5
        assertEquals(0.5f, sortedDistances[2], 0.0001f)

        // Opposite vector (furthest away) should have a score of 0.0
        assertEquals(0.0f, sortedDistances[3], 0.0001f)
    }
}