package com.example.a212268_nazatulaini_lab1

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FoodDetail : Screen("food_detail/{itemName}") {
        fun createRoute(itemName: String) = "food_detail/$itemName"
    }
    object NonFoodDetail : Screen("nonfood_detail/{itemName}") {
        fun createRoute(itemName: String) = "nonfood_detail/$itemName"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            }
        ) {
            ReServeApp(
                onFoodItemClick = { itemName ->
                    navController.navigate(Screen.FoodDetail.createRoute(itemName))
                },
                onNonFoodItemClick = { itemName ->
                    navController.navigate(Screen.NonFoodDetail.createRoute(itemName))
                }
            )
        }

        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("itemName") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) { backStackEntry ->
            val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
            FoodDetailScreen(
                itemName = itemName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NonFoodDetail.route,
            arguments = listOf(navArgument("itemName") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) { backStackEntry ->
            val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
            NonFoodDetailScreen(
                itemName = itemName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
