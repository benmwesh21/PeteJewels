package com.example.pete.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val phoneNumber: String,
    val amount: String,
    val status: String, // PENDING, SUCCESS, FAILED
    val timestamp: Long,
    val itemsJson: String // Using JSON to store the list of items for simplicity without requiring TypeConverters
)
