package com.bitchat.android.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.mesh.BluetoothMeshService
import androidx.compose.material3.ColorScheme
import com.bitchat.android.ui.theme.BASE_FONT_SIZE
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for ChatScreen UI components
 */

fun getRSSIColor(rssi: Int, colorScheme: ColorScheme): Color {
    return when {
        rssi >= -50 -> colorScheme.primary
        rssi >= -60 -> colorScheme.primary.copy(alpha = 0.8f)
        rssi >= -70 -> colorScheme.secondary
        rssi >= -80 -> colorScheme.error.copy(alpha = 0.7f)
        else -> colorScheme.error
    }
}

fun formatMessageAsAnnotatedString(
    message: BitchatMessage,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    colorScheme: ColorScheme,
    timeFormatter: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    
    val isSelf = message.senderPeerID == meshService.myPeerID || 
                 message.sender == currentUserNickname ||
                 message.sender.startsWith("$currentUserNickname#")
    
    if (message.sender != "system") {
        if (!isSelf) {
            val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f
            val baseColor = getPeerColor(message, isDark)
            val (baseName, suffix) = splitSuffix(message.sender)
            
            builder.pushStyle(SpanStyle(
                color = baseColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Medium
            ))
            builder.append("<@")
            builder.pop()
            
            builder.pushStyle(SpanStyle(
                color = baseColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Medium
            ))
            val nicknameStart = builder.length
            val truncatedBase = truncateNickname(baseName)
            builder.append(truncatedBase)
            val nicknameEnd = builder.length
            
            builder.addStringAnnotation(
                tag = "nickname_click",
                annotation = (message.originalSender ?: message.sender),
                start = nicknameStart,
                end = nicknameEnd
            )
            builder.pop()
            
            if (suffix.isNotEmpty()) {
                builder.pushStyle(SpanStyle(
                    color = baseColor.copy(alpha = 0.6f),
                    fontSize = BASE_FONT_SIZE.sp,
                    fontWeight = FontWeight.Medium
                ))
                builder.append(suffix)
                builder.pop()
            }
            
            builder.pushStyle(SpanStyle(
                color = baseColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Medium
            ))
            builder.append("> ")
            builder.pop()
        }
        
        appendModernFormattedContent(builder, message.content, message.mentions, currentUserNickname, colorScheme)
        
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append(" [${timeFormatter.format(message.timestamp)}]")
        builder.pop()
        
    } else {
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = (BASE_FONT_SIZE - 2).sp,
            fontStyle = FontStyle.Italic
        ))
        builder.append("* ${message.content} *")
        builder.pop()
        
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append(" [${timeFormatter.format(message.timestamp)}]")
        builder.pop()
    }
    
    return builder.toAnnotatedString()
}

fun formatMessageHeaderAnnotatedString(
    message: BitchatMessage,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    colorScheme: ColorScheme,
    timeFormatter: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
): AnnotatedString {
    val builder = AnnotatedString.Builder()

    val isSelf = message.senderPeerID == meshService.myPeerID ||
            message.sender == currentUserNickname ||
            message.sender.startsWith("$currentUserNickname#")

    if (message.sender != "system") {
        if (!isSelf) {
            val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f
            val baseColor = getPeerColor(message, isDark)
            val (baseName, suffix) = splitSuffix(message.sender)

            builder.pushStyle(SpanStyle(
                color = baseColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Medium
            ))
            builder.append("<@")
            builder.pop()

            builder.pushStyle(SpanStyle(
                color = baseColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Medium
            ))
            val nicknameStart = builder.length
            builder.append(truncateNickname(baseName))
            val nicknameEnd = builder.length
            
            builder.addStringAnnotation(
                tag = "nickname_click",
                annotation = (message.originalSender ?: message.sender),
                start = nicknameStart,
                end = nicknameEnd
            )
            builder.pop()

            if (suffix.isNotEmpty()) {
                builder.pushStyle(SpanStyle(
                    color = baseColor.copy(alpha = 0.6f),
                    fontSize = BASE_FONT_SIZE.sp,
                    fontWeight = FontWeight.Medium
                ))
                builder.append(suffix)
                builder.pop()
            }

            builder.pushStyle(SpanStyle(
                color = baseColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Medium
            ))
            builder.append(">")
            builder.pop()
            
            builder.append("  ")
        }

        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append("[${timeFormatter.format(message.timestamp)}]")
        builder.pop()
    } else {
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = (BASE_FONT_SIZE - 2).sp,
            fontStyle = FontStyle.Italic
        ))
        builder.append("* ${message.content} *")
        builder.pop()
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append(" [${timeFormatter.format(message.timestamp)}]")
        builder.pop()
    }

    return builder.toAnnotatedString()
}

fun getPeerColor(message: BitchatMessage, isDark: Boolean): Color {
    val seed = message.sender.lowercase()
    return colorForPeerSeed(seed, isDark)
}

fun colorForPeerSeed(seed: String, isDark: Boolean): Color {
    var hash = 5381UL
    for (byte in seed.toByteArray()) {
        hash = ((hash shl 5) + hash) + byte.toUByte().toULong()
    }
    
    val hue = (hash % 360UL).toDouble() / 360.0
    val saturation = if (isDark) 0.55 else 0.65
    val brightness = if (isDark) 0.90 else 0.40
    
    return Color.hsv(
        hue = (hue * 360).toFloat(),
        saturation = saturation.toFloat(),
        value = brightness.toFloat()
    )
}

fun splitSuffix(name: String): Pair<String, String> {
    if (name.length < 5) return Pair(name, "")
    
    val suffix = name.takeLast(5)
    if (suffix.startsWith("#") && suffix.drop(1).all { 
        it.isDigit() || it.lowercaseChar() in 'a'..'f' 
    }) {
        val base = name.dropLast(5)
        return Pair(base, suffix)
    }
    
    return Pair(name, "")
}

private fun appendModernFormattedContent(
    builder: AnnotatedString.Builder,
    content: String,
    mentions: List<String>?,
    currentUserNickname: String,
    colorScheme: ColorScheme
) {
    val mentionPattern = "@([\\p{L}0-9_]+(?:#[a-fA-F0-9]{4})?)".toRegex()
    val mentionMatches = mentionPattern.findAll(content).toList()
    val allMatches = mentionMatches.map { it.range to "mention" }.sortedBy { it.first.first }
    
    var lastEnd = 0
    val isMentioned = mentions?.contains(currentUserNickname) == true
    
    for ((range, type) in allMatches) {
        if (lastEnd < range.first) {
            val beforeText = content.substring(lastEnd, range.first)
            if (beforeText.isNotEmpty()) {
                builder.pushStyle(SpanStyle(
                    color = colorScheme.onSurface,
                    fontSize = BASE_FONT_SIZE.sp,
                    fontWeight = if (isMentioned) FontWeight.Bold else FontWeight.Normal
                ))
                builder.append(beforeText)
                builder.pop()
            }
        }
        
        val matchText = content.substring(range.first, range.last + 1)
        if (type == "mention") {
            val mentionWithoutAt = matchText.removePrefix("@")
            val (mBase, mSuffix) = splitSuffix(mentionWithoutAt)
            val isMentionToMe = mBase == currentUserNickname
            val mentionColor = if (isMentionToMe) colorScheme.primary else colorScheme.secondary
            
            builder.pushStyle(SpanStyle(
                color = mentionColor,
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = FontWeight.Bold
            ))
            builder.append("@")
            builder.append(truncateNickname(mBase))
            if (mSuffix.isNotEmpty()) {
                builder.pushStyle(SpanStyle(color = mentionColor.copy(alpha = 0.6f)))
                builder.append(mSuffix)
                builder.pop()
            }
            builder.pop()
        }
        
        lastEnd = range.last + 1
    }
    
    if (lastEnd < content.length) {
        val remainingText = content.substring(lastEnd)
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isMentioned) FontWeight.Bold else FontWeight.Normal
        ))
        builder.append(remainingText)
        builder.pop()
    }
}

fun truncateNickname(nickname: String): String {
    return if (nickname.length > 16) nickname.take(15) + "…" else nickname
}
