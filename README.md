# Positrarx Libraries

This repository contains two Android libraries for enhancing app functionality:

- **Positrarx Scanner Library**: A library for QR code and barcode scanning using ML Kit.
- **Customer Call Tracker Library**: A library for identifying incoming calls and displaying customer information in a popup.

## Positrarx Scanner Library

A lightweight Android library for QR code and barcode scanning built using ML Kit. This library provides an easy-to-use scanner activity that can be integrated into any Android project.

### Features

- Quick and easy barcode/QR code scanning
- Built using ML Kit for reliable scanning
- Simple integration with just a few lines of code
- Supports both QR codes and barcodes
- Customizable scanner UI and behavior
- Configurable orientation lock and sound options

### Installation

Add JitPack repository to your project's `build.gradle.kts` file:

```gradle
allprojects {
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle.kts` file:

```gradle
implementation("com.github.jithukrishnaju-tech:positrarx-scanner:1.0.1")
```

### Requirements

- Android SDK 21 (Android 5.0) or higher

- Camera permission must be handled by the implementing application

- Add camera permission in `AndroidManifest.xml`:

  ```xml
  <uses-permission android:name="android.permission.CAMERA" />
  ```

### Usage

#### Scanner Configuration

The library provides a `ScannerConfigBuilder` class to customize the scanner behavior and appearance. Here are the available configuration options:

```kotlin
val scannerConfig = ScannerConfigBuilder()
    .setPoweredByText("Powered by ")           // Set custom powered by text
    .setGuideText("Scan QR/Barcode")          // Set custom guide text
    .setCompanyLogo(R.drawable.logo) // Set company logo
    .setBeepSound(false)                      // Enable/disable beep sound on scan
    .setOrientationLock(true)                 // Lock screen orientation during scanning
    .build()
```

#### Basic Implementation

1. Set up the activity result launcher to handle scanner results:

```kotlin
private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val scannedData = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
        val scanFormat = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_FORMAT)
        // Handle the scanned data here
    }
}
```

2. Start the scanner with configuration:

```kotlin
private fun startScannerActivity() {
    val intent = Intent(requireContext(), ScannerActivity::class.java)
    
    // Create and apply scanner configuration
    val scannerConfig = ScannerConfigBuilder()
        .setPoweredByText("Powered by ")
        .setGuideText("Scan QR/Barcode")
        .setBeepSound(false)
        .setOrientationLock(true)
        .build()
    
    intent.putExtras(scannerConfig)
    scannerLauncher.launch(intent)
}
```

#### Intent Extras

The scanner activity returns the following extras in the result intent:

- `ScannerActivity.EXTRA_SCAN_RESULT`: Contains the raw value of the scanned code
- `ScannerActivity.EXTRA_SCAN_FORMAT`: Contains the format of the scanned code

#### Sample Implementation

Here's a complete example of how to implement the scanner in a Fragment:

```kotlin
class ScannerFragment : Fragment() {
    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedData = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            val scanFormat = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_FORMAT)
            
            // Handle your scanned data here
            handleScannedData(scannedData, scanFormat)
        }
    }

    private fun startScannerActivity() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        
        // Configure scanner options
        val scannerConfig = ScannerConfigBuilder()
            .setPoweredByText("Powered by ")
            .setGuideText("Scan QR/Barcode")
            .setCompanyLogo(R.drawable.logo)
            .setBeepSound(false)
            .setOrientationLock(true)
            .build()
            
        intent.putExtras(scannerConfig)
        scannerLauncher.launch(intent)
    }

    private fun handleScannedData(data: String?, format: String?) {
        // Handle your specific business logic here
    }
}
```

#### Configuration Options

| Method | Description | Default Value |
| --- | --- | --- |
| `setPoweredByText(text: String)` | Sets the powered by text shown in the scanner UI | "Powered by " |
| `setGuideText(text: String)` | Sets the guidance text shown to the user | "Scan QR/Barcode" |
| `setCompanyLogo(drawable: Int)` | Sets the desired logo in the scanner UI | Default library logo |
| `setBeepSound(enabled: Boolean)` | Enables or disables beep sound on successful scan | true |
| `setOrientationLock(enabled: Boolean)` | Locks the screen orientation during scanning | false |

---

## Customer Call Tracker Library

