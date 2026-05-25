package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val iconName: String,
    val category: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderTimestamp: Long,
    val totalAmount: Double,
    val paymentMethod: String,
    val status: String, // "Processing", "Dispatched", "Delivered"
    val itemsSummary: String,
    val customerName: String,
    val customerPhone: String
)
