package com.example.data.repository

import com.example.data.local.RehanStoreDatabase
import com.example.data.local.entity.CartEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.WishlistEntity
import com.example.data.model.CartItem
import com.example.data.model.DeliveryAddress
import com.example.data.model.Order
import com.example.data.model.OrderItemSummary
import com.example.data.model.Product
import com.example.data.model.TrackingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RehanStoreRepository(private val database: RehanStoreDatabase) {

    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val wishlistDao = database.wishlistDao()
    private val orderDao = database.orderDao()

    // Ensure database is populated if empty
    suspend fun checkAndSeedInitialData() {
        if (productDao.getProductCount() == 0) {
            RehanStoreDatabase.populateInitialProducts(productDao)
        }
    }

    val allProducts: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomain() }
    }

    val sellerProducts: Flow<List<Product>> = productDao.getSellerProducts().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    suspend fun deleteProduct(id: String) {
        productDao.deleteProduct(id)
    }

    // Wishlist
    val wishlistIds: Flow<Set<String>> = wishlistDao.getAllWishlist().map { entities ->
        entities.map { it.productId }.toSet()
    }

    suspend fun toggleWishlist(productId: String, currentlyInWishlist: Boolean) {
        if (currentlyInWishlist) {
            wishlistDao.removeFromWishlist(productId)
        } else {
            wishlistDao.addToWishlist(WishlistEntity(productId = productId))
        }
    }

    // Cart
    val cartItems: Flow<List<CartItem>> = combine(
        cartDao.getAllCartItems(),
        productDao.getAllProducts()
    ) { cartEntities, productEntities ->
        val productMap = productEntities.associateBy { it.id }
        cartEntities.mapNotNull { cartEntity ->
            val productEntity = productMap[cartEntity.productId]
            if (productEntity != null) {
                CartItem(
                    product = productEntity.toDomain(),
                    quantity = cartEntity.quantity
                )
            } else null
        }
    }

    suspend fun addToCart(productId: String, quantity: Int = 1) {
        cartDao.insertCartItem(CartEntity(productId = productId, quantity = quantity))
    }

    suspend fun updateCartQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.removeCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, newQuantity)
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.removeCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Orders
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getOrderById(orderId: String): Flow<Order?> {
        return orderDao.getOrderById(orderId).map { it?.toDomain() }
    }

    suspend fun placeOrder(
        items: List<CartItem>,
        totalAmount: Double,
        paymentMethod: String,
        address: DeliveryAddress
    ): String {
        val orderId = "RS-" + (10000..99999).random()
        val transactionId = "TXN_" + UUID.randomUUID().toString().take(12).uppercase()
        val orderItemsSummary = items.map {
            OrderItemSummary(
                productId = it.product.id,
                title = it.product.title,
                price = it.product.price,
                quantity = it.quantity,
                imageUrl = it.product.imageUrl
            )
        }

        val orderEntity = OrderEntity(
            orderId = orderId,
            createdAt = System.currentTimeMillis(),
            itemsJson = serializeOrderItems(orderItemsSummary),
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == "Cash on Delivery (COD)") "PENDING_COD" else "PAID",
            transactionId = transactionId,
            addressJson = serializeAddress(address),
            trackingStatus = TrackingStatus.ORDER_PLACED.name,
            deliveryAgentName = "Vikram Singh",
            deliveryAgentPhone = "+91 98112 34567",
            deliveryOtp = (1000..9999).random().toString(),
            expectedDelivery = "Tomorrow, by 8:00 PM"
        )

        orderDao.insertOrder(orderEntity)
        cartDao.clearCart()
        return orderId
    }

    suspend fun updateOrderStatus(orderId: String, status: TrackingStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
    }

    suspend fun cancelOrder(orderId: String) {
        orderDao.cancelOrder(orderId)
    }

    // Serialization helpers using standard Android org.json
    private fun serializeOrderItems(items: List<OrderItemSummary>): String {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("productId", item.productId)
                put("title", item.title)
                put("price", item.price)
                put("quantity", item.quantity)
                put("imageUrl", item.imageUrl)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun deserializeOrderItems(json: String): List<OrderItemSummary> {
        val list = mutableListOf<OrderItemSummary>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    OrderItemSummary(
                        productId = obj.optString("productId"),
                        title = obj.optString("title"),
                        price = obj.optDouble("price", 0.0),
                        quantity = obj.optInt("quantity", 1),
                        imageUrl = obj.optString("imageUrl")
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    private fun serializeAddress(address: DeliveryAddress): String {
        return JSONObject().apply {
            put("fullName", address.fullName)
            put("phone", address.phone)
            put("pincode", address.pincode)
            put("streetAddress", address.streetAddress)
            put("city", address.city)
            put("state", address.state)
            put("type", address.type)
        }.toString()
    }

    private fun deserializeAddress(json: String): DeliveryAddress {
        return try {
            val obj = JSONObject(json)
            DeliveryAddress(
                fullName = obj.optString("fullName", "Mohd Rehan"),
                phone = obj.optString("phone", "9876543210"),
                pincode = obj.optString("pincode", "110001"),
                streetAddress = obj.optString("streetAddress", "Main Market"),
                city = obj.optString("city", "New Delhi"),
                state = obj.optString("state", "Delhi"),
                type = obj.optString("type", "Home")
            )
        } catch (_: Exception) {
            DeliveryAddress()
        }
    }

    private fun ProductEntity.toDomain() = Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        originalPrice = originalPrice,
        rating = rating,
        reviewCount = reviewCount,
        imageUrl = imageUrl,
        badge = badge,
        sellerName = sellerName,
        inStock = inStock,
        stockCount = stockCount,
        isSellerItem = isSellerItem,
        brand = brand,
        specs = specs
    )

    private fun Product.toEntity() = ProductEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        originalPrice = originalPrice,
        rating = rating,
        reviewCount = reviewCount,
        imageUrl = imageUrl,
        badge = badge,
        sellerName = sellerName,
        inStock = inStock,
        stockCount = stockCount,
        isSellerItem = isSellerItem,
        brand = brand,
        specs = specs
    )

    private fun OrderEntity.toDomain(): Order {
        val tracking = try {
            TrackingStatus.valueOf(trackingStatus)
        } catch (_: Exception) {
            TrackingStatus.ORDER_PLACED
        }
        return Order(
            orderId = orderId,
            createdAt = createdAt,
            items = deserializeOrderItems(itemsJson),
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            transactionId = transactionId,
            address = deserializeAddress(addressJson),
            trackingStatus = tracking,
            deliveryAgentName = deliveryAgentName,
            deliveryAgentPhone = deliveryAgentPhone,
            deliveryOtp = deliveryOtp,
            expectedDelivery = expectedDelivery
        )
    }
}
