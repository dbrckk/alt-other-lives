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

@Composable
fun HomeScreen(photoUri: Uri?, onPhotoSelected: (Uri) -> Unit, onContinue: () -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onPhotoSelected)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("ALT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AltAccent)
            Spacer(Modifier.height(14.dp))
            Text("See the lives\nyou could have lived.", fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text("One photo. One choice. A completely different timeline.", color = AltMuted, fontSize = 17.sp)
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(32.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF29213D), Color(0xFF111116))))
                .clickable { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(model = photoUri, contentDescription = "Selected photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.48f)).padding(16.dp)
                ) {
                    Text("Tap to change photo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", fontSize = 48.sp, color = AltPrimary)
                    Text("Choose your photo", fontSize = 19.sp, fontWeight = FontWeight.Medium)
                    Text("Your original stays on this device.", color = AltMuted, fontSize = 13.sp)
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = photoUri != null,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AltPrimary,
                contentColor = Color(0xFF16111F),
                disabledContainerColor = Color(0xFF25242B),
                disabledContentColor = AltDimmed
            )
        ) { Text("Choose another life", fontWeight = FontWeight.Bold) }
    }
}
