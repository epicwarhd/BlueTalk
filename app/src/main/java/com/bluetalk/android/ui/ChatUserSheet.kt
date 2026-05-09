package com.bluetalk.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetalk.android.ui.theme.BASE_FONT_SIZE
import androidx.compose.ui.res.stringResource
import com.bluetalk.android.R
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.bluetalk.android.core.ui.component.sheet.BlueTalkBottomSheet
import com.bluetalk.android.model.BlueTalkMessage
import com.bluetalk.android.ui.theme.BlueTalkTheme

/**
 * User Action Sheet for selecting actions on a specific user.
 * Modernized with Ocean Blue theme and rounded elements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatUserSheet(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    targetNickname: String,
    selectedMessage: BlueTalkMessage? = null,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val colorScheme = MaterialTheme.colorScheme
    
    val nickname by viewModel.nickname.collectAsState()
    
    if (isPresented) {
        BlueTalkBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.at_nickname, targetNickname),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = colorScheme.onSurface
                    )
                    
                    Text(
                        text = if (selectedMessage != null) stringResource(R.string.choose_action_message_or_user) else stringResource(R.string.choose_action_user),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                
                // Action list in a modern card
                Surface(
                    color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Copy message action
                        selectedMessage?.let { message ->
                            UserActionRow(
                                title = stringResource(R.string.action_copy_message_title),
                                subtitle = stringResource(R.string.action_copy_message_subtitle),
                                titleColor = colorScheme.primary,
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    onDismiss()
                                }
                            )
                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                        
                        if (selectedMessage?.sender != nickname) {
                            // Send private message action
                            UserActionRow(
                                title = stringResource(R.string.action_private_message_title, targetNickname),
                                subtitle = stringResource(R.string.action_private_message_subtitle),
                                titleColor = colorScheme.secondary,
                                onClick = {
                                    val peerID = selectedMessage?.senderPeerID ?: viewModel.getPeerIDForNickname(targetNickname)
                                    if (peerID != null) {
                                        viewModel.showPrivateChatSheet(peerID)
                                    }

                                    onDismiss()
                                }
                            )

                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Slap action
                            UserActionRow(
                                title = stringResource(R.string.action_slap_title, targetNickname),
                                subtitle = stringResource(R.string.action_slap_subtitle),
                                titleColor = colorScheme.primary,
                                onClick = {
                                    viewModel.sendMessage("/slap $targetNickname")
                                    onDismiss()
                                }
                            )
                            
                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Hug action  
                            UserActionRow(
                                title = stringResource(R.string.action_hug_title, targetNickname),
                                subtitle = stringResource(R.string.action_hug_subtitle),
                                titleColor = colorScheme.primary,
                                onClick = {
                                    viewModel.sendMessage("/hug $targetNickname")
                                    onDismiss()
                                }
                            )
                            
                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Block action
                            UserActionRow(
                                title = stringResource(R.string.action_block_title, targetNickname),
                                subtitle = stringResource(R.string.action_block_subtitle),
                                titleColor = colorScheme.error,
                                onClick = {
                                    viewModel.sendMessage("/block $targetNickname")
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
                
                // Close button
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.close_plain),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun UserActionRow(
    title: String,
    subtitle: String,
    titleColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = titleColor
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
