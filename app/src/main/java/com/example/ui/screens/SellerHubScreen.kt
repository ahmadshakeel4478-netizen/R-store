package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.TrackingStatus
import com.example.ui.theme.*

@Composable
fun SellerHubScreen(
    sellerProducts: List<Product>,
    orders: List<Order>,
    onAddNewProduct: () -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateStock: (Product, Int) -> Unit,
    onAdvanceOrderStatus: (String, TrackingStatus) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Products (${sellerProducts.size})", "Orders & Dispatch (${orders.size})", "Earnings")

    val totalSales = orders.sumOf { it.totalAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Seller Banner
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rehan Seller Hub",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RehanGold
                            ) {
                                Text(
                                    text = "MERCHANT",
                                    color = RehanBlueDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Apna saman bechein aur orders dispatch karein",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = onAddNewProduct,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RehanGold,
                            contentColor = RehanBlueDark
                        ),
                        modifier = Modifier.testTag("add_product_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SellerStatCard("Total Sales", "₹${totalSales.toInt()}", Icons.Default.CurrencyRupee, Modifier.weight(1f))
                    SellerStatCard("Active Items", "${sellerProducts.size}", Icons.Default.Inventory, Modifier.weight(1f))
                    SellerStatCard("Total Orders", "${orders.size}", Icons.Default.LocalShipping, Modifier.weight(1f))
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> SellerProductsTab(
                products = sellerProducts,
                onAddNewProduct = onAddNewProduct,
                onDeleteProduct = onDeleteProduct,
                onUpdateStock = onUpdateStock
            )
            1 -> SellerOrdersTab(
                orders = orders,
                onAdvanceOrderStatus = onAdvanceOrderStatus
            )
            2 -> SellerEarningsTab(totalSales = totalSales, ordersCount = orders.size)
        }
    }
}

@Composable
fun SellerStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = RehanGoldLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Text(text = title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun SellerProductsTab(
    products: List<Product>,
    onAddNewProduct: () -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateStock: (Product, Int) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Inventory,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No items listed yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Add your first product to sell on Rehan Store!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAddNewProduct) {
                    Text("Add Product Now")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.category,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = product.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Selling: ₹${product.price.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MRP: ₹${product.originalPrice.toInt()}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "Current Stock: ${product.stockCount} units",
                                    fontSize = 11.sp,
                                    color = if (product.stockCount > 0) RehanGreenSuccess else RehanRedSale,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            IconButton(
                                onClick = { onDeleteProduct(product.id) },
                                modifier = Modifier.testTag("delete_seller_product_${product.id}")
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = RehanRedSale)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick stock adjustment buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Manage Stock:", fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { onUpdateStock(product, maxOf(0, product.stockCount - 5)) },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("-5", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { onUpdateStock(product, product.stockCount + 10) },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("+10 Stock", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerOrdersTab(
    orders: List<Order>,
    onAdvanceOrderStatus: (String, TrackingStatus) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No customer orders yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("When buyers purchase your items, orders will show up here to dispatch!", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders, key = { it.orderId }) { order ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order: ${order.orderId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = order.trackingStatus.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Customer: ${order.address.fullName} • ${order.address.phone}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Address: ${order.address.streetAddress}, ${order.address.city} - ${order.address.pincode}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Payment: ${order.paymentMethod} • ₹${order.totalAmount.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Advance Tracking Dispatch Controls
                        Text(
                            text = "Seller Tracking Control (Advance Order Movement):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            when (order.trackingStatus) {
                                TrackingStatus.ORDER_PLACED -> {
                                    Button(
                                        onClick = { onAdvanceOrderStatus(order.orderId, TrackingStatus.CONFIRMED) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("1. Confirm & Pack Order", fontSize = 12.sp)
                                    }
                                }
                                TrackingStatus.CONFIRMED -> {
                                    Button(
                                        onClick = { onAdvanceOrderStatus(order.orderId, TrackingStatus.SHIPPED) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("2. Dispatch / Mark Shipped", fontSize = 12.sp)
                                    }
                                }
                                TrackingStatus.SHIPPED -> {
                                    Button(
                                        onClick = { onAdvanceOrderStatus(order.orderId, TrackingStatus.OUT_FOR_DELIVERY) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RehanGold, contentColor = RehanBlueDark)
                                    ) {
                                        Text("3. Send Out for Delivery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                TrackingStatus.OUT_FOR_DELIVERY -> {
                                    Button(
                                        onClick = { onAdvanceOrderStatus(order.orderId, TrackingStatus.DELIVERED) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RehanGreenSuccess)
                                    ) {
                                        Text("4. Mark Delivered (OTP Verified)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                TrackingStatus.DELIVERED -> {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RehanGreenSuccess.copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Order Successfully Delivered & Settled",
                                            color = RehanGreenSuccess,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerEarningsTab(totalSales: Double, ordersCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settled Balance", fontSize = 12.sp, color = Color.Gray)
                Text("₹${totalSales.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = RehanGreenSuccess)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Transferred directly to registered bank account weekly on Tuesdays", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Commission Rate", fontSize = 12.sp)
                    Text("0% (Special Rehan Store Zero Fee)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RehanGreenSuccess)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Dispatched Orders", fontSize = 12.sp)
                    Text("$ordersCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
