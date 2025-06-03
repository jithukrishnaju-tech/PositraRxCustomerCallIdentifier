package com.jk.positraRx.scanner

import com.jk.positraRx.scanner.models.ScanResult
import com.jk.positraRx.scanner.models.ScannerError

interface ScannerCallback {
    fun onCodeScanned(result: ScanResult)
    fun onError(error: ScannerError)
}