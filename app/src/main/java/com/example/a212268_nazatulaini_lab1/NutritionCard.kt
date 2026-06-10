// app/src/main/java/com/example/a212268_nazatulaini_lab1/NutritionCard.kt
// PURPOSE: Satisfies "Data from the Internet (Web API)" requirement.
// Drop this composable anywhere inside FoodDetailScreen to show live nutrition data.
// It self-loads — just pass the food item name.

package com.example.a212268_nazatulaini_lab1

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212268_nazatulaini_lab1.api.NutritionInfo
import com.example.a212268_nazatulaini_lab1.api.OpenFoodFactsClient
import kotlinx.coroutines.launch

// ── State sealed class ────────────────────────────────────────────────────

private sealed class NutritionState {
    object Loading : NutritionState()
    data class Success(val data: NutritionInfo) : NutritionState()
    object NotFound : NutritionState()
    object Error : NutritionState()
}

// ── Main composable ────────────────────────────────────────────────────────

@Composable
fun NutritionCard(itemName: String) {
    var state by remember { mutableStateOf<NutritionState>(NutritionState.Loading) }
    val scope = rememberCoroutineScope()

    // Fetch once when the item name is first seen
    LaunchedEffect(itemName) {
        state = NutritionState.Loading
        scope.launch {
            val result = OpenFoodFactsClient.getNutritionInfo(itemName)
            state = when {
                result == null                    -> NutritionState.Error
                result.caloriesPer100g == null &&
                        result.allergens.isEmpty()        -> NutritionState.NotFound
                else                              -> NutritionState.Success(result)
            }
        }
    }

    AnimatedVisibility(
        visible = state !is NutritionState.NotFound,
        enter = fadeIn() + expandVertically(),
        exit  = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Nutrition Info",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "via Open Food Facts",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))

                when (val s = state) {
                    is NutritionState.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Fetching nutrition data...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is NutritionState.Success -> {
                        val d = s.data

                        // Nutriscore badge
                        if (d.nutriscoreGrade != null) {
                            val badgeColor = when (d.nutriscoreGrade) {
                                "A" -> Color(0xFF2E7D32)
                                "B" -> Color(0xFF558B2F)
                                "C" -> Color(0xFFF9A825)
                                "D" -> Color(0xFFE65100)
                                else -> Color(0xFFB71C1C)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "Nutri-Score  ${d.nutriscoreGrade}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    color = badgeColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // Macros grid
                        if (d.caloriesPer100g != null || d.proteinPer100g != null) {
                            Text(
                                "Per 100g",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (d.caloriesPer100g != null)
                                    MacroChip(
                                        Modifier.weight(1f),
                                        "%.0f kcal".format(d.caloriesPer100g),
                                        "Energy"
                                    )
                                if (d.proteinPer100g != null)
                                    MacroChip(
                                        Modifier.weight(1f),
                                        "%.1fg".format(d.proteinPer100g),
                                        "Protein"
                                    )
                                if (d.carbsPer100g != null)
                                    MacroChip(
                                        Modifier.weight(1f),
                                        "%.1fg".format(d.carbsPer100g),
                                        "Carbs"
                                    )
                                if (d.fatPer100g != null)
                                    MacroChip(
                                        Modifier.weight(1f),
                                        "%.1fg".format(d.fatPer100g),
                                        "Fat"
                                    )
                            }
                        }

                        // Allergens
                        if (d.allergens.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Allergens",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                d.allergens.take(4).forEach { allergen ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            allergen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Labels
                        if (d.labels.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                d.labels.take(3).forEach { label ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1B5E20).copy(alpha = 0.10f)
                                    ) {
                                        Text(
                                            label,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = Color(0xFF2E7D32),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is NutritionState.Error -> {
                        Text(
                            "Could not load nutrition data.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is NutritionState.NotFound -> { /* hidden by AnimatedVisibility */ }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(modifier: Modifier, value: String, label: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}