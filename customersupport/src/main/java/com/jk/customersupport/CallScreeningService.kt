package com.jk.customersupport
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.abs

class CallScreeningService : CallScreeningService() {
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var popupView: View? = null
    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var callStateListener: CallStateListener
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var isPopupShown: Boolean = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            Log.i("CallerTagService", "Ignoring non-incoming call")
            return
        }
        Log.d("CallerTagService", "Getting calls")

        val number = callDetails.handle?.schemeSpecificPart ?: return

//        val apiCallback = CallerTagManager.getInstance().getApiCallback()
        val apiCallback = CallerTagManager.getInstance().getApiCallback()
        if (apiCallback == null) {
            Log.e("CallerTagService", "API callback not initialized")
            return
        }

        apiCallback.checkCustomer(number) { customerData ->
            customerData?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    showPopup(it)
                }
            }
        }

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateListener = CallStateListener()
            telephonyManager.registerTelephonyCallback(executor, callStateListener)
        }
    }

    private fun showPopup(customerData: CustomerData) {
        if (isPopupShown) {
            Log.i("CallerTagService", "Popup already shown")
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.OPAQUE
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 100
            }
        }

        val layoutResId = CallerTagManager.getInstance().getCustomPopupLayout()
            ?: R.layout.default_caller_popup

//        val layoutResId = CallerTagManager.getInstance().getCustomPopupLayout()
//            ?: R.layout.default_caller_popup

        popupView = LayoutInflater.from(this).inflate(layoutResId, null)

        populatePopupData(customerData)

        val closeBtn = popupView?.findViewById<ImageView>(R.id.closeButton)
        closeBtn?.setOnClickListener {
            removePopup()
        }

        setupDragFunctionality(popupView!!, layoutParams!!, windowManager!!)
        windowManager?.addView(popupView, layoutParams)
        isPopupShown = true
    }

    private fun populatePopupData(customerData: CustomerData) {
        popupView?.apply {
            findViewById<TextView>(R.id.callerName)?.text = customerData.name
            findViewById<TextView>(R.id.phoneNumber)?.text = customerData.phoneNumber
        }
    }

    private fun setupDragFunctionality(view: View, params: WindowManager.LayoutParams, windowManager: WindowManager) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val moved = abs(event.rawX - initialTouchX) < 5 && abs(event.rawY - initialTouchY) < 5
                    if (moved) {
                        v.performClick()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun removePopup() {
        if (!isPopupShown) return

        popupView.let {
            try {
                windowManager?.removeView(it)
            } catch (e: IllegalArgumentException) {
                Log.e("CallerTagService", "View already removed or not attached", e)
            }
            windowManager = null
            layoutParams = null
            isPopupShown = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private inner class CallStateListener : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    // No action needed
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.d("CallState", "Call answered or active")
                    removePopup()
                    cleanupTelephonyCallback()
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    Log.d("CallState", "Call ended or idle")
                    removePopup()
                    cleanupTelephonyCallback()
                }
            }
        }
    }

    private fun cleanupTelephonyCallback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.unregisterTelephonyCallback(callStateListener)
            }
        } catch (e: IllegalStateException) {
            Log.e("CallerTagService", "Failed to unregister TelephonyCallback", e)
        }
    }
}