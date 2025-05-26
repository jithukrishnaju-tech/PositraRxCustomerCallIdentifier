package com.jk.customercalltracker

data class CustomerData(
    val name: String,
    val phoneNumber: String,
    val additionalInfo: String? = null,
    val isVip: Boolean = false
)