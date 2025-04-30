package ph.edu.cksc.college.appdev.appdev2025.screens

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    auth: FirebaseAuth, firestore: FirebaseFirestore
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
    //val dataList = storageService.entries.collectAsState(emptyList())
    val dataList =
        storageService.getFilteredEntries(searchQuery).collectAsState(initial = emptyList())
    Log.d("User", storageService.currentUser.toString())

    var isSearchExpanded by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var darkMode by remember { mutableStateOf(sharedPref.getBoolean("darkmode", false)) }

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
                                // crashes because auth.signOut takes time so just exit app
                                //navController.navigate(AUTH_SCREEN)
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
                    Row() {
                        Text("Dark mode")
                        Switch(
                            checked = darkMode,
                            onCheckedChange = {
                                darkMode = it
                                val editor = sharedPref.edit()
                                editor.putBoolean("darkmode", darkMode)
                                editor.apply()
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "App",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
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
                            Text("App Dev Diary")
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
                            IconButton(onClick = {
                                isSearchExpanded = !isSearchExpanded
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                )
                            }
                        },
                    )
                    if (isSearchExpanded) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { query -> searchQuery = query },
                            onSearch = { },
                            active = false,
                            onActiveChange = { },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navController.navigate("$DIARY_ENTRY_SCREEN/")
                }) {
                    Icon(Icons.Filled.Add, "")
                }
            }
        ) { innerPadding ->
            MainScrollContent(dataList.value, innerPadding, navController)
        }
    }
}

@Composable
fun MainScrollContent(
    dataList: List<DiaryEntry>,
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.padding(innerPadding)
    ) {
        LazyColumn {
            items(dataList) { item ->
                DiaryEntryCard(item, navController)
            }
        }
    }
}

@Composable
fun DiaryList(messages: MutableList<DiaryEntry>) {
    val navController = rememberNavController()
    LazyColumn {
        items(messages) { message ->
            DiaryEntryCard(message, navController)
        }
    }
}

@Preview
@Composable
fun PreviewMainScreen() {
    val navController = rememberNavController()
    AppDev2025Theme(dynamicColor = false) {
        MainScreen(navController, FirebaseAuth.getInstance(), Firebase.firestore)
    }
}

@Composable
fun DiaryEntryCard(entry: DiaryEntry, navController: NavHostController) {
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
                            // If the message is expanded, we display all its content
                            // otherwise we only display the first line
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

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
                navController
            )
        }
    }
}