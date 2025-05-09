package ph.edu.cksc.college.appdev.appdev2025.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

const val SNAKE_GAME_SCREEN = "snake_game_screen"

data class SnakePart(val x: Int, val y: Int)
data class Food(val x: Int, val y: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(navController: NavHostController) {
    val gridSize = 15
    var snake by remember { mutableStateOf(listOf(SnakePart(gridSize / 2, gridSize / 2))) }
    var food by remember { mutableStateOf(generateFood(gridSize)) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var gameSessionId by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    fun resetGame() {
        snake = listOf(SnakePart(gridSize / 2, gridSize / 2))
        direction = Direction.RIGHT
        score = 0
        gameOver = false
        isPaused = false
        food = generateFood(gridSize)
        gameSessionId++
    }

    // Request focus when the composable is first shown
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(gameSessionId) {
        while (true) {
            if (!gameOver && !isPaused && snake.isNotEmpty()) {
                delay(150)
                val newSnake = moveSnakeWrap(snake, direction, gridSize)
                if (newSnake.isEmpty()) continue
                snake = newSnake
                // Check if snake ate food
                if (snake.isNotEmpty() && snake.first().x == food.x && snake.first().y == food.y) {
                    score += 10
                    food = generateFood(gridSize)
                    // Don't remove the last part to make snake grow
                } else {
                    // Remove the last part if no food was eaten
                    if (snake.isNotEmpty()) snake = snake.dropLast(1)
                }
                // Check for collisions with self
                if (snake.isNotEmpty() && checkCollision(snake)) {
                    gameOver = true
                }
            } else {
                delay(50) // Prevent CPU spinning when paused or game over
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake Game") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Score: $score",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF2C3E50))
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.DirectionUp -> { if (direction != Direction.DOWN) direction = Direction.UP; true }
                                Key.DirectionDown -> { if (direction != Direction.UP) direction = Direction.DOWN; true }
                                Key.DirectionLeft -> { if (direction != Direction.RIGHT) direction = Direction.LEFT; true }
                                Key.DirectionRight -> { if (direction != Direction.LEFT) direction = Direction.RIGHT; true }
                                else -> false
                            }
                        } else false
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                contentAlignment = Alignment.Center
            ) {
                val cellSize = (maxWidth / gridSize)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw snake
                    snake.forEachIndexed { index, part ->
                        val color = if (index == 0) Color(0xFF27AE60) else Color(0xFF2ECC71)
                        drawRect(
                            color = color,
                            topLeft = Offset(part.x * cellSize.toPx(), part.y * cellSize.toPx()),
                            size = androidx.compose.ui.geometry.Size(cellSize.toPx(), cellSize.toPx())
                        )
                    }
                    // Draw food
                    drawRect(
                        color = Color(0xFFE74C3C),
                        topLeft = Offset(food.x * cellSize.toPx(), food.y * cellSize.toPx()),
                        size = androidx.compose.ui.geometry.Size(cellSize.toPx(), cellSize.toPx())
                    )
                }
            }

            // Direction controls
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Up button
                Button(
                    onClick = { if (direction != Direction.DOWN) direction = Direction.UP },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                ) {
                    Text("↑", style = MaterialTheme.typography.titleLarge)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left button
                    Button(
                        onClick = { if (direction != Direction.RIGHT) direction = Direction.LEFT },
                        modifier = Modifier.padding(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                    ) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                    // Right button
                    Button(
                        onClick = { if (direction != Direction.LEFT) direction = Direction.RIGHT },
                        modifier = Modifier.padding(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                    ) {
                        Text("→", style = MaterialTheme.typography.titleLarge)
                    }
                }
                // Down button
                Button(
                    onClick = { if (direction != Direction.UP) direction = Direction.DOWN },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                ) {
                    Text("↓", style = MaterialTheme.typography.titleLarge)
                }
            }

            if (gameOver) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Game Over") },
                    text = { Text("Your score: $score") },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                        ) {
                            Text("Play Again")
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isPaused = !isPaused },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB)),
                    enabled = !gameOver
                ) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

fun moveSnake(snake: List<SnakePart>, direction: Direction): List<SnakePart> {
    val head = snake.first()
    val newHead = when (direction) {
        Direction.UP -> SnakePart(head.x, head.y - 1)
        Direction.DOWN -> SnakePart(head.x, head.y + 1)
        Direction.LEFT -> SnakePart(head.x - 1, head.y)
        Direction.RIGHT -> SnakePart(head.x + 1, head.y)
    }
    return listOf(newHead) + snake
}

fun moveSnakeWrap(snake: List<SnakePart>, direction: Direction, gridSize: Int): List<SnakePart> {
    val head = snake.first()
    val newHead = when (direction) {
        Direction.UP -> SnakePart(head.x, (head.y - 1 + gridSize) % gridSize)
        Direction.DOWN -> SnakePart(head.x, (head.y + 1) % gridSize)
        Direction.LEFT -> SnakePart((head.x - 1 + gridSize) % gridSize, head.y)
        Direction.RIGHT -> SnakePart((head.x + 1) % gridSize, head.y)
    }
    return listOf(newHead) + snake
}

fun generateFood(gridSize: Int): Food {
    return Food(
        Random.nextInt(gridSize),
        Random.nextInt(gridSize)
    )
}

fun checkCollision(snake: List<SnakePart>): Boolean {
    val head = snake.first()
    return snake.drop(1).any { it.x == head.x && it.y == head.y }
} 