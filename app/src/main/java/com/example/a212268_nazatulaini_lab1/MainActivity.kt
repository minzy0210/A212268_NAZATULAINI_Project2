package com.example.a212268_nazatulaini_lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a212268_nazatulaini_lab1.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(dynamicColor = false) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun ReServeApp(
    onFoodItemClick: (String) -> Unit = {},
    onNonFoodItemClick: (String) -> Unit = {},
    viewModel: ReServeViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchMode by remember { mutableStateOf(false) }

    // State from ViewModel
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val allFood = viewModel.getFoodItems().map { it.name }
    val allNonFood = viewModel.getNonFoodItems().map { it.name }
    val goingSoon = viewModel.getGoingSoon().map { it.name }
    val filteredResults = if (searchQuery.isBlank()) emptyList()
    else viewModel.searchItems(searchQuery).map { it.name }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                CustomBottomNavigation(
                    onHomeClick = { isSearchMode = false },
                    onSearchClick = { isSearchMode = true }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (isSearchMode) {
                    Text(
                        "Search Items",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Found ${filteredResults.size} items",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    filteredResults.forEach { item ->
                        FullWidthItemCard(
                            name = item,
                            imageRes = getItemImage(item),
                            onItemClick = {
                                if (allFood.contains(item)) onFoodItemClick(item)
                                else onNonFoodItemClick(item)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                } else {
                    HeaderSection(cartCount = cartItems.size)
                    Spacer(modifier = Modifier.height(20.dp))

                    FilterSection(selectedFilter) { selectedFilter = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (selectedFilter == "All") {
                        PromotionSection()
                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalRow("Food Items", allFood) { itemName ->
                            onFoodItemClick(itemName)
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalRow("Non-food Items", allNonFood) { itemName ->
                            onNonFoodItemClick(itemName)
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalRow("Going Soon", goingSoon) { itemName ->
                            onFoodItemClick(itemName)
                        }

                    } else {
                        val itemsToShow = if (selectedFilter == "Food") allFood else allNonFood
                        Text(
                            "Category: $selectedFilter",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        itemsToShow.forEach { item ->
                            FullWidthItemCard(
                                name = item,
                                imageRes = getItemImage(item),
                                onItemClick = {
                                    if (selectedFilter == "Food") onFoodItemClick(item)
                                    else onNonFoodItemClick(item)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Type to search...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(28.dp),
        singleLine = true
    )
}

@Composable
fun HorizontalRow(title: String, items: List<String>, onItemClick: (String) -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                "All >",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { name ->
                SmallItemCard(name, getItemImage(name), onClick = { onItemClick(name) })
            }
        }
    }
}

@Composable
fun SmallItemCard(name: String, imageRes: Int, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "21.6km",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FullWidthItemCard(name: String, imageRes: Int, onItemClick: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    "Available for reservation near you. Tap the button below to reserve this item before it's gone!",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { onItemClick() },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("View Details →", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

fun getItemImage(name: String): Int {
    return when (name) {
        "Apple" -> R.drawable.apple
        "Bread" -> R.drawable.bread
        "Milk" -> R.drawable.milk
        "Cake" -> R.drawable.cake
        "Banana" -> R.drawable.banana
        "Pizza" -> R.drawable.pizza
        "Guitar" -> R.drawable.guitar
        "Trampoline" -> R.drawable.trampoline
        "Plant Pot" -> R.drawable.plantpot
        "Chair" -> R.drawable.chair
        "Table" -> R.drawable.table
        "Books" -> R.drawable.books
        else -> R.drawable.ic_launcher_background
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Food", "Non-food").forEach { tag ->
            FilterChip(
                selected = selected == tag,
                onClick = { onSelect(tag) },
                label = { Text(tag) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    labelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun HeaderSection(cartCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Listings within 5km",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "Kajang Municipal Council",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
        }
        
        // Cart Icon with Badge
        BadgedBox(
            badge = {
                if (cartCount > 0) {
                    Badge { Text(cartCount.toString()) }
                }
            }
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PromotionSection() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PromoCard(
                "One person's surplus is another's treasure",
                MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        item {
            PromoCard(
                "Eco-friendly sharing!",
                MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

@Composable
fun PromoCard(text: String, color: Color) {
    Card(
        modifier = Modifier.size(240.dp, 100.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.9f))
    ) {
        Box(
            Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CustomBottomNavigation(onHomeClick: () -> Unit, onSearchClick: () -> Unit) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReServeAppPreview() {
    AppTheme(dynamicColor = false) {
        ReServeApp()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReServeAppDarkThemePreview() {
    AppTheme(darkTheme = true, dynamicColor = false) {
        ReServeApp()
    }
}
