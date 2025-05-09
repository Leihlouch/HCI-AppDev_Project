package ph.edu.cksc.college.appdev.appdev2025.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.bar.SimpleBarDrawer
import me.bytebeats.views.charts.pie.PieChart
import me.bytebeats.views.charts.pie.PieChartData
import me.bytebeats.views.charts.pie.render.SimpleSliceDrawer
import me.bytebeats.views.charts.simpleChartAnimation
import ph.edu.cksc.college.appdev.appdev2025.data.DiaryEntry
import ph.edu.cksc.college.appdev.appdev2025.data.moodList // Import moodList
import ph.edu.cksc.college.appdev.appdev2025.service.StorageService
import ph.edu.cksc.college.appdev.appdev2025.ui.theme.AppDev2025Theme
import kotlin.random.Random

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavHostController) {
    val storageService = StorageService(FirebaseAuth.getInstance(), Firebase.firestore)
    val entries = remember { mutableStateListOf<DiaryEntry>() }
    val coroutineScope = rememberCoroutineScope()

    // Collect diary entries
    LaunchedEffect(Unit) {
        storageService.entries.collect { newEntries ->
            entries.clear()
            entries.addAll(newEntries)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Stats")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        StatsContent(innerPadding, entries)
    }
}

@Composable
fun StatsContent(innerPadding: PaddingValues, entries: List<DiaryEntry>) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                PieChartView(entries)
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                BarChartView(entries)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.5f)) {
                PieChartView(entries)
            }
            Box(modifier = Modifier.fillMaxHeight()) {
                BarChartView(entries)
            }
        }
    }
}

@Composable
fun BarChartView(entries: List<DiaryEntry>) {
    // Calculate mood counts from entries
    val moodCounts = MutableList(moodList.size) { 0 }
    entries.forEach { entry ->
        if (entry.mood in moodList.indices) {
            moodCounts[entry.mood]++
        }
    }

    // Use mood colors for bars with actual counts
    val bars = moodList.mapIndexed { index, mood ->
        BarChartData.Bar(
            label = mood.mood,
            value = moodCounts[index].toFloat(),
            color = mood.color
        )
    }

    BarChart(
        barChartData = BarChartData(bars = bars),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        animation = simpleChartAnimation(),
        barDrawer = SimpleBarDrawer()
    )
}

@Composable
fun PieChartView(entries: List<DiaryEntry>) {
    // Calculate mood counts from entries
    val moodCounts = MutableList(moodList.size) { 0 }
    entries.forEach { entry ->
        if (entry.mood in moodList.indices) {
            moodCounts[entry.mood]++
        }
    }

    // Use mood colors for pie slices with actual counts
    val slices = moodList.mapIndexed { index, mood ->
        PieChartData.Slice(
            moodCounts[index].toFloat(),
            mood.color
        )
    }

    PieChart(
        pieChartData = PieChartData(slices = slices),
        modifier = Modifier.fillMaxSize(),
        animation = simpleChartAnimation(),
        sliceDrawer = SimpleSliceDrawer()
    )
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val navController = rememberNavController()
    AppDev2025Theme {
        StatsScreen(navController)
    }
}
