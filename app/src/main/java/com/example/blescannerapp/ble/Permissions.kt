
//This file ensures your app requests the necessary Bluetooth and location permissions at runtime,
// adapting to the Android version.
package com.example.blescannerapp.ble

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

//Checks Android version to choose the correct set of permissions:
//
//On Android 12+ (S or API 31), BLUETOOTH_SCAN and BLUETOOTH_CONNECT are needed.
//
//On older versions, BLUETOOTH and BLUETOOTH_ADMIN are used.
val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}


//Extension function for ComponentActivity (like MainActivity).
//
//Allows you to call:
//requestBluetoothPermissions { granted -> ... }

fun ComponentActivity.requestBluetoothPermissions(onResult: (Boolean) -> Unit) {

    //Uses ActivityResultContracts.RequestMultiplePermissions() to handle multiple permission requests in one go.
    //When user responds, it checks whether all permissions were granted and then calls onResult(true/false).
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        onResult(granted)
    }

    //Before launching the dialog, it checks which permissions are not yet granted.
    val missingPermissions = bluetoothPermissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PERMISSION_GRANTED
    }

    //If any permission is missing → request it.
    //
    //If all are granted → immediately proceed with onResult(true).
    if (missingPermissions.isNotEmpty()) {
        requestPermissionLauncher.launch(missingPermissions.toTypedArray())
    } else {
        onResult(true)
    }
}
