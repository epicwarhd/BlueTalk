package com.bitchat.android.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.bitchat.android.R
import com.bitchat.android.ui.theme.BitchatTheme

/**
 * Screen shown when checking location services status with a modern, approachable style.
 */
@Composable
fun LocationCheckScreen(
    modifier: Modifier,
    status: LocationStatus,
    onEnableLocation: () -> Unit,
    onRetry: () -> Unit,
    isLoading: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            LocationStatus.DISABLED -> {
                LocationDisabledContent(
                    onEnableLocation = onEnableLocation,
                    onRetry = onRetry,
                    colorScheme = colorScheme,
                    isLoading = isLoading
                )
            }
            LocationStatus.NOT_AVAILABLE -> {
                LocationNotAvailableContent(
                    colorScheme = colorScheme
                )
            }
            LocationStatus.ENABLED -> {
                LocationCheckingContent(
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
private fun LocationDisabledContent(
    onEnableLocation: () -> Unit,
    onRetry: () -> Unit,
    colorScheme: ColorScheme,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Icon Header
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = colorScheme.primaryContainer.copy(alpha = 0.7f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = colorScheme.primary
                )
            }
        }

        Text(
            text = stringResource(R.string.location_services_required),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Privacy Assurance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.privacy_first),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.primary
                    )
                }
                
                Text(
                    text = stringResource(R.string.location_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.location_needs_for),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    
                    Text(
                        text = stringResource(R.string.location_needs_bullets),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEnableLocation,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.open_location_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.check_again),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationNotAvailableContent(
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = colorScheme.errorContainer
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
            text = stringResource(R.string.location_services_unavailable),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.errorContainer.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.location_unavailable_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LocationCheckingContent(
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
            color = colorScheme.onSurface
        )

        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = colorScheme.primary,
            strokeWidth = 4.dp
        )

        Text(
            text = stringResource(R.string.checking_location_services),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Location Check - Disabled Light")
@Composable
fun LocationCheckDisabledLightPreview() {
    BitchatTheme(darkTheme = false) {
        Surface {
            LocationCheckScreen(
                modifier = Modifier.fillMaxSize(),
                status = LocationStatus.DISABLED,
                onEnableLocation = {},
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Location Check - Disabled Dark")
@Composable
fun LocationCheckDisabledDarkPreview() {
    BitchatTheme(darkTheme = true) {
        Surface {
            LocationCheckScreen(
                modifier = Modifier.fillMaxSize(),
                status = LocationStatus.DISABLED,
                onEnableLocation = {},
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Location Check - Checking Dark")
@Composable
fun LocationCheckCheckingDarkPreview() {
    BitchatTheme(darkTheme = true) {
        Surface {
            LocationCheckScreen(
                modifier = Modifier.fillMaxSize(),
                status = LocationStatus.ENABLED,
                onEnableLocation = {},
                onRetry = {}
            )
        }
    }
}
