package ph.edu.cksc.college.appdev.appdev2025.screens

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import ph.edu.cksc.college.appdev.appdev2025.ABOUT_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.AUTH_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.DIARY_ENTRY_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.FAVEFOOD_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.MAP_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.R
import ph.edu.cksc.college.appdev.appdev2025.STATS_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.TODO_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.data.DiaryEntry
import ph.edu.cksc.college.appdev.appdev2025.data.moodList
import ph.edu.cksc.college.appdev.appdev2025.service.AuthService
import ph.edu.cksc.college.appdev.appdev2025.service.StorageService
import ph.edu.cksc.college.appdev.appdev2025.ui.theme.AppDev2025Theme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            Log.d("Authenticated", "user")
            navController.navigate(AUTH_SCREEN)
        }
    }
    val authService = AuthService(LocalContext.current, auth, firestore)
    val storageService = StorageService(auth, firestore)
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val firestoreEntries by storageService.searchEntries(searchQuery).collectAsState(initial = emptyList())
    var entries by remember { mutableStateOf(firestoreEntries) }
    val coroutineScope = rememberCoroutineScope()

    // Keep local entries in sync with Firestore
    LaunchedEffect(firestoreEntries) {
        entries = firestoreEntries
    }

    // Add state for deleted entries
    var deletedEntryIds by remember { mutableStateOf(setOf<String>()) }

    // Filter out deleted entries immediately
    val filteredEntries = entries.filter { it.id !in deletedEntryIds }

    var isSearchExpanded by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "My Diary",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Image(
                        painter = painterResource(R.drawable.diary_icon),
                        contentDescription = "Contact profile picture",
                        modifier = Modifier
                            .size(96.dp)
                            .clickable {
                                navController.navigate(ABOUT_SCREEN)
                            }
                    )
                    HorizontalDivider()

                    Text(
                        "User " + auth.currentUser?.email,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (auth.currentUser == null) {
                        NavigationDrawerItem(
                            label = { Text("Login") },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Login,
                                    contentDescription = null
                                )
                            },
                            selected = false,
                            onClick = { navController.navigate(AUTH_SCREEN) }
                        )
                    } else {
                        NavigationDrawerItem(
                            label = { Text("Logout") },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null
                                )
                            },
                            selected = false,
                            onClick = {
                                authService.logoutUser()
                                activity?.finish()
                            }
                        )
                    }
                    NavigationDrawerItem(
                        label = { Text("Exit") },
                        icon = { Icon(Icons.Outlined.Close, contentDescription = null) },
                        selected = false,
                        onClick = {
                            activity?.finish()
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (darkMode) "Dark mode" else "Light mode",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { onThemeChange(it) }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "App",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = { Text("To-Do List") },
                        icon = { Icon(Icons.Outlined.CheckBox, contentDescription = null) },
                        selected = false,
                        onClick = { navController.navigate(TODO_SCREEN) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Snake Game") },
                        icon = { Icon(Icons.Outlined.Games, contentDescription = null) },
                        selected = false,
                        onClick = { navController.navigate(SNAKE_GAME_SCREEN) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Block Blast") },
                        icon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                        selected = false,
                        onClick = { navController.navigate(BLOCK_BLAST_SCREEN) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Maps") },
                        icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                        selected = false,
                        onClick = { navController.navigate(MAP_SCREEN) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Favorite Restaurants & Foods") },
                        icon = { Icon(Icons.Outlined.Fastfood, contentDescription = null) },
                        selected = false,
                        onClick = { navController.navigate(FAVEFOOD_SCREEN) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Stats") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.PieChart, contentDescription = null) },
                        onClick = { navController.navigate(STATS_SCREEN) },
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        badge = { Text("20") }, // Placeholder
                        onClick = { /* Handle click */ }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("App Dev Diary")
                                IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                                    Icon(
                                        imageVector = if (isSearchExpanded) Icons.Filled.Close else Icons.Filled.Search,
                                        contentDescription = if (isSearchExpanded) "Close Search" else "Search"
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { onThemeChange(!darkMode) }) {
                                Icon(
                                    imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = if (darkMode) "Light Mode" else "Dark Mode"
                                )
                            }
                        }
                    )
                    if (isSearchExpanded) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { query -> searchQuery = query },
                            onSearch = { },
                            active = false,
                            onActiveChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search by title, content, mood, or theme song") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Clear Search"
                                        )
                                    }
                                }
                            }
                        ) {
                            // Search suggestions could go here
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("$DIARY_ENTRY_SCREEN/")
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Entry",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { innerPadding ->
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isNotEmpty()) Icons.Filled.Search else Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) 
                                "No entries found for '$searchQuery'" 
                            else 
                                "No diary entries yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                MainScrollContent(
                    dataList = filteredEntries,
                    innerPadding = innerPadding,
                    navController = navController,
                    onDelete = { id ->
                        coroutineScope.launch {
                            // Add to deleted entries set immediately
                            deletedEntryIds = deletedEntryIds + id
                            // Then delete from Firestore
                            storageService.delete(id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainScrollContent(
    dataList: List<DiaryEntry>,
    innerPadding: PaddingValues,
    navController: NavHostController,
    onDelete: (String) -> Unit
) {
    Box(
        modifier = Modifier.padding(innerPadding)
    ) {
        LazyColumn {
            items(dataList) { item ->
                DiaryEntryCard(
                    entry = item,
                    navController = navController,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun DiaryList(messages: MutableList<DiaryEntry>) {
    val navController = rememberNavController()
    LazyColumn {
        items(messages) { message ->
            DiaryEntryCard(
                entry = message,
                navController = navController,
                onDelete = {} // Dummy lambda for previews or non-deleting context
            )
        }
    }
}

@Preview
@Composable
fun PreviewMainScreen() {
    val navController = rememberNavController()
    AppDev2025Theme(dynamicColor = false) {
        MainScreen(navController, FirebaseAuth.getInstance(), Firebase.firestore, false, {})
    }
}

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    navController: NavHostController,
    onDelete: (String) -> Unit
) {
    Surface(
        tonalElevation = 5.dp,
        modifier = Modifier.padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            // Content of the Diary Entry
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    Log.d("Id", entry.id)
                    navController.navigate("$DIARY_ENTRY_SCREEN/${entry.id}")
                }) {
                    Icon(
                        imageVector = moodList[entry.mood].icon,
                        tint = moodList[entry.mood].color,
                        contentDescription = "About"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                var isExpanded by remember { mutableStateOf(false) }

                val surfaceColor by animateColorAsState(
                    if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                )

                val formatter = DateTimeFormatter.ofPattern("EEEE MMMM d, yyyy h:mm a")
                val date = LocalDateTime.parse(entry.dateTime)

                Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
                    Text(
                        text = entry.title,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = "Theme Song: ${entry.themeSong ?: "No theme song"}",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row() {
                        Text(
                            text = formatter.format(date),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 1.dp,
                        color = surfaceColor,
                        modifier = Modifier
                            .animateContentSize()
                            .padding(1.dp)
                    ) {
                        Text(
                            text = entry.content,
                            modifier = Modifier.padding(all = 4.dp),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Stars
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
            ) {
                Row {
                    repeat(entry.star) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Trash bin icon at the bottom right
            IconButton(
                onClick = { onDelete(entry.id) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Entry",
                    tint = Color.Red
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewDiaryEntryCard() {
    val navController = rememberNavController()
    AppDev2025Theme(dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DiaryEntryCard(
                entry = DiaryEntry(
                    "1", 0, 1,
                    "Lexi",
                    "Test...Test...Test...",
                    LocalDateTime.of(2024, 1, 1, 7, 30).toString()
                ),
                navController = navController,
                onDelete = {} // Dummy lambda for preview
            )
        }
    }
}