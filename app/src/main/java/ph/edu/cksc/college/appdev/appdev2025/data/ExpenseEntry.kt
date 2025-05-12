package ph.edu.cksc.college.appdev.appdev2025.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

data class ExpenseEntry(
    val id: String = "",
    val category: Int = 0,
    val amount: Double = 0.0,
    val description: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val userId: String = ""
)

data class ExpenseCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val expenseCategoryList = listOf(
    ExpenseCategory("Food", Icons.Default.Restaurant, Color(0xFF4CAF50)),
    ExpenseCategory("Transportation", Icons.Default.DirectionsCar, Color(0xFF2196F3)),
    ExpenseCategory("Shopping", Icons.Default.ShoppingCart, Color(0xFF9C27B0)),
    ExpenseCategory("Bills", Icons.Default.Receipt, Color(0xFFF44336)),
    ExpenseCategory("Entertainment", Icons.Default.Movie, Color(0xFFFF9800)),
    ExpenseCategory("Health", Icons.Default.LocalHospital, Color(0xFFE91E63)),
    ExpenseCategory("Education", Icons.Default.School, Color(0xFF3F51B5)),
    ExpenseCategory("Others", Icons.Default.More, Color(0xFF607D8B))
) 