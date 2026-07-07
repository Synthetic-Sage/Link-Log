package com.synthetic.linklog.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.synthetic.linklog.ui.downloads.DownloadsScreen
import com.synthetic.linklog.ui.home.FoldersScreen
import com.synthetic.linklog.ui.home.HomeViewModel
import com.synthetic.linklog.ui.home.LinksScreen
import com.synthetic.linklog.ui.linkdetails.LinkDetailsScreen
import com.synthetic.linklog.ui.player.VideoPlayerScreen
import com.synthetic.linklog.ui.settings.SettingsScreen
import com.synthetic.linklog.ui.theme.LocalDarkTheme
import com.synthetic.linklog.ui.theme.Spacing
import kotlinx.coroutines.launch

// ── Routes ────────────────────────────────────────────────────────────────────
private const val ROUTE_FOLDERS   = "folders"
private const val ROUTE_LINKS     = "links"
private const val ROUTE_DOWNLOADS = "downloads"
private const val ROUTE_SETTINGS  = "settings"
private const val ROUTE_LINK_DETAIL = "link/{linkId}"
private const val ROUTE_PLAYER    = "player/{linkId}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController = rememberNavController(),
    initialSharedUrl: String? = null,
    darkTheme: Boolean = LocalDarkTheme.current,
    onThemeToggle: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show Add Link dialog when a URL is shared from another app
    var showAddLinkDialog by remember { mutableStateOf(initialSharedUrl != null) }
    var sharedUrl by remember { mutableStateOf(initialSharedUrl ?: "") }
    LaunchedEffect(initialSharedUrl) {
        if (!initialSharedUrl.isNullOrBlank()) {
            sharedUrl = initialSharedUrl
            showAddLinkDialog = true
        }
    }

    // Search bar state (backed by ViewModel)
    val isSearchActive by homeViewModel.isSearchActive.collectAsState()
    val searchQuery   by homeViewModel.searchQuery.collectAsState()
    val activeFilter  by homeViewModel.activeSearchFilter.collectAsState()
    var filterDropdownExpanded by remember { mutableStateOf(false) }

    // Bottom nav is only visible on top-level routes
    val showBottomBar = currentRoute in listOf(ROUTE_FOLDERS, ROUTE_LINKS, ROUTE_DOWNLOADS, ROUTE_SETTINGS)
    val showFab = currentRoute in listOf(ROUTE_FOLDERS, ROUTE_LINKS)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBottomBar) {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { homeViewModel.setSearchQuery(it) },
                                placeholder = { Text("Search links…") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Link Log")
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                homeViewModel.setSearchActive(false)
                                homeViewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
                            }
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            Box {
                                TextButton(onClick = { filterDropdownExpanded = true }) {
                                    Text(activeFilter)
                                    Icon(Icons.Filled.ArrowDropDown, null)
                                }
                                DropdownMenu(
                                    expanded = filterDropdownExpanded,
                                    onDismissRequest = { filterDropdownExpanded = false }
                                ) {
                                    listOf("All", "@title", "@notes").forEach { f ->
                                        DropdownMenuItem(
                                            text = { Text(f) },
                                            onClick = {
                                                homeViewModel.setSearchFilter(f)
                                                filterDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            IconButton(onClick = { navController.navigate(ROUTE_DOWNLOADS) { launchSingleTop = true; restoreState = true } }) {
                                Icon(Icons.Filled.Download, "Downloads")
                            }
                            IconButton(onClick = { homeViewModel.setSearchActive(true) }) {
                                Icon(Icons.Filled.Search, "Search")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Folder, "Folders") },
                        label = { Text("Folders") },
                        selected = currentRoute == ROUTE_FOLDERS || currentRoute == ROUTE_LINKS,
                        onClick = {
                            navController.navigate(ROUTE_FOLDERS) {
                                popUpTo(ROUTE_FOLDERS) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Download, "Downloads") },
                        label = { Text("Downloads") },
                        selected = currentRoute == ROUTE_DOWNLOADS,
                        onClick = {
                            navController.navigate(ROUTE_DOWNLOADS) {
                                popUpTo(ROUTE_FOLDERS) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == ROUTE_SETTINGS,
                        onClick = {
                            navController.navigate(ROUTE_SETTINGS) {
                                popUpTo(ROUTE_FOLDERS) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                when (currentRoute) {
                    ROUTE_FOLDERS -> {
                        // Folder-add FAB — distinct shape: square with folder-plus icon
                        LargeFloatingActionButton(
                            onClick = { homeViewModel.triggerAddFolder = true },
                            modifier = Modifier.semantics { contentDescription = "btn_folder_add" },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Filled.CreateNewFolder, "Add Folder", modifier = Modifier.size(Spacing.xl))
                        }
                    }
                    ROUTE_LINKS -> {
                        // Link-add FAB — rounded circle with link-plus icon (visually distinct from folder FAB)
                        FloatingActionButton(
                            onClick = { showAddLinkDialog = true },
                            modifier = Modifier.semantics { contentDescription = "btn_link_add" },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Filled.AddLink, "Add Link")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_FOLDERS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_FOLDERS) {
                FoldersScreen(
                    viewModel = homeViewModel,
                    onFolderClick = { _ ->
                        navController.navigate(ROUTE_LINKS) {
                            launchSingleTop = true
                        }
                    },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(ROUTE_LINKS) {
                LinksScreen(
                    viewModel = homeViewModel,
                    onLinkClick = { linkId -> navController.navigate("link/$linkId") },
                    onAddLinkClick = { showAddLinkDialog = true },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(ROUTE_DOWNLOADS) {
                DownloadsScreen(onVideoClick = { linkId -> navController.navigate("player/$linkId") })
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(onThemeToggle = onThemeToggle, isDarkTheme = darkTheme)
            }
            composable(
                route = ROUTE_LINK_DETAIL,
                arguments = listOf(navArgument("linkId") { type = NavType.LongType })
            ) {
                LinkDetailsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = ROUTE_PLAYER,
                arguments = listOf(navArgument("linkId") { type = NavType.LongType })
            ) { backStackEntry ->
                VideoPlayerScreen(linkId = backStackEntry.arguments?.getLong("linkId") ?: 0L)
            }
        }

        // ── Share-to-Save / Add Link sheet ─────────────────────────────────
        if (showAddLinkDialog) {
            AddLinkBottomSheet(
                viewModel = homeViewModel,
                initialUrl = sharedUrl,
                onDismiss = {
                    showAddLinkDialog = false
                    sharedUrl = ""
                }
            )
        }
    }

    // ── Onboarding welcome dialog ─────────────────────────────────────────────
    val isFirstLaunch by homeViewModel.isFirstLaunch.collectAsState()
    var showOnboarding by remember { mutableStateOf(false) }
    LaunchedEffect(isFirstLaunch) { if (isFirstLaunch) showOnboarding = true }

    if (showOnboarding) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Welcome to Link Log! 👋") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("Here's a quick guide:")
                    Text("• Bottom nav: Folders → Downloads → Settings")
                    Text("• Tap the 📁+ FAB to create a folder")
                    Text("• Tap the 🔗+ FAB (inside a folder) to add a link")
                    Text("• Share any URL from any app directly to Link Log")
                    Text("• Tap the rank badge on a card to reorder links")
                }
            },
            confirmButton = {
                Button(onClick = {
                    homeViewModel.completeOnboarding()
                    showOnboarding = false
                }) { Text("Got it!") }
            }
        )
    }
}
