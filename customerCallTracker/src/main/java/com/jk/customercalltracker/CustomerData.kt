package com.jk.customercalltracker

data class CustomerData(
    val name: String,
    val phoneNumber: String,
    val isVerified: Boolean? = null,
    val isPhoneVerified: Boolean? = null
)