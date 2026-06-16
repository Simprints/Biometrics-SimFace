package com.simprints.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simprints.sample.ui.composables.ComparisonResultCard
import com.simprints.sample.ui.composables.DisplayFaceResult
import com.simprints.sample.ui.composables.TestImagesSection
import com.simprints.sample.ui.models.images.SimFaceTestImageActions
import com.simprints.sample.ui.models.images.SimFaceTestImageUiState

@Composable
fun SimFaceTestImageDemoScreen(
    modifier: Modifier = Modifier,
    uiState: SimFaceTestImageUiState,
    actions: SimFaceTestImageActions,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(text = "SimFace Test Images Demo", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        TestImagesSection(
            isBusy = uiState.isProcessing || uiState.isComparing,
            result1 = uiState.result1,
            result2 = uiState.result2,
            result3 = uiState.result3,
            onLoadObama1 = actions.onLoadObama1,
            onLoadObama2 = actions.onLoadObama2,
            onLoadBush = actions.onLoadBush,
            onLoadLowQuality = actions.onLoadLowQuality,
            onCompareObamaToObama = actions.onCompareObamaToObama,
            onCompareObamaToBush = actions.onCompareObamaToBush,
        )

        if (uiState.isProcessing || uiState.isComparing) {
            CircularProgressIndicator()
            Text(
                text = if (uiState.isProcessing) "Processing image..." else "Comparing faces...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ComparisonResultCard(comparisonResult = uiState.comparisonResult)

        uiState.result1?.let { res -> DisplayFaceResult(res, "Obama 1", MaterialTheme.colorScheme.primary) }
        uiState.result2?.let { res -> DisplayFaceResult(res, "Obama 2", MaterialTheme.colorScheme.secondary) }
        uiState.result3?.let { res -> DisplayFaceResult(res, "Bush 1", MaterialTheme.colorScheme.tertiary) }
        uiState.result4?.let { res -> DisplayFaceResult(res, "Low Quality", MaterialTheme.colorScheme.tertiary) }
    }
}
