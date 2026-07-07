package com.synthetic.linklog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.synthetic.linklog.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkBottomSheet(
    viewModel: HomeViewModel,
    initialUrl: String,
    onDismiss: () -> Unit
) {
    val groups by viewModel.groups.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val folders by viewModel.folders.collectAsState()
    
    var url by remember { mutableStateOf(initialUrl) }
    var selectedFolderId by remember { mutableStateOf(viewModel.selectedFolderId.value) }

    var groupDropdownExpanded by remember { mutableStateOf(false) }
    
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    var hasAttemptedPaste by remember { mutableStateOf(false) }

    // Auto-paste logic
    LaunchedEffect(Unit) {
        if (url.isBlank() && !hasAttemptedPaste) {
            hasAttemptedPaste = true
            val clipText = clipboardManager.getText()?.text
            if (!clipText.isNullOrBlank() && (clipText.startsWith("http://") || clipText.startsWith("https://"))) {
                url = clipText
            }
        }
    }

    val isPlaylist = url.contains("list=")
    var importPlaylist by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Save Link", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // URL Input with Quick Paste
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    clipboardManager.getText()?.text?.let { url = it }
                }) {
                    Text("Paste")
                }
            }
            
            if (isPlaylist) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { importPlaylist = !importPlaylist },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = importPlaylist, onCheckedChange = { importPlaylist = it })
                    Text("Import all videos from playlist", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Group Cylinder Dropdown
            val selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "Select Group"
            ExposedDropdownMenuBox(
                expanded = groupDropdownExpanded,
                onExpandedChange = { groupDropdownExpanded = !groupDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedGroupName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                    shape = RoundedCornerShape(50), // Cylinder style
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = groupDropdownExpanded,
                    onDismissRequest = { groupDropdownExpanded = false }
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                viewModel.selectGroup(group.id)
                                selectedFolderId = null
                                groupDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Folders", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            // Folders List (Scrollable)
            Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 200.dp)) {
                if (folders.isEmpty()) {
                    Text(
                        "No folders in this group",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(folders) { folder ->
                            val isSelected = selectedFolderId == folder.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFolderId = folder.id },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Folder,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            
            // Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (url.isNotBlank()) {
                            if (isPlaylist && importPlaylist) {
                                viewModel.addPlaylist(url, selectedFolderId)
                            } else {
                                viewModel.addLink(url, selectedFolderId)
                            }
                            onDismiss()
                        }
                    },
                    enabled = url.isNotBlank()
                ) {
                    Text("Save")
                }
            }
            
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
