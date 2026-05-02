package com.bitchat.android.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bitchat.android.geohash.ChannelID
import kotlinx.coroutines.launch
import com.bitchat.android.geohash.GeohashChannel
import com.bitchat.android.geohash.GeohashChannelLevel
import com.bitchat.android.geohash.LocationChannelManager
import com.bitchat.android.geohash.GeohashBookmarksStore
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.R
import com.bitchat.android.core.ui.component.sheet.BitchatBottomSheet
import com.bitchat.android.core.ui.component.sheet.BitchatSheetTopBar
import com.bitchat.android.core.ui.component.sheet.BitchatSheetTitle
import com.bitchat.android.ui.theme.BitchatTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Location Channels Sheet for selecting geohash-based location channels.
 * Modernized with Ocean Blue theme and rounded elements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationChannelsSheet(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationManager = LocationChannelManager.getInstance(context)
    val bookmarksStore = remember { GeohashBookmarksStore.getInstance(context) }

    val permissionState by locationManager.permissionState.collectAsStateWithLifecycle()
    val availableChannels by locationManager.availableChannels.collectAsStateWithLifecycle()
    val selectedChannel by locationManager.selectedChannel.collectAsStateWithLifecycle()
    val locationNames by locationManager.locationNames.collectAsStateWithLifecycle()
    val locationServicesEnabled by locationManager.effectiveLocationEnabled.collectAsStateWithLifecycle()

    val bookmarks by bookmarksStore.bookmarks.collectAsStateWithLifecycle()
    val bookmarkNames by bookmarksStore.bookmarkNames.collectAsStateWithLifecycle()

    val geohashParticipantCounts by viewModel.geohashParticipantCounts.collectAsStateWithLifecycle()

    var customGeohash by remember { mutableStateOf("") }
    var customError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0f,
        label = "topBarAlpha"
    )

    val mapPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val gh = result.data?.getStringExtra(GeohashPickerActivity.EXTRA_RESULT_GEOHASH)
            if (!gh.isNullOrBlank()) {
                customGeohash = gh
                customError = null
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    if (isPresented) {
        BitchatBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 72.dp, bottom = 48.dp)
                ) {
                    item(key = "header") {
                        Text(
                            text = stringResource(R.string.location_channels_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    if (locationServicesEnabled) {
                        item(key = "permissions") {
                            Surface(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                color = colorScheme.primaryContainer.copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    when (permissionState) {
                                        LocationChannelManager.PermissionState.DENIED -> {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Warning, contentDescription = null, tint = colorScheme.error, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = stringResource(R.string.location_permission_denied),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = colorScheme.error
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                        data = Uri.fromParts("package", context.packageName, null)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            ) {
                                                Text(text = stringResource(R.string.open_settings), style = MaterialTheme.typography.labelLarge)
                                            }
                                        }
                                        LocationChannelManager.PermissionState.AUTHORIZED -> {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = stringResource(R.string.location_permission_granted),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Mesh option
                    item(key = "mesh") {
                        ChannelRow(
                            title = meshTitleWithCount(viewModel),
                            subtitle = stringResource(R.string.location_bluetooth_subtitle, bluetoothRangeString()),
                            isSelected = selectedChannel is ChannelID.Mesh,
                            titleColor = colorScheme.primary,
                            titleBold = meshCount(viewModel) > 0,
                            trailingContent = null,
                            onClick = {
                                locationManager.select(ChannelID.Mesh)
                                onDismiss()
                            }
                        )
                    }

                    // Nearby options
                    if (availableChannels.isNotEmpty() && locationServicesEnabled) {
                        val nearbyChannels = availableChannels.filter { it.level != GeohashChannelLevel.BUILDING }
                        items(nearbyChannels) { channel ->
                            val coverage = coverageString(channel.geohash.length)
                            val nameBase = locationNames[channel.level]
                            val namePart = nameBase?.let { formattedNamePrefix(channel.level) + it }
                            val subtitlePrefix = "#${channel.geohash} • $coverage"
                            val participantCount = geohashParticipantCounts[channel.geohash] ?: 0
                            val highlight = participantCount > 0
                            val isBookmarked = bookmarksStore.isBookmarked(channel.geohash)

                            ChannelRow(
                                title = geohashTitleWithCount(channel, participantCount),
                                subtitle = subtitlePrefix + (namePart?.let { " • $it" } ?: ""),
                                isSelected = isChannelSelected(channel, selectedChannel),
                                titleColor = colorScheme.secondary,
                                titleBold = highlight,
                                trailingContent = {
                                IconButton(onClick = { bookmarksStore.toggle(channel.geohash) }) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        tint = if (isBookmarked) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.38f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                },
                                onClick = {
                                    locationManager.setTeleported(false)
                                    locationManager.select(ChannelID.Location(channel))
                                    onDismiss()
                                }
                            )
                        }
                    } else if (permissionState == LocationChannelManager.PermissionState.AUTHORIZED && locationServicesEnabled) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.finding_nearby_channels),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Bookmarked
                    if (bookmarks.isNotEmpty()) {
                        item(key = "bookmarked_header") {
                            SectionHeader(text = stringResource(R.string.bookmarked))
                        }
                        items(bookmarks) { gh ->
                            val level = levelForLength(gh.length)
                            val channel = GeohashChannel(level = level, geohash = gh)
                            val coverage = coverageString(gh.length)
                            val name = bookmarkNames[gh]
                            val subtitle = "#${gh} • $coverage" + (name?.let { " • ${formattedNamePrefix(level)}$it" } ?: "")
                            val participantCount = geohashParticipantCounts[gh] ?: 0
                            val title = geohashHashTitleWithCount(gh, participantCount)

                            ChannelRow(
                                title = title,
                                subtitle = subtitle,
                                isSelected = isChannelSelected(channel, selectedChannel),
                                titleColor = colorScheme.secondary,
                                titleBold = participantCount > 0,
                                trailingContent = {
                                    IconButton(onClick = { bookmarksStore.toggle(gh) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    val inRegional = availableChannels.any { it.geohash == gh }
                                    locationManager.setTeleported(!inRegional && availableChannels.isNotEmpty())
                                    locationManager.select(ChannelID.Location(channel))
                                    onDismiss()
                                }
                            )
                            LaunchedEffect(gh) { bookmarksStore.resolveNameIfNeeded(gh) }
                        }
                    }

                    // Custom geohash teleport
                    item(key = "custom_geohash") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                                    
                                    Box(modifier = Modifier.weight(1f)) {
                                        BasicTextField(
                                            value = customGeohash,
                                            onValueChange = { newValue ->
                                                val allowed = "0123456789bcdefghjkmnpqrstuvwxyz".toSet()
                                                customGeohash = newValue.lowercase().replace("#", "").filter { it in allowed }.take(12)
                                                customError = null
                                            },
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.fillMaxWidth().onFocusChanged { 
                                                if (it.isFocused) {
                                                    coroutineScope.launch { 
                                                        sheetState.expand()
                                                        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                                                    }
                                                }
                                            },
                                            singleLine = true
                                        )
                                        if (customGeohash.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.geohash_placeholder),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            val initial = when {
                                                customGeohash.isNotBlank() -> customGeohash
                                                selectedChannel is ChannelID.Location -> (selectedChannel as ChannelID.Location).channel.geohash
                                                else -> ""
                                            }
                                            val intent = Intent(context, GeohashPickerActivity::class.java).apply {
                                                putExtra(GeohashPickerActivity.EXTRA_INITIAL_GEOHASH, initial)
                                            }
                                            mapPickerLauncher.launch(intent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                val normalized = customGeohash.trim().lowercase()
                                val isValid = validateGeohash(normalized)

                                Button(
                                    onClick = {
                                        if (isValid) {
                                            val level = levelForLength(normalized.length)
                                            val channel = GeohashChannel(level = level, geohash = normalized)
                                            locationManager.setTeleported(true)
                                            locationManager.select(ChannelID.Location(channel))
                                            onDismiss()
                                        } else {
                                            customError = context.getString(R.string.invalid_geohash)
                                        }
                                    },
                                    enabled = isValid,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = stringResource(R.string.teleport))
                                }
                            }
                        }
                    }

                    if (customError != null) {
                        item(key = "geohash_error") {
                            Text(
                                text = customError!!,
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.error,
                                modifier = Modifier.padding(horizontal = 40.dp)
                            )
                        }
                    }

                    item(key = "location_toggle") {
                        Box(modifier = Modifier.padding(24.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (locationServicesEnabled) locationManager.disableLocationServices()
                                    else locationManager.enableLocationServices()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (locationServicesEnabled) colorScheme.error else colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (locationServicesEnabled) Icons.Default.LocationOff else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (locationServicesEnabled) stringResource(R.string.disable_location_services) else stringResource(R.string.enable_location_services),
                                )
                            }
                        }
                    }
                }

                BitchatSheetTopBar(
                    onClose = onDismiss,
                    modifier = modifier.align(Alignment.TopCenter),
                    backgroundAlpha = topBarAlpha,
                    title = {
                        BitchatSheetTitle(text = stringResource(R.string.location_channels_title))
                    }
                )
            }
        }
    }

    DisposableEffect(isPresented, permissionState, locationServicesEnabled) {
        if (isPresented && permissionState == LocationChannelManager.PermissionState.AUTHORIZED && locationServicesEnabled) {
            locationManager.refreshChannels()
            locationManager.beginLiveRefresh()
        }
        onDispose { locationManager.endLiveRefresh() }
    }

    LaunchedEffect(isPresented, availableChannels, bookmarks) {
        if (isPresented) {
            val geohashes = (availableChannels.map { it.geohash } + bookmarks).toSet().toList()
            viewModel.beginGeohashSampling(geohashes)
        } else {
            viewModel.endGeohashSampling()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.endGeohashSampling() }
    }
}

@Composable
private fun SectionHeader(text: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        ),
        color = colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun ChannelRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    titleColor: Color? = null,
    titleBold: Boolean = false,
    trailingContent: (@Composable (() -> Unit))? = null,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        color = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val (baseTitle, countSuffix) = splitTitleAndCount(title)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = baseTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (titleBold || isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) colorScheme.primary else (titleColor ?: colorScheme.onSurface)
                    )

                    countSuffix?.let { count ->
                        Surface(
                            color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = count.replace("[", "").replace("]", ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (trailingContent != null) {
                    trailingContent()
                }
            }
        }
    }
}

