package com.village.generalstore.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.village.generalstore.ui.customer.CustomerViewModel

@Composable
fun WelcomeScreen(
    onNavigateToCustomer: () -> Unit,
    onNavigateToSeller: (String) -> Unit
) {
    val customerViewModel: CustomerViewModel = hiltViewModel()
    val stores by customerViewModel.stores.collectAsState()

    var showSellerLogin by remember { mutableStateOf(false) }
    var showRegistration by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Registration States
    var regStoreName by remember { mutableStateOf("") }
    var regOwnerName by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regImageUrl by remember { mutableStateOf("") }
    var regPasscode by remember { mutableStateOf("") }
    var regResult by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .safeDrawingPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Store Icon / Mascot
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Store Icon",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Welcome texts
                Text(
                    text = "Apna Kirana Store",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Welcome to your local general store! Choose your mode to continue.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (!showSellerLogin && !showRegistration) {
                    // Customer Mode Button (Premium styling)
                    Button(
                        onClick = onNavigateToCustomer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Enter as Customer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seller Mode Button
                    TextButton(
                        onClick = { showSellerLogin = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Store Owner Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    TextButton(onClick = { showRegistration = true }) {
                        Text("Register New Store")
                    }
                } else if (showRegistration) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Register Your Store", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(value = regStoreName, onValueChange = { regStoreName = it }, label = { Text("Store Name (Mandatory)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = regOwnerName, onValueChange = { regOwnerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = regPhone, onValueChange = { regPhone = it }, label = { Text("Mobile Number (Mandatory)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = regAddress, onValueChange = { regAddress = it }, label = { Text("Store Location / Address") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = regImageUrl, onValueChange = { regImageUrl = it }, label = { Text("Store Image URL") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = regPasscode, onValueChange = { regPasscode = it }, label = { Text("Set 4-Digit Passcode") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth())
                            
                            if (regResult != null) {
                                Text(regResult!!, color = if (regResult!!.startsWith("Registered")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
                            }
                            
                            val scope = androidx.compose.runtime.rememberCoroutineScope()
                            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val firebaseService = com.village.generalstore.data.remote.FirebaseService(firestore)
                            
                            Button(
                                onClick = {
                                    if (regStoreName.isBlank() || regPhone.isBlank() || regPasscode.isBlank()) {
                                        regResult = "Store Name, Phone, and Passcode are required"
                                        return@Button
                                    }
                                    if (isRegistering) return@Button
                                    isRegistering = true
                                    
                                    scope.launch {
                                        if (firebaseService.isStoreNameTaken(regStoreName.trim())) {
                                            regResult = "Error: Store name '$regStoreName' is already taken"
                                            isRegistering = false
                                            return@launch
                                        }
                                        
                                        val newStore = com.village.generalstore.domain.model.Store(
                                            name = regStoreName.trim(),
                                            ownerName = regOwnerName.trim(),
                                            address = regAddress.trim(),
                                            phone = regPhone.trim(),
                                            imageUrl = regImageUrl.trim(),
                                            passcode = regPasscode.trim()
                                        )
                                        val id = firebaseService.registerStore(newStore)
                                        regResult = "Registered! Your Store ID is: $id"
                                        isRegistering = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isRegistering
                            ) {
                                if (isRegistering) {
                                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Register Store")
                                }
                            }
                            TextButton(onClick = { showRegistration = false; showSellerLogin = true }) {
                                Text("Back to Login")
                            }
                        }
                    }
                } else {
                    // Seller Login Fields
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Store Owner Login",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it; loginError = null },
                                label = { Text("Registered Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; loginError = null },
                                label = { Text("Enter Passcode") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (loginError != null) {
                                Text(
                                    text = loginError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            val scope = androidx.compose.runtime.rememberCoroutineScope()
                            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val firebaseService = com.village.generalstore.data.remote.FirebaseService(firestore)
                            var isLoggingIn by remember { mutableStateOf(false) }

                            Button(
                                onClick = {
                                    if (phoneInput.isBlank() || password.isBlank()) {
                                        loginError = "Please enter phone and passcode"
                                        return@Button
                                    }
                                    isLoggingIn = true
                                    scope.launch {
                                        val store = firebaseService.getStoreByPhone(phoneInput.trim())
                                        if (store != null) {
                                            if (store.passcode == password.trim()) {
                                                onNavigateToSeller(store.id)
                                            } else {
                                                loginError = "Incorrect passcode"
                                            }
                                        } else {
                                            loginError = "No store registered with this number"
                                        }
                                        isLoggingIn = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoggingIn
                            ) {
                                if (isLoggingIn) {
                                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(onClick = { showSellerLogin = false }) {
                                Text("Back to Selection")
                            }
                        }
                    }
                }
            }
        }
    }
}
