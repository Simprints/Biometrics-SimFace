package com.simprints.sample.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simprints.sample.ui.models.FaceResult

@Composable
fun DisplayFaceResult(
    result: FaceResult,
    title: String,
    titleColor: Color,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)

            result.bitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Processed face $title",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                )
            }

            Text(
                text = result.message,
                fontSize = 14.sp,
                fontWeight = if (result.success) FontWeight.Normal else FontWeight.Bold,
                color = if (result.success) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}
