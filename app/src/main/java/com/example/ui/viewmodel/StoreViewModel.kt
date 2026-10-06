package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RehanStoreDatabase
import com.example.data.model.CartItem
import com.example.data.model.DeliveryAddress
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.TrackingStatus
import com.example.data.repository.RehanStoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    CATEGORIES,
    PRODUCT_DETAIL,
    CART,
    CHECKOUT,
    ORDER_SUCCESS,
    MY_ORDERS,
    ORDER_TRACKING,
    SELLER_HUB,
    ADD_PRODUCT,
    SETTINGS
}

enum class SortOption(val title: String) {
    RECOMMENDED("Popularity"),
    PRICE_LOW_HIGH("Price -- Low to High"),
    PRICE_HIGH_LOW("Price -- High to Low"),
    RATING("Customer Rating"),
    DISCOUNT("Discount %")
}

data class CheckoutUiState(
    val address: DeliveryAddress = DeliveryAddress(),
    val paymentMethod: String = "UPI (Google Pay / PhonePe)",
    val upiId: String = "rehan@okhdfcbank",
    val selectedUpiApp: String = "Google Pay",
    val cardNumber: String = "4532 8923 1290 8421",
    val cardExpiry: String = "08/29",
    val cardCvv: String = "742",
    val cardHolder: String = "MOHD REHAN",
    val selectedBank: String = "HDFC Bank",
    val codCaptchaInput: String = "",
    val codGeneratedCaptcha: String = "8392",
    val isProcessingPayment: Boolean = false,
    val paymentError: String? = null,
    val isOtpPromptVisible: Boolean = false,
    val otpInput: String = "",
    val lastPlacedOrderId: String? = null
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RehanStoreRepository

    init {
        val database = RehanStoreDatabase.getDatabase(application, viewModelScope)
        repository = RehanStoreRepository(database)
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val previous = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        return false
    }

    // Selected product & order
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        navigateTo(Screen.PRODUCT_DETAIL)
    }

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    fun selectOrderForTracking(order: Order) {
        _selectedOrder.value = order
        navigateTo(Screen.ORDER_TRACKING)
    }

    // Search and filtering
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedSort = MutableStateFlow(SortOption.RECOMMENDED)

    // Applied coupon
    val appliedCoupon = MutableStateFlow<String?>(null)
    val couponDiscount = MutableStateFlow(0.0)

    fun applyCoupon(code: String): Boolean {
        val clean = code.trim().uppercase()
        return if (clean == "REHAN100") {
            appliedCoupon.value = "REHAN100"
            couponDiscount.value = 100.0
            true
        } else if (clean == "SUPER50") {
            appliedCoupon.value = "SUPER50"
            couponDiscount.value = 50.0
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        appliedCoupon.value = null
        couponDiscount.value = 0.0
    }

    // Products Flow
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        searchQuery,
        selectedCategory,
        selectedSort
    ) { products, query, category, sort ->
        var list = products

        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.brand.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOption.RECOMMENDED -> list
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.DISCOUNT -> list.sortedByDescending { it.discountPercent }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wishlist
    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val currentlyIn = wishlistIds.value.contains(product.id)
            repository.toggleWishlist(product.id, currentlyIn)
        }
    }

    // Cart
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotalCount: StateFlow<Int> = cartItems.combine(appliedCoupon) { items, _ ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = cartItems.combine(appliedCoupon) { items, _ ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartOriginalTotal: StateFlow<Double> = cartItems.combine(appliedCoupon) { items, _ ->
        items.sumOf { it.product.originalPrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.product.id == product.id }
            val newQty = (existing?.quantity ?: 0) + quantity
            repository.addToCart(product.id, newQty)
        }
    }

    fun updateCartQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, newQuantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Orders
    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seller Hub Products
    val sellerProducts: StateFlow<List<Product>> = repository.allProducts.combine(appliedCoupon) { list, _ ->
        list.filter { it.isSellerItem }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Checkout UI State
    val checkoutState = MutableStateFlow(CheckoutUiState())

    fun updateAddress(address: DeliveryAddress) {
        checkoutState.value = checkoutState.value.copy(address = address)
    }

    fun updatePaymentMethod(method: String) {
        checkoutState.value = checkoutState.value.copy(
            paymentMethod = method,
            paymentError = null
        )
    }

    fun updateUpiApp(app: String) {
        checkoutState.value = checkoutState.value.copy(selectedUpiApp = app)
    }

    fun updateUpiId(id: String) {
        checkoutState.value = checkoutState.value.copy(upiId = id)
    }

    fun updateCardDetails(number: String, expiry: String, cvv: String, holder: String) {
        checkoutState.value = checkoutState.value.copy(
            cardNumber = number,
            cardExpiry = expiry,
            cardCvv = cvv,
            cardHolder = holder
        )
    }

    fun updateBank(bank: String) {
        checkoutState.value = checkoutState.value.copy(selectedBank = bank)
    }

    fun updateCodCaptcha(input: String) {
        checkoutState.value = checkoutState.value.copy(codCaptchaInput = input)
    }

    fun refreshCodCaptcha() {
        val code = (1000..9999).random().toString()
        checkoutState.value = checkoutState.value.copy(codGeneratedCaptcha = code, codCaptchaInput = "")
    }

    fun processPaymentAndOrder(onSuccess: (String) -> Unit) {
        val state = checkoutState.value
        val items = cartItems.value
        if (items.isEmpty()) return

        val total = maxOf(0.0, cartSubtotal.value - couponDiscount.value)

        // Validation based on payment method
        if (state.paymentMethod.startsWith("Cash on Delivery")) {
            if (state.codCaptchaInput.trim() != state.codGeneratedCaptcha) {
                checkoutState.value = state.copy(paymentError = "Incorrect confirmation code! Please enter the 4-digit code shown.")
                return
            }
        }

        checkoutState.value = state.copy(isProcessingPayment = true, paymentError = null)

        viewModelScope.launch {
            // Realistic simulated secure gateway processing (1.5 seconds)
            delay(1500)

            val orderId = repository.placeOrder(
                items = items,
                totalAmount = total,
                paymentMethod = state.paymentMethod,
                address = state.address
            )

            checkoutState.value = state.copy(
                isProcessingPayment = false,
                lastPlacedOrderId = orderId
            )

            // Select latest order for view
            val placedOrder = repository.allProducts.combine(orders) { _, ords ->
                ords.find { it.orderId == orderId }
            }

            removeCoupon()
            onSuccess(orderId)
        }
    }

    // Seller Hub Actions
    fun addNewProductBySeller(
        title: String,
        category: String,
        price: Double,
        originalPrice: Double,
        stockCount: Int,
        description: String,
        imageUrl: String,
        sellerName: String
    ) {
        viewModelScope.launch {
            val newId = "REHAN_SELLER_" + System.currentTimeMillis().toString().takeLast(6)
            val product = Product(
                id = newId,
                title = title,
                description = description,
                category = category,
                price = price,
                originalPrice = if (originalPrice > price) originalPrice else price * 1.25,
                rating = 5.0,
                reviewCount = 1,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80" },
                badge = "Seller Exclusive",
                sellerName = sellerName.ifBlank { "Rehan Store Official" },
                inStock = true,
                stockCount = stockCount,
                isSellerItem = true,
                brand = "Rehan Store",
                specs = "100% Authentic Seller Product | Dispatched in 24 Hours | Verified Quality"
            )
            repository.addProduct(product)
            navigateTo(Screen.SELLER_HUB)
        }
    }

    fun updateProductStock(product: Product, newStock: Int) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(stockCount = newStock, inStock = newStock > 0))
        }
    }

    fun deleteSellerProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    // Advance Tracking Status (from Seller Hub or Admin Demo)
    fun advanceOrderTrackingStatus(orderId: String, nextStatus: TrackingStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, nextStatus)
            // also update selectedOrder if matching
            val cur = _selectedOrder.value
            if (cur != null && cur.orderId == orderId) {
                _selectedOrder.value = cur.copy(trackingStatus = nextStatus)
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
            if (_selectedOrder.value?.orderId == orderId) {
                navigateBack()
            }
        }
    }
}
