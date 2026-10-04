package com.alt.otherlives.feature.framing

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alt.otherlives.R
import com.alt.otherlives.core.designsystem.AltAccent
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.designsystem.AltPrimary
import com.alt.otherlives.core.media.SourcePhotoQualityIssue
import com.alt.otherlives.core.media.NormalizedCropRect

@Composable
fun PhotoFramingScreen(
    photoUri: Uri,
    qualityIssues: Set<SourcePhotoQualityIssue>,
    crop: NormalizedCropRect = NormalizedCropRect.Full,
    onCropChange: (NormalizedCropRect) -> Unit = {},
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val cropCenterX = (crop.left + crop.right) / 2f
    val cropCenterY = (crop.top + crop.bottom) / 2f
    val cropZoom = (1f / crop.width).coerceIn(1f, 2.85f)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("‹ " + stringResource(R.string.common_back))
        }

        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.framing_title),
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.framing_subtitle),
            color = AltMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF121116)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(R.string.framing_photo_content_description),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cropZoom
                        scaleY = cropZoom
                        translationX = (0.5f - cropCenterX) * size.width * cropZoom
                        translationY = (0.5f - cropCenterY) * size.height * cropZoom
                    },
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .size(width = 190.dp, height = 250.dp)
                    .border(
                        width = 2.dp,
                        color = AltAccent.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(95.dp)
                    )
            )

            Text(
                stringResource(R.string.framing_center_face),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(18.dp)
                    .background(
                        Color.Black.copy(alpha = 0.68f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.framing_zoom),
            color = AltMuted,
            fontSize = 12.sp
        )
        Slider(
            value = cropZoom,
            onValueChange = { requestedZoom ->
                val factor = requestedZoom / cropZoom
                onCropChange(crop.scaledAroundCenter(factor))
            },
            valueRange = 1f..2.85f
        )

        val halfWidth = crop.width / 2f
        Text(
            stringResource(R.string.framing_horizontal),
            color = AltMuted,
            fontSize = 12.sp
        )
        Slider(
            value = cropCenterX,
            onValueChange = { requestedCenter ->
                onCropChange(crop.movedBy(requestedCenter - cropCenterX, 0f))
            },
            valueRange = if (halfWidth < 0.5f) {
                halfWidth..(1f - halfWidth)
            } else {
                0f..1f
            },
            enabled = halfWidth < 0.5f
        )

        val halfHeight = crop.height / 2f
        Text(
            stringResource(R.string.framing_vertical),
            color = AltMuted,
            fontSize = 12.sp
        )
        Slider(
            value = cropCenterY,
            onValueChange = { requestedCenter ->
                onCropChange(crop.movedBy(0f, requestedCenter - cropCenterY))
            },
            valueRange = if (halfHeight < 0.5f) {
                halfHeight..(1f - halfHeight)
            } else {
                0f..1f
            },
            enabled = halfHeight < 0.5f
        )

        TextButton(
            onClick = { onCropChange(NormalizedCropRect.Full) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.framing_reset))
        }

        if (SourcePhotoQualityIssue.EXTREME_ASPECT_RATIO in qualityIssues) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.framing_panorama_warning),
                color = AltMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
        if (SourcePhotoQualityIssue.TOO_SMALL in qualityIssues) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.framing_resolution_warning),
                color = AltMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.framing_change_photo))
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AltPrimary,
                    contentColor = Color(0xFF16111F)
                )
            ) {
                Text(
                    stringResource(R.string.framing_confirm),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
