package com.village.generalstore.domain.model

data class Store(
    val id: String = "",
    val name: String = "",
    val ownerName: String = "",
    val address: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val passcode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
