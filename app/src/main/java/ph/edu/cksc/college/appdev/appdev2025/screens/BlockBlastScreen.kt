package ph.edu.cksc.college.appdev.appdev2025.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity

const val BLOCK_BLAST_SCREEN = "block_blast_screen"

data class BlockPiece(val shape: List<Pair<Int, Int>>, val color: Color)

fun randomBlockColor(): Color {
    val colors = listOf(
        Color(0xFF0984E3), // blue
        Color(0xFF00B894), // green
        Color(0xFFFDCB6E), // yellow
        Color(0xFFEA8685), // red
        Color(0xFF6C5CE7), // purple
        Color(0xFFFF7675), // pink
        Color(0xFF00B8D4), // cyan
        Color(0xFF636E72)  // gray
    )
    return colors.random()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockBlastScreen(navController: NavHostController) {
    val gridSize = 9
    val cellSize = 32.dp
    var grid by remember { mutableStateOf(Array(gridSize) { BooleanArray(gridSize) }) }
    var colorGrid by remember { mutableStateOf(Array(gridSize) { Array<Color?>(gridSize) { null } }) }
    var score by remember { mutableStateOf(0) }
    var availablePieces by remember { mutableStateOf(List<BlockPiece?>(2) { generateRandomBlockPiece() }) }
    var draggingPieceIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    var showGameOver by remember { mutableStateOf(false) }
    var dragCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragCursorPos by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    fun canPlacePiece(piece: BlockPiece, x: Int, y: Int, grid: Array<BooleanArray>): Boolean {
        return piece.shape.all { (dx, dy) ->
            val nx = x + dx
            val ny = y + dy
            nx in 0 until gridSize && ny in 0 until gridSize && !grid[ny][nx]
        }
    }

    fun canPlaceAnywhere(piece: BlockPiece, grid: Array<BooleanArray>): Boolean {
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                if (canPlacePiece(piece, x, y, grid)) return true
            }
        }
        return false
    }

    fun canPlaceAnyPiece(pieces: List<BlockPiece?>, grid: Array<BooleanArray>): Boolean {
        return pieces.filterNotNull().any { canPlaceAnywhere(it, grid) }
    }

    fun clearLines(grid: Array<BooleanArray>, colorGrid: Array<Array<Color?>>): Pair<Array<BooleanArray>, Array<Array<Color?>>> {
        val newGrid = grid.map { it.copyOf() }.toTypedArray()
        val newColorGrid = colorGrid.map { it.copyOf() }.toTypedArray()
        // Clear full rows
        for (y in 0 until gridSize) {
            if (newGrid[y].all { it }) {
                for (x in 0 until gridSize) {
                    newGrid[y][x] = false
                    newColorGrid[y][x] = null
                }
                score += gridSize
            }
        }
        // Clear full columns
        for (x in 0 until gridSize) {
            if ((0 until gridSize).all { newGrid[it][x] }) {
                for (y in 0 until gridSize) {
                    newGrid[y][x] = false
                    newColorGrid[y][x] = null
                }
                score += gridSize
            }
        }
        return Pair(newGrid, newColorGrid)
    }

    fun placePiece(piece: BlockPiece, x: Int, y: Int, pieceIndex: Int) {
        var newGrid = grid.map { it.copyOf() }.toTypedArray()
        var newColorGrid = colorGrid.map { it.copyOf() }.toTypedArray()
        piece.shape.forEach { (dx, dy) ->
            val nx = x + dx
            val ny = y + dy
            if (nx in 0 until gridSize && ny in 0 until gridSize) {
                newGrid[ny][nx] = true
                newColorGrid[ny][nx] = piece.color
            }
        }
        val (clearedGrid, clearedColorGrid) = clearLines(newGrid, newColorGrid)
        grid = clearedGrid
        colorGrid = clearedColorGrid
        score += piece.shape.size
        // Mark this piece as used (null)
        availablePieces = availablePieces.mapIndexed { idx, p -> if (idx == pieceIndex) null else p }
        // If both are null, refresh both
        if (availablePieces.all { it == null }) {
            availablePieces = List<BlockPiece?>(2) { generateRandomBlockPiece() }
        }
        if (!canPlaceAnyPiece(availablePieces, clearedGrid)) {
            showGameOver = true
        }
    }

    fun resetGame() {
        grid = Array(gridSize) { BooleanArray(gridSize) }
        colorGrid = Array(gridSize) { Array<Color?>(gridSize) { null } }
        score = 0
        availablePieces = List<BlockPiece?>(2) { generateRandomBlockPiece() }
        showGameOver = false
        dragging = false
        draggingOffset = Offset.Zero
        dragCell = null
        draggingPieceIndex = null
        dragCursorPos = Offset.Zero
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block Blast") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            Text("Score: $score", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
            // Game grid
            Box(
                modifier = Modifier
                    .size(cellSize * gridSize)
                    .background(Color(0xFF222F3E), RoundedCornerShape(8.dp))
            ) {
                for (y in 0 until gridSize) {
                    for (x in 0 until gridSize) {
                        Box(
                            modifier = Modifier
                                .offset((x * cellSize.value).dp, (y * cellSize.value).dp)
                                .size(cellSize)
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(2.dp))
                                .background(colorGrid[y][x] ?: Color.Transparent, RoundedCornerShape(2.dp))
                        )
                    }
                }
                // Show dragging piece preview at cursor
                if (dragging && dragCell != null && draggingPieceIndex != null) {
                    val piece = availablePieces[draggingPieceIndex!!]
                    if (piece != null) {
                        val minX = piece.shape.minOfOrNull { it.first } ?: 0
                        val minY = piece.shape.minOfOrNull { it.second } ?: 0
                        val cellSizePx = with(density) { cellSize.toPx() }
                        val anchorOffset = Offset(minX * cellSizePx, minY * cellSizePx)
                        for ((dx, dy) in piece.shape) {
                            val px = dragCursorPos.x + (dx - minX) * cellSizePx - anchorOffset.x
                            val py = dragCursorPos.y + (dy - minY) * cellSizePx - anchorOffset.y
                            // Only draw if within grid bounds
                            if (px >= 0 && py >= 0 && px < gridSize * cellSizePx && py < gridSize * cellSizePx) {
                                Box(
                                    modifier = Modifier
                                        .absoluteOffset { IntOffset(px.roundToInt(), py.roundToInt()) }
                                        .size(cellSize)
                                        .background(piece.color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            // Two piece previews
            Text("Drag one of the pieces below onto the grid", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                availablePieces.forEachIndexed { idx, piece ->
                    if (piece != null) {
                        Box(
                            modifier = Modifier
                                .size(cellSize * 5)
                                .pointerInput(piece, idx) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            dragging = true
                                            draggingOffset = Offset.Zero
                                            dragCell = null
                                            draggingPieceIndex = idx
                                            dragCursorPos = offset
                                        },
                                        onDrag = { change, dragAmount ->
                                            draggingOffset += Offset(dragAmount.x, dragAmount.y)
                                            dragCursorPos += Offset(dragAmount.x, dragAmount.y)
                                            val cellSizePx = with(density) { cellSize.toPx() }
                                            val cellX = (dragCursorPos.x / cellSizePx).roundToInt()
                                            val cellY = (dragCursorPos.y / cellSizePx).roundToInt()
                                            dragCell = Pair(cellX, cellY)
                                        },
                                        onDragEnd = {
                                            dragging = false
                                            val minX = piece.shape.minOfOrNull { it.first } ?: 0
                                            val minY = piece.shape.minOfOrNull { it.second } ?: 0
                                            if (dragCell != null && draggingPieceIndex == idx && canPlacePiece(piece, dragCell!!.first - minX, dragCell!!.second - minY, grid)) {
                                                placePiece(piece, dragCell!!.first - minX, dragCell!!.second - minY, idx)
                                            }
                                            draggingOffset = Offset.Zero
                                            dragCell = null
                                            draggingPieceIndex = null
                                            dragCursorPos = Offset.Zero
                                        },
                                        onDragCancel = {
                                            dragging = false
                                            draggingOffset = Offset.Zero
                                            dragCell = null
                                            draggingPieceIndex = null
                                            dragCursorPos = Offset.Zero
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val minX = piece.shape.minOfOrNull { it.first } ?: 0
                            val minY = piece.shape.minOfOrNull { it.second } ?: 0
                            for ((dx, dy) in piece.shape) {
                                Box(
                                    modifier = Modifier
                                        .absoluteOffset { IntOffset(((dx - minX) * with(density) { cellSize.toPx() }).roundToInt(), ((dy - minY) * with(density) { cellSize.toPx() }).roundToInt()) }
                                        .size(cellSize)
                                        .background(piece.color, RoundedCornerShape(4.dp))
                                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    } else {
                        // Show empty space for used piece
                        Spacer(modifier = Modifier.size(cellSize * 5))
                    }
                }
            }
            if (showGameOver) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Game Over") },
                    text = { Text("Your score: $score") },
                    confirmButton = {
                        Button(onClick = { resetGame() }) { Text("Play Again") }
                    }
                )
            }
        }
    }
}

fun generateRandomBlockPiece(): BlockPiece {
    val shapes = listOf(
        listOf(Pair(0, 0)), // single block
        listOf(Pair(0, 0), Pair(1, 0)), // 2 horizontal
        listOf(Pair(0, 0), Pair(0, 1)), // 2 vertical
        listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1)), // L shape
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)), // 3 horizontal
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)), // 3 vertical
        listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1)), // small square
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0)), // 4 horizontal
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)), // 4 vertical
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(1, 1)), // T shape
        listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1)), // S shape
        listOf(Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(1, 1)), // Z shape
        listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1)), // L mirrored
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1)), // L right
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 2)), // L down
        listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(1, 2)), // L tall
        listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1)), // 2x2 square
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(1, 1)), // fat T
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1), Pair(2, 2)), // L long
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(2, 0)), // corner
    )
    return BlockPiece(
        shape = shapes[Random.nextInt(shapes.size)],
        color = randomBlockColor()
    )
}
