package ph.edu.cksc.college.appdev.appdev2025.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.bar.SimpleBarDrawer
import me.bytebeats.views.charts.pie.PieChart
import me.bytebeats.views.charts.pie.PieChartData
import me.bytebeats.views.charts.pie.render.SimpleSliceDrawer
import me.bytebeats.views.charts.simpleChartAnimation
import ph.edu.cksc.college.appdev.appdev2025.data.moodList // Import moodList
import ph.edu.cksc.college.appdev.appdev2025.ui.theme.AppDev2025Theme
import kotlin.random.Random

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavHostController) {
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
        StatsContent(innerPadding)
    }
}

@Composable
fun StatsContent(innerPadding: PaddingValues) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                PieChartView()
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                BarChartView()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.5f)) {
                PieChartView()
            }
            Box(modifier = Modifier.fillMaxHeight()) { // This will take the remaining height
                BarChartView()
            }
        }
    }
}

@Composable
fun BarChartView() {
    // Use mood colors for bars
    val bars = moodList.mapIndexed { index, mood ->
        BarChartData.Bar(
            label = mood.mood,
            value = Random.nextFloat() * 100, // You might want to use actual data here
            color = mood.color
        )
    }

    BarChart(
        barChartData = BarChartData(bars = bars),
        modifier = Modifier
            .fillMaxSize() // Fill the available space in the parent Box
            .padding(top = 12.dp),
        animation = simpleChartAnimation(),
        barDrawer = SimpleBarDrawer()
    )
}

@Composable
fun PieChartView() {
    // Use mood colors for pie slices
    val slices = moodList.map { mood ->
        PieChartData.Slice(
            randomLength(), // You might want to use actual data here
            mood.color
        )
    }

    PieChart(
        pieChartData = PieChartData(slices = slices),
        // Optional properties.
        modifier = Modifier.fillMaxSize(), // Fill the available space in the parent Box
        animation = simpleChartAnimation(),
        sliceDrawer = SimpleSliceDrawer()
    )
}

private fun randomLength(): Float = Random.Default.nextInt(10, 30).toFloat()
// Removed randomColor() as we are now using mood colors

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val navController = rememberNavController()
    AppDev2025Theme {
        StatsScreen(navController)
    }
}