private fun splitTitleAndCount(title: String): Pair<String, String?> {
    val lastBracketIndex = title.lastIndexOf('[')
    return if (lastBracketIndex != -1) {
        val prefix = title.substring(0, lastBracketIndex).trim()
        val suffix = title.substring(lastBracketIndex)
        Pair(prefix, suffix)
    } else {
        Pair(title, null)
    }
}

@Composable
private fun meshTitleWithCount(viewModel: ChatViewModel): String {
    val meshCount = meshCount(viewModel)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val peopleText = ctx.resources.getQuantityString(com.bitchat.android.R.plurals.people_count, meshCount, meshCount)
    val meshLabel = stringResource(com.bitchat.android.R.string.mesh_label)
    return "$meshLabel [$peopleText]"
}

private fun meshCount(viewModel: ChatViewModel): Int {
    val myID = viewModel.meshService.myPeerID
    return viewModel.connectedPeers.value.count { peerID -> peerID != myID }
}

@Composable
private fun geohashTitleWithCount(channel: GeohashChannel, participantCount: Int): String {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val isHighPrecision = channel.level.precision > 5
    val peopleText = if (isHighPrecision && participantCount == 0) {
        ctx.resources.getQuantityString(com.bitchat.android.R.plurals.people_count, 0, 0).replace("0", "?")
    } else {
        ctx.resources.getQuantityString(com.bitchat.android.R.plurals.people_count, participantCount, participantCount)
    }

    val levelName = when (channel.level) {
        com.bitchat.android.geohash.GeohashChannelLevel.BUILDING -> "Building"
        com.bitchat.android.geohash.GeohashChannelLevel.BLOCK -> stringResource(com.bitchat.android.R.string.location_level_block)
        com.bitchat.android.geohash.GeohashChannelLevel.NEIGHBORHOOD -> stringResource(com.bitchat.android.R.string.location_level_neighborhood)
        com.bitchat.android.geohash.GeohashChannelLevel.CITY -> stringResource(com.bitchat.android.R.string.location_level_city)
        com.bitchat.android.geohash.GeohashChannelLevel.PROVINCE -> stringResource(com.bitchat.android.R.string.location_level_province)
        com.bitchat.android.geohash.GeohashChannelLevel.REGION -> stringResource(com.bitchat.android.R.string.location_level_region)
    }
    return "$levelName [$peopleText]"
}

