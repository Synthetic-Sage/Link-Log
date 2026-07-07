package com.synthetic.linklog.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import com.synthetic.linklog.ui.components.EmptyState
import com.synthetic.linklog.ui.components.LinkCard
import com.synthetic.linklog.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun LinksScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLinkClick: (Long) -> Unit = {},
    onAddLinkClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val links by viewModel.displayedLinks.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // Undo delete support
    var lastDeletedLink by remember { mutableStateOf<com.synthetic.linklog.data.local.entity.Link?>(null) }

    if (links.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Link,
            headline = "No links in this folder",
            body = "Tap the + button to add your first link, or share a URL from any app.",
            ctaLabel = "Add Link",
            onCta = onAddLinkClick
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(
                top = Spacing.lg,
                bottom = Spacing.xxl   // so FAB doesn't cover last card
            )
        ) {
            items(links, key = { it.id }) { link ->
                LinkCard(
                    link = link,
                    onCardClick = { onLinkClick(link.id) },
                    onRankChange = { newRank -> viewModel.updateLinkRank(link, newRank) },
                    onCustomTitleSave = { title -> viewModel.updateCustomTitle(link, title) },
                    onDelete = {
                        lastDeletedLink = link
                        viewModel.deleteLink(link)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "\"${link.customTitle ?: link.title ?: "Link"}\" deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                lastDeletedLink?.let { viewModel.restoreLink(it) }
                            }
                        }
                    },
                    modifier = Modifier.semantics { contentDescription = "btn_link_delete" }
                )
            }
        }
    }
}
