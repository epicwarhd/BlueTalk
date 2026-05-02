package com.bitchat.android.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.mesh.BluetoothMeshService
import androidx.compose.material3.ColorScheme
import com.bitchat.android.ui.theme.BASE_FONT_SIZE
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for ChatScreen UI components
 * Extracted from ChatScreen.kt for better organization
 */

/**
 * Get RSSI-based color for signal strength visualization
 */
fun getRSSIColor(rssi: Int, colorScheme: ColorScheme): Color {
    return when {
        rssi >= -50 -> colorScheme.primary // Strong signal
        rssi >= -60 -> colorScheme.primary.copy(alpha = 0.8f)
        rssi >= -70 -> colorScheme.secondary
        rssi >= -80 -> colorScheme.error.copy(alpha = 0.7f)
        else -> colorScheme.error // Weak signal
    }
}

/**
 * Format message as annotated string with modern style
 * Timestamp at END, peer colors, hashtag suffix handling
 */
fun formatMessageAsAnnotatedString(
    message: BitchatMessage,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    colorScheme: ColorScheme,
    timeFormatter: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f
    
    // Determine if this message was sent by self
    val isSelf = message.senderPeerID == meshService.myPeerID || 
                 message.sender == currentUserNickname ||
                 message.sender.startsWith("$currentUserNickname#")
    
    if (message.sender != "system") {
        // Get base color for this peer
        val baseColor = if (isSelf) {
            colorScheme.primary // Use theme primary for self
        } else {
            getPeerColor(message, isDark)
        }
        
        // Split sender into base name and hashtag suffix
        val (baseName, suffix) = splitSuffix(message.sender)
        
        // Sender prefix "<@"
        builder.pushStyle(SpanStyle(
            color = baseColor,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
        ))
        builder.append("<@")
        builder.pop()
        
        // Base name (clickable)
        builder.pushStyle(SpanStyle(
            color = baseColor,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
        ))
        val nicknameStart = builder.length
        val truncatedBase = truncateNickname(baseName)
        builder.append(truncatedBase)
        val nicknameEnd = builder.length
        
        // Add click annotation for nickname
        if (!isSelf) {
            builder.addStringAnnotation(
                tag = "nickname_click",
                annotation = (message.originalSender ?: message.sender),
                start = nicknameStart,
                end = nicknameEnd
            )
        }
        builder.pop()
        
        // Hashtag suffix in lighter color
        if (suffix.isNotEmpty()) {
            builder.pushStyle(SpanStyle(
                color = baseColor.copy(alpha = 0.6f),
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
            ))
            builder.append(suffix)
            builder.pop()
        }
        
        // Sender suffix "> "
        builder.pushStyle(SpanStyle(
            color = baseColor,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
        ))
        builder.append("> ")
        builder.pop()
        
        // Message content with modern hashtag and mention highlighting
        appendModernFormattedContent(builder, message.content, message.mentions, currentUserNickname, baseColor, isSelf, isDark, colorScheme)
        
        // Modern timestamp at the END (smaller, subdued)
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append(" [${timeFormatter.format(message.timestamp)}]")
        // If message has valid PoW difficulty, append bits
        message.powDifficulty?.let { bits ->
            if (bits > 0) {
                builder.append(" ⛨${bits}b")
            }
        }
        builder.pop()
        
    } else {
        // System message
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = (BASE_FONT_SIZE - 2).sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        ))
        builder.append("* ${message.content} *")
        builder.pop()
        
        // Timestamp for system messages too
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append(" [${timeFormatter.format(message.timestamp)}]")
        builder.pop()
    }
    
    return builder.toAnnotatedString()
}

/**
 * Build only the nickname + timestamp header line for a message
 */
