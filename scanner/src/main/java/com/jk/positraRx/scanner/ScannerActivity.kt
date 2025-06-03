package com.jk.positraRx.scanner

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jk.positraRx.scanner.models.ScanResult
import com.jk.positraRx.scanner.models.ScannerConfig
import com.jk.positraRx.scanner.models.ScannerError

class ScannerActivity : AppCompatActivity() {
    private lateinit var scannerView: ScannerView
    private lateinit var resultTextView: TextView
    private lateinit var config: ScannerConfig

    private val scannerCallback = object : ScannerCallback {
        override fun onCodeScanned(result: ScanResult) {
            handleScanResult(result)
        }

        override fun onError(error: ScannerError) {
            handleError(error)
        }
    }

    companion object {
        const val EXTRA_SCAN_RESULT = "scan_result"
        const val EXTRA_SCAN_FORMAT = "scan_format"
        const val EXTRA_SCAN_METADATA = "scan_metadata"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = ScannerConfig.fromBundle(intent.extras)
        if (config.lockOrientation) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_scanner)
        scannerView = findViewById(R.id.scanner_view)
        resultTextView = findViewById(R.id.resultTextView)
        config.guideText?.let { scannerView.setGuideText(it) }
        config.poweredByText?.let { scannerView.setPoweredByText(it) }
        initializeScanner()
    }
    private fun initializeScanner() {
        scannerView.startScanning(scannerCallback, config)
    }

    private fun handleScanResult(result: ScanResult) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_SCAN_RESULT, result.rawValue)
            putExtra(EXTRA_SCAN_FORMAT, result.format)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun handleError(error: ScannerError) {
        when (error) {
            is ScannerError.ProcessingError -> {
                Toast.makeText(this, "Failed to process image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {
                setResult(Activity.RESULT_CANCELED, Intent().apply {
                    putExtra("error", error.toString())
                })
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            scannerView.handleGalleryResult(data)
        }
    }

    override fun onStop() {
        super.onStop()
        scannerView.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView.cleanup()
        scannerView.stopScanning()
    }
}