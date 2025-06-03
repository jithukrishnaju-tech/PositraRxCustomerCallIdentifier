package com.jk.positraRx.scanner.models

import com.jk.positraRx.scanner.models.BarcodeFormat

data class ScanResult(
    val rawValue: String,
    val format: BarcodeFormat,
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