fun formatMessageHeaderAnnotatedString(
    message: BitchatMessage,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    colorScheme: ColorScheme,
    timeFormatter: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f

    val isSelf = message.senderPeerID == meshService.myPeerID ||
            message.sender == currentUserNickname ||
            message.sender.startsWith("$currentUserNickname#")

    if (message.sender != "system") {
        val baseColor = if (isSelf) colorScheme.primary else getPeerColor(message, isDark)
        val (baseName, suffix) = splitSuffix(message.sender)

        // "<@"
        builder.pushStyle(SpanStyle(
            color = baseColor,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
        ))
        builder.append("<@")
        builder.pop()

        // Base name (clickable when not self)
        builder.pushStyle(SpanStyle(
            color = baseColor,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
        ))
        val nicknameStart = builder.length
        builder.append(truncateNickname(baseName))
        val nicknameEnd = builder.length
        if (!isSelf) {
            builder.addStringAnnotation(
                tag = "nickname_click",
                annotation = (message.originalSender ?: message.sender),
                start = nicknameStart,
                end = nicknameEnd
            )
        }
        builder.pop()

        // Hashtag suffix
        if (suffix.isNotEmpty()) {
            builder.pushStyle(SpanStyle(
                color = baseColor.copy(alpha = 0.6f),
                fontSize = BASE_FONT_SIZE.sp,
                fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
            ))
            builder.append(suffix)
            builder.pop()
        }

        // Sender suffix ">"
        builder.pushStyle(SpanStyle(
            color = baseColor,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium
        ))
        builder.append(">")
        builder.pop()

        // Timestamp and optional PoW bits
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = (BASE_FONT_SIZE - 4).sp
        ))
        builder.append("  [${timeFormatter.format(message.timestamp)}]")
        message.powDifficulty?.let { bits ->
            if (bits > 0) builder.append(" ⛨${bits}b")
        }
        builder.pop()
    } else {
        // System message header
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = (BASE_FONT_SIZE - 2).sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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

/**
 * Peer color assignment using djb2 hash algorithm
 */
fun getPeerColor(message: BitchatMessage, isDark: Boolean): Color {
    val seed = when {
        message.senderPeerID?.startsWith("nostr:") == true || message.senderPeerID?.startsWith("nostr_") == true -> {
            "nostr:${message.senderPeerID.lowercase()}"
        }
        message.senderPeerID?.length == 16 -> {
            "noise:${message.senderPeerID.lowercase()}"
        }
        message.senderPeerID?.length == 64 -> {
            "noise:${message.senderPeerID.lowercase()}"
        }
        else -> {
            message.sender.lowercase()
        }
    }
    
    return colorForPeerSeed(seed, isDark)
}

/**
 * Generate consistent peer color using djb2 hash
 */
fun colorForPeerSeed(seed: String, isDark: Boolean): Color {
    var hash = 5381UL
    for (byte in seed.toByteArray()) {
        hash = ((hash shl 5) + hash) + byte.toUByte().toULong()
    }
    
    var hue = (hash % 360UL).toDouble() / 360.0
    
    // Adjusted logic to ensure peer colors don't clash too much with our new theme colors
    val saturation = if (isDark) 0.55 else 0.65
    val brightness = if (isDark) 0.90 else 0.40
    
    return Color.hsv(
        hue = (hue * 360).toFloat(),
        saturation = saturation.toFloat(),
        value = brightness.toFloat()
    )
}

/**
 * Split a name into base and a '#abcd' suffix if present
 */
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

/**
 * Modernized content formatting with proper hashtag and mention handling
 */
private fun appendModernFormattedContent(
    builder: AnnotatedString.Builder,
    content: String,
    mentions: List<String>?,
    currentUserNickname: String,
    baseColor: Color,
    isSelf: Boolean,
    isDark: Boolean,
    colorScheme: ColorScheme
) {
    val hashtagPattern = "#([a-zA-Z0-9_]+)".toRegex()
    val mentionPattern = "@([\\p{L}0-9_]+(?:#[a-fA-F0-9]{4})?)".toRegex()
    
    val hashtagMatches = hashtagPattern.findAll(content).toList()
    val mentionMatches = mentionPattern.findAll(content).toList()
    
    val mentionRanges = mentionMatches.map { it.range }
    fun overlapsMention(range: IntRange): Boolean {
        return mentionRanges.any { mentionRange ->
            range.first < mentionRange.last && range.last > mentionRange.first
        }
    }
    
    val allMatches = mutableListOf<Pair<IntRange, String>>()
    
    for (match in hashtagMatches) {
        if (!overlapsMention(match.range)) {
            allMatches.add(match.range to "hashtag")
        }
    }
    
    for (match in mentionMatches) {
        allMatches.add(match.range to "mention") 
    }

    val geoMatches = MessageSpecialParser.findStandaloneGeohashes(content)
    for (gm in geoMatches) {
        val range = gm.start until gm.endExclusive
        if (!overlapsMention(range)) {
            allMatches.add(range to "geohash")
        }
    }

    val urlMatches = MessageSpecialParser.findUrls(content)
    for (um in urlMatches) {
        val range = um.start until um.endExclusive
        if (!overlapsMention(range)) {
            allMatches.add(range to "url")
        }
    }

    fun rangesOverlap(a: IntRange, b: IntRange): Boolean {
        return a.first < b.last && a.last > b.first
    }
    val urlRanges = allMatches.filter { it.second == "url" }.map { it.first }
    val geoRanges = allMatches.filter { it.second == "geohash" }.map { it.first }
    if (geoRanges.isNotEmpty() || urlRanges.isNotEmpty()) {
        val iterator = allMatches.listIterator()
        while (iterator.hasNext()) {
            val (range, type) = iterator.next()
            val overlapsGeo = geoRanges.any { rangesOverlap(range, it) }
            val overlapsUrl = urlRanges.any { rangesOverlap(range, it) }
            if ((type == "hashtag" && overlapsGeo) || (type == "geohash" && overlapsUrl)) iterator.remove()
        }
    }
    
    allMatches.sortBy { it.first.first }
    
    var lastEnd = 0
    val isMentioned = mentions?.contains(currentUserNickname) == true
    
    for ((range, type) in allMatches) {
        if (lastEnd < range.first) {
            val beforeText = content.substring(lastEnd, range.first)
            if (beforeText.isNotEmpty()) {
                builder.pushStyle(SpanStyle(
                    color = colorScheme.onSurface, // Default text color
                    fontSize = BASE_FONT_SIZE.sp,
                    fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal
                ))
                if (isMentioned) {
                    builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    builder.append(beforeText)
                    builder.pop()
                } else {
                    builder.append(beforeText)
                }
                builder.pop()
            }
        }
        
        val matchText = content.substring(range.first, range.last + 1)
        when (type) {
            "mention" -> {
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
            "hashtag" -> {
                builder.pushStyle(SpanStyle(
                    color = colorScheme.onSurface,
                    fontSize = BASE_FONT_SIZE.sp,
                    fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal
                ))
                if (isMentioned) {
                    builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    builder.append(matchText)
                    builder.pop()
                } else {
                    builder.append(matchText)
                }
                builder.pop()
            }
            else -> {
                if (type == "geohash") {
                    builder.pushStyle(SpanStyle(
                        color = colorScheme.secondary,
                        fontSize = BASE_FONT_SIZE.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ))
                    val start = builder.length
                    builder.append(matchText)
                    val end = builder.length
                    val geohash = matchText.removePrefix("#").lowercase()
                    builder.addStringAnnotation(
                        tag = "geohash_click",
                        annotation = geohash,
                        start = start,
                        end = end
                    )
                    builder.pop()
                } else if (type == "url") {
                    builder.pushStyle(SpanStyle(
                        color = colorScheme.primary,
                        fontSize = BASE_FONT_SIZE.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ))
                    val start = builder.length
                    builder.append(matchText)
                    val end = builder.length
                    builder.addStringAnnotation(
                        tag = "url_click",
                        annotation = matchText,
                        start = start,
                        end = end
                    )
                    builder.pop()
                } else {
                    builder.pushStyle(SpanStyle(
                        color = colorScheme.onSurface,
                        fontSize = BASE_FONT_SIZE.sp,
                        fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal
                    ))
                    builder.append(matchText)
                    builder.pop()
                }
            }
        }
        
        lastEnd = range.last + 1
    }
    
    if (lastEnd < content.length) {
        val remainingText = content.substring(lastEnd)
        builder.pushStyle(SpanStyle(
            color = colorScheme.onSurface,
            fontSize = BASE_FONT_SIZE.sp,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal
        ))
        if (isMentioned) {
            builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            builder.append(remainingText)
            builder.pop()
        } else {
            builder.append(remainingText)
        }
        builder.pop()
    }
}

/**
 * Helper to truncate long nicknames (matches iOS truncateNickname)
 */
fun truncateNickname(nickname: String): String {
    return if (nickname.length > 16) nickname.take(15) + "…" else nickname
}
