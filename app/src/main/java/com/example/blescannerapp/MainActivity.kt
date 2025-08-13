package com.example.blescannerapp

import android.os.Bundle
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.blescannerapp.ui.DeviceScreen
import com.example.blescannerapp.ui.ScanScreen

class MainActivity : ComponentActivity() {

    // Stores the BLE device that the user taps from the list.
    private var selectedDevice: ScanResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch the Jetpack Compose UI
        setContent {
            BLEScannerApp(
                onDeviceSelected = {
                    selectedDevice = it
                },
                getSelectedDevice = { selectedDevice }
            )
        }
    }
}

@Composable
fun BLEScannerApp(
    onDeviceSelected: (ScanResult) -> Unit,
    getSelectedDevice: () -> ScanResult?
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "scan") {

        composable("scan") {
            ScanScreen(
                onDeviceSelected = { result ->
                    onDeviceSelected(result)
                    navController.navigate("device/${result.device.address}")
                }
            )
        }

        composable(
            route = "device/{deviceAddress}",
            arguments = listOf(navArgument("deviceAddress") { type = NavType.StringType })
        ) {
            val device = getSelectedDevice()
            if (device != null) {
                DeviceScreen(
                    scanResult = device,
                    onBack = { navController.popBackStack() },
                    onConnect = {
                        Log.d("MainActivity", "Connected to device: ${it.device.name}")
                    }
                )
            } else {
                Text("No device selected. Please go back.")
            }
        }
    }
}
