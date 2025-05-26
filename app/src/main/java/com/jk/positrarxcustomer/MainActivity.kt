package com.jk.positrarxcustomer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jk.customercalltracker.ApiCallback
import com.jk.customercalltracker.CallerTagManager
import com.jk.customercalltracker.CustomerData
import com.jk.customercalltracker.ItemNameMe


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CallerTagManager.getInstance().initialize(object : ApiCallback {
            override fun checkCustomer(phoneNumber: String, onResult: (CustomerData?) -> Unit) {

                val customerData = CustomerData(
                    name = "Ignore this customer",
                    phoneNumber = phoneNumber,
                )
                onResult(customerData)
            }
        })
        val data = ItemNameMe("jithu")
        Toast.makeText(this, "data is ${data.name}", Toast.LENGTH_SHORT).show()


    }
}