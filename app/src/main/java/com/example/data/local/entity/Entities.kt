package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val originalPrice: Double,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    val badge: String?,
    val sellerName: String,
    val inStock: Boolean,
    val stockCount: Int,
    val isSellerItem: Boolean,
    val brand: String,
    val specs: String
)

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val createdAt: Long,
    val itemsJson: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val transactionId: String,
    val addressJson: String,
    val trackingStatus: String,
    val deliveryAgentName: String,
    val deliveryAgentPhone: String,
    val deliveryOtp: String,
    val expectedDelivery: String
)
