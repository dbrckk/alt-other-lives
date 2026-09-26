package com.alt.otherlives.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alt.otherlives.core.designsystem.AltDimmed
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.designsystem.AltPrimary
import com.alt.otherlives.core.designsystem.AltAccent
import com.alt.otherlives.R

@Composable
fun HomeScreen(
    photoUri: Uri?,
    isImportingPhoto: Boolean,
    onPhotoSelected: (Uri) -> Unit,
    onContinue: () -> Unit,
    onHistory: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onPhotoSelected)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.home_brand), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AltAccent)
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.home_headline), fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.home_subtitle), color = AltMuted, fontSize = 17.sp)
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(32.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF29213D), Color(0xFF111116))))
                .clickable(enabled = !isImportingPhoto) { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(model = photoUri, contentDescription = stringResource(R.string.home_selected_photo), modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.48f)).padding(16.dp)
                ) {
                    Text(
                        if (isImportingPhoto) stringResource(R.string.home_importing_photo) else stringResource(R.string.home_change_photo),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", fontSize = 48.sp, color = AltPrimary)
                    Text(stringResource(R.string.home_choose_photo), fontSize = 19.sp, fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.home_photo_local), color = AltMuted, fontSize = 13.sp)
                }
            }
        }

        Column {
            Button(
            onClick = onContinue,
            enabled = photoUri != null && !isImportingPhoto,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AltPrimary,
                contentColor = Color(0xFF16111F),
                disabledContainerColor = Color(0xFF25242B),
                disabledContentColor = AltDimmed
            )
        ) {
            Text(
                if (isImportingPhoto) stringResource(R.string.home_preparing_photo) else stringResource(R.string.home_choose_life),
                fontWeight = FontWeight.Bold
            )
        }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.TextButton(
                onClick = onHistory,
                enabled = !isImportingPhoto,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.home_view_history), color = AltMuted) }
        }
    }
}
