package com.bitchat.android.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.bitchat.android.ui.theme.BitchatTheme

/**
 * Screen shown when checking battery optimization status with a modern, approachable style.
 */
@Composable
fun BatteryOptimizationScreen(
    modifier: Modifier,
    status: BatteryOptimizationStatus,
    onDisableBatteryOptimization: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    
    // Initialize preference manager
    LaunchedEffect(Unit) {
        BatteryOptimizationPreferenceManager.init(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            BatteryOptimizationStatus.ENABLED -> {
                BatteryOptimizationEnabledContent(
                    onDisableBatteryOptimization = onDisableBatteryOptimization,
                    onRetry = onRetry,
                    onSkip = onSkip,
                    colorScheme = colorScheme,
                    isLoading = isLoading
                )
            }
            
            BatteryOptimizationStatus.DISABLED -> {
                BatteryOptimizationCheckingContent(
                    colorScheme = colorScheme
                )
            }
            
            BatteryOptimizationStatus.NOT_SUPPORTED -> {
                BatteryOptimizationNotSupportedContent(
                    onRetry = onRetry,
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
private fun BatteryOptimizationEnabledContent(
    onDisableBatteryOptimization: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    colorScheme: ColorScheme,
    isLoading: Boolean
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer.copy(alpha = 0.7f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.BatteryAlert,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.battery_optimization_detected_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.battery_optimization_explanation_short),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // Benefits section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.benefits_of_disabling),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                    
                    HorizontalDivider(color = colorScheme.onSecondaryContainer.copy(alpha = 0.1f))

                    Text(
                        text = stringResource(R.string.battery_benefits_short),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        // Fixed buttons at the bottom
        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onDisableBatteryOptimization,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.disable_battery_optimization),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.check_again))
                }
                
                TextButton(
                    onClick = {
                        BatteryOptimizationPreferenceManager.setSkipped(context, true)
                        onSkip()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !isLoading
                ) {
                    Text(text = stringResource(R.string.battery_optimization_skip))
                }
            }
        }
    }
}

@Composable
private fun BatteryOptimizationCheckingContent(
    colorScheme: ColorScheme
) {
    Column(
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
            text = stringResource(R.string.battery_optimization_disabled_title),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BatteryOptimizationNotSupportedContent(
    onRetry: () -> Unit,
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
            color = colorScheme.primaryContainer.copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = colorScheme.primary
                )
            }
        }

        Text(
            text = stringResource(R.string.battery_optimization_not_required),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = stringResource(R.string.battery_optimization_not_supported_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.continue_btn),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true, name = "Battery Optimization - Enabled Light")
@Composable
fun BatteryOptimizationEnabledLightPreview() {
    BitchatTheme(darkTheme = false) {
        Surface {
            BatteryOptimizationScreen(
                modifier = Modifier.fillMaxSize(),
                status = BatteryOptimizationStatus.ENABLED,
                onDisableBatteryOptimization = {},
                onRetry = {},
                onSkip = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Battery Optimization - Enabled Dark")
@Composable
fun BatteryOptimizationEnabledDarkPreview() {
    BitchatTheme(darkTheme = true) {
        Surface {
            BatteryOptimizationScreen(
                modifier = Modifier.fillMaxSize(),
                status = BatteryOptimizationStatus.ENABLED,
                onDisableBatteryOptimization = {},
                onRetry = {},
                onSkip = {}
            )
        }
    }
}
