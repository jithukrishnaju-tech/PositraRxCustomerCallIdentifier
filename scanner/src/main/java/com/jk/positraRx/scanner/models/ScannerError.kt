package com.jk.positraRx.scanner.models

sealed class ScannerError {
    object CameraPermissionDenied : ScannerError()
    object CameraInitializationError : ScannerError()
    data class ProcessingError(val message: String) : ScannerError()
}