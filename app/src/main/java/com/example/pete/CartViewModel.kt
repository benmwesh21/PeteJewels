package com.example.pete

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<JewelryItem>>(emptyList())
    val cartItems: StateFlow<List<JewelryItem>> = _cartItems.asStateFlow()

    private val _wishlistItems = MutableStateFlow<List<JewelryItem>>(emptyList())
    val wishlistItems: StateFlow<List<JewelryItem>> = _wishlistItems.asStateFlow()

    fun addToCart(item: JewelryItem) {
        _cartItems.value = _cartItems.value + item
    }

    fun removeFromCart(item: JewelryItem) {
        _cartItems.value = _cartItems.value - item
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun toggleWishlist(item: JewelryItem) {
        if (_wishlistItems.value.contains(item)) {
            _wishlistItems.value = _wishlistItems.value - item
        } else {
            _wishlistItems.value = _wishlistItems.value + item
        }
    }

    fun removeFromWishlist(item: JewelryItem) {
        _wishlistItems.value = _wishlistItems.value - item
    }
}
