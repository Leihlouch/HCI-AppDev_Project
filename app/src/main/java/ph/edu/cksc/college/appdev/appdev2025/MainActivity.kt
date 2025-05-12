package ph.edu.cksc.college.appdev.appdev2025

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import ph.edu.cksc.college.appdev.appdev2025.data.DiaryEntry
import ph.edu.cksc.college.appdev.appdev2025.screens.AboutScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.AuthScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.DiaryEntryScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.DiaryEntryView
import ph.edu.cksc.college.appdev.appdev2025.screens.FavoriteFoodScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.MainScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.MapScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.RegisterScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.SNAKE_GAME_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.screens.StatsScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.ToDoScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.SnakeGameScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.BlockBlastScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.BLOCK_BLAST_SCREEN
import ph.edu.cksc.college.appdev.appdev2025.screens.ExpenseTrackerScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.ExpenseEntryViewScreen
import ph.edu.cksc.college.appdev.appdev2025.screens.SettingsScreen
import ph.edu.cksc.college.appdev.appdev2025.service.StorageService
import ph.edu.cksc.college.appdev.appdev2025.ui.theme.AppDev2025Theme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore

        setContent {
            MainContent()
        }
    }

    @Composable
    fun MainContent() {
        val sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        var darkMode by remember { mutableStateOf(sharedPref.getBoolean("darkmode", false)) }

        AppDev2025Theme(
            dynamicColor = false,
            darkTheme = darkMode
        ) {
            AppNavigation(darkMode) { newDarkMode ->
                darkMode = newDarkMode
                val editor = sharedPref.edit()
                editor.putBoolean("darkmode", newDarkMode)
                editor.apply()
            }
        }
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun AppNavigation(darkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
        val scope = rememberCoroutineScope()
        val storageService = StorageService(auth, firestore)
        val navController = rememberNavController()
        val viewModel = object: DiaryEntryView {
            @SuppressLint("UnrememberedMutableState")
            override var diaryEntry = mutableStateOf(DiaryEntry())
            override var modified: Boolean = false

            override fun onTitleChange(newValue: String) {
                diaryEntry.value = diaryEntry.value.copy(title = newValue)
                modified = true
            }
            override fun onContentChange(newValue: String) {
                diaryEntry.value = diaryEntry.value.copy(content = newValue)
                modified = true
            }
            override fun onMoodChange(newValue: Int) {
                diaryEntry.value = diaryEntry.value.copy(mood = newValue)
                modified = true
            }
            override fun onStarChange(newValue: Int) {
                diaryEntry.value = diaryEntry.value.copy(star = newValue)
                modified = true
            }
            override fun onThemeSongChange(newValue: String) {
                diaryEntry.value = diaryEntry.value.copy(themeSong = newValue)
                modified = true
            }

            override fun onDateTimeChange(newValue: LocalDateTime) {
                val newDueDate = newValue.toString()
                diaryEntry.value = diaryEntry.value.copy(dateTime = newDueDate)
                modified = true
            }
            override fun onDoneClick(popUpScreen: () -> Unit) {
                scope.launch {
                    val editedEntry = diaryEntry.value
                    if (editedEntry.id.isBlank()) {
                        storageService.save(editedEntry)
                    } else {
                        storageService.update(editedEntry)
                    }
                    popUpScreen()
                }
            }
        }
        NavHost(navController = navController, startDestination = MAIN_SCREEN) {
            composable(AUTH_SCREEN) { AuthScreen(navController, auth, firestore) }
            composable(MAIN_SCREEN) {
                MainScreen(
                    navController = navController,
                    auth = auth,
                    firestore = firestore,
                    darkMode = darkMode,
                    onThemeChange = onThemeChange
                )
            }
            composable(ABOUT_SCREEN) { AboutScreen(navController) }
            composable(MAP_SCREEN) { MapScreen(navController) }
            composable(TODO_SCREEN) { ToDoScreen(navController, auth, firestore)}
            composable(SNAKE_GAME_SCREEN) { SnakeGameScreen(navController) }
            composable(FAVEFOOD_SCREEN) { FavoriteFoodScreen(navController) }
            composable(STATS_SCREEN) { StatsScreen(navController) }
            composable(REGISTER_SCREEN) { RegisterScreen(navController) }
            composable(BLOCK_BLAST_SCREEN) { BlockBlastScreen(navController) }
            composable(EXPENSE_TRACKER_SCREEN) { ExpenseTrackerScreen(navController, auth, firestore) }
            composable(SETTINGS_SCREEN) { 
                SettingsScreen(
                    
                )
            }
            composable("$DIARY_ENTRY_SCREEN/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val arguments = requireNotNull(backStackEntry.arguments)
                val id = arguments.getString("id") ?: ""
                LaunchedEffect(id) {
                    if (id.isNotEmpty()) {
                        val entry = storageService.getDiaryEntry(id)
                        if (entry != null) {
                            viewModel.diaryEntry.value = entry
                        }
                    }
                }
                DiaryEntryScreen(id = id, navController = navController, viewModel = viewModel, auth = auth, firestore = firestore)
            }
            composable("expense_entry/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ExpenseEntryViewScreen(
                    expenseId = id,
                    navController = navController,
                    auth = auth,
                    firestore = firestore
                )
            }
        }
    }
}