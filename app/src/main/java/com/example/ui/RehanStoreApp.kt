package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.RehanBottomBar
import com.example.ui.components.RehanTopHeader
import com.example.ui.screens.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun RehanStoreApp(
    viewModel: StoreViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistIds.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartTotalCount.collectAsStateWithLifecycle()
    val subtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val originalTotal by viewModel.cartOriginalTotal.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val sellerProducts by viewModel.sellerProducts.collectAsStateWithLifecycle()
    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val couponDiscount by viewModel.couponDiscount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()

    // Handle Hardware/Gesture Back Press
    BackHandler(enabled = currentScreen != Screen.HOME) {
        val popped = viewModel.navigateBack()
        if (!popped) {
            viewModel.navigateTo(Screen.HOME)
        }
    }

    Scaffold(
        topBar = {
            RehanTopHeader(
                currentScreen = currentScreen,
                cartCount = cartCount,
                pincode = checkoutState.address.pincode,
                city = checkoutState.address.city,
                onNavigate = { viewModel.navigateTo(it) },
                onBackClick = {
                    val popped = viewModel.navigateBack()
                    if (!popped) viewModel.navigateTo(Screen.HOME)
                }
            )
        },
        bottomBar = {
            // Hide bottom bar on Checkout, Product Detail, and Add Product for full immersion
            if (currentScreen != Screen.CHECKOUT &&
                currentScreen != Screen.PRODUCT_DETAIL &&
                currentScreen != Screen.ADD_PRODUCT &&
                currentScreen != Screen.ORDER_SUCCESS
            ) {
                RehanBottomBar(
                    currentScreen = currentScreen,
                    cartCount = cartCount,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        products = allProducts,
                        wishlistIds = wishlistIds,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedSort = selectedSort,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        onCategorySelect = { viewModel.selectedCategory.value = it },
                        onSortSelect = { viewModel.selectedSort.value = it },
                        onProductClick = { viewModel.selectProduct(it) },
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it) },
                        onOpenSellerHub = { viewModel.navigateTo(Screen.SELLER_HUB) },
                        onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                    )
                }

                Screen.CATEGORIES -> {
                    CategoriesScreen(
                        onCategoryClick = { category ->
                            viewModel.selectedCategory.value = category
                            viewModel.navigateTo(Screen.HOME)
                        }
                    )
                }

                Screen.PRODUCT_DETAIL -> {
                    selectedProduct?.let { product ->
                        ProductDetailScreen(
                            product = product,
                            isInWishlist = wishlistIds.contains(product.id),
                            onAddToCart = { viewModel.addToCart(it) },
                            onBuyNow = {
                                viewModel.addToCart(it)
                                viewModel.navigateTo(Screen.CHECKOUT)
                            },
                            onToggleWishlist = { viewModel.toggleWishlist(it) }
                        )
                    } ?: run {
                        viewModel.navigateTo(Screen.HOME)
                    }
                }

                Screen.CART -> {
                    CartScreen(
                        cartItems = cartItems,
                        subtotal = subtotal,
                        originalTotal = originalTotal,
                        address = checkoutState.address,
                        appliedCoupon = appliedCoupon,
                        couponDiscount = couponDiscount,
                        onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                        onRemoveItem = { id -> viewModel.removeFromCart(id) },
                        onApplyCoupon = { code -> viewModel.applyCoupon(code) },
                        onRemoveCoupon = { viewModel.removeCoupon() },
                        onCheckout = { viewModel.navigateTo(Screen.CHECKOUT) },
                        onStartShopping = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.CHECKOUT -> {
                    CheckoutScreen(
                        checkoutState = checkoutState,
                        cartItems = cartItems,
                        subtotal = subtotal,
                        couponDiscount = couponDiscount,
                        onUpdateAddress = { viewModel.updateAddress(it) },
                        onUpdatePaymentMethod = { viewModel.updatePaymentMethod(it) },
                        onUpdateUpiApp = { viewModel.updateUpiApp(it) },
                        onUpdateUpiId = { viewModel.updateUpiId(it) },
                        onUpdateCardDetails = { num, exp, cvv, holder -> viewModel.updateCardDetails(num, exp, cvv, holder) },
                        onUpdateBank = { viewModel.updateBank(it) },
                        onUpdateCodCaptcha = { viewModel.updateCodCaptcha(it) },
                        onRefreshCodCaptcha = { viewModel.refreshCodCaptcha() },
                        onProcessOrder = {
                            viewModel.processPaymentAndOrder { orderId ->
                                viewModel.navigateTo(Screen.ORDER_SUCCESS)
                            }
                        }
                    )
                }

                Screen.ORDER_SUCCESS -> {
                    OrderSuccessScreen(
                        orderId = checkoutState.lastPlacedOrderId ?: "RS-99214",
                        onTrackOrder = { orderId ->
                            val matched = orders.find { it.orderId == orderId }
                            if (matched != null) {
                                viewModel.selectOrderForTracking(matched)
                            } else {
                                viewModel.navigateTo(Screen.MY_ORDERS)
                            }
                        },
                        onContinueShopping = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.MY_ORDERS -> {
                    MyOrdersScreen(
                        orders = orders,
                        onSelectOrder = { viewModel.selectOrderForTracking(it) },
                        onStartShopping = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.ORDER_TRACKING -> {
                    selectedOrder?.let { order ->
                        LiveOrderTrackingScreen(
                            order = order,
                            onCancelOrder = { orderId ->
                                viewModel.cancelOrder(orderId)
                            }
                        )
                    } ?: run {
                        viewModel.navigateTo(Screen.MY_ORDERS)
                    }
                }

                Screen.SELLER_HUB -> {
                    SellerHubScreen(
                        sellerProducts = sellerProducts,
                        orders = orders,
                        onAddNewProduct = { viewModel.navigateTo(Screen.ADD_PRODUCT) },
                        onDeleteProduct = { viewModel.deleteSellerProduct(it) },
                        onUpdateStock = { prod, stock -> viewModel.updateProductStock(prod, stock) },
                        onAdvanceOrderStatus = { orderId, status ->
                            viewModel.advanceOrderTrackingStatus(orderId, status)
                        }
                    )
                }

                Screen.ADD_PRODUCT -> {
                    AddProductScreen(
                        onPublishProduct = { title, cat, price, mrp, stock, desc, img, seller ->
                            viewModel.addNewProductBySeller(
                                title = title,
                                category = cat,
                                price = price,
                                originalPrice = mrp,
                                stockCount = stock,
                                description = desc,
                                imageUrl = img,
                                sellerName = seller
                            )
                        }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        onOpenSellerHub = { viewModel.navigateTo(Screen.SELLER_HUB) },
                        onOpenOrders = { viewModel.navigateTo(Screen.MY_ORDERS) },
                        onOpenCart = { viewModel.navigateTo(Screen.CART) }
                    )
                }
            }
        }
    }
}
