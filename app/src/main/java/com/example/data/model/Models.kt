package com.example.data.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val originalPrice: Double,
    val rating: Double = 4.5,
    val reviewCount: Int = 128,
    val imageUrl: String = "",
    val badge: String? = null,
    val sellerName: String = "Rehan Store Official",
    val inStock: Boolean = true,
    val stockCount: Int = 50,
    val isSellerItem: Boolean = false,
    val brand: String = "Rehan Brand",
    val specs: String = "High Quality, 100% Genuine, 7 Days Replacement"
) {
    val discountPercent: Int
        get() = if (originalPrice > price) {
            (((originalPrice - price) / originalPrice) * 100).toInt()
        } else 0
}

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class DeliveryAddress(
    val fullName: String = "Mohd Rehan",
    val phone: String = "9876543210",
    val pincode: String = "110001",
    val streetAddress: String = "Flat 402, Royal Enclave, Main Market Road",
    val city: String = "New Delhi",
    val state: String = "Delhi",
    val type: String = "Home"
)

enum class TrackingStatus(val label: String, val stepIndex: Int, val description: String) {
    ORDER_PLACED("Order Placed", 0, "Your order has been received by Rehan Store"),
    CONFIRMED("Confirmed", 1, "Seller confirmed your order and is packing it"),
    SHIPPED("Shipped", 2, "Package dispatched via Rehan Express Logistics"),
    OUT_FOR_DELIVERY("Out for Delivery", 3, "Delivery executive is arriving at your doorstep"),
    DELIVERED("Delivered", 4, "Package delivered successfully")
}

data class TrackingMilestone(
    val status: TrackingStatus,
    val timestamp: String,
    val location: String,
    val isCompleted: Boolean
)

data class OrderItemSummary(
    val productId: String,
    val title: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

data class Order(
    val orderId: String,
    val createdAt: Long,
    val items: List<OrderItemSummary>,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val transactionId: String,
    val address: DeliveryAddress,
    val trackingStatus: TrackingStatus,
    val deliveryAgentName: String = "Vikram Singh",
    val deliveryAgentPhone: String = "+91 98112 34567",
    val deliveryOtp: String = "4829",
    val expectedDelivery: String = "Tomorrow, by 8:00 PM"
)
