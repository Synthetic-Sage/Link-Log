package com.synthetic.linklog.ui.linkdetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.synthetic.linklog.service.DownloadService
import com.synthetic.linklog.ui.home.HomeViewModel
import com.synthetic.linklog.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDetailsScreen(
    viewModel: LinkDetailsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val link by viewModel.link.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Notes state — starts read-only per spec
    var isEditingNotes by remember { mutableStateOf(false) }
    var tempNotes by remember { mutableStateOf("") }

    var showDownloadSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(link?.customTitle ?: link?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (link != null) {
            val currentLink = link!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Hero Thumbnail ─────────────────────────────────────────────
                AsyncImage(
                    model = currentLink.imageUrl,
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ImageSize.heroHeight)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(Spacing.lg)) {

                    // ── Title ──────────────────────────────────────────────────
                    Text(
                        text = currentLink.customTitle ?: currentLink.title ?: "No Title",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    // ── Action Row ─────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        // Open
                        FilledTonalButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentLink.url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "btn_link_copy" }
                        ) {
                            Icon(Icons.Filled.PlayArrow, null)
                            Spacer(Modifier.width(Spacing.xs))
                            Text("Open")
                        }

                        // Copy
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentLink.url))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "btn_link_copy" }
                        ) {
                            Icon(Icons.Filled.ContentCopy, null)
                            Spacer(Modifier.width(Spacing.xs))
                            Text("Copy")
                        }

                        // Download
                        OutlinedButton(
                            onClick = { showDownloadSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "btn_link_download" }
                        ) {
                            Icon(Icons.Filled.Download, null)
                            Spacer(Modifier.width(Spacing.xs))
                            Text("Save")
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    // ── Description ────────────────────────────────────────────
                    if (!currentLink.description.isNullOrBlank()) {
                        Text(
                            "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = currentLink.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Spacing.xl))
                    }

                    // ── Notes Section — read-only by default, pencil to edit ───
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isEditingNotes) {
                            IconButton(
                                onClick = {
                                    tempNotes = currentLink.notes ?: ""
                                    isEditingNotes = true
                                },
                                modifier = Modifier.semantics { contentDescription = "btn_notes_edit" }
                            ) {
                                Icon(Icons.Filled.Edit, "Edit Notes")
                            }
                        } else {
                            Row {
                                // ❎ cancel
                                IconButton(
                                    onClick = { isEditingNotes = false },
                                    modifier = Modifier.semantics { contentDescription = "btn_notes_edit" }
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        "Cancel",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                // ✅ save
                                IconButton(
                                    onClick = {
                                        viewModel.updateNotes(tempNotes)
                                        isEditingNotes = false
                                    },
                                    modifier = Modifier.semantics { contentDescription = "btn_notes_save" }
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        "Save Notes",
                                        tint = SuccessGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    if (isEditingNotes) {
                        OutlinedTextField(
                            value = tempNotes,
                            onValueChange = { tempNotes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            placeholder = { Text("Write your notes here…") }
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = currentLink.notes?.takeIf { it.isNotBlank() }
                                    ?: "No notes yet. Tap ✏️ to add one.",
                                modifier = Modifier.padding(Spacing.md),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentLink.notes.isNullOrBlank())
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xxl))
                }
            }

            // ── Download bottom sheet ──────────────────────────────────────────
            if (showDownloadSheet) {
                ModalBottomSheet(onDismissRequest = { showDownloadSheet = false }) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text(
                            "Download Options",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.lg))
                        listOf("Video 1080p", "Video 720p", "Audio Only (MP3)").forEach { label ->
                            ListItem(
                                headlineContent = { Text(label) },
                                modifier = Modifier
                                    .semantics { contentDescription = "btn_link_download" }
                                    .fillMaxWidth()
                                    .clickable {
                                        homeViewModel.downloadMedia(context, currentLink.url, currentLink.id)
                                        showDownloadSheet = false
                                    }
                            )
                        }
                        Spacer(Modifier.height(Spacing.xxl))
                    }
                }
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

