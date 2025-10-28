package com.simprints.simq

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.simq.utils.ScoringFunctions
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScoringFunctionsTest {
    @Test
    fun rampScoreReturns0WhenBelowMinimum() {
        val score = ScoringFunctions.rampScore(x = 10.0, min = 20.0, max = 100.0)
        assertThat(score).isWithin(0.001).of(0.0)
    }

    @Test
    fun rampScoreReturns1WhenAboveMaximum() {
        val score = ScoringFunctions.rampScore(x = 150.0, min = 20.0, max = 100.0)
        assertThat(score).isWithin(0.001).of(1.0)
    }

    @Test
    fun rampScoreReturns0_5AtMidpoint() {
        val score = ScoringFunctions.rampScore(x = 60.0, min = 20.0, max = 100.0)
        assertThat(score).isWithin(0.001).of(0.5)
    }

    @Test
    fun rampScoreInterpolatesLinearly() {
        val score = ScoringFunctions.rampScore(x = 30.0, min = 20.0, max = 120.0)
        // (30 - 20) / (120 - 20) = 10/100 = 0.1
        assertThat(score).isWithin(0.001).of(0.1)
    }

    @Test
    fun plateauScoreReturns1InOptimalRange() {
        val score1 =
            ScoringFunctions.plateauScore(
                x = 100.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(score1).isWithin(0.001).of(1.0)

        val score2 =
            ScoringFunctions.plateauScore(
                x = 80.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(score2).isWithin(0.001).of(1.0)

        val score3 =
            ScoringFunctions.plateauScore(
                x = 150.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(score3).isWithin(0.001).of(1.0)
    }

    @Test
    fun plateauScoreDecreasesOutsideOptimalRange() {
        // Below center
        val scoreLow =
            ScoringFunctions.plateauScore(
                x = 50.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(scoreLow).isLessThan(1.0)
        assertThat(scoreLow).isGreaterThan(0.0)

        // Above center
        val scoreHigh =
            ScoringFunctions.plateauScore(
                x = 170.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(scoreHigh).isLessThan(1.0)
        assertThat(scoreHigh).isGreaterThan(0.0)
    }

    @Test
    fun plateauScoreIsClampedBetween0And1() {
        val scoreVeryLow =
            ScoringFunctions.plateauScore(
                x = 0.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(scoreVeryLow).isAtLeast(0.0)
        assertThat(scoreVeryLow).isAtMost(1.0)

        val scoreVeryHigh =
            ScoringFunctions.plateauScore(
                x = 300.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )
        assertThat(scoreVeryHigh).isAtLeast(0.0)
        assertThat(scoreVeryHigh).isAtMost(1.0)
    }

    @Test
    fun plateauScoreIsSymmetricAroundOptimalRange() {
        val scoreLow =
            ScoringFunctions.plateauScore(
                x = 50.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )

        val scoreHigh =
            ScoringFunctions.plateauScore(
                x = 180.0,
                centerLow = 80.0,
                centerHigh = 150.0,
                edgeLow = 30.0,
                edgeHigh = 190.0,
                steepness = 0.3,
            )

        // Should be approximately equal due to symmetry
        assertThat(scoreHigh).isWithin(0.1).of(scoreLow)
    }
}
