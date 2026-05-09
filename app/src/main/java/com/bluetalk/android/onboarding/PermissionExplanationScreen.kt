package com.bluetalk.android.onboarding

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetalk.android.R
import com.bluetalk.android.ui.theme.BlueTalkTheme

/**
 * Permission explanation screen shown before requesting permissions.
 * Modernized with Ocean Blue theme and approachable layout.
 */
@Composable
fun PermissionExplanationScreen(
    modifier: Modifier,
    permissionCategories: List<PermissionCategory>,
    onContinue: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.about_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Privacy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.privacy_protected),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.privacy_bullets),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Section header
            Text(
                text = stringResource(R.string.permissions_header).uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Permission categories
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                permissionCategories.forEach { category ->
                    PermissionCategoryCard(
                        category = category,
                        colorScheme = colorScheme
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom button
        }

        // Fixed button at bottom with a gradient fade
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, colorScheme.surface),
                        startY = 0f,
                        endY = 50f
                    )
                )
                .padding(24.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.grant_permissions),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun PermissionCategoryCard(
    category: PermissionCategory,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getPermissionIcon(category.type),
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = category.type.nameValue,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun getPermissionIcon(permissionType: PermissionType): ImageVector {
    return when (permissionType) {
        PermissionType.NEARBY_DEVICES -> Icons.Default.Bluetooth
        PermissionType.PRECISE_LOCATION -> Icons.Default.LocationOn
        PermissionType.BACKGROUND_LOCATION -> Icons.Default.LocationOn
        PermissionType.MICROPHONE -> Icons.Default.Mic
        PermissionType.NOTIFICATIONS -> Icons.Default.Notifications
        PermissionType.BATTERY_OPTIMIZATION -> Icons.Default.Power
        PermissionType.OTHER -> Icons.Default.Settings
    }
}

@Preview(showBackground = true, name = "Permission Explanation - Light")
@Composable
fun PermissionExplanationLightPreview() {
    val categories = listOf(
        PermissionCategory(PermissionType.NEARBY_DEVICES, "Required to discover BlueTalk users", emptyList(), false, ""),
        PermissionCategory(PermissionType.PRECISE_LOCATION, "Required for Bluetooth scanning", emptyList(), false, "")
    )
    BlueTalkTheme(darkTheme = false) {
        Surface {
            PermissionExplanationScreen(
                modifier = Modifier.fillMaxSize(),
                permissionCategories = categories,
                onContinue = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Permission Explanation - Dark")
@Composable
fun PermissionExplanationDarkPreview() {
    val categories = listOf(
        PermissionCategory(PermissionType.NEARBY_DEVICES, "Required to discover BlueTalk users", emptyList(), false, ""),
        PermissionCategory(PermissionType.PRECISE_LOCATION, "Required for Bluetooth scanning", emptyList(), false, "")
    )
    BlueTalkTheme(darkTheme = true) {
        Surface {
            PermissionExplanationScreen(
                modifier = Modifier.fillMaxSize(),
                permissionCategories = categories,
                onContinue = {}
            )
        }
    }
}
