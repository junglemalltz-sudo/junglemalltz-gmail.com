package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class JungleMallRepository(private val dao: JungleMallDao) {
    val cartItems: Flow<List<CartItem>> = dao.getCartItems()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()

    suspend fun addToCart(product: Product, qty: Int = 1) {
        val currentItems = dao.getCartItems().firstOrNull() ?: emptyList()
        val existing = currentItems.find { it.productId == product.id }
        if (existing != null) {
            dao.updateCartItem(existing.copy(quantity = existing.quantity + qty))
        } else {
            dao.insertCartItem(
                CartItem(
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    quantity = qty,
                    iconName = product.iconName,
                    category = product.category
                )
            )
        }
    }

    suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            val currentItems = dao.getCartItems().firstOrNull() ?: emptyList()
            val existing = currentItems.find { it.productId == productId }
            if (existing != null) {
                dao.deleteCartItem(existing)
            }
        } else {
            val currentItems = dao.getCartItems().firstOrNull() ?: emptyList()
            val existing = currentItems.find { it.productId == productId }
            if (existing != null) {
                dao.updateCartItem(existing.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeFromCart(productId: String) {
        val currentItems = dao.getCartItems().firstOrNull() ?: emptyList()
        val existing = currentItems.find { it.productId == productId }
        if (existing != null) {
            dao.deleteCartItem(existing)
        }
    }

    suspend fun checkout(customerName: String, customerPhone: String, paymentMethod: String): Order? {
        val currentItems = dao.getCartItems().firstOrNull() ?: emptyList()
        if (currentItems.isEmpty()) return null

        val totalAmount = currentItems.sumOf { it.price * it.quantity }
        val summary = currentItems.joinToString(", ") { "${it.productName} (x${it.quantity})" }

        val order = Order(
            orderTimestamp = System.currentTimeMillis(),
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            status = "Processing",
            itemsSummary = summary,
            customerName = customerName,
            customerPhone = customerPhone
        )

        dao.insertOrder(order)
        dao.clearCart()
        return order
    }

    suspend fun clearCart() {
        dao.clearCart()
    }
}
