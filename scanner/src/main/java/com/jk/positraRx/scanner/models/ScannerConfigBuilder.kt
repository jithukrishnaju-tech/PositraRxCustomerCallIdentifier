package com.jk.positraRx.scanner.models

import android.os.Bundle
import androidx.annotation.DrawableRes

class ScannerConfigBuilder {
    private var poweredByText: String? = null
    private var companyLogoResId: Int? = null
    private var guideText: String? = null
    private val enabledFormats = mutableListOf<BarcodeFormat>()
    private var enableBeepSound: Boolean = true
    private var lockOrientation: Boolean = false

    fun setPoweredByText(text: String) = apply { poweredByText = text }
    fun setCompanyLogo(@DrawableRes logoResId: Int) = apply { companyLogoResId = logoResId }
    fun setGuideText(text: String) = apply { guideText = text }
    fun addFormat(format: BarcodeFormat) = apply { enabledFormats.add(format) }
    fun setFormats(formats: List<BarcodeFormat>) = apply { enabledFormats.addAll(formats) }
    fun setBeepSound(enable: Boolean) = apply { enableBeepSound = enable }
    fun setOrientationLock(lock: Boolean) = apply { lockOrientation = lock }

    fun build(): Bundle {
        return Bundle().apply {
            putString(EXTRA_POWERED_BY_TEXT, poweredByText)
            companyLogoResId?.let { putInt(EXTRA_COMPANY_LOGO, it) }
            putString(EXTRA_GUIDE_TEXT, guideText)
            putSerializable(EXTRA_ENABLED_FORMATS, ArrayList(enabledFormats))
            putBoolean(EXTRA_ENABLE_BEEP, enableBeepSound)
            putBoolean(EXTRA_LOCK_ORIENTATION, lockOrientation)
        }
    }

    companion object {
        const val EXTRA_POWERED_BY_TEXT = "extra_powered_by_text"
        const val EXTRA_COMPANY_LOGO = "extra_company_logo"
        const val EXTRA_GUIDE_TEXT = "extra_guide_text"
        const val EXTRA_ENABLED_FORMATS = "extra_enabled_formats"
        const val EXTRA_ENABLE_BEEP = "extra_enable_beep"
        const val EXTRA_LOCK_ORIENTATION = "extra_lock_orientation"
    }
}