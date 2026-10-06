package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.DeliveryAddress
import com.example.ui.theme.*
import com.example.ui.viewmodel.CheckoutUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    checkoutState: CheckoutUiState,
    cartItems: List<CartItem>,
    subtotal: Double,
    couponDiscount: Double,
    onUpdateAddress: (DeliveryAddress) -> Unit,
    onUpdatePaymentMethod: (String) -> Unit,
    onUpdateUpiApp: (String) -> Unit,
    onUpdateUpiId: (String) -> Unit,
    onUpdateCardDetails: (String, String, String, String) -> Unit,
    onUpdateBank: (String) -> Unit,
    onUpdateCodCaptcha: (String) -> Unit,
    onRefreshCodCaptcha: () -> Unit,
    onProcessOrder: () -> Unit
) {
    var showEditAddressDialog by remember { mutableStateOf(false) }

    val totalPayable = maxOf(0.0, subtotal - couponDiscount)

    val paymentOptions = listOf(
        "UPI (Google Pay / PhonePe / Paytm)",
        "Credit / Debit Card",
        "Net Banking",
        "Cash on Delivery (COD)"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 90.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Steps indicator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckoutStep(1, "Address", isDone = true)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = RehanGreenSuccess)
                    CheckoutStep(2, "Summary", isDone = true)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.primary)
                    CheckoutStep(3, "Payment", isActive = true)
                }
            }

            // Delivery Address Block
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. DELIVERY ADDRESS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            TextButton(
                                onClick = { showEditAddressDialog = true },
                                modifier = Modifier.testTag("edit_address_button")
                            ) {
                                Text("Change", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = checkoutState.address.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE2E8F0))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(checkoutState.address.type, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${checkoutState.address.streetAddress}, ${checkoutState.address.city}, ${checkoutState.address.state} - ${checkoutState.address.pincode}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Phone: +91 ${checkoutState.address.phone}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Order Summary Preview
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. ORDER SUMMARY (${cartItems.size} items)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        cartItems.take(2).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.product.title}",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "₹${(item.product.price * item.quantity).toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        if (cartItems.size > 2) {
                            Text(
                                text = "+ ${cartItems.size - 2} more items",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Secure Payment Options
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = RehanGreenSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3. SECURE PAYMENT OPTIONS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        paymentOptions.forEach { method ->
                            val isSelected = checkoutState.paymentMethod.startsWith(method.take(10))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                         else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable { onUpdatePaymentMethod(method) }
                                    .testTag("payment_option_${method.take(5)}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onUpdatePaymentMethod(method) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = method,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                            if (method.startsWith("UPI")) {
                                                Text("Pay via Google Pay, PhonePe, Paytm, or UPI ID", fontSize = 11.sp, color = Color.Gray)
                                            } else if (method.startsWith("Credit")) {
                                                Text("Visa, MasterCard, RuPay with Instant OTP", fontSize = 11.sp, color = Color.Gray)
                                            } else if (method.startsWith("Cash on Delivery")) {
                                                Text("Pay cash/card when delivery reaches doorstep", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    // Sub-options if selected
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        when {
                                            method.startsWith("UPI") -> {
                                                UpiPaymentSection(
                                                    selectedApp = checkoutState.selectedUpiApp,
                                                    upiId = checkoutState.upiId,
                                                    onSelectApp = onUpdateUpiApp,
                                                    onUpiIdChange = onUpdateUpiId
                                                )
                                            }
                                            method.startsWith("Credit") -> {
                                                CardPaymentSection(
                                                    cardNumber = checkoutState.cardNumber,
                                                    expiry = checkoutState.cardExpiry,
                                                    cvv = checkoutState.cardCvv,
                                                    holder = checkoutState.cardHolder,
                                                    onChange = onUpdateCardDetails
                                                )
                                            }
                                            method.startsWith("Net Banking") -> {
                                                NetBankingSection(
                                                    selectedBank = checkoutState.selectedBank,
                                                    onBankSelect = onUpdateBank
                                                )
                                            }
                                            method.startsWith("Cash on Delivery") -> {
                                                CodSection(
                                                    captchaCode = checkoutState.codGeneratedCaptcha,
                                                    captchaInput = checkoutState.codCaptchaInput,
                                                    onInputChange = onUpdateCodCaptcha,
                                                    onRefreshCaptcha = onRefreshCodCaptcha
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Error message
                        if (checkoutState.paymentError != null) {
                            Text(
                                text = checkoutState.paymentError,
                                color = RehanRedSale,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sticky Pay Now Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 14.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Payable Amount",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "₹${totalPayable.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onProcessOrder,
                    enabled = !checkoutState.isProcessingPayment,
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 180.dp)
                        .testTag("checkout_pay_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RehanGold,
                        contentColor = RehanBlueDark
                    )
                ) {
                    if (checkoutState.isProcessingPayment) {
                        CircularProgressIndicator(
                            color = RehanBlueDark,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Pay Securely",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (checkoutState.paymentMethod.startsWith("Cash on Delivery")) "Confirm Order" else "Pay Securely",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Edit Address Modal
    if (showEditAddressDialog) {
        var name by remember { mutableStateOf(checkoutState.address.fullName) }
        var phone by remember { mutableStateOf(checkoutState.address.phone) }
        var pin by remember { mutableStateOf(checkoutState.address.pincode) }
        var street by remember { mutableStateOf(checkoutState.address.streetAddress) }
        var city by remember { mutableStateOf(checkoutState.address.city) }
        var state by remember { mutableStateOf(checkoutState.address.state) }
        var type by remember { mutableStateOf(checkoutState.address.type) }

        AlertDialog(
            onDismissRequest = { showEditAddressDialog = false },
            title = { Text("Edit Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Pincode") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("House / Street") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateAddress(
                        DeliveryAddress(
                            fullName = name,
                            phone = phone,
                            pincode = pin,
                            streetAddress = street,
                            city = city,
                            state = state,
                            type = type
                        )
                    )
                    showEditAddressDialog = false
                }) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CheckoutStep(step: Int, title: String, isDone: Boolean = false, isActive: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    if (isDone) RehanGreenSuccess
                    else if (isActive) MaterialTheme.colorScheme.primary
                    else Color.Gray.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else {
                Text(step.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive || isDone) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }
}

@Composable
fun UpiPaymentSection(
    selectedApp: String,
    upiId: String,
    onSelectApp: (String) -> Unit,
    onUpiIdChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
    ) {
        Text("Select UPI App", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Google Pay", "PhonePe", "Paytm").forEach { app ->
                val isAppSelected = selectedApp == app
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAppSelected) MaterialTheme.colorScheme.primary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectApp(app) }
                ) {
                    Text(
                        text = app,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAppSelected) Color.White else Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Or enter UPI ID / VPA", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = upiId,
            onValueChange = onUpiIdChange,
            placeholder = { Text("e.g. mobile@okhdfcbank", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(6.dp)
        )
    }
}

@Composable
fun CardPaymentSection(
    cardNumber: String,
    expiry: String,
    cvv: String,
    holder: String,
    onChange: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { onChange(it, expiry, cvv, holder) },
            label = { Text("Card Number", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = expiry,
                onValueChange = { onChange(cardNumber, it, cvv, holder) },
                label = { Text("Expiry (MM/YY)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = { onChange(cardNumber, expiry, it, holder) },
                label = { Text("CVV", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = holder,
            onValueChange = { onChange(cardNumber, expiry, cvv, it) },
            label = { Text("Name on Card", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NetBankingSection(
    selectedBank: String,
    onBankSelect: (String) -> Unit
) {
    val banks = listOf("HDFC Bank", "State Bank of India", "ICICI Bank", "Axis Bank", "Kotak Mahindra")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
    ) {
        Text("Popular Banks", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        banks.forEach { bank ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBankSelect(bank) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedBank == bank, onClick = { onBankSelect(bank) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(bank, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CodSection(
    captchaCode: String,
    captchaInput: String,
    onInputChange: (String) -> Unit,
    onRefreshCaptcha: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
    ) {
        Text("Anti-Fraud Security Check", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Enter the 4-digit verification code below to confirm Cash on Delivery:", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = RehanBlueDark,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = captchaCode,
                        color = RehanGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                }
            }
            IconButton(onClick = onRefreshCaptcha) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Captcha")
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = captchaInput,
                onValueChange = onInputChange,
                placeholder = { Text("Code", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}
