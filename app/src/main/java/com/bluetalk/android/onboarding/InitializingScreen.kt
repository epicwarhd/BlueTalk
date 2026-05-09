package com.bluetalk.android.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetalk.android.R
import com.bluetalk.android.ui.theme.BlueTalkTheme

/**
 * Loading screen shown during app initialization with a modern, approachable style.
 */
@Composable
fun InitializingScreen(modifier: Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Animated dots for loading text
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surface,
                        colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon / Logo Placeholder
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.initializing_mesh_network),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary.copy(alpha = dotAlpha)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress bar
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colorScheme.primary,
                trackColor = colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Information Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setting_up_bluetooth),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.should_take_seconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Error screen shown if initialization fails, with a modern UI.
 */
@Composable
fun InitializationErrorScreen(
    modifier: Modifier,
    errorMessage: String,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = colorScheme.errorContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = colorScheme.error
                    )
                }
            }

            Text(
                text = stringResource(R.string.setup_not_complete),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Detailed Error Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.try_again),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                FilledTonalButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.open_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Initializing Screen - Light")
@Composable
fun InitializingScreenLightPreview() {
    BlueTalkTheme(darkTheme = false) {
        Surface {
            InitializingScreen(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true, name = "Initializing Screen - Dark")
@Composable
fun InitializingScreenDarkPreview() {
    BlueTalkTheme(darkTheme = true) {
        Surface {
            InitializingScreen(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true, name = "Error Screen - Light")
@Composable
fun InitializationErrorScreenLightPreview() {
    BlueTalkTheme(darkTheme = false) {
        Surface {
            InitializationErrorScreen(
                modifier = Modifier.fillMaxSize(),
                errorMessage = "Bluetooth initialization failed: Error code 133",
                onRetry = {},
                onOpenSettings = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Error Screen - Dark")
@Composable
fun InitializationErrorScreenDarkPreview() {
    BlueTalkTheme(darkTheme = true) {
        Surface {
            InitializationErrorScreen(
                modifier = Modifier.fillMaxSize(),
                errorMessage = "Bluetooth initialization failed: Error code 133",
                onRetry = {},
                onOpenSettings = {}
            )
        }
    }
}