@Composable
private fun geohashHashTitleWithCount(geohash: String, participantCount: Int): String {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val level = levelForLength(geohash.length)
    val isHighPrecision = level.precision > 5

    val peopleText = if (isHighPrecision && participantCount == 0) {
        ctx.resources.getQuantityString(com.bitchat.android.R.plurals.people_count, 0, 0).replace("0", "?")
    } else {
        ctx.resources.getQuantityString(com.bitchat.android.R.plurals.people_count, participantCount, participantCount)
    }
    
    return "#$geohash [$peopleText]"
}

private fun isChannelSelected(channel: GeohashChannel, selectedChannel: ChannelID?): Boolean {
    return when (selectedChannel) {
        is ChannelID.Location -> selectedChannel.channel == channel
        else -> false
    }
}

private fun validateGeohash(geohash: String): Boolean {
    if (geohash.isEmpty() || geohash.length > 12) return false
    val allowed = "0123456789bcdefghjkmnpqrstuvwxyz".toSet()
    return geohash.all { it in allowed }
}

private fun levelForLength(length: Int): GeohashChannelLevel {
    return when (length) {
        in 0..2 -> GeohashChannelLevel.REGION
        in 3..4 -> GeohashChannelLevel.PROVINCE
        5 -> GeohashChannelLevel.CITY
        6 -> GeohashChannelLevel.NEIGHBORHOOD
        7 -> GeohashChannelLevel.BLOCK
        8 -> GeohashChannelLevel.BUILDING
        else -> if (length > 8) GeohashChannelLevel.BUILDING else GeohashChannelLevel.BLOCK
    }
}

private fun coverageString(precision: Int): String {
    val maxMeters = when (precision) {
        2 -> 1_250_000.0
        3 -> 156_000.0
        4 -> 39_100.0
        5 -> 4_890.0
        6 -> 1_220.0
        7 -> 153.0
        8 -> 38.2
        9 -> 4.77
        10 -> 1.19
        else -> if (precision <= 1) 5_000_000.0 else 1.19 * Math.pow(0.25, (precision - 10).toDouble())
    }
    val km = maxMeters / 1000.0
    return "~${formatDistance(km)} km"
}

private fun formatDistance(value: Double): String {
    return when {
        value >= 100 -> String.format("%.0f", value)
        value >= 10 -> String.format("%.1f", value)
        else -> String.format("%.1f", value)
    }
}

private fun bluetoothRangeString(): String {
    return "~10–50 m"
}

private fun formattedNamePrefix(level: GeohashChannelLevel): String {
    return "~"
}

@Preview(showBackground = true, name = "Location Channels - Light")
@Composable
fun LocationChannelsLightPreview() {
    BitchatTheme(darkTheme = false) {
        Surface {
            // Preview logic would go here, requires mock ViewModel
        }
    }
}
