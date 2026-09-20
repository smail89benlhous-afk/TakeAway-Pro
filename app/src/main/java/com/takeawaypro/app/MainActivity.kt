package com.takeawaypro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.takeawaypro.app.screens.AdminDashboardScreen
import com.takeawaypro.app.screens.AdminLoginScreen
import com.takeawaypro.app.screens.AdminNotificationsScreen
import com.takeawaypro.app.screens.AdminOrderDetailScreen
import com.takeawaypro.app.screens.AdminProductFormScreen
import com.takeawaypro.app.screens.AdminProductsScreen
import com.takeawaypro.app.screens.AdminRestaurantDetailScreen
import com.takeawaypro.app.screens.AdminRestaurantsScreen
import com.takeawaypro.app.screens.CartScreen
import com.takeawaypro.app.screens.CheckoutScreen
import com.takeawaypro.app.screens.HomeScreen
import com.takeawaypro.app.screens.LoginScreen
import com.takeawaypro.app.screens.OrderTrackingScreen
import com.takeawaypro.app.screens.RegisterScreen
import com.takeawaypro.app.screens.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TakeAwayProApp() }
    }
}

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val TRACKING = "tracking/{orderId}"
    fun tracking(orderId: Int) = "tracking/$orderId"

    // Admin / owner console
    const val ADMIN_LOGIN = "admin/login"
    const val ADMIN_DASHBOARD = "admin/dashboard"
    const val ADMIN_PRODUCTS = "admin/products"
    const val ADMIN_PRODUCT_FORM = "admin/products/form?productId={productId}"
    fun adminProductForm(productId: String?) =
        "admin/products/form?productId=${productId ?: ""}"
    const val ADMIN_RESTAURANTS = "admin/restaurants"
    const val ADMIN_RESTAURANT_DETAIL = "admin/restaurants/{id}"
    fun adminRestaurantDetail(id: Int) = "admin/restaurants/$id"
    const val ADMIN_NOTIFICATIONS = "admin/notifications"
    const val ADMIN_ORDER_DETAIL = "admin/orders/{orderId}"
    fun adminOrderDetail(orderId: Int) = "admin/orders/$orderId"
}

@Composable
fun TakeAwayProApp() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val appData = remember { AppData(LocalStore(context)) }

    MaterialTheme {
        Surface(modifier = Modifier) {
            NavHost(navController = navController, startDestination = Routes.WELCOME) {

                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        onLogin = { navController.navigate(Routes.LOGIN) },
                        onRegister = { navController.navigate(Routes.REGISTER) },
                        onBrowse = { navController.navigate(Routes.HOME) },
                        onAdminAccess = { navController.navigate(Routes.ADMIN_LOGIN) }
                    )
                }

                composable(Routes.ADMIN_LOGIN) {
                    AdminLoginScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onUnlocked = {
                            navController.navigate(Routes.ADMIN_DASHBOARD) {
                                popUpTo(Routes.ADMIN_LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onLoginSuccess = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        },
                        onGoToRegister = { navController.navigate(Routes.REGISTER) }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onAccountCreated = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        appData = appData,
                        onOpenCart = { navController.navigate(Routes.CART) }
                    )
                }

                composable(Routes.CART) {
                    CartScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onCheckout = { navController.navigate(Routes.CHECKOUT) }
                    )
                }

                composable(Routes.CHECKOUT) {
                    CheckoutScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onOrderConfirmed = { orderId ->
                            navController.navigate(Routes.tracking(orderId)) {
                                popUpTo(Routes.HOME)
                            }
                        }
                    )
                }

                composable(
                    Routes.TRACKING,
                    arguments = listOf(navArgument("orderId") { type = NavType.IntType })
                ) { backStackEntry ->
                    OrderTrackingScreen(
                        appData = appData,
                        orderId = backStackEntry.arguments?.getInt("orderId") ?: 0,
                        onBackToHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    )
                }

                // ---------- Admin / owner console ----------

                composable(Routes.ADMIN_DASHBOARD) {
                    AdminDashboardScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onOpenProducts = { navController.navigate(Routes.ADMIN_PRODUCTS) },
                        onOpenRestaurants = { navController.navigate(Routes.ADMIN_RESTAURANTS) },
                        onOpenNotifications = { navController.navigate(Routes.ADMIN_NOTIFICATIONS) }
                    )
                }

                composable(Routes.ADMIN_PRODUCTS) {
                    AdminProductsScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onAddProduct = { navController.navigate(Routes.adminProductForm(null)) },
                        onEditProduct = { productId ->
                            navController.navigate(Routes.adminProductForm(productId))
                        }
                    )
                }

                composable(
                    Routes.ADMIN_PRODUCT_FORM,
                    arguments = listOf(navArgument("productId") {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")?.ifBlank { null }
                    AdminProductFormScreen(
                        appData = appData,
                        productId = productId,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable(Routes.ADMIN_RESTAURANTS) {
                    AdminRestaurantsScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onOpenRestaurant = { id ->
                            navController.navigate(Routes.adminRestaurantDetail(id))
                        }
                    )
                }

                composable(
                    Routes.ADMIN_RESTAURANT_DETAIL,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    AdminRestaurantDetailScreen(
                        appData = appData,
                        restaurantId = backStackEntry.arguments?.getInt("id") ?: 0,
                        onBack = { navController.popBackStack() },
                        onOpenOrder = { orderId ->
                            navController.navigate(Routes.adminOrderDetail(orderId))
                        }
                    )
                }

                composable(Routes.ADMIN_NOTIFICATIONS) {
                    AdminNotificationsScreen(
                        appData = appData,
                        onBack = { navController.popBackStack() },
                        onOpenOrder = { orderId ->
                            navController.navigate(Routes.adminOrderDetail(orderId))
                        }
                    )
                }

                composable(
                    Routes.ADMIN_ORDER_DETAIL,
                    arguments = listOf(navArgument("orderId") { type = NavType.IntType })
                ) { backStackEntry ->
                    AdminOrderDetailScreen(
                        appData = appData,
                        orderId = backStackEntry.arguments?.getInt("orderId") ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
