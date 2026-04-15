package com.reciperoulette.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.reciperoulette.ui.auth.AuthScreen
import com.reciperoulette.ui.camera.CameraPermissionHandler
import com.reciperoulette.ui.camera.CameraScreen
import com.reciperoulette.ui.favorites.FavoritesScreen
import com.reciperoulette.ui.home.HomeScreen
import com.reciperoulette.ui.recipe.RecipeScreen
import com.reciperoulette.viewmodel.AuthViewModel
import com.reciperoulette.viewmodel.RecipeViewModel

// Route constants
object Routes {
    const val HOME = "home"
    const val AUTH = "auth"
    const val CAMERA = "camera"
    const val RECIPE = "recipe"
    const val FAVORITES = "favorites"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Default.Home),
    BottomNavItem(Routes.CAMERA, "Scan", Icons.Default.Camera),
    BottomNavItem(Routes.FAVORITES, "Saved", Icons.Default.Favorite)
)

// Routes where the bottom bar should be visible
val bottomBarRoutes = setOf(Routes.HOME, Routes.FAVORITES)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val recipeViewModel: RecipeViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy
                                ?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
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
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStartClick = { navController.navigate(Routes.CAMERA) }
                )
            }

            composable(Routes.AUTH) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.popBackStack()
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Routes.CAMERA) {
                CameraPermissionHandler(
                    onBack = { navController.popBackStack() },
                    content = {
                        CameraScreen(
                            onPhotoTaken = { bitmap ->
                                recipeViewModel.analyzeImage(bitmap)
                                navController.navigate(Routes.RECIPE)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                )
            }

            composable(Routes.RECIPE) {
                RecipeScreen(
                    viewModel = recipeViewModel,
                    onScanAgain = {
                        navController.navigate(Routes.CAMERA) {
                            popUpTo(Routes.RECIPE) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onSignInClick = { navController.navigate(Routes.AUTH) }
                )
            }
        }
    }
}