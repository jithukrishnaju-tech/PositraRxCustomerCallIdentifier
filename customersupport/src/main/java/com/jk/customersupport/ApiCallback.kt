package com.jk.customersupport

interface ApiCallback {
    fun checkCustomer(phoneNumber: String, onResult: (CustomerData?) -> Unit)
}