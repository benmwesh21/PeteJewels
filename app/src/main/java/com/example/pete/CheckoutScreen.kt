package com.example.pete

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pete.ui.theme.LuxuryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    checkoutViewModel: CheckoutViewModel = viewModel(),
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val paymentState by checkoutViewModel.paymentState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.Success -> {
                cartViewModel.clearCart()
                checkoutViewModel.resetState()
                onPaymentSuccess()
            }
            is PaymentState.Failed -> {
                val msg = (paymentState as PaymentState.Failed).message
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                checkoutViewModel.resetState()
            }
            else -> {}
        }
    }

    // Calculate total purely for display (rough calculation based on string format, ideally should be Int)
    val totalAmount = cartItems.sumOf { item ->
        item.price.replace("Ksh ", "").replace(",", "").toIntOrNull() ?: 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = LuxuryGold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LuxuryGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(16.dp)
            ) {
                Button(
                    onClick = {
                        if (phoneNumber.isNotBlank()) {
                            val itemsJson = "[" + cartItems.joinToString(",") { it.id.toString() } + "]"
                            checkoutViewModel.initiatePayment(phoneNumber, totalAmount.toString(), itemsJson)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    enabled = paymentState == PaymentState.Idle && phoneNumber.isNotBlank()
                ) {
                    when (paymentState) {
                        is PaymentState.Processing -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        }
                        is PaymentState.AwaitingPin -> {
                            Text("Awaiting PIN...", fontWeight = FontWeight.Bold)
                        }
                        else -> {
                            Text("Pay Ksh ${"%,d".format(totalAmount)} with M-Pesa", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.price, fontWeight = FontWeight.Medium)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Ksh ${"%,d".format(totalAmount)}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, LuxuryGold, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("M-Pesa (Daraja API)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Lipa Na M-Pesa Online", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("M-Pesa Phone Number") },
                placeholder = { Text("e.g. 254700000000") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }
    }
}
