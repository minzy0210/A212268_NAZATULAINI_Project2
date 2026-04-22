package com.example.a212268_nazatulaini_lab1

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReServeViewModel : ViewModel() {

    private val _items = MutableStateFlow(
        listOf(
            Item("Apple", "Food"),
            Item("Bread", "Food"),
            Item("Milk", "Food"),
            Item("Cake", "Food"),
            Item("Banana", "Food"),
            Item("Pizza", "Food"),
            Item("Guitar", "Non-food"),
            Item("Trampoline", "Non-food"),
            Item("Plant Pot", "Non-food"),
            Item("Chair", "Non-food"),
            Item("Table", "Non-food"),
            Item("Books", "Non-food")
        )
    )
    val items: StateFlow<List<Item>> = _items

    private val _cartItems = MutableStateFlow<List<String>>(emptyList())
    val cartItems: StateFlow<List<String>> = _cartItems.asStateFlow()

    fun addToCart(itemName: String) {
        _cartItems.update { currentCart ->
            if (!currentCart.contains(itemName)) {
                currentCart + itemName
            } else {
                currentCart
            }
        }
    }

    fun getFoodItems() = _items.value.filter { it.category == "Food" }
    fun getNonFoodItems() = _items.value.filter { it.category == "Non-food" }
    fun getGoingSoon() = _items.value.filter { it.name == "Bread" || it.name == "Milk" }

    fun searchItems(query: String): List<Item> {
        return if (query.isBlank()) emptyList()
        else _items.value.filter { it.name.contains(query, ignoreCase = true) }
    }
}