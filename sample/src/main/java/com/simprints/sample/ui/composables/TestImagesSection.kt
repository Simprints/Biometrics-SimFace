package com.simprints.sample.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simprints.sample.ui.models.FaceResult

@Composable
fun TestImagesSection(
    isBusy: Boolean,
    result1: FaceResult?,
    result2: FaceResult?,
    result3: FaceResult?,
    onLoadObama1: () -> Unit,
    onLoadObama2: () -> Unit,
    onLoadBush: () -> Unit,
    onLoadLowQuality: () -> Unit,
    onCompareObamaToObama: () -> Unit,
    onCompareObamaToBush: () -> Unit,
) {
    Text(text = "Test Images", fontSize = 18.sp, fontWeight = FontWeight.Bold)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onLoadObama1, enabled = !isBusy) { Text("Load Obama 1") }
        Button(onClick = onLoadObama2, enabled = !isBusy) { Text("Load Obama 2") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onLoadBush, enabled = !isBusy) { Text("Load Bush 1") }
        Button(onClick = onLoadLowQuality, enabled = !isBusy) { Text("Load Low Quality") }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onCompareObamaToObama,
            enabled = !isBusy && result1 != null && result2 != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.weight(1f),
        ) {
            Text("Compare Obama with Obama", textAlign = TextAlign.Center)
        }

        Button(
            onClick = onCompareObamaToBush,
            enabled = !isBusy && result1 != null && result3 != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.weight(1f),
        ) {
            Text("Compare Obama with Bush", textAlign = TextAlign.Center)
        }
    }
}
