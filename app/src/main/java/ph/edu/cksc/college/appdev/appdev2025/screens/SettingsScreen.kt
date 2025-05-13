package ph.edu.cksc.college.appdev.appdev2025.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    var notificationsEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true)) 
    }
    var soundEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("sound_enabled", true)) 
    }
    var vibrationEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("vibration_enabled", true)) 
    }
    var autoSaveEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("auto_save_enabled", true)) 
    }

    fun updateSetting(key: String, value: Boolean) {
        sharedPrefs.edit().putBoolean(key, value).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Theme Settings
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = if (darkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    title = "Dark Mode",
                    description = "Enable dark theme for the app",
                    trailing = {
                        Switch(
                            checked = darkMode,
                            onCheckedChange = onThemeChange
                        )
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Notification Settings
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Enable Notifications",
                    description = "Receive notifications for important updates",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { 
                                notificationsEnabled = it
                                updateSetting("notifications_enabled", it)
                            }
                        )
                    }
                )
                if (notificationsEnabled) {
                    SettingsItem(
                        icon = Icons.Filled.VolumeUp,
                        title = "Sound",
                        description = "Play sound for notifications",
                        trailing = {
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { 
                                    soundEnabled = it
                                    updateSetting("sound_enabled", it)
                                }
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Filled.Vibration,
                        title = "Vibration",
                        description = "Vibrate for notifications",
                        trailing = {
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = { 
                                    vibrationEnabled = it
                                    updateSetting("vibration_enabled", it)
                                }
                            )
                        }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // General Settings
            SettingsSection(title = "General") {
                SettingsItem(
                    icon = Icons.Filled.Save,
                    title = "Auto Save",
                    description = "Automatically save changes",
                    trailing = {
                        Switch(
                            checked = autoSaveEnabled,
                            onCheckedChange = { 
                                autoSaveEnabled = it
                                updateSetting("auto_save_enabled", it)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
    }
}