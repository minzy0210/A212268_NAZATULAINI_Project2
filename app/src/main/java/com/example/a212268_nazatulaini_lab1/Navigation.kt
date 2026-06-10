// app/src/main/java/com/example/a212268_nazatulaini_lab1/Navigation.kt
// CHANGES:
//  - ProfileViewModel created and passed through
//  - "profile" route added
//  - Person icon in CustomBottomNavigation navigates to "profile"
//  - CustomBottomNavigation has a new onProfileClick param

package com.example.a212268_nazatulaini_lab1

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider

@Composable
fun AppNavigation(
    viewModel: ReServeViewModel,
    chatViewModel: ChatViewModel,
    profileViewModel: ProfileViewModel               // ← ADD
) {
    val navController = rememberNavController()

    val goHome: () -> Unit = {
        navController.navigate("home") {
            popUpTo("home") { inclusive = false }
        }
    }

    fun navigateToDetail(itemName: String) {
        val userItem = viewModel.getUserListedItem(itemName)
        when {
            userItem != null -> {
                if (userItem.sellerName == "Me") {
                    navController.navigate(
                        "my_listing_detail/${Uri.encode(userItem.name)}/${Uri.encode(userItem.category)}"
                    )
                } else if (userItem.category.equals("Food", ignoreCase = true)) {
                    navController.navigate("foodDetail/${Uri.encode(itemName)}")
                } else {
                    navController.navigate("nonFoodDetail/${Uri.encode(itemName)}")
                }
            }
            viewModel.getFoodItems().any { it.name == itemName } ->
                navController.navigate("foodDetail/${Uri.encode(itemName)}")
            else ->
                navController.navigate("nonFoodDetail/${Uri.encode(itemName)}")
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            ReServeApp(
                onFoodItemClick     = { navigateToDetail(it) },
                onNonFoodItemClick  = { navigateToDetail(it) },
                onCartClick         = { navController.navigate("cart") },
                onAddClick          = { navController.navigate("add_item") },
                onEmailClick        = { owner, item -> navController.navigate("chat_detail/$owner/$item") },
                chatViewModel       = chatViewModel,
                onAllFoodClick      = { navController.navigate("category/Food") },
                onAllNonFoodClick   = { navController.navigate("category/Non-food") },
                onAllGoingSoonClick = { navController.navigate("going_soon") },
                onHomeClick         = goHome,
                onProfileClick      = { navController.navigate("profile") },  // ← ADD
                viewModel           = viewModel
            )
        }
        composable("foodDetail/{itemName}") { back ->
            val itemName = back.arguments?.getString("itemName") ?: ""
            FoodDetailScreen(
                itemName = itemName,
                onBack = { navController.popBackStack() },
                onHomeClick = goHome,
                onMessageOwner = { owner, item -> navController.navigate("chat_detail/$owner/$item") },
                viewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }
        composable("nonFoodDetail/{itemName}") { back ->
            val itemName = back.arguments?.getString("itemName") ?: ""
            NonFoodDetailScreen(
                itemName = itemName,
                onBack = { navController.popBackStack() },
                onHomeClick = goHome,
                onMessageOwner = { owner, item -> navController.navigate("chat_detail/$owner/$item") },
                viewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }
        composable("cart") {
            CartScreen(
                onBack = { navController.popBackStack() },
                onHomeClick = goHome,
                viewModel = viewModel
            )
        }
        composable("chat_detail/{ownerName}/{itemName}") { back ->
            val owner = back.arguments?.getString("ownerName") ?: ""
            val item  = back.arguments?.getString("itemName") ?: ""
            ChatDetailScreen(
                ownerName = owner,
                itemName  = item,
                onBack    = { navController.popBackStack() },
                onHomeClick  = goHome,
                chatViewModel = chatViewModel
            )
        }
        composable("add_item") {
            AddItemScreen(
                onBack      = { navController.popBackStack() },
                onHomeClick = goHome,
                onViewItem  = { name, cat ->
                    navController.navigate("my_listing_detail/$name/$cat")
                },
                viewModel = viewModel
            )
        }
        composable("going_soon") {
            GoingSoonScreen(
                onBack      = { navController.popBackStack() },
                onItemClick = { navigateToDetail(it) },
                onHomeClick = goHome,
                viewModel   = viewModel
            )
        }
        composable(
            "my_listing_detail/{itemName}/{category}",
            arguments = listOf(
                navArgument("itemName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { back ->
            val name = Uri.decode(back.arguments?.getString("itemName") ?: "")
            val cat  = Uri.decode(back.arguments?.getString("category") ?: "Food")
            MyListingDetailScreen(
                itemName    = name,
                category    = cat,
                onBack      = { navController.popBackStack() },
                onHomeClick = goHome,
                onDeleted   = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                viewModel = viewModel
            )
        }
        composable("category/{filter}") { back ->
            val filter = back.arguments?.getString("filter") ?: "Food"
            CategoryScreen(
                filter             = filter,
                onBack             = { navController.popBackStack() },
                onHomeClick        = goHome,
                onFoodItemClick    = { navigateToDetail(it) },
                onNonFoodItemClick = { navigateToDetail(it) },
                viewModel          = viewModel
            )
        }

        // ── NEW: Profile screen ───────────────────────────────────────
        composable("profile") {
            ProfileScreen(
                onBack          = { navController.popBackStack() },
                onHomeClick     = goHome,
                profileViewModel = profileViewModel
            )
        }
    }
}