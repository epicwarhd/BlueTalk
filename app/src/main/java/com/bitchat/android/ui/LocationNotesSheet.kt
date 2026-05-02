package com.bitchat.android.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.bitchat.android.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.core.ui.component.sheet.BitchatBottomSheet
import com.bitchat.android.core.ui.component.sheet.BitchatSheetTopBar
import com.bitchat.android.core.ui.component.sheet.BitchatSheetTitle
import com.bitchat.android.geohash.GeohashChannelLevel
import com.bitchat.android.geohash.LocationChannelManager
import com.bitchat.android.nostr.LocationNotesManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

/**
 * Location Notes Sheet modernized with Ocean Blue theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationNotesSheet(
    geohash: String,
    locationName: String?,
    nickname: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    
    val notesManager = remember { LocationNotesManager.getInstance() }
    val locationManager = remember { LocationChannelManager.getInstance(context) }
    
    val notes by notesManager.notes.collectAsStateWithLifecycle()
    val state by notesManager.state.collectAsStateWithLifecycle(LocationNotesManager.State.IDLE)
    val errorMessage by notesManager.errorMessage.collectAsStateWithLifecycle()
    val initialLoadComplete by notesManager.initialLoadComplete.collectAsStateWithLifecycle(false)
    
    val count = notes.size
    
    val locationNames by locationManager.locationNames.collectAsStateWithLifecycle()
    val displayLocationName = locationNames[GeohashChannelLevel.BUILDING]?.takeIf { it.isNotEmpty() }
        ?: locationNames[GeohashChannelLevel.BLOCK]?.takeIf { it.isNotEmpty() }
    
    var draft by remember { mutableStateOf("") }
    val sendButtonEnabled = draft.trim().isNotEmpty() && state != LocationNotesManager.State.NO_RELAYS
    
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

    LaunchedEffect(Unit) {
        locationManager.refreshChannels()
    }

    LaunchedEffect(geohash) {
        notesManager.setGeohash(geohash)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            notesManager.cancel()
        }
    }

    BitchatBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 72.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "notes_header") {
                    LocationNotesHeader(
                        locationName = displayLocationName,
                        state = state,
                        colorScheme = colorScheme
                    )
                }

                when {
                    state == LocationNotesManager.State.NO_RELAYS -> {
                        item {
                            NoRelaysRow(
                                onRetry = { notesManager.refresh() },
                                colorScheme = colorScheme
                            )
                        }
                    }
                    state == LocationNotesManager.State.LOADING && !initialLoadComplete -> {
                        item {
                            LoadingRow(colorScheme = colorScheme)
                        }
                    }
                    notes.isEmpty() -> {
                        item {
                            EmptyRow(colorScheme = colorScheme)
                        }
                    }
                    else -> {
                        items(notes, key = { it.id }) { note ->
                            NoteRow(note = note, colorScheme = colorScheme)
                        }
                    }
                }

                errorMessage?.let { error ->
                    if (state != LocationNotesManager.State.NO_RELAYS) {
                        item {
                            ErrorRow(
                                message = error,
                                onDismiss = { notesManager.clearError() },
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
            }

            BitchatSheetTopBar(
                onClose = onDismiss,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundAlpha = topBarAlpha,
                title = {
                    BitchatSheetTitle(
                        text = pluralStringResource(
                            id = R.plurals.location_notes_title,
                            count = count,
                            geohash,
                            count
                        )
                    )
                }
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                LocationNotesInputSection(
                    draft = draft,
                    onDraftChange = { draft = it },
                    sendButtonEnabled = sendButtonEnabled,
                    colorScheme = colorScheme,
                    onSend = {
                        val content = draft.trim()
                        if (content.isNotEmpty()) {
                            notesManager.send(content, nickname)
                            draft = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationNotesHeader(
    locationName: String?,
    state: LocationNotesManager.State,
    colorScheme: ColorScheme,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        locationName?.let { name ->
            if (name.isNotEmpty()) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary
                )
            }
        }
        
        Text(
            text = stringResource(R.string.location_notes_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        
        if (state == LocationNotesManager.State.NO_RELAYS) {
            Surface(
                color = colorScheme.errorContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.location_notes_relays_unavailable),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NoteRow(note: LocationNotesManager.Note, colorScheme: ColorScheme) {
    val baseName = note.displayName.split("#", limit = 2).firstOrNull() ?: note.displayName
    val ts = timestampText(note.createdAt)
    
    Surface(
        color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "@$baseName",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.secondary
                )
                if (ts.isNotEmpty()) {
                    Text(
                        text = ts,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NoRelaysRow(onRetry: () -> Unit, colorScheme: ColorScheme) {
    Surface(
        color = colorScheme.errorContainer.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.location_notes_no_relays_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.error
            )
            Text(
                text = stringResource(R.string.location_notes_no_relays_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.retry), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LoadingRow(colorScheme: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.loading_location_notes),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyRow(colorScheme: ColorScheme) {
    Surface(
        color = colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.location_notes_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.location_notes_empty_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorRow(message: String, onDismiss: () -> Unit, colorScheme: ColorScheme) {
    Surface(
        color = colorScheme.errorContainer.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = colorScheme.error, modifier = Modifier.size(20.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun LocationNotesInputSection(
    draft: String,
    onDraftChange: (String) -> Unit,
    sendButtonEnabled: Boolean,
    colorScheme: ColorScheme,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (draft.isEmpty()) {
                    Text(
                        text = stringResource(R.string.location_notes_input_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        IconButton(
            onClick = { if (sendButtonEnabled) onSend() },
            enabled = sendButtonEnabled,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (!sendButtonEnabled) colorScheme.onSurface.copy(alpha = 0.12f) else colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = stringResource(R.string.send_message),
                    modifier = Modifier.size(20.dp),
                    tint = if (sendButtonEnabled) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

private fun timestampText(createdAt: Int): String {
    val date = Date(createdAt * 1000L)
    val now = Date()
    val calendar = Calendar.getInstance()
    calendar.time = date
    val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
    val dateYear = calendar.get(Calendar.YEAR)
    calendar.time = now
    val nowDay = calendar.get(Calendar.DAY_OF_YEAR)
    val nowYear = calendar.get(Calendar.YEAR)
    val daysDiff = if (dateYear == nowYear) nowDay - dateDay else ((now.time - date.time) / (1000 * 60 * 60 * 24)).toInt()
    
    return if (daysDiff < 7) {
        val diffSeconds = (now.time - date.time) / 1000
        when {
            diffSeconds < 60 -> ""
            diffSeconds < 3600 -> "${(diffSeconds / 60).toInt()}m ago"
            diffSeconds < 86400 -> "${(diffSeconds / 3600).toInt()}h ago"
            else -> "${(diffSeconds / 86400).toInt()}d ago"
        }
    } else {
        val formatter = if (dateYear == nowYear) SimpleDateFormat("MMM d", Locale.getDefault()) else SimpleDateFormat("MMM d, y", Locale.getDefault())
        formatter.format(date)
    }
}
