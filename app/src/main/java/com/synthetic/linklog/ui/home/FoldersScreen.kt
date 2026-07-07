package com.synthetic.linklog.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.synthetic.linklog.data.local.entity.Folder
import com.synthetic.linklog.ui.components.CylinderDropdown
import com.synthetic.linklog.ui.components.EmptyState
import com.synthetic.linklog.ui.components.FolderCard
import com.synthetic.linklog.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onFolderClick: (Long) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val folders by viewModel.folders.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val scope = rememberCoroutineScope()

    // Group cylinder data
    val groupNames = listOf("All Groups") + groups.map { it.name }
    val selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "All Groups"

    // Add-folder dialog
    var showAddFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Rename dialog
    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Deleted folder for undo
    var lastDeleted by remember { mutableStateOf<Folder?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
    ) {
        Spacer(Modifier.height(Spacing.lg))

        // ── Group cylinder dropdown ──────────────────────────────────────────
        CylinderDropdown(
            label = "Group",
            items = groupNames,
            selectedItem = selectedGroupName,
            onSelect = { name ->
                val g = groups.find { it.name == name }
                viewModel.selectGroup(g?.id)
            },
            onAddClick = {
                // open add-group inline: just set a flag the dialog reads
                viewModel.pendingAddGroup = true
            },
            addButtonTag = "btn_group_add",
            placeholder = "Search groups…",
            modifier = Modifier.semantics { contentDescription = "btn_group_search" }
        )

        Spacer(Modifier.height(Spacing.lg))

        // ── Folder grid ──────────────────────────────────────────────────────
        if (folders.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.FolderOpen,
                headline = "No folders yet",
                body = "Create a folder to organise your links.",
                ctaLabel = "Add Folder",
                onCta = { showAddFolder = true }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(bottom = Spacing.xl)
            ) {
                items(folders, key = { it.id }) { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = {
                            viewModel.selectFolder(folder.id)
                            onFolderClick(folder.id)
                        },
                        onRename = {
                            folderToRename = folder
                            renameInput = folder.name
                        },
                        onDelete = {
                            lastDeleted = folder
                            viewModel.deleteFolder(folder)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "\"${folder.name}\" deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    lastDeleted?.let { viewModel.restoreFolder(it) }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // ── FAB: Add Folder ──────────────────────────────────────────────────────
    // (FAB is actually in MainScaffold, but we expose the dialog trigger here)
    LaunchedEffect(viewModel.triggerAddFolder) {
        if (viewModel.triggerAddFolder) {
            showAddFolder = true
            viewModel.triggerAddFolder = false
        }
    }

    // ── Add Group dialog ─────────────────────────────────────────────────────
    if (viewModel.pendingAddGroup) {
        var newGroupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.pendingAddGroup = false },
            title = { Text("New Group") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Group name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) viewModel.addGroup(newGroupName)
                        viewModel.pendingAddGroup = false
                    },
                    modifier = Modifier.semantics { contentDescription = "btn_group_add" }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.pendingAddGroup = false }) { Text("Cancel") }
            }
        )
    }

    // ── Add Folder dialog ─────────────────────────────────────────────────────
    if (showAddFolder) {
        AlertDialog(
            onDismissRequest = { showAddFolder = false; newFolderName = "" },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) viewModel.addFolder(newFolderName)
                        showAddFolder = false
                        newFolderName = ""
                    },
                    modifier = Modifier.semantics { contentDescription = "btn_folder_add" }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddFolder = false; newFolderName = "" }) { Text("Cancel") }
            }
        )
    }

    // ── Rename dialog ─────────────────────────────────────────────────────────
    folderToRename?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToRename = null },
            title = { Text("Rename Folder") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameInput.isNotBlank()) viewModel.updateFolder(folder.copy(name = renameInput))
                    folderToRename = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { folderToRename = null }) { Text("Cancel") }
            }
        )
    }
}
