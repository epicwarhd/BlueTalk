package com.bluetalk.android.ui

import com.bluetalk.android.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetalk.android.core.ui.component.button.CloseButton
import com.bluetalk.android.core.ui.component.sheet.BlueTalkBottomSheet
import com.bluetalk.android.core.ui.component.sheet.BlueTalkSheetCenterTopBar
import com.bluetalk.android.core.ui.component.sheet.BlueTalkSheetTitle
import com.bluetalk.android.core.ui.component.sheet.BlueTalkSheetTopBar
import com.bluetalk.android.ui.theme.BlueTalkTheme


/**
 * Sheet components for ChatScreen - mesh-only version
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshPeerListSheet(
    isPresented: Boolean,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onShowVerification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val connectedPeers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val joinedChannels by viewModel.joinedChannels.collectAsStateWithLifecycle()
    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val selectedPrivatePeer by viewModel.selectedPrivateChatPeer.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val unreadChannelMessages by viewModel.unreadChannelMessages.collectAsStateWithLifecycle()
    val peerNicknames by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val peerRSSI by viewModel.peerRSSI.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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

    if (isPresented) {
        BlueTalkBottomSheet(
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
                    if (joinedChannels.isNotEmpty()) {
                        item(key = "channels_header") {
                            SectionHeader(text = stringResource(id = R.string.channels))
                        }

                        items(
                            items = joinedChannels.toList(),
                            key = { "channel_$it" }
                        ) { channel ->
                            val isSelected = channel == currentChannel
                            val unreadCount = unreadChannelMessages[channel] ?: 0

                            ChannelRow(
                                channel = channel,
                                isSelected = isSelected,
                                unreadCount = unreadCount,
                                colorScheme = colorScheme,
                                onChannelClick = {
                                    if (channel.startsWith("@")) {
                                        val peerName = channel.removePrefix("@")
                                        val peerID =
                                            peerNicknames.entries.firstOrNull { it.value == peerName }?.key
                                    if (peerID != null) {
                                            viewModel.showPrivateChatSheet(peerID)
                                            onDismiss()
                                        }
                                    } else {
                                        viewModel.switchToChannel(channel)
                                        onDismiss()
                                    }
                                },
                                onLeaveChannel = {
                                    viewModel.leaveChannel(channel)
                                },
                            )
                        }
                    }

                    item(key = "people_section") {
                        PeopleSection(
                            modifier = Modifier.padding(top = if (joinedChannels.isNotEmpty()) 24.dp else 0.dp),
                            connectedPeers = connectedPeers,
                            peerNicknames = peerNicknames,
                            peerRSSI = peerRSSI,
                            nickname = nickname,
                            colorScheme = colorScheme,
                            selectedPrivatePeer = selectedPrivatePeer,
                            viewModel = viewModel,
                                onPrivateChatStart = { peerID ->
                                    viewModel.showPrivateChatSheet(peerID)
                                    onDismiss()
                                }
                        )
                    }
                }

                BlueTalkSheetTopBar(
                    title = {
                        BlueTalkSheetTitle(text = stringResource(id = R.string.your_network))
                    },
                    backgroundAlpha = topBarAlpha,
                    actions = {
                        IconButton(
                            onClick = onShowVerification,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode,
                                contentDescription = stringResource(R.string.verify_title),
                                tint = colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    onClose = onDismiss,
                )
            }
        }

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
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun ChannelRow(
    channel: String,
    isSelected: Boolean,
    unreadCount: Int,
    colorScheme: ColorScheme,
    onChannelClick: () -> Unit,
    onLeaveChannel: () -> Unit,
) {
    Surface(
        onClick = onChannelClick,
        color = if (isSelected) {
            colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (unreadCount > 0) {
                    UnreadBadge(
                        count = unreadCount,
                        colorScheme = colorScheme
                    )
                }
                
                Text(
                    text = channel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            IconButton(
                onClick = onLeaveChannel,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.leave_channel),
                    tint = colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}



@Composable
fun PeopleSection(
    modifier: Modifier  = Modifier,
    connectedPeers: List<String>,
    peerNicknames: Map<String, String>,
    peerRSSI: Map<String, Int>,
    nickname: String,
    colorScheme: ColorScheme,
    selectedPrivatePeer: String?,
    viewModel: ChatViewModel,
    onPrivateChatStart: (String) -> Unit
) {
    Column(modifier = modifier) {
        SectionHeader(text = stringResource(id = R.string.people))

        if (connectedPeers.isEmpty()) {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.no_one_connected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        val hasUnreadPrivateMessages by viewModel.unreadPrivateMessages.collectAsStateWithLifecycle()
        val privateChats by viewModel.privateChats.collectAsStateWithLifecycle()
        val favoritePeers by viewModel.favoritePeers.collectAsStateWithLifecycle()
        val peerFingerprints by viewModel.peerFingerprints.collectAsStateWithLifecycle()
        val verifiedFingerprints by viewModel.verifiedFingerprints.collectAsStateWithLifecycle()

        val peerFavoriteStates = remember(favoritePeers, peerFingerprints, connectedPeers) {
            connectedPeers.associateWith { peerID ->
                val fingerprint = peerFingerprints[peerID]
                fingerprint != null && favoritePeers.contains(fingerprint)
            }
        }

        val peerVerifiedStates = remember(verifiedFingerprints, peerFingerprints, connectedPeers) {
            connectedPeers.associateWith { peerID ->
                viewModel.isPeerVerified(peerID, verifiedFingerprints)
            }
        }

        val sortedPeers = connectedPeers.sortedWith(
            compareBy<String> { !hasUnreadPrivateMessages.contains(it) }
            .thenByDescending { privateChats[it]?.maxByOrNull { msg -> msg.timestamp }?.timestamp?.time ?: 0L }
            .thenBy { !(peerFavoriteStates[it] ?: false) }
            .thenBy { (if (it == nickname) "You" else (peerNicknames[it] ?: it)).lowercase() }
        )
        
        fun computeDisplayNameForPeerId(key: String): String {
            return if (key == nickname) "You" else (peerNicknames[key] ?: (privateChats[key]?.lastOrNull()?.sender ?: key.take(12)))
        }

        val baseNameCounts = mutableMapOf<String, Int>()

        sortedPeers.forEach { pid ->
            val dn = computeDisplayNameForPeerId(pid)
            val (b, _) = splitSuffix(dn)
            if (b != "You") baseNameCounts[b] = (baseNameCounts[b] ?: 0) + 1
        }

        val offlineFavorites = com.bluetalk.android.favorites.FavoritesPersistenceService.shared.getOurFavorites()
        offlineFavorites.forEach { fav ->
            val favPeerID = fav.peerNoisePublicKey.joinToString("") { b -> "%02x".format(b) }
            val dn = peerNicknames[favPeerID] ?: fav.peerNickname
            val (b, _) = splitSuffix(dn)
            if (b != "You") baseNameCounts[b] = (baseNameCounts[b] ?: 0) + 1
        }

        sortedPeers.forEach { peerID ->
            val isFavorite = peerFavoriteStates[peerID] ?: false
            val isVerified = peerVerifiedStates[peerID] ?: false

            val combinedHasUnread = hasUnreadPrivateMessages.contains(peerID)
            val combinedUnreadCount = privateChats[peerID]?.count { msg -> msg.sender != nickname && combinedHasUnread } ?: 0

            val displayName = if (peerID == nickname) "You" else (peerNicknames[peerID] ?: (privateChats[peerID]?.lastOrNull()?.sender ?: peerID.take(12)))
            val (bName, _) = splitSuffix(displayName)
            val showHash = (baseNameCounts[bName] ?: 0) > 1

            val directMap by viewModel.peerDirect.collectAsStateWithLifecycle()
            val isDirectLive = directMap[peerID] ?: try { viewModel.meshService.getPeerInfo(peerID)?.isDirectConnection == true } catch (_: Exception) { false }
            PeerItem(
                peerID = peerID,
                displayName = displayName,
                isDirect = isDirectLive,
                isSelected = peerID == selectedPrivatePeer,
                isFavorite = isFavorite,
                isVerified = isVerified,
                hasUnreadDM = combinedHasUnread,
                colorScheme = colorScheme,
                viewModel = viewModel,
                onItemClick = { onPrivateChatStart(peerID) },
                onToggleFavorite = { 
                    viewModel.toggleFavorite(peerID) 
                },
                unreadCount = if (combinedUnreadCount > 0) combinedUnreadCount else if (combinedHasUnread) 1 else 0,
                showHashSuffix = showHash
            )
        }

        offlineFavorites.forEach { fav ->
            val favPeerID = fav.peerNoisePublicKey.joinToString("") { b -> "%02x".format(b) }
            if (connectedPeers.contains(favPeerID)) return@forEach

            val hasUnread = hasUnreadPrivateMessages.contains(favPeerID)
            val dn = peerNicknames[favPeerID] ?: fav.peerNickname
            val (bName, _) = splitSuffix(dn)
            val showHash = (baseNameCounts[bName] ?: 0) > 1

            val isVerified = viewModel.isNoisePublicKeyVerified(fav.peerNoisePublicKey, verifiedFingerprints)

            val unreadCount = privateChats[favPeerID]?.count { msg -> msg.sender != nickname && hasUnreadPrivateMessages.contains(favPeerID) } ?: 0

            PeerItem(
                peerID = favPeerID,
                displayName = dn,
                isDirect = false,
                isSelected = favPeerID == selectedPrivatePeer,
                isFavorite = true,
                isVerified = isVerified,
                hasUnreadDM = hasUnread,
                colorScheme = colorScheme,
                viewModel = viewModel,
                onItemClick = { onPrivateChatStart(favPeerID) },
                onToggleFavorite = { 
                    viewModel.toggleFavorite(favPeerID)
                },
                unreadCount = if (unreadCount > 0) unreadCount else if (hasUnread) 1 else 0,
                showHashSuffix = showHash
            )
        }
    }
}

@Composable
private fun PeerItem(
    peerID: String,
    displayName: String,
    isDirect: Boolean,
    isSelected: Boolean,
    isFavorite: Boolean,
    isVerified: Boolean,
    hasUnreadDM: Boolean,
    colorScheme: ColorScheme,
    viewModel: ChatViewModel,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    unreadCount: Int = 0,
    showHashSuffix: Boolean = true
) {
    val currentNickname by viewModel.nickname.collectAsStateWithLifecycle()
    val (baseNameRaw, suffixRaw) = splitSuffix(displayName)
    val baseName = truncateNickname(baseNameRaw)
    val suffix = if (showHashSuffix) suffixRaw else ""
    val isMe = displayName == "You" || peerID == currentNickname

    val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f
    val assignedColor = viewModel.colorForMeshPeer(peerID, isDark)
    val baseColor = if (isMe) colorScheme.primary else assignedColor

    Surface(
        onClick = onItemClick,
        color = if (isSelected) {
            colorScheme.primaryContainer.copy(alpha = 0.2f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasUnreadDM) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = stringResource(R.string.cd_unread_message),
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.secondary
                    )
                } else if (!isDirect && isFavorite) {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = stringResource(R.string.cd_offline_favorite),
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                } else {
                    Icon(
                        imageVector = if (isDirect) Icons.Outlined.Bluetooth else Icons.Filled.Route,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = baseName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = baseColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (suffix.isNotEmpty()) {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.bodyMedium,
                            color = baseColor.copy(alpha = 0.6f)
                        )
                    }
                }
                
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.primary
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isFavorite) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.24f)
                )
            }
        }
    }
}

@Composable
private fun UnreadBadge(
    count: Int,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Surface(
            modifier = modifier.defaultMinSize(minWidth = 20.dp, minHeight = 20.dp),
            color = colorScheme.primary,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Nested Private Chat Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatSheet(
    isPresented: Boolean,
    peerID: String,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val privateChats by viewModel.privateChats.collectAsStateWithLifecycle()
    val peerNicknames by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val connectedPeers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val peerDirectMap by viewModel.peerDirect.collectAsStateWithLifecycle()
    val peerSessionStates by viewModel.peerSessionStates.collectAsStateWithLifecycle()
    val favoritePeers by viewModel.favoritePeers.collectAsStateWithLifecycle()
    val peerFingerprints by viewModel.peerFingerprints.collectAsStateWithLifecycle()
    val verifiedFingerprints by viewModel.verifiedFingerprints.collectAsStateWithLifecycle()

    LaunchedEffect(peerID) {
        viewModel.startPrivateChat(peerID)
    }

    val titleText = peerNicknames[peerID] ?: peerID.take(12)

    val messages = privateChats[peerID] ?: emptyList()
    val isDirect = peerDirectMap[peerID] == true
    val isConnected = connectedPeers.contains(peerID) || isDirect
    val sessionState = peerSessionStates[peerID]
    val fingerprint = peerFingerprints[peerID]
    val isFavorite = remember(favoritePeers, fingerprint) {
        if (fingerprint != null) favoritePeers.contains(fingerprint) else viewModel.isFavorite(peerID)
    }

    val isVerified = remember(peerID, verifiedFingerprints) {
        viewModel.isPeerVerified(peerID, verifiedFingerprints)
    }

    val securityModifier = Modifier.clickable { viewModel.showSecurityVerificationSheet() }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isPresented) {
        BlueTalkBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(72.dp))

                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                    var forceScrollToBottom by remember { mutableStateOf(false) }
                    var isScrolledUp by remember { mutableStateOf(false) }

                    MessagesList(
                        messages = messages,
                        currentUserNickname = nickname,
                        meshService = viewModel.meshService,
                        modifier = Modifier.weight(1f),
                        forceScrollToBottom = forceScrollToBottom,
                        onScrolledUpChanged = { isUp -> isScrolledUp = isUp },
                        onNicknameClick = { },
                        onMessageLongPress = { },
                        onCancelTransfer = { msg -> viewModel.cancelMediaSend(msg.id) },
                        onImageClick = { _, _, _ -> }
                    )

                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                    var messageText by remember {
                        mutableStateOf(
                            androidx.compose.ui.text.input.TextFieldValue("")
                        )
                    }

                    ChatInputSection(
                        messageText = messageText,
                        onMessageTextChange = { newText ->
                            messageText = newText
                            viewModel.updateMentionSuggestions(newText.text)
                        },
                        onSend = {
                            if (messageText.text.trim().isNotEmpty()) {
                                viewModel.sendMessage(messageText.text.trim())
                                messageText = androidx.compose.ui.text.input.TextFieldValue("")
                                forceScrollToBottom = !forceScrollToBottom
                            }
                        },
                        onSendVoiceNote = { peer, channel, path ->
                            viewModel.sendVoiceNote(peer, channel, path)
                        },
                        onSendImageNote = { peer, channel, path ->
                            viewModel.sendImageNote(peer, channel, path)
                        },
                        onSendFileNote = { peer, channel, path ->
                            viewModel.sendFileNote(peer, channel, path)
                        },
                        showCommandSuggestions = false,
                        commandSuggestions = emptyList(),
                        showMentionSuggestions = false,
                        mentionSuggestions = emptyList(),
                        onCommandSuggestionClick = { },
                        onMentionSuggestionClick = { },
                        selectedPrivatePeer = peerID,
                        currentChannel = null,
                        nickname = nickname,
                        colorScheme = colorScheme,
                        showMediaButtons = true
                    )
                }

                BlueTalkSheetCenterTopBar(
                    onClose = onDismiss,
                    modifier = Modifier.align(Alignment.TopCenter),
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    title = {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isDirect) {
                                Icon(
                                    imageVector = Icons.Outlined.SettingsInputAntenna,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colorScheme.primary
                                )
                            } else if (isConnected) {
                                Icon(
                                    imageVector = Icons.Filled.Route,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colorScheme.primary
                                )
                            }

                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.then(securityModifier)
                            ) {
                                NoiseSessionIcon(
                                    sessionState = sessionState,
                                    modifier = Modifier.size(16.dp)
                                )

                                if (isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = colorScheme.primary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleFavorite(peerID) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isFavorite) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
