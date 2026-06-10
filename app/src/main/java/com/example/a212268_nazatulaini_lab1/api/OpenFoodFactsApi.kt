
package com.example.a212268_nazatulaini_lab1.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ── Data classes (subset of the API response we care about) ───────────────

data class FoodSearchResponse(
    val products: List<FoodProduct>?,
    val count: Int?
)

data class FoodProduct(
    val id: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("nutriments") val nutriments: Nutriments?,
    @SerializedName("allergens_tags") val allergensTags: List<String>?,
    @SerializedName("labels_tags") val labelsTags: List<String>?,
    @SerializedName("nutriscore_grade") val nutriscoreGrade: String?
)

data class Nutriments(
    @SerializedName("energy-kcal_100g") val caloriesPer100g: Double?,
    @SerializedName("proteins_100g") val proteinPer100g: Double?,
    @SerializedName("carbohydrates_100g") val carbsPer100g: Double?,
    @SerializedName("fat_100g") val fatPer100g: Double?
)

// A clean display model — what the UI actually binds to
data class NutritionInfo(
    val productName: String,
    val caloriesPer100g: Double?,
    val proteinPer100g: Double?,
    val carbsPer100g: Double?,
    val fatPer100g: Double?,
    val allergens: List<String>,   // e.g. ["Gluten", "Nuts"]
    val labels: List<String>,      // e.g. ["Organic", "Vegan"]
    val nutriscoreGrade: String?   // A–E or null
)

// ── Retrofit interface ─────────────────────────────────────────────────────

interface OpenFoodFactsService {

    // Search by product name, 1 result is enough for enrichment
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms")   searchTerms: String,
        @Query("search_simple")  searchSimple: Int = 1,
        @Query("action")         action: String = "process",
        @Query("json")           json: Int = 1,
        @Query("page_size")      pageSize: Int = 1,
        @Query("fields")         fields: String =
            "product_name,nutriments,allergens_tags,labels_tags,nutriscore_grade"
    ): FoodSearchResponse
}

// ── Singleton Retrofit instance ────────────────────────────────────────────

object OpenFoodFactsClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    val service: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    // Helper: fetch and map to NutritionInfo; returns null on any failure
    suspend fun getNutritionInfo(itemName: String): NutritionInfo? {
        return try {
            val response = service.searchProducts(itemName)
            val product = response.products?.firstOrNull() ?: return null

            NutritionInfo(
                productName    = product.productName ?: itemName,
                caloriesPer100g = product.nutriments?.caloriesPer100g,
                proteinPer100g  = product.nutriments?.proteinPer100g,
                carbsPer100g    = product.nutriments?.carbsPer100g,
                fatPer100g      = product.nutriments?.fatPer100g,
                allergens      = product.allergensTags
                    ?.map { it.removePrefix("en:").replace("-", " ").capitalizeWords() }
                    ?: emptyList(),
                labels         = product.labelsTags
                    ?.map { it.removePrefix("en:").replace("-", " ").capitalizeWords() }
                    ?.take(4)  // cap to keep UI tidy
                    ?: emptyList(),
                nutriscoreGrade = product.nutriscoreGrade?.uppercase()
            )
        } catch (e: Exception) {
            null   // gracefully degrade — no crash, just no card
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
}