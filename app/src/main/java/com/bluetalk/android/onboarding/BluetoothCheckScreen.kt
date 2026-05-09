package com.bluetalk.android.onboarding

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetalk.android.R
import com.bluetalk.android.ui.theme.BlueTalkTheme

/**
 * Screen shown when checking Bluetooth status with a modern, approachable style.
 */
@Composable
fun BluetoothCheckScreen(
    modifier: Modifier,
    status: BluetoothStatus,
    onEnableBluetooth: () -> Unit,
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
            BluetoothStatus.DISABLED -> {
                BluetoothDisabledContent(
                    onEnableBluetooth = onEnableBluetooth,
                    onRetry = onRetry,
                    colorScheme = colorScheme,
                    isLoading = isLoading
                )
            }
            BluetoothStatus.NOT_SUPPORTED -> {
                BluetoothNotSupportedContent(
                    colorScheme = colorScheme
                )
            }
            BluetoothStatus.ENABLED -> {
                BluetoothCheckingContent(
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
private fun BluetoothDisabledContent(
    onEnableBluetooth: () -> Unit,
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
        // Bluetooth Icon Header
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = colorScheme.primaryContainer.copy(alpha = 0.7f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Bluetooth,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = colorScheme.primary
                )
            }
        }

        Text(
            text = stringResource(R.string.bluetooth_required),
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
                Text(
                    text = stringResource(R.string.bluetooth_needs_for),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = stringResource(R.string.bluetooth_needs_bullets),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEnableBluetooth,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.enable_bluetooth),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BluetoothNotSupportedContent(
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
            text = stringResource(R.string.bluetooth_not_supported),
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
                text = stringResource(R.string.bluetooth_unsupported_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BluetoothCheckingContent(
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
            text = stringResource(R.string.checking_bluetooth_status),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Bluetooth Check - Disabled Light")
@Composable
fun BluetoothCheckDisabledLightPreview() {
    BlueTalkTheme(darkTheme = false) {
        Surface {
            BluetoothCheckScreen(
                modifier = Modifier.fillMaxSize(),
                status = BluetoothStatus.DISABLED,
                onEnableBluetooth = {},
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Bluetooth Check - Disabled Dark")
@Composable
fun BluetoothCheckDisabledDarkPreview() {
    BlueTalkTheme(darkTheme = true) {
        Surface {
            BluetoothCheckScreen(
                modifier = Modifier.fillMaxSize(),
                status = BluetoothStatus.DISABLED,
                onEnableBluetooth = {},
                onRetry = {}
            )
        }
    }
}
