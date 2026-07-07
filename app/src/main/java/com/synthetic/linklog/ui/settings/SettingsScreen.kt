package com.synthetic.linklog.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.synthetic.linklog.ui.theme.Spacing
import com.synthetic.linklog.util.BackupManager
import kotlinx.coroutines.launch
import android.widget.Toast

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {}
) {
    val youtubeApiKey  by viewModel.youtubeApiKey.collectAsState()
    val videoQuality   by viewModel.videoQuality.collectAsState()
    val downloadFormat by viewModel.downloadFormat.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = BackupManager.createBackup(context, uri)
                Toast.makeText(context, if (success) "Backup exported" else "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = BackupManager.restoreBackup(context, uri)
                Toast.makeText(context, if (success) "Backup imported. Restart app!" else "Import failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg)
            .padding(bottom = Spacing.xl)
    ) {
        Spacer(Modifier.height(Spacing.lg))
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(Spacing.xl))

        // ── Appearance ────────────────────────────────────────────────────────
        Text(
            "APPEARANCE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.sm))
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isDarkTheme) "Deep indigo theme" else "Warm amber theme",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() })
            }
        }

        Spacer(Modifier.height(Spacing.xl))

        // ── Integrations ─────────────────────────────────────────────────────
        Text(
            "INTEGRATIONS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.sm))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text("YouTube API Key", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = youtubeApiKey ?: "",
                    onValueChange = { viewModel.setYoutubeApiKey(it) },
                    label = { Text("Enter API key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(Spacing.xl))

        // ── Data Management ───────────────────────────────────────────────────
        Text(
            "DATA MANAGEMENT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.sm))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick = { exportLauncher.launch("linklog_backup.zip") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Backup")
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/zip")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import Backup")
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.xl))

        // ── Downloader ────────────────────────────────────────────────────────
        Text(
            "DOWNLOADER",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.sm))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text("Video Quality", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    listOf("best", "1080p", "720p").forEach { q ->
                        FilterChip(
                            selected = videoQuality == q,
                            onClick = { viewModel.setVideoQuality(q) },
                            label = { Text(q) }
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                Text("Format", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    listOf("mp4", "mp3").forEach { f ->
                        FilterChip(
                            selected = downloadFormat == f,
                            onClick = { viewModel.setDownloadFormat(f) },
                            label = { Text(f.uppercase()) }
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                Button(
                    onClick = { viewModel.updateYoutubeDl() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Update yt-dlp Engine") }
            }
        }
    }
}
