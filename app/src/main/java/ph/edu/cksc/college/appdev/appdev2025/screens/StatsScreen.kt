package ph.edu.cksc.college.appdev.appdev2025.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import me.bytebeats.views.charts.pie.PieChart
import me.bytebeats.views.charts.pie.PieChartData
import me.bytebeats.views.charts.pie.render.SimpleSliceDrawer
import me.bytebeats.views.charts.simpleChartAnimation
import ph.edu.cksc.college.appdev.appdev2025.screens.ui.theme.AppDev2025Theme
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
        StatsScrollContent(innerPadding)
    }
}

@Composable
fun StatsScrollContent(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.padding(innerPadding)
    ) {
        PieChartView()
    }
}

@Composable
fun PieChartView() {
    PieChart(
        pieChartData = PieChartData(
            slices = listOf(
                PieChartData.Slice(
                    randomLength(),
                    randomColor()
                ),
                PieChartData.Slice(randomLength(), randomColor()),
                PieChartData.Slice(randomLength(), randomColor())
            )
        ),
        // Optional properties.
        modifier = Modifier.fillMaxHeight(0.5f),
        animation = simpleChartAnimation(),
        sliceDrawer = SimpleSliceDrawer()
    )
}

private val colors = mutableListOf(
    Color(0XFFF44336),
    Color(0XFFE91E63),
    Color(0XFF9C27B0),
    Color(0XFF673AB7),
    Color(0XFF3F51B5),
    Color(0XFF03A9F4),
    Color(0XFF009688),
    Color(0XFFCDDC39),
    Color(0XFFFFC107),
    Color(0XFFFF5722),
    Color(0XFF795548),
    Color(0XFF9E9E9E),
    Color(0XFF607D8B)
)

private fun randomLength(): Float = Random.Default.nextInt(10, 30).toFloat()
private fun randomColor(): Color {
    val randomIndex = Random.Default.nextInt(colors.size)
    return colors.removeAt(randomIndex)
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val navController = rememberNavController()
    AppDev2025Theme {
        StatsScreen(navController)
    }
}