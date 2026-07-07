package com.synthetic.linklog.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.synthetic.linklog.data.local.entity.Link
import com.synthetic.linklog.ui.theme.*
import java.net.URI

/**
 * Full-spec Link card:
 * - 16dp rounded corners, 2dp shadow elevation
 * - 80×80dp thumbnail left-aligned with rank badge top-left
 * - Bold title (2-line ellipsis) + muted description (2-line) + domain small-caps
 * - Rank badge: gold/silver/bronze/black, tappable → highlights blue, keyboard opens
 * - 3-dot menu (right side): pencil Custom Title + trash Delete Title + delete link
 */
@Composable
fun LinkCard(
    link: Link,
    onCardClick: () -> Unit,
    onRankChange: (Int) -> Unit,
    onCustomTitleSave: (String?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ── Rank editing state ────────────────────────────────────────────────────
    var isEditingRank by remember { mutableStateOf(false) }
    var rankInput by remember(link.id) { mutableStateOf(link.userRank.toString()) }
    val rankFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // ── Title editing state ───────────────────────────────────────────────────
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleInput by remember(link.id) {
        mutableStateOf(link.customTitle ?: link.title ?: "")
    }
    var menuExpanded by remember { mutableStateOf(false) }

    // ── Rank badge colour ─────────────────────────────────────────────────────
    val badgeBg by animateColorAsState(
        targetValue = when {
            isEditingRank -> RankEditBlue
            link.userRank == 1 -> RankGold
            link.userRank == 2 -> RankSilver
            link.userRank == 3 -> RankBronze
            else -> RankDefault
        },
        label = "rankBadgeColor"
    )

    val domainLabel = remember(link.url) { extractDomain(link.url) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Elevation.card, MaterialTheme.shapes.large)
            .clickable(enabled = !isEditingRank && !isEditingTitle) { onCardClick() },
        shape = MaterialTheme.shapes.large,    // 16dp per spec
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // ── Thumbnail + Rank Badge ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Large thumbnail, YouTube-style
            ) {
                AsyncImage(
                    model = link.imageUrl,
                    contentDescription = "Thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Rank badge — top-left corner
                Box(
                    modifier = Modifier
                        .padding(Spacing.xs)
                        .size(ImageSize.rankBadge)    // 28dp
                        .clip(CircleShape)
                        .background(badgeBg)
                        .clickable { isEditingRank = true }
                        .align(Alignment.TopStart)
                        .semantics { contentDescription = "handle_link_reorder" },
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditingRank) {
                        LaunchedEffect(Unit) { rankFocusRequester.requestFocus() }
                        BasicTextField(
                            value = rankInput,
                            onValueChange = { v -> if (v.length <= 4 && v.all { it.isDigit() }) rankInput = v },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                val n = rankInput.toIntOrNull()
                                if (n != null && n > 0) onRankChange(n)
                                rankInput = link.userRank.toString()
                                isEditingRank = false
                                focusManager.clearFocus()
                            }),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .focusRequester(rankFocusRequester)
                                .width(22.dp),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = link.userRank.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Info Area (Title, Description, Menu) ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalAlignment = Alignment.Top
            ) {
                // ── Text content ───────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = Spacing.sm)
                ) {
                    if (isEditingTitle) {
                        // Title edit mode: text field + ✅/❎
                        Column {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(
                                    onClick = {
                                        isEditingTitle = false
                                        titleInput = link.customTitle ?: link.title ?: ""
                                    },
                                    modifier = Modifier.semantics { contentDescription = "btn_link_edit" }
                                ) {
                                    Icon(Icons.Filled.Close, "Cancel", tint = MaterialTheme.colorScheme.error)
                                }
                                IconButton(
                                    onClick = {
                                        onCustomTitleSave(titleInput.takeIf { it.isNotBlank() })
                                        isEditingTitle = false
                                    },
                                    modifier = Modifier.semantics { contentDescription = "btn_title_edit" }
                                ) {
                                    Icon(Icons.Filled.Check, "Save", tint = SuccessGreen)
                                }
                            }
                        }
                    } else {
                        // Normal display
                        Text(
                            text = link.customTitle ?: link.title ?: "Unknown",
                            style = MaterialTheme.typography.titleLarge,   // bold per spec
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!link.description.isNullOrBlank()) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                text = link.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (domainLabel.isNotEmpty()) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                text = domainLabel.uppercase(),    // small-caps via uppercase + labelSmall wide tracking
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // ── 3-dot menu (right side) ──
                Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.semantics { contentDescription = "btn_link_edit" }
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    // Custom Title option
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        text = { Text("✏️ Custom Title") },
                        onClick = {
                            menuExpanded = false
                            titleInput = link.customTitle ?: link.title ?: ""
                            isEditingTitle = true
                        },
                        modifier = Modifier.semantics { contentDescription = "btn_title_edit" }
                    )
                    // Delete custom title
                    if (link.customTitle != null) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Filled.Delete, null) },
                            text = { Text("🗑 Delete Title") },
                            onClick = {
                                menuExpanded = false
                                onCustomTitleSave(null)
                            },
                            modifier = Modifier.semantics { contentDescription = "btn_title_delete" }
                        )
                    }
                    HorizontalDivider()
                    // Copy link
                    DropdownMenuItem(
                        text = { Text("Copy Link") },
                        onClick = { menuExpanded = false /* handled in screen */ },
                        modifier = Modifier.semantics { contentDescription = "btn_link_copy" }
                    )
                    // Delete link
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        text = { Text("Delete Link", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        modifier = Modifier.semantics { contentDescription = "btn_link_delete" }
                    )
                }
            }
        }
    }
}
}

private fun extractDomain(url: String): String = try {
    URI(url).host?.removePrefix("www.") ?: ""
} catch (_: Exception) { "" }
