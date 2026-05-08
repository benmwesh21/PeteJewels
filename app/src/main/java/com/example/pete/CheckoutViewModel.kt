package com.example.pete

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pete.data.AppDatabase
import com.example.pete.data.OrderEntity
import com.example.pete.network.RetrofitClient
import com.example.pete.network.StkPushRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PaymentState {
    object Idle : PaymentState()
    object Processing : PaymentState()
    object AwaitingPin : PaymentState()
    object Success : PaymentState()
    data class Failed(val message: String) : PaymentState()
}

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val orderDao = db.orderDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()
    
    private var firestoreListener: ListenerRegistration? = null

    fun initiatePayment(phoneNumber: String, amount: String, itemsJson: String) {
        _paymentState.value = PaymentState.Processing
        
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: "anonymous"
                
                val request = StkPushRequest(
                    phoneNumber = phoneNumber,
                    amount = amount,
                    userId = userId,
                    itemsJson = itemsJson
                )
                
                // Call our backend to trigger STK Push
                val response = RetrofitClient.paymentApi.triggerStkPush(request)
                
                if (response.success && response.orderId != null) {
                    _paymentState.value = PaymentState.AwaitingPin
                    listenToOrderInFirestore(response.orderId, phoneNumber, amount, itemsJson)
                } else {
                    _paymentState.value = PaymentState.Failed(response.message ?: "Failed to initiate payment")
                }
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Payment Error", e)
                _paymentState.value = PaymentState.Failed(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
    
    private fun listenToOrderInFirestore(orderId: String, phoneNumber: String, amount: String, itemsJson: String) {
        val docRef = firestore.collection("orders").document(orderId)
        
        firestoreListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("CheckoutViewModel", "Listen failed.", e)
                _paymentState.value = PaymentState.Failed("Failed to verify payment status.")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                
                when (status) {
                    "SUCCESS" -> {
                        _paymentState.value = PaymentState.Success
                        saveOrderLocally(orderId, phoneNumber, amount, itemsJson, "SUCCESS")
                        firestoreListener?.remove() // Stop listening
                    }
                    "FAILED" -> {
                        _paymentState.value = PaymentState.Failed("Payment failed or was cancelled.")
                        firestoreListener?.remove()
                    }
                    // PENDING means we keep waiting
                }
            }
        }
    }
    
    private fun saveOrderLocally(orderId: String, phoneNumber: String, amount: String, itemsJson: String, status: String) {
        viewModelScope.launch {
            val order = OrderEntity(
                orderId = orderId,
                phoneNumber = phoneNumber,
                amount = amount,
                status = status,
                timestamp = System.currentTimeMillis(),
                itemsJson = itemsJson
            )
            orderDao.insertOrder(order)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }
    
    fun resetState() {
        _paymentState.value = PaymentState.Idle
    }
}
