package com.bluetalk.android.ui
// [Goose] TODO: Replace inline file attachment stub with FilePickerButton abstraction that dispatches via FileShareDispatcher


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetalk.android.R
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.withStyle
import com.bluetalk.android.ui.theme.BASE_FONT_SIZE
import com.bluetalk.android.features.voice.normalizeAmplitudeSample
import com.bluetalk.android.features.voice.AudioWaveformExtractor
import com.bluetalk.android.ui.media.RealtimeScrollingWaveform
import com.bluetalk.android.ui.media.ImagePickerButton
import com.bluetalk.android.ui.media.FilePickerButton

/**
 * Input components for ChatScreen
 * Extracted from ChatScreen.kt for better organization
 */

/**
 * VisualTransformation that styles slash commands with background and color
 */
class SlashCommandVisualTransformation(private val colorScheme: ColorScheme) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val slashCommandRegex = Regex("(/\\w+)(?=\\s|$)")
        val annotatedString = buildAnnotatedString {
            var lastIndex = 0

            slashCommandRegex.findAll(text.text).forEach { match ->
                if (match.range.first > lastIndex) {
                    append(text.text.substring(lastIndex, match.range.first))
                }

                withStyle(
                    style = SpanStyle(
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        background = colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    append(match.value)
                }

                lastIndex = match.range.last + 1
            }

            if (lastIndex < text.text.length) {
                append(text.text.substring(lastIndex))
            }
        }

        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }
}

/**
 * VisualTransformation that styles mentions with color
 */
class MentionVisualTransformation(private val colorScheme: ColorScheme) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val mentionRegex = Regex("@([\\p{L}0-9_]+(?:#[a-fA-F0-9]{4})?)")
        val annotatedString = buildAnnotatedString {
            var lastIndex = 0
            
            mentionRegex.findAll(text.text).forEach { match ->
                if (match.range.first > lastIndex) {
                    append(text.text.substring(lastIndex, match.range.first))
                }
                
                withStyle(
                    style = SpanStyle(
                        color = colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(match.value)
                }
                
                lastIndex = match.range.last + 1
            }
            
            if (lastIndex < text.text.length) {
                append(text.text.substring(lastIndex))
            }
        }
        
        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }
}

/**
 * VisualTransformation that combines multiple visual transformations
 */
class CombinedVisualTransformation(private val transformations: List<VisualTransformation>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var resultText = text
        transformations.forEach { transformation ->
            resultText = transformation.filter(resultText).text
        }
        return TransformedText(
            text = resultText,
            offsetMapping = OffsetMapping.Identity
        )
    }
}





@Composable
fun MessageInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onSendVoiceNote: (String?, String?, String) -> Unit,
    onSendImageNote: (String?, String?, String) -> Unit,
    onSendFileNote: (String?, String?, String) -> Unit,
    selectedPrivatePeer: String?,
    currentChannel: String?,
    nickname: String,
    showMediaButtons: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isFocused = remember { mutableStateOf(false) }
    val hasText = value.text.isNotBlank()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isRecording by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var amplitude by remember { mutableStateOf(0) }

    Surface(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isFocused.value) BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(if (isRecording) Color.Transparent else colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { 
                        if (hasText) onSend()
                    }),
                    visualTransformation = CombinedVisualTransformation(
                        listOf(SlashCommandVisualTransformation(colorScheme), MentionVisualTransformation(colorScheme))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused.value = focusState.isFocused
                        }
                )

                if (value.text.isEmpty() && !isRecording) {
                    Text(
                        text = stringResource(R.string.type_a_message_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isRecording) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RealtimeScrollingWaveform(
                            modifier = Modifier.weight(1f).height(32.dp),
                            amplitudeNorm = normalizeAmplitudeSample(amplitude)
                        )
                        Spacer(Modifier.width(16.dp))
                        val secs = (elapsedMs / 1000).toInt()
                        val mm = secs / 60
                        val ss = secs % 60
                        Text(
                            text = String.format("%02d:%02d", mm, ss),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.primary
                        )
                    }
                }
            }
            
            if (value.text.isEmpty() && showMediaButtons) {
                val bg = colorScheme.primary.copy(alpha = 0.8f)
                val latestSelectedPeer = rememberUpdatedState(selectedPrivatePeer)
                val latestChannel = rememberUpdatedState(currentChannel)
                val latestOnSendVoiceNote = rememberUpdatedState(onSendVoiceNote)

                if (!isRecording) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ImagePickerButton(
                            onImageReady = { outPath ->
                                onSendImageNote(latestSelectedPeer.value, latestChannel.value, outPath)
                            }
                        )
                    }
                }

                VoiceRecordButton(
                    backgroundColor = bg,
                    onStart = {
                        isRecording = true
                        elapsedMs = 0L
                        if (isFocused.value) {
                            try { focusRequester.requestFocus() } catch (_: Exception) {}
                        }
                    },
                    onAmplitude = { amp, ms ->
                        amplitude = amp
                        elapsedMs = ms
                    },
                    onFinish = { path ->
                        isRecording = false
                        AudioWaveformExtractor.extractAsync(path, sampleCount = 120) { arr ->
                            if (arr != null) {
                                try { com.bluetalk.android.features.voice.VoiceWaveformCache.put(path, arr) } catch (_: Exception) {}
                            }
                        }
                        latestOnSendVoiceNote.value(
                            latestSelectedPeer.value,
                            latestChannel.value,
                            path
                        )
                    }
                )
                
            } else {
                IconButton(
                    onClick = { if (hasText) onSend() },
                    enabled = hasText,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = if (!hasText) {
                                    colorScheme.onSurface.copy(alpha = 0.12f)
                                } else {
                                    colorScheme.primary
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = stringResource(id = R.string.send_message),
                            modifier = Modifier.size(20.dp),
                            tint = if (hasText) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommandSuggestionsBox(
    suggestions: List<CommandSuggestion>,
    onSuggestionClick: (CommandSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .maxHeight(200.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            suggestions.forEach { suggestion: CommandSuggestion ->
                CommandSuggestionItem(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

private fun Modifier.maxHeight(maxHeight: androidx.compose.ui.unit.Dp): Modifier = this.then(
    Modifier.heightIn(max = maxHeight)
)

@Composable
fun CommandSuggestionItem(
    suggestion: CommandSuggestion,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val allCommands = if (suggestion.aliases.isNotEmpty()) {
            listOf(suggestion.command) + suggestion.aliases
        } else {
            listOf(suggestion.command)
        }

        Text(
            text = allCommands.joinToString(", "),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )

        suggestion.syntax?.let { syntax ->
            Text(
                text = syntax,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Text(
            text = suggestion.description,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MentionSuggestionsBox(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .maxHeight(200.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            suggestions.forEach { suggestion: String ->
                MentionSuggestionItem(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
fun MentionSuggestionItem(
    suggestion: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AlternateEmail,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = suggestion,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = stringResource(R.string.mention),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
