package com.synthetic.linklog.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.synthetic.linklog.data.local.entity.Folder
import com.synthetic.linklog.ui.theme.*

/**
 * Folder card in the 2-column grid.
 * - Long-press opens confirm delete dialog
 * - 3-dot menu: Rename (btn_folder_edit) + Delete (btn_folder_delete)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCard(
    folder: Folder,
    linkCount: Int = 0,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(CardDim.folderCardHeight)
            .shadow(Elevation.card, MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteConfirm = true }
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(Spacing.md)) {
            // Folder icon + name centered
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                if (linkCount > 0) {
                    Text(
                        text = "$linkCount link${if (linkCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3-dot menu top-right
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.semantics { contentDescription = "btn_folder_edit" }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Folder options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        text = { Text("Rename") },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        },
                        modifier = Modifier.semantics { contentDescription = "btn_folder_edit" }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            showDeleteConfirm = true
                        },
                        modifier = Modifier.semantics { contentDescription = "btn_folder_delete" }
                    )
                }
            }
        }
    }

    // Delete confirm dialog (shown when folder has content, or always to be safe)
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"${folder.name}\"?") },
            text = {
                Text(
                    if (linkCount > 0)
                        "This folder contains $linkCount link${if (linkCount != 1) "s" else ""}. All links inside will be permanently deleted."
                    else
                        "Are you sure you want to delete this folder?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
