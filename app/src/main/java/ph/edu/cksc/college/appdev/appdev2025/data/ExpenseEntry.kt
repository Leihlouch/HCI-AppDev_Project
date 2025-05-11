package ph.edu.cksc.college.appdev.appdev2025.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime

data class ExpenseEntry(
    val id: String = "",
    val category: Int = 0,
    val amount: Double = 0.0,
    val description: String = "",
    val dateTime: String = LocalDateTime.now().toString(),
    val userId: String = ""
)

data class ExpenseCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val expenseCategoryList = listOf(
    ExpenseCategory("Food", Icons.Filled.Restaurant, Color(0xffd4a302)),
    ExpenseCategory("Transport", Icons.Filled.DirectionsCar, Color(0xff109900)),
    ExpenseCategory("Shopping", Icons.Filled.ShoppingCart, Color(0xffee0000)),
    ExpenseCategory("Bills", Icons.Filled.Receipt, Color(0xfffc7b03)),
    ExpenseCategory("Entertainment", Icons.Filled.Movie, Color(0xffff0000)),
    ExpenseCategory("Health", Icons.Filled.LocalHospital, Color(0xffee00ee)),
    ExpenseCategory("Education", Icons.Filled.School, Color(0xff0468bf)),
    ExpenseCategory("Housing", Icons.Filled.Home, Color(0xff5a5ae8)),
    ExpenseCategory("Utilities", Icons.Filled.Power, Color(0xff888888)),
    ExpenseCategory("Other", Icons.Filled.MoreHoriz, Color(0xffdd0000))
) 