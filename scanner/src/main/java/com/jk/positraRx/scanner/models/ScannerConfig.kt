package com.jk.positraRx.scanner.models


import android.os.Bundle
import com.jk.positraRx.scanner.models.ScannerConfigBuilder.Companion.EXTRA_COMPANY_LOGO
import com.jk.positraRx.scanner.models.ScannerConfigBuilder.Companion.EXTRA_ENABLED_FORMATS
import com.jk.positraRx.scanner.models.ScannerConfigBuilder.Companion.EXTRA_ENABLE_BEEP
import com.jk.positraRx.scanner.models.ScannerConfigBuilder.Companion.EXTRA_GUIDE_TEXT
import com.jk.positraRx.scanner.models.ScannerConfigBuilder.Companion.EXTRA_LOCK_ORIENTATION
import com.jk.positraRx.scanner.models.ScannerConfigBuilder.Companion.EXTRA_POWERED_BY_TEXT

data class ScannerConfig(
    val poweredByText: String? = null,
    val guideText: String? = null,
    val companyLogoResId: Int? = null,
    val enabledFormats: List<BarcodeFormat> = listOf(),
    val enableBeepSound: Boolean = true,
    val lockOrientation: Boolean = false
) {
    companion object {
        fun fromBundle(bundle: Bundle?): ScannerConfig {
            return bundle?.let {
                ScannerConfig(
                    poweredByText = it.getString(EXTRA_POWERED_BY_TEXT),
                    companyLogoResId = if (it.containsKey(EXTRA_COMPANY_LOGO)) it.getInt(EXTRA_COMPANY_LOGO) else null,
                    guideText = it.getString(EXTRA_GUIDE_TEXT),
                    enabledFormats = it.getSerializable(EXTRA_ENABLED_FORMATS) as? List<BarcodeFormat> ?: listOf(),
                    enableBeepSound = it.getBoolean(EXTRA_ENABLE_BEEP, true),
                    lockOrientation = it.getBoolean(EXTRA_LOCK_ORIENTATION, false)
                )
            } ?: ScannerConfig()
        }
    }
}