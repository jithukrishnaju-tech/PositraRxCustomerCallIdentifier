package com.jk.positraRx.scanner

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.jk.positraRx.scanner.models.BarcodeFormat
import com.jk.positraRx.scanner.models.ScanResult
import com.jk.positraRx.scanner.models.ScannerConfig
import com.jk.positraRx.scanner.models.ScannerError
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class ScannerManager(
    private val context: Context,
    private val previewView: PreviewView,
    private val feedbackUtil: FeedbackUtil
) {

    private var camera: Camera? = null
    private var isScanning = false
    private var callback: ScannerCallback? = null
    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startScanning(callback: ScannerCallback, config: ScannerConfig) {
        this.callback = callback
        if (!isScanning) {
            isScanning = true
            initializeCamera(config)
        }
    }

    fun processGalleryImage(uri: Uri) {
        try {
            val bitmap = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                else -> {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }

            val image = InputImage.fromBitmap(bitmap, 0)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        handleBarcode(barcodes[0])
                    } else {
                        callback?.onError(ScannerError.ProcessingError("No barcode found in image"))
                    }
                }
                .addOnFailureListener { e ->
                    callback?.onError(ScannerError.ProcessingError(e.message ?: "Failed to process image"))
                }
        } catch (e: Exception) {
            callback?.onError(ScannerError.ProcessingError("Failed to load image: ${e.message}"))
        }
    }

    fun stopScanning() {
        isScanning = false
        cameraExecutor.shutdown()
    }

    fun toggleTorch(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun initializeCamera(config: ScannerConfig) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                setupCamera(cameraProvider, config)
            } catch (e: Exception) {
                callback?.onError(ScannerError.CameraInitializationError)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @ExperimentalGetImage
    private fun setupCamera(cameraProvider: ProcessCameraProvider, config: ScannerConfig) {
        try {
            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            camera = cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

//            if (config.autoFocusEnabled) {
            camera?.cameraControl?.enableTorch(false)
//            }

        } catch (e: Exception) {
            callback?.onError(ScannerError.CameraInitializationError)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        imageProxy.image?.let { mediaImage ->
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        handleBarcode(barcodes[0])
                    }
                }
                .addOnFailureListener { e ->
                    callback?.onError(ScannerError.ProcessingError(e.message ?: "Unknown error"))
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun handleBarcode(barcode: Barcode) {
        if (!isScanning) return

        val scanResult = ScanResult(
            rawValue = barcode.rawValue ?: "",
            format = getBarcodeFormat(barcode.format),
            metadata = extractMetadata(barcode)
        )
        feedbackUtil.playBeepSound()
        callback?.onCodeScanned(scanResult)
    }

    private fun getBarcodeFormat(format: Int): BarcodeFormat {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.FORMAT_CODE_128 -> BarcodeFormat.BARCODE_128
            Barcode.FORMAT_CODE_39 -> BarcodeFormat.BARCODE_39
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
            else -> BarcodeFormat.UNKNOWN
        }
    }

    private fun extractMetadata(barcode: Barcode): Map<String, Any> {
        val metadata = mutableMapOf<String, Any>()
        when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                barcode.url?.let {
                    metadata["url"] = it.url ?: ""
                }
            }

            Barcode.TYPE_EMAIL -> {
                barcode.email?.let {
                    metadata["email"] = it.address ?: ""
                }
            }

            Barcode.TYPE_PHONE -> {
                barcode.phone?.let {
                    metadata["phone"] = it.number ?: ""
                }
            }

            Barcode.TYPE_SMS -> {
                barcode.sms?.let {
                    metadata["sms"] = it.message ?: ""
                }
            }
        }
        return metadata
    }
}