package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmbeddingProcessorTest {
    private lateinit var simFace: SimFace
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @Test
    fun get_embedding_with_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val result = simFace.getEmbedding(bitmap)
        val resultFloat = Utils.byteArrayToFloatArray(result)

        assertTrue(Utils.byteArrayToFloatArray(result).size == 512)

        // Define the expected output for our image (the output is computed on Android)
        val expectedEmbedding = floatArrayOf(
            0.1135F, -0.1753F, 0.0677F, -0.0124F, 0.1674F, -0.0951F, 0.0191F, -0.2586F, 0.091F,
            -0.2461F, 0.0696F, 0.0089F, 0.0044F, -0.0732F, 0.0606F, 0.037F, -0.1594F, 0.268F,
            -0.1401F, 0.1506F, -0.2159F, -0.0905F, 0.087F, -0.1309F, -0.0511F, 0.1077F, 0.0931F,
            0.0872F, 0.086F, 0.1203F, -0.1676F, 0.0235F, -0.2806F, -0.0329F, 0.1529F, 0.1186F,
            0.03F, 0.0686F, -0.0713F, -0.1314F, -0.0986F, -0.0724F, 0.1329F, 0.0092F, -0.1193F,
            -0.0391F, -0.1797F, -0.127F, 0.2789F, 0.0851F, 0.078F, 0.1067F, 0.0375F, 0.1518F,
            0.0008F, 0.1018F, 0.1809F, 0.1209F, -0.1117F, -0.1472F, 0.0554F, 0.0977F, -0.1013F,
            -0.0611F, 0.0388F, 0.2212F, 0.0033F, 0.0463F, 0.0278F, 0.0485F, -0.1307F, -0.0365F,
            -0.0826F, 0.0405F, -0.0073F, 0.299F, 0.1135F, -0.0102F, -0.0982F, -0.1587F, -0.1466F,
            0.0591F, -0.0091F, 0.0691F, -0.0868F, -0.0896F, -0.0628F, -0.0852F, -0.0948F, 0.0316F,
            -0.0861F, -0.1777F, -0.3523F, -0.186F, -0.1411F, 0.012F, -0.1373F, 0.1749F, -0.0249F,
            0.0509F, 0.0131F, 0.1686F, 0.0551F, 0.2373F, -0.064F, -0.028F, -0.1848F, -0.0349F,
            0.2469F, 0.0965F, -0.1407F, 0.0004F, 0.0209F, -0.0247F, -0.0216F, 0.0652F, -0.0333F,
            0.0948F, -0.0806F, 0.1441F, -0.117F, -0.1104F, 0.07F, -0.0372F, -0.0341F, 0.1117F,
            0.0481F, -0.2373F, 0.135F, 0.014F, -0.0972F, -0.0469F, 0.0211F, -0.1202F, 0.0437F,
            0.0257F, 0.1639F, 0.0143F, 0.0503F, -0.0142F, 0.0327F, 0.0882F, 0.0063F, -0.0172F,
            0.0412F, -0.1465F, 0.22F, 0.0429F, -0.1867F, 0.1597F, -0.1326F, 0.0647F, 0.3016F,
            -0.0428F, -0.2369F, -0.0455F, -0.0397F, -0.1079F, 0.1862F, -0.0116F, 0.0553F, 0.1248F,
            0.131F, 0.0128F, -0.0781F, 0.0971F, 0.0904F, -0.0411F, 0.0961F, -0.1152F, 0.199F,
            0.1153F, -0.3224F, -0.1733F, 0.05F, -0.0446F, -0.1369F, 0.1701F, 0.2333F, -0.2317F,
            0.002F, -0.0351F, 0.0046F, 0.1207F, -0.2001F, -0.0382F, -0.0422F, 0.1825F, -0.0938F,
            0.2165F, 0.0996F, 0.1071F, 0.0128F, 0.1434F, -0.1021F, -0.1902F, -0.0408F, -0.0902F,
            -0.031F, -0.0502F, -0.0982F, -0.0567F, 0.1543F, 0.1186F, -0.0727F, -0.0838F, -0.0971F,
            -0.1439F, -0.2429F, -0.0308F, 0.1349F, 0.0538F, 0.0568F, -0.2891F, 0.0614F, 0.1271F,
            -0.1079F, 0.074F, -0.0999F, -0.1479F, -0.0597F, -0.2288F, 0.1506F, 0.1161F, -0.0138F,
            -0.1488F, -0.0501F, 0.0919F, -0.0453F, -0.0628F, -0.059F, 0.1338F, 0.155F, 0.0091F,
            0.0771F, 0.0666F, 0.0869F, 0.0258F, -0.0684F, 0.0951F, 0.0452F, -0.2022F, 0.1382F,
            0.0733F, 0.055F, 0.0729F, 0.0788F, -0.2598F, 0.0132F, 0.114F, -0.0869F, -0.1626F,
            0.0236F, 0.0724F, 0.0425F, -0.0393F, 0.1494F, -0.0671F, -0.0336F, -0.0595F, -0.1619F,
            0.0663F, -0.08F, -0.205F, -0.003F, 0.0969F, 0.1377F, 0.0062F, -0.0457F, -0.126F,
            0.0655F, -0.0487F, -0.0257F, 0.0424F, 0.2309F, -0.099F, 0.0163F, -0.0458F, -0.0571F,
            -0.0574F, 0.0281F, 0.1171F, -0.1953F, -0.0976F, -0.05F, -0.2563F, -0.0281F, -0.0871F,
            0.3235F, 0.0788F, 0.2908F, 0.1366F, 0.0607F, -0.0818F, -0.0054F, -0.0376F, -0.1022F,
            0.0616F, -0.1894F, -0.0358F, 0.2847F, 0.0595F, -0.1124F, 0.1173F, 0.174F, 0.0755F,
            0.0573F, -0.0637F, -0.0562F, -0.135F, -0.0006F, -0.1519F, -0.1022F, 0.1712F, 0.0848F,
            0.0547F, 0.0366F, -0.0063F, -0.0713F, -0.0349F, -0.0271F, -0.1853F, -0.1445F, 0.0208F,
            0.0398F, 0.1813F, 0.0798F, -0.0562F, -0.1128F, -0.0147F, 0.1764F, -0.0833F, 0.1118F,
            -0.2129F, -0.1F, -0.1167F, -0.2579F, 0.0198F, 0.0885F, -0.095F, -0.0838F, -0.144F,
            0.0299F, -0.0591F, -0.102F, 0.049F, 0.0343F, 0.1298F, 0.0956F, -0.0458F, -0.0474F,
            0.0315F, 0.1484F, 0.2539F, -0.0043F, -0.186F, -0.2215F, 0.1075F, 0.1652F, 0.2939F,
            0.0095F, -0.0881F, 0.1055F, -0.1063F, 0.0289F, -0.1739F, 0.0477F, -0.1992F, -0.1366F,
            -0.0555F, 0.0207F, -0.0222F, -0.0415F, -0.1433F, -0.0271F, 0.0524F, -0.1316F, 0.2686F,
            0.1246F, 0.2177F, 0.1663F, -0.1196F, -0.0851F, -0.1035F, 0.0225F, 0.0033F, -0.1908F,
            0.1418F, 0.1604F, -0.1515F, -0.1319F, 0.0213F, -0.2297F, -0.0265F, -0.081F, 0.0559F,
            0.1326F, -0.1169F, 0.0283F, 0.0001F, 0.0463F, -0.0764F, 0.0277F, 0.1246F, -0.1428F,
            -0.1475F, 0.0022F, 0.1023F, -0.0439F, 0.0696F, 0.0047F, -0.1234F, -0.0703F, -0.0483F,
            0.0474F, 0.2345F, 0.0725F, 0.1313F, -0.1151F, -0.0591F, -0.2275F, -0.2104F, 0.0691F,
            -0.0486F, -0.059F, -0.2237F, -0.1017F, 0.0346F, 0.1812F, -0.0554F, 0.1307F, 0.1192F,
            0.1939F, 0.1052F, 0.0822F, -0.0595F, -0.1773F, 0.1384F, -0.1298F, -0.1249F, -0.0362F,
            -0.1397F, 0.0448F, 0.116F, 0.1894F, 0.0727F, -0.2113F, 0.0221F, -0.0064F, 0.0144F,
            0.09F, 0.0875F, -0.1423F, 0.127F, -0.0044F, 0.0062F, 0.1248F, 0.0782F, 0.2054F,
            0.1255F, 0.0374F, -0.0638F, -0.0931F, -0.024F, -0.2686F, -0.1965F, 0.0769F, -0.1417F,
            0.06F, 0.0964F, -0.0566F, -0.2128F, -0.0131F, 0.1782F, 0.019F, 0.0467F, 0.0065F,
            0.1045F, -0.1654F, 0.0802F, 0.1897F, 0.1411F, -0.0211F, 0.0146F, -0.0734F, 0.0369F,
            0.0555F, 0.0482F, 0.0454F, -0.1672F, 0.007F, 0.0741F, 0.0571F, 0.1477F, 0.0634F,
            0.0808F, -0.1018F, 0.0234F, -0.1357F, 0.0398F, 0.0549F, 0.1207F, 0.1144F, -0.1265F,
            0.0115F, 0.1952F, -0.0571F, -0.0351F, -0.0523F, -0.1019F, 0.0142F, 0.0551F,
        )

        // Verify results
        assertArrayEquals(expectedEmbedding, resultFloat, 0.1F)
    }

    @Test
    fun compare_embeddings_between_different_images() {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_bad_face")

        val embedding1 = simFace.getEmbedding(bitmap1)
        val embedding2 = simFace.getEmbedding(bitmap2)

        assertTrue(!embedding1.contentEquals(embedding2)) // Embeddings should be different
    }

    @Test
    fun consistency_test_with_same_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")

        val embedding1 = simFace.getEmbedding(bitmap)
        val embedding2 = simFace.getEmbedding(bitmap)

        assertArrayEquals(embedding1, embedding2) // Embeddings should be identical
    }
}
