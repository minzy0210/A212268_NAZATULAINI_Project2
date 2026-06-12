package com.example.a212268_nazatulaini_lab1

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter

private enum class MyListingDialog { NONE, DELETE, SOLD }

/**
 * Pass the shared [ReServeViewModel] instance down from the NavHost —
 * never call viewModel() here directly, because ReServeViewModel requires
 * a custom Factory (it depends on ReServeRepository).
 * The factory is only wired up in MainActivity / the top-level composition.
 */
@Composable
fun MyListingDetailScreen(
    itemName: String,
    category: String,
    onBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onDeleted: () -> Unit = {},
    viewModel: ReServeViewModel          // ← always injected from NavHost; no default
) {
    // ── Observe live Room-backed StateFlows ───────────────────────────
    val userListedItems    by viewModel.userListedItems.collectAsStateWithLifecycle()
    val reservedQuantities by viewModel.reservedQuantities.collectAsStateWithLifecycle()
    val borrowedItems      by viewModel.borrowedItems.collectAsStateWithLifecycle()

    val userItem     = userListedItems.firstOrNull { it.name == itemName }
    val reservedCount = reservedQuantities[itemName] ?: 0

    var showDialog   by remember { mutableStateOf(MyListingDialog.NONE) }
    var isMarkedSold by remember { mutableStateOf(false) }

    val isFood = category.equals("Food", ignoreCase = true)
    val discountedPrice = if (userItem != null && isFood)
        userItem.originalPrice * (1 - userItem.discountPercent / 100.0)
    else 0.0

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Background ────────────────────────────────────────────────
        Image(
            painter = painterResource(R.drawable.wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                CustomBottomNavigation(
                    onHomeClick   = onHomeClick,
                    onSearchClick = onHomeClick,
                    onEmailClick  = onHomeClick,
                    onAddClick    = onHomeClick
                )
            }
        ) { innerPadding ->

            if (userItem == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Show a spinner briefly while Room loads; if it stays null, show error
                    val userListedItemsLoaded by viewModel.userListedItems.collectAsStateWithLifecycle()
                    val confirmed = userListedItemsLoaded.any { it.name == itemName }
                    if (confirmed || userListedItemsLoaded.isNotEmpty()) {
                        // Room has loaded but item genuinely not found
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Listing not found", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = onBack) { Text("Go Back") }
                        }
                    } else {
                        // Still loading from Room
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                return@Scaffold
            }

            Column(modifier = Modifier.fillMaxSize()) {

                // ── Hero Image ────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {

                    // Show user's uploaded photo, or fall back to bundled drawable
                    if (userItem.photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(userItem.photoUri),
                            contentDescription = itemName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(getItemImage(itemName)),
                            contentDescription = itemName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )

                    // Top bar: back button + MY LISTING badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("MY LISTING", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Item name + optional SOLD badge at bottom of hero
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        if (isMarkedSold) {
                            Surface(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    "SOLD OUT",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(
                            itemName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp
                            )
                        )
                    }
                } // end Hero Box

                // ── Info Card (slides up over the hero) ───────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                            .padding(bottom = innerPadding.calculateBottomPadding() + 24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {

                        // ── Active / Sold status banner ───────────────
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isMarkedSold)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else
                                Color(0xFF1B5E20).copy(alpha = 0.12f)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isMarkedSold) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    null,
                                    tint = if (isMarkedSold) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    if (isMarkedSold) "This listing is marked as sold / unavailable"
                                    else "Active listing — visible to the community",
                                    color = if (isMarkedSold) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Price row (Food) or Deposit row (Non-Food) ─
                        if (isFood) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Discounted Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "RM %.2f".format(discountedPrice),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        "Original: RM %.2f  •  ${userItem.discountPercent}% off".format(userItem.originalPrice),
                                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "Expires: ${userItem.expiresIn}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            val condColor = when (userItem.condition) {
                                "Excellent" -> Color(0xFF2E7D32)
                                "Good"      -> Color(0xFF1565C0)
                                else        -> Color(0xFFE65100)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Refundable Deposit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        if (userItem.deposit == 0.0) "FREE" else "RM %.2f".format(userItem.deposit),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (userItem.deposit == 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Surface(color = condColor.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
                                    Text(
                                        userItem.condition,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = condColor, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(20.dp))

                        // ── Listing details grid ──────────────────────
                        Text("Listing Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailRow(Icons.Default.LocationOn, "Location",  userItem.location)
                                DetailRow(Icons.Default.Info,       "Category",  userItem.category)
                                if (isFood) {
                                    DetailRow(Icons.Default.List,      "Quantity", "${userItem.quantity} units")
                                    DetailRow(Icons.Default.DateRange, "Expiry",   userItem.expiresIn)
                                } else {
                                    DetailRow(Icons.Default.DateRange,   "Max Borrow",      "${userItem.maxBorrowDays} days")
                                    DetailRow(Icons.Default.CheckCircle, "Available Until", userItem.availableUntil)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Description ───────────────────────────────
                        Text("Description", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            userItem.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(20.dp))

                        // ── Performance stats ─────────────────────────
                        Text("Listing Performance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(Modifier.weight(1f), Icons.Default.Favorite, "12", "Views",    MaterialTheme.colorScheme.primary)
                            StatCard(Modifier.weight(1f), Icons.Default.Email,    "3",  "Messages", MaterialTheme.colorScheme.tertiary)
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon     = if (isFood) Icons.Default.ShoppingCart else Icons.Default.Person,
                                // reservedCount comes from the live Room-backed StateFlow
                                value    = if (isFood) "$reservedCount"
                                else if (itemName in borrowedItems) "1" else "0",
                                label    = if (isFood) "Reserved" else "Borrowed",
                                color    = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // ── Manage actions ────────────────────────────
                        Text("Manage Listing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick  = { showDialog = MyListingDialog.SOLD },
                            enabled  = !isMarkedSold,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = if (isMarkedSold) Color.Gray else MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Icon(if (isMarkedSold) Icons.Default.Lock else Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isMarkedSold) "Already Marked as Sold" else "Mark as Sold / Unavailable",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick  = { showDialog = MyListingDialog.DELETE },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            border   = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Listing", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        } // end Scaffold

        // ── Delete confirmation dialog ─────────────────────────────────
        if (showDialog == MyListingDialog.DELETE) {
            ConfirmDialog(
                iconRes    = Icons.Default.Delete,
                iconBg     = MaterialTheme.colorScheme.errorContainer,
                iconTint   = MaterialTheme.colorScheme.error,
                title      = "Delete Listing?",
                body       = "\"$itemName\" will be permanently removed from the community listings.",
                confirmText  = "Delete",
                confirmColor = MaterialTheme.colorScheme.error,
                onDismiss  = { showDialog = MyListingDialog.NONE },
                onConfirm  = {
                    // deleteUserItem(UserListedItemEntity) — matches the @Delete DAO signature
                    // The ViewModel's deleteUserItem accepts a UserListedItem (domain model),
                    // which the repository maps to UserListedItemEntity before calling the DAO.
                    viewModel.deleteUserItem(userItem!!)
                    showDialog = MyListingDialog.NONE
                    onDeleted()
                }
            )
        }

        // ── Mark-as-sold confirmation dialog ──────────────────────────
        if (showDialog == MyListingDialog.SOLD) {
            ConfirmDialog(
                iconRes      = Icons.Default.CheckCircle,
                iconBg       = Color(0xFF1B5E20).copy(alpha = 0.15f),
                iconTint     = Color(0xFF2E7D32),
                title        = "Mark as Sold?",
                body         = "\"$itemName\" will be marked as sold / unavailable. Others won't be able to reserve it anymore.",
                confirmText  = "Confirm",
                confirmColor = Color(0xFF2E7D32),
                onDismiss    = { showDialog = MyListingDialog.NONE },
                onConfirm    = { isMarkedSold = true; showDialog = MyListingDialog.NONE }
            )
        }
    }
}

// ── Reusable modal dialog ──────────────────────────────────────────────────

@Composable
private fun ConfirmDialog(
    iconRes: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    body: String,
    confirmText: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = iconBg) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(iconRes, null, tint = iconTint, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                Text(body, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Cancel") }
                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = confirmColor)
                    ) { Text(confirmText, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ── Small row / card helpers ───────────────────────────────────────────────

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.width(110.dp))
        Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, color: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.10f)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}