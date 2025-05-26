package com.jk.customercalltracker

class CallerTagManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: CallerTagManager? = null

        fun getInstance(): CallerTagManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CallerTagManager().also { INSTANCE = it }
            }
        }
    }

    private var apiCallback: ApiCallback? = null
    private var customPopupLayout: Int? = null

    fun initialize(callback: ApiCallback) {
        this.apiCallback = callback
    }

    fun setCustomPopupLayout(layoutResId: Int) {
        this.customPopupLayout = layoutResId
    }

    internal fun getApiCallback(): ApiCallback? = apiCallback

    internal fun getCustomPopupLayout(): Int? = customPopupLayout
}