package com.alt.otherlives.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
    photoIsLikelyPremiumSource: Boolean?,
    isImportingPhoto: Boolean,
    onPhotoSelected: (Uri) -> Unit,
    onContinue: () -> Unit,
    onHistory: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val homeLayout = HomeResponsiveLayout.resolve(
        screenHeightDp = configuration.screenHeightDp,
        fontScale = fontScale
    )
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onPhotoSelected)
    }
    val photoActionDescription = stringResource(
        if (photoUri == null) {
            R.string.home_photo_action_choose
        } else {
            R.string.home_photo_action_change
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 34.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_brand),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AltAccent
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.home_brand_descriptor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AltDimmed
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.home_headline),
                fontSize = homeLayout.headlineSizeSp.sp,
                lineHeight = homeLayout.headlineLineHeightSp.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.home_subtitle),
                color = AltMuted,
                fontSize = 16.sp,
                lineHeight = 23.sp
            )
        }

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(homeLayout.photoHeroHeightDp.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF32264B), Color(0xFF16131E), Color(0xFF101014))
                    )
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = photoActionDescription
                    role = Role.Button
                }
                .clickable(
                    enabled = !isImportingPhoto,
                    role = Role.Button
                ) { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = stringResource(R.string.home_selected_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.72f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.home_photo_ready),
                            color = AltAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        photoIsLikelyPremiumSource?.let { premium ->
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(
                                    if (premium) {
                                        R.string.home_source_premium
                                    } else {
                                        R.string.home_source_low_quality
                                    }
                                ),
                                color = if (premium) AltAccent else AltMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (isImportingPhoto) {
                            stringResource(R.string.home_importing_photo)
                        } else {
                            stringResource(R.string.home_change_photo)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (photoIsLikelyPremiumSource == false) {
                        Spacer(Modifier.height(7.dp))
                        Text(
                            stringResource(R.string.home_source_low_quality_hint),
                            color = AltMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 34.dp)
                ) {
                    Text("＋", fontSize = 44.sp, color = AltPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.home_choose_photo),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        stringResource(R.string.home_photo_prompt),
                        color = AltMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(R.string.home_photo_local),
                        color = AltAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.home_step_photo),
                    color = if (photoUri != null) AltAccent else AltMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.home_step_choice),
                    color = AltMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.home_step_reveal),
                    color = AltMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                enabled = photoUri != null && !isImportingPhoto,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AltPrimary,
                    contentColor = Color(0xFF16111F),
                    disabledContainerColor = Color(0xFF25242B),
                    disabledContentColor = AltDimmed
                )
            ) {
                Text(
                    if (isImportingPhoto) {
                        stringResource(R.string.home_preparing_photo)
                    } else {
                        stringResource(R.string.home_choose_life)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            androidx.compose.material3.TextButton(
                onClick = onHistory,
                enabled = !isImportingPhoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.home_view_history), color = AltMuted)
            }
        }
    }
}
