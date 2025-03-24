package com.simprints.simface.matcher

import com.simprints.simface.core.Utils
import kotlin.math.sqrt

internal class CosineDistanceMatchProcessor() : MatchProcessor {

    @Throws(IllegalArgumentException::class)
    override fun verificationScore(probe: ByteArray, matchAgainst: ByteArray): Double {
        require(probe.size == matchAgainst.size) { "Arrays must be of the same size." }

        val array1 = Utils.byteArrayToFloatArray(probe)
        val array2 = Utils.byteArrayToFloatArray(matchAgainst)
    
        // Calculate the dot product of array1 and array2
        val dotProduct = array1.zip(array2) { x, y -> x * y }.sum()
        // Calculate the magnitudes of array1 and array2
        val magnitudeA = sqrt(array1.map { it * it }.sum())
        val magnitudeB = sqrt(array2.map { it * it }.sum())
        // Check to avoid division by zero
        check(magnitudeA > 0.0 && magnitudeB > 0.0) { "Arrays must not be zero vectors." }
        // Calculate cosine similarity
        val cosineSimilarity = dotProduct / (magnitudeA * magnitudeB)
        // Calculate cosine distance
        val cosineDistance = 1 - cosineSimilarity
    
        return (2.0 - 1.0 * cosineDistance) / 2
    }

    override fun identificationScore(
        probe: ByteArray,
        matchReferences: List<ByteArray>
    ): Map<ByteArray, Double> {
        return matchReferences.associateWith { matchAgainst ->
            verificationScore(probe, matchAgainst)
        }.toList()
            .sortedByDescending { (_, distance) -> distance }
            .toMap()
    }
}
