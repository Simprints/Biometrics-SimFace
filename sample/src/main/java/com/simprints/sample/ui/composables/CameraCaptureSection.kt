package com.simprints.sample.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simprints.sample.ui.models.FaceResult

@Composable
fun CameraCaptureSection(
    isBusy: Boolean,
    capturedImage1: FaceResult?,
    capturedImage2: FaceResult?,
    onCaptureFace1: () -> Unit,
    onCaptureFace2: () -> Unit,
    onCompareCaptured: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "📸 Camera Capture & Compare",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onCaptureFace1,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Capture Face 1")
                }

                Button(
                    onClick = onCaptureFace2,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Capture Face 2")
                }
            }

            Button(
                onClick = onCompareCaptured,
                enabled = !isBusy && capturedImage1 != null && capturedImage2 != null,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Compare Captured Faces")
            }
        }
    }
}