A lightweight Android library for identifying incoming calls and displaying customer information in a popup. The library intercepts incoming calls, performs an API call to verify if the caller is a customer, and shows a popup with customer details if verified, and based on the customer verfication show different color on the pop up to indicates the importance of the customer.

### Features

- Intercepts incoming calls using a `CallScreenService`
- Displays customer information in a customizable popup using `WindowManager`
- Library will always intercepts incoming call even if the phone is off.
- Requires an API callback to fetch customer data by using the number which given by the library.

### Installation

Add JitPack repository to your project's `build.gradle.kts` file:

```gradle
allprojects {
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle.kts` file:

```gradle
dependencies {
    implementation("com.github.jithukrishnaju-tech:PositraRxCustomerCallIdentifier:1.0.6")
}
```

### Requirements

- Android SDK 29 (Android 10) or higher

- Permissions must be handled by the implementing application:
    
  - `READ_PHONE_STATE`
  - `READ_CALL_LOG`
  - `SYSTEM_ALERT_WINDOW`



- Declare the call screening service in `AndroidManifest.xml`:

  ```xml
  <service
    android:name="com.jk.customercalltracker.CallerTagService"
    android:exported="true"
    android:permission="android.permission.BIND_SCREENING_SERVICE">
    <intent-filter>
        <action android:name="android.telecom.CallScreeningService" />
    </intent-filter>
  </service>
  ```

### Usage

#### Setting Up the Foreground Service

The library requires a foreground service to make api calls so that app will show pop up even if the phone is off or app is destroyed. 


#### Initializing the CallerTagManager

Initialize the `CallerTagManager` with an `ApiCallback` to handle customer data retrieval. The callback should make an API call to verify the caller's phone number and return a `CustomerData` object.

```kotlin
CallerTagManager.getInstance().initialize(object : ApiCallback {
    override fun checkCustomer(phoneNumber: String, onResult: (CustomerData?) -> Unit) {
        // Example: Normalize phone number and make an API call
        val cleanedPhoneNumber = phoneNumber.replace(Regex("[^0-9]"), "").takeLast(10)
        // Perform API call to check if the phone number belongs to a customer
        // Example response:
        val customerData = CustomerData(
            name = "John Doe",
            phoneNumber = cleanedPhoneNumber,
            isVerified = true,
            isPhoneVerified = true,
            oldVerificationMethod = "PHONEVERIFIED"
        )
        onResult(customerData)
    }
})
```


#### Sample Implementation

Here's a complete example of setting up the library in an foreground service (Make sure to call it in a foreground service)

```kotlin
@AndroidEntryPoint
class CustomerApiService : Service() {
    @Inject
    lateinit var customerService: CustomerService

    override fun onCreate() {
        super.onCreate()
        setupForegroundNotification()
        CallerTagManager.getInstance().initialize(provideApiCallback())
    }

    private fun provideApiCallback(): ApiCallback {
            return object : ApiCallback {
                override fun checkCustomer(phoneNumber: String, onResult: (CustomerData?) -> Unit) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = customerService.getCustomerDataFromPhoneNumber(cleanedPhoneNumber.toLong())
                            if (response.isSuccessful) {
                                val responseBody = response.body() as CustomerDataResponse
                                val customerData =
                                    responseBody.name?.let {
                                        CustomerData(
                                            it,
                                            cleanedPhoneNumber,
                                            responseBody.isVerified,
                                            responseBody.isPhoneVerified,
                                            responseBody.oldVerificationMethod
                                        )
                                    }
                                onResult(customerData)
                            } else {
                                onResult(null)
                            }
                        } catch (e: Exception) {
                            onResult(null)
                        }
                    }
                }
            }
        }
}
```

### CustomerData Model

The `CustomerData` class is used to pass customer information to the library:

```kotlin
data class CustomerData(
    val name: String,
    val phoneNumber: String,
    val isVerified: Boolean,
    val isPhoneVerified: Boolean,
    val oldVerificationMethod: String,
)
```

### Notes

- The library uses `WindowManager` to display popups, requiring the `SYSTEM_ALERT_WINDOW` permission. Ensure this permission is granted, typically via a user prompt for overlay permissions.
- The `ApiCallback` implementation should handle API calls efficiently to avoid delays during incoming calls.
- If no customer data is returned (`onResult(null)`), the library will not display a popup.
