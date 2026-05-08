package com.example.pete

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.pete.ui.theme.PeteTheme
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null, val label: String = "") {
    object Login : Screen("login")
    object Main : Screen("main")
    object Boutique : Screen("boutique", Icons.Default.Store, "Shop")
    object Cart : Screen("cart", Icons.Default.ShoppingCart, "Cart")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object Checkout : Screen("checkout")
    object Wishlist : Screen("wishlist")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeteTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val startDestination = if (auth.currentUser != null) Screen.Main.route else Screen.Login.route
                val cartViewModel: CartViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Main.route) {
                            MainScreen(
                                cartViewModel = cartViewModel,
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Main.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    cartViewModel: CartViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val items = listOf(
        Screen.Boutique,
        Screen.Cart,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // Only show bottom bar on top level destinations
            val showBottomBar = items.any { it.route == currentDestination?.route }
            
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Boutique.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Boutique.route) {
                BoutiqueScreen(
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                )
            }
            
            composable(Screen.Cart.route) {
                CartScreen(
                    cartViewModel = cartViewModel,
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogoutClick = onLogout,
                    onWishlistClick = { navController.navigate(Screen.Wishlist.route) }
                )
            }
            
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    cartViewModel = cartViewModel,
                    onBackClick = { navController.popBackStack() },
                    onPaymentSuccess = {
                        android.widget.Toast.makeText(context, "Payment successful via M-Pesa!", android.widget.Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.Boutique.route) {
                            popUpTo(Screen.Boutique.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    cartViewModel = cartViewModel,
                    onBackClick = { navController.popBackStack() },
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                )
            }
            
            composable(Screen.Detail.route) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
                val item = jewelryList.find { it.id == itemId }
                if (item != null) {
                    val wishlistItems by cartViewModel.wishlistItems.collectAsState()
                    val isWishlisted = wishlistItems.contains(item)
                    
                    GemstoneDetailScreen(
                        item = item,
                        isWishlisted = isWishlisted,
                        onWishlistToggle = { cartViewModel.toggleWishlist(item) },
                        onBackClick = { navController.popBackStack() },
                        onPayClick = { 
                            cartViewModel.addToCart(item)
                            android.widget.Toast.makeText(context, "${item.price} added to cart", android.widget.Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onTrackClick = { /* Handle Tracking */ }
                    )
                }
            }
        }
    }
}
