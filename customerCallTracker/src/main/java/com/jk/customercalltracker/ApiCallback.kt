package com.jk.customercalltracker

interface ApiCallback {
    fun checkCustomer(phoneNumber: String, onResult: (CustomerData?) -> Unit)
}