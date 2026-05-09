package com.bluetalk.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
 

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Intent
import android.net.Uri
import com.bluetalk.android.model.BlueTalkMessage
import com.bluetalk.android.model.DeliveryStatus
import com.bluetalk.android.mesh.BluetoothMeshService
import java.text.SimpleDateFormat
import java.util.*
import com.bluetalk.android.ui.media.VoiceNotePlayer
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import com.bluetalk.android.ui.media.FileMessageItem
import com.bluetalk.android.model.BlueTalkMessageType
import com.bluetalk.android.R
import androidx.compose.ui.res.stringResource


// VoiceNotePlayer moved to com.bluetalk.android.ui.media.VoiceNotePlayer

/**
 * Message display components for ChatScreen
 * Extracted from ChatScreen.kt for better organization
 */

@Composable
fun MessagesList(
    messages: List<BlueTalkMessage>,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    modifier: Modifier = Modifier,
    forceScrollToBottom: Boolean = false,
    onScrolledUpChanged: ((Boolean) -> Unit)? = null,
    onNicknameClick: ((String) -> Unit)? = null,
    onMessageLongPress: ((BlueTalkMessage) -> Unit)? = null,
    onCancelTransfer: ((BlueTalkMessage) -> Unit)? = null,
    onImageClick: ((String, List<String>, Int) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    
    // Track if this is the first time messages are being loaded
    var hasScrolledToInitialPosition by remember { mutableStateOf(false) }
    var followIncomingMessages by remember { mutableStateOf(true) }
    
    // Smart scroll: auto-scroll to bottom for initial load, then follow unless user scrolls away
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val isFirstLoad = !hasScrolledToInitialPosition
            if (isFirstLoad || followIncomingMessages) {
                listState.scrollToItem(0)
                if (isFirstLoad) {
                    hasScrolledToInitialPosition = true
                }
            }
        }
    }
    
    // Track whether user has scrolled away from the latest messages
    val isAtLatest by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1
            firstVisibleIndex <= 2
        }
    }
    LaunchedEffect(isAtLatest) {
        followIncomingMessages = isAtLatest
        onScrolledUpChanged?.invoke(!isAtLatest)
    }
    
    // Force scroll to bottom when requested (e.g., when user sends a message)
    LaunchedEffect(forceScrollToBottom) {
        if (messages.isNotEmpty()) {
            // With reverseLayout=true and reversed data, latest is at index 0
            followIncomingMessages = true
            listState.scrollToItem(0)
        }
    }
    
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
        reverseLayout = true
    ) {
        items(
            items = messages.asReversed(),
            key = { it.id }
        ) { message ->
                MessageItem(
                    message = message,
                    messages = messages,
                    currentUserNickname = currentUserNickname,
                    meshService = meshService,
                    onNicknameClick = onNicknameClick,
                    onMessageLongPress = onMessageLongPress,
                    onCancelTransfer = onCancelTransfer,
                    onImageClick = onImageClick
                )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: BlueTalkMessage,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    messages: List<BlueTalkMessage> = emptyList(),
    onNicknameClick: ((String) -> Unit)? = null,
    onMessageLongPress: ((BlueTalkMessage) -> Unit)? = null,
    onCancelTransfer: ((BlueTalkMessage) -> Unit)? = null,
    onImageClick: ((String, List<String>, Int) -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    // Determine if the message was sent by the current user
    val isSelf = message.senderPeerID == meshService.myPeerID || 
                 message.sender == currentUserNickname ||
                 message.sender.startsWith("$currentUserNickname#")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Don't let messages take full width
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // Own private messages delivery status padding
                val endPad = if (message.isPrivate && isSelf) 20.dp else 0.dp
                
                MessageTextWithClickableNicknames(
                    message = message,
                    messages = messages,
                    currentUserNickname = currentUserNickname,
                    meshService = meshService,
                    colorScheme = colorScheme,
                    timeFormatter = timeFormatter,
                    onNicknameClick = onNicknameClick,
                    onMessageLongPress = onMessageLongPress,
                    onCancelTransfer = onCancelTransfer,
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(end = endPad)
                )
            }

            // Delivery status for private messages (overlay)
            if (message.isPrivate && isSelf) {
                message.deliveryStatus?.let { status ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp)
                    ) {
                        DeliveryStatusIcon(status = status)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
    private fun MessageTextWithClickableNicknames(
        message: BlueTalkMessage,
        messages: List<BlueTalkMessage>,
        currentUserNickname: String,
        meshService: BluetoothMeshService,
        colorScheme: ColorScheme,
        timeFormatter: SimpleDateFormat,
        onNicknameClick: ((String) -> Unit)?,
        onMessageLongPress: ((BlueTalkMessage) -> Unit)?,
        onCancelTransfer: ((BlueTalkMessage) -> Unit)?,
        onImageClick: ((String, List<String>, Int) -> Unit)?,
        modifier: Modifier = Modifier
    ) {
    // Image special rendering
    if (message.type == BlueTalkMessageType.Image) {
        com.bluetalk.android.ui.media.ImageMessageItem(
            message = message,
            messages = messages,
            currentUserNickname = currentUserNickname,
            meshService = meshService,
            colorScheme = colorScheme,
            timeFormatter = timeFormatter,
            onNicknameClick = onNicknameClick,
            onMessageLongPress = onMessageLongPress,
            onCancelTransfer = onCancelTransfer,
            onImageClick = onImageClick,
            modifier = modifier
        )
        return
    }

    // Voice note special rendering
    if (message.type == BlueTalkMessageType.Audio) {
        com.bluetalk.android.ui.media.AudioMessageItem(
            message = message,
            currentUserNickname = currentUserNickname,
            meshService = meshService,
            colorScheme = colorScheme,
            timeFormatter = timeFormatter,
            onNicknameClick = onNicknameClick,
            onMessageLongPress = onMessageLongPress,
            onCancelTransfer = onCancelTransfer,
            modifier = modifier
        )
        return
    }

    // File special rendering
    if (message.type == BlueTalkMessageType.File) {
        val path = message.content.trim()
        // Derive sending progress if applicable
        val (overrideProgress, _) = when (val st = message.deliveryStatus) {
            is com.bluetalk.android.model.DeliveryStatus.PartiallyDelivered -> {
                if (st.total > 0 && st.reached < st.total) {
                    (st.reached.toFloat() / st.total.toFloat()) to colorScheme.primary
                } else null to null
            }
            else -> null to null
        }
        Column(modifier = modifier.fillMaxWidth()) {
            // Header: nickname + timestamp line above the file
            val headerText = formatMessageHeaderAnnotatedString(
                message = message,
                currentUserNickname = currentUserNickname,
                meshService = meshService,
                colorScheme = colorScheme,
                timeFormatter = timeFormatter
            )
            val haptic = LocalHapticFeedback.current
            var headerLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
            Text(
                text = headerText,
                fontFamily = FontFamily.Default,
                color = colorScheme.onSurface,
                modifier = Modifier.pointerInput(message.id) {
                    detectTapGestures(onTap = { pos ->
                        val layout = headerLayout ?: return@detectTapGestures
                        val offset = layout.getOffsetForPosition(pos)
                        val ann = headerText.getStringAnnotations("nickname_click", offset, offset)
                        if (ann.isNotEmpty() && onNicknameClick != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNicknameClick.invoke(ann.first().item)
                        }
                    }, onLongPress = { onMessageLongPress?.invoke(message) })
                },
                onTextLayout = { headerLayout = it }
            )

            // Try to load the file packet from the path
            val packet = try {
                val file = java.io.File(path)
                if (file.exists()) {
                    com.bluetalk.android.model.BlueTalkFilePacket(
                        fileName = file.name,
                        fileSize = file.length(),
                        mimeType = com.bluetalk.android.features.file.FileUtils.getMimeTypeFromExtension(file.name),
                        content = file.readBytes()
                    )
                } else null
            } catch (e: Exception) {
                null
            }

            val isSelf = message.senderPeerID == meshService.myPeerID || 
                         message.sender == currentUserNickname ||
                         message.sender.startsWith("$currentUserNickname#")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start) {
                Box {
                    if (packet != null) {
                        if (overrideProgress != null) {
                            // Show sending animation while in-flight
                            com.bluetalk.android.ui.media.FileSendingAnimation(
                                fileName = packet.fileName,
                                progress = overrideProgress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Static file display with open/save dialog
                            FileMessageItem(
                                packet = packet,
                                onFileClick = {
                                    // handled inside FileMessageItem via dialog
                                }
                            )
                        }

                        // Cancel button overlay during sending
                        val showCancel = message.sender == currentUserNickname && (message.deliveryStatus is DeliveryStatus.PartiallyDelivered)
                        if (showCancel) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .background(colorScheme.error.copy(alpha = 0.6f), CircleShape)
                                    .clickable { onCancelTransfer?.invoke(message) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.cd_cancel), tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        Text(text = stringResource(R.string.file_unavailable), fontFamily = FontFamily.Default, color = colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
        return
    }

    // Normal message display
    val annotatedText = formatMessageAsAnnotatedString(
        message = message,
        currentUserNickname = currentUserNickname,
        meshService = meshService,
        colorScheme = colorScheme,
        timeFormatter = timeFormatter
    )
    
    // Check if this message was sent by self to avoid click interactions on own nickname
    val isSelf = message.senderPeerID == meshService.myPeerID || 
                 message.sender == currentUserNickname ||
                 message.sender.startsWith("$currentUserNickname#")
    
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = annotatedText,
        modifier = modifier.pointerInput(message) {
            detectTapGestures(
                onTap = { position ->
                    val layout = textLayoutResult ?: return@detectTapGestures
                    val offset = layout.getOffsetForPosition(position)
                    // Nickname click only when not self
                    if (!isSelf && onNicknameClick != null) {
                        val nicknameAnnotations = annotatedText.getStringAnnotations(
                            tag = "nickname_click",
                            start = offset,
                            end = offset
                        )
                        if (nicknameAnnotations.isNotEmpty()) {
                            val nickname = nicknameAnnotations.first().item
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNicknameClick.invoke(nickname)
                            return@detectTapGestures
                        }
                    }
                    // URL open (all messages)
                    val urlAnnotations = annotatedText.getStringAnnotations(
                        tag = "url_click",
                        start = offset,
                        end = offset
                    )
                    if (urlAnnotations.isNotEmpty()) {
                        val raw = urlAnnotations.first().item
                        val resolved = if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) raw else "https://$raw"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resolved))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        return@detectTapGestures
                    }
                },
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMessageLongPress?.invoke(message)
                }
            )
        },
        fontFamily = FontFamily.Default,
        softWrap = true,
        overflow = TextOverflow.Visible,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = colorScheme.onSurface
        ),
        onTextLayout = { result -> textLayoutResult = result }
    )
}

@Composable
fun DeliveryStatusIcon(status: DeliveryStatus) {
    val colorScheme = MaterialTheme.colorScheme
    
    when (status) {
        is DeliveryStatus.Sending -> {
            Text(
                text = stringResource(R.string.status_sending),
                fontSize = 11.sp,
                color = colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        is DeliveryStatus.Sent -> {
            Text(
                text = stringResource(R.string.status_pending),
                fontSize = 11.sp,
                color = colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        is DeliveryStatus.Delivered -> {
            Text(
                text = stringResource(R.string.status_sent),
                fontSize = 11.sp,
                color = colorScheme.primary.copy(alpha = 0.9f)
            )
        }
        is DeliveryStatus.Read -> {
            Text(
                text = stringResource(R.string.status_delivered),
                fontSize = 11.sp,
                color = colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        is DeliveryStatus.Failed -> {
            Text(
                text = stringResource(R.string.status_failed),
                fontSize = 11.sp,
                color = colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        is DeliveryStatus.PartiallyDelivered -> {
            Text(
                text = stringResource(R.string.status_sent),
                fontSize = 11.sp,
                color = colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}
