package com.bitchat.android.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.bitchat.android.core.ui.component.button.CloseButton
import com.bitchat.android.core.ui.component.sheet.BitchatBottomSheet
import com.bitchat.android.net.ArtiTorManager
import com.bitchat.android.net.TorMode
import com.bitchat.android.net.TorPreferenceManager
import com.bitchat.android.nostr.NostrProofOfWork
import com.bitchat.android.nostr.PoWPreferenceManager
import com.bitchat.android.ui.theme.BitchatTheme

/**
 * Feature row for displaying app capabilities
 */
@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Theme selection chip
 */
@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
            )
        }
    }
}

/**
 * Unified settings toggle row
 */
@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    statusIndicator: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (enabled) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.38f)
                )
                statusIndicator?.invoke()
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.8f else 0.4f),
                lineHeight = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onCheckedChange(it) },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = colorScheme.primary,
                checkedThumbColor = colorScheme.onPrimary
            )
        )
    }
}

/**
 * Modernized About/Settings Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onShowDebug: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    val lazyListState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0f,
        label = "topBarAlpha"
    )

    val colorScheme = MaterialTheme.colorScheme
    
    if (isPresented) {
        BitchatBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismiss,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 72.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header Section
                    item(key = "header") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1).sp
                                ),
                                color = colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.version_prefix, versionName ?: ""),
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.about_tagline),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Features Section
                    item(key = "features") {
                        SettingsGroup(title = stringResource(R.string.permissions_header).uppercase()) {
                            Column {
                                FeatureRow(
                                    icon = Icons.Default.Bluetooth,
                                    title = stringResource(R.string.about_offline_mesh_title),
                                    subtitle = stringResource(R.string.about_offline_mesh_desc)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                FeatureRow(
                                    icon = Icons.Default.Public,
                                    title = stringResource(R.string.about_online_geohash_title),
                                    subtitle = stringResource(R.string.about_online_geohash_desc)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                FeatureRow(
                                    icon = Icons.Default.Lock,
                                    title = stringResource(R.string.about_e2e_title),
                                    subtitle = stringResource(R.string.about_e2e_desc)
                                )
                            }
                        }
                    }

                    // Appearance Section
                    item(key = "appearance") {
                        SettingsGroup(title = stringResource(R.string.about_appearance).uppercase()) {
                            val themePref by com.bitchat.android.ui.theme.ThemePreferenceManager.themeFlow.collectAsState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemeChip(
                                    label = stringResource(R.string.about_system),
                                    selected = themePref.isSystem,
                                    onClick = { com.bitchat.android.ui.theme.ThemePreferenceManager.set(context, com.bitchat.android.ui.theme.ThemePreference.System) },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeChip(
                                    label = stringResource(R.string.about_light),
                                    selected = themePref.isLight,
                                    onClick = { com.bitchat.android.ui.theme.ThemePreferenceManager.set(context, com.bitchat.android.ui.theme.ThemePreference.Light) },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeChip(
                                    label = stringResource(R.string.about_dark),
                                    selected = themePref.isDark,
                                    onClick = { com.bitchat.android.ui.theme.ThemePreferenceManager.set(context, com.bitchat.android.ui.theme.ThemePreference.Dark) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Settings Section
                    item(key = "settings") {
                        LaunchedEffect(Unit) { PoWPreferenceManager.init(context) }
                        val powEnabled by PoWPreferenceManager.powEnabled.collectAsState()
                        var backgroundEnabled by remember { mutableStateOf(com.bitchat.android.service.MeshServicePreferences.isBackgroundEnabled(true)) }
                        val torMode = remember { mutableStateOf(TorPreferenceManager.get(context)) }
                        val torProvider = remember { ArtiTorManager.getInstance() }
                        val torStatus by torProvider.statusFlow.collectAsState()
                        val torAvailable = remember { torProvider.isTorAvailable() }

                        SettingsGroup(title = stringResource(R.string.about_network).uppercase()) {
                            Column {
                                SettingsToggleRow(
                                    icon = Icons.Default.Bluetooth,
                                    title = stringResource(R.string.about_background_title),
                                    subtitle = stringResource(R.string.about_background_desc),
                                    checked = backgroundEnabled,
                                    onCheckedChange = { enabled ->
                                        backgroundEnabled = enabled
                                        com.bitchat.android.service.MeshServicePreferences.setBackgroundEnabled(enabled)
                                        if (!enabled) {
                                            com.bitchat.android.service.MeshForegroundService.stop(context)
                                        } else {
                                            com.bitchat.android.service.MeshForegroundService.start(context)
                                        }
                                    }
                                )
                                
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                
                                SettingsToggleRow(
                                    icon = Icons.Default.Speed,
                                    title = stringResource(R.string.about_pow),
                                    subtitle = stringResource(R.string.about_pow_tip),
                                    checked = powEnabled,
                                    onCheckedChange = { PoWPreferenceManager.setPowEnabled(it) }
                                )
                                
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                
                                SettingsToggleRow(
                                    icon = Icons.Default.Security,
                                    title = "Tor Network",
                                    subtitle = stringResource(R.string.about_tor_route),
                                    checked = torMode.value == TorMode.ON,
                                    onCheckedChange = { enabled ->
                                        if (torAvailable) {
                                            torMode.value = if (enabled) TorMode.ON else TorMode.OFF
                                            TorPreferenceManager.set(context, torMode.value)
                                        }
                                    },
                                    enabled = torAvailable,
                                    statusIndicator = if (torMode.value == TorMode.ON) {
                                        {
                                            val statusColor = when {
                                                torStatus.running && torStatus.bootstrapPercent >= 100 -> colorScheme.primary
                                                torStatus.running -> colorScheme.secondary
                                                else -> colorScheme.error
                                            }
                                            Surface(
                                                color = statusColor,
                                                shape = CircleShape,
                                                modifier = Modifier.size(10.dp)
                                            ) {}
                                        }
                                    } else null
                                )
                                
                                if (!torAvailable) {
                                    Text(
                                        text = stringResource(R.string.tor_not_available_in_this_build),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.error,
                                        modifier = Modifier.padding(start = 56.dp, bottom = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // PoW Difficulty Slider
                    item(key = "pow_slider") {
                        val powEnabled by PoWPreferenceManager.powEnabled.collectAsState()
                        val powDifficulty by PoWPreferenceManager.powDifficulty.collectAsState()
                        
                        if (powEnabled) {
                            SettingsGroup {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Mining Difficulty",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colorScheme.onSurface
                                        )
                                        Badge(
                                            containerColor = colorScheme.primaryContainer,
                                            contentColor = colorScheme.onPrimaryContainer
                                        ) {
                                            Text(
                                                text = "$powDifficulty bits",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    
                                    Slider(
                                        value = powDifficulty.toFloat(),
                                        onValueChange = { PoWPreferenceManager.setPowDifficulty(it.toInt()) },
                                        valueRange = 0f..32f,
                                        steps = 31,
                                        colors = SliderDefaults.colors(
                                            thumbColor = colorScheme.primary,
                                            activeTrackColor = colorScheme.primary
                                        )
                                    )
                                    
                                    Text(
                                        text = "${NostrProofOfWork.estimateMiningTime(powDifficulty)} estimate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Emergency Warning
                    item(key = "warning") {
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            color = colorScheme.errorContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.about_emergency_title),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.error
                                    )
                                    Text(
                                        text = stringResource(R.string.about_emergency_tip),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onErrorContainer,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Footer
                    item(key = "footer") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (onShowDebug != null) {
                                OutlinedButton(
                                    onClick = onShowDebug,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = stringResource(R.string.about_debug_settings))
                                }
                            }
                            Text(
                                text = stringResource(R.string.about_footer),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Top Bar
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(64.dp),
                    color = colorScheme.surface.copy(alpha = topBarAlpha),
                    tonalElevation = if (isScrolled) 2.dp else 0.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.align(Alignment.Center),
                            color = colorScheme.onSurface.copy(alpha = topBarAlpha)
                        )
                        CloseButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
            content = content
        )
    }
}

/**
 * Password prompt dialog
 */
@Composable
fun PasswordPromptDialog(
    show: Boolean,
    channelName: String?,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show && channelName != null) {
        val colorScheme = MaterialTheme.colorScheme
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.pwd_prompt_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.pwd_prompt_message, channelName ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = onPasswordChange,
                        label = { Text(stringResource(R.string.pwd_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.join))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            containerColor = colorScheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Preview(showBackground = true, name = "About Sheet - Light")
@Composable
fun AboutSheetLightPreview() {
    BitchatTheme(darkTheme = false) {
        Surface {
            AboutSheet(isPresented = true, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "About Sheet - Dark")
@Composable
fun AboutSheetDarkPreview() {
    BitchatTheme(darkTheme = true) {
        Surface {
            AboutSheet(isPresented = true, onDismiss = {})
        }
    }
}
