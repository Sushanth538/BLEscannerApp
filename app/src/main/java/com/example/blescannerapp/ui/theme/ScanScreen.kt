package com.example.blescannerapp.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.bluetooth.le.ScanResult
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blescannerapp.ble.ScanViewModel

@Composable
fun ScanScreen(
    onDeviceSelected: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val viewModel: ScanViewModel = viewModel()
    val devices by viewModel.scanResults.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    var permissionsGranted by remember { mutableStateOf(false) }

    //This handles runtime permission requests based on the Android version:
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
    }

    //The launcher asks for all the required permissions at once:
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            permissionsGranted = results.all { it.value }
            if (!permissionsGranted) {
                Toast.makeText(context, "Permissions are required to scan", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    // ✅ Show toast when connected to device
    LaunchedEffect(connectionStatus) {
        if (connectionStatus.startsWith("Connected to")) {
            Toast.makeText(context, "✅ $connectionStatus", Toast.LENGTH_SHORT).show()
        }
    }

    //Checks if both Bluetooth and GPS are ON:

    fun isBluetoothAndLocationEnabled(): Boolean {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        return isBluetoothEnabled && isLocationEnabled
    }

    //If either is OFF, promptUserToEnableSettings() sends user to system settings.

    fun promptUserToEnableSettings() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(enableLocationIntent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = {
                if (!permissionsGranted) {
                    launcher.launch(permissions)
                    return@Button
                }

                if (isBluetoothAndLocationEnabled()) {
                    // starts scanning for BLE devices using logic from your ScanViewModel.
                    viewModel.startScan()
                } else {
                    Toast.makeText(context, "Please enable Bluetooth and Location", Toast.LENGTH_SHORT).show()
                    promptUserToEnableSettings()
                }
            },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text("Scanning...")
            } else {
                Text("Scan for Devices")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Nearby BLE Devices", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        //list the nearby BLE devices like this:
        //You attempt connection via viewModel.connectToDevice(...).
        //You call onDeviceSelected(result) to move to DeviceScreen.

        LazyColumn {
            items(devices) { result ->
                val name = result.device.name ?: result.scanRecord?.deviceName ?: "Unnamed"
                val address = result.device.address

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            Toast.makeText(context, "Connecting to $name", Toast.LENGTH_SHORT).show()
                            viewModel.connectToDevice(context, result.device) //  Use correct function
                            //This triggers navigation to your DeviceScreen,
                            // where you show details and can trigger connection again (if needed).
                            onDeviceSelected(result) // Optional callback
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Name: $name")
                        Text("Address: $address")
                        Text("RSSI: ${result.rssi}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Show connection status at bottom
        if (connectionStatus.isNotBlank()) {
            Text(
                text = when {
                    isScanning -> " Scanning..."
                    connectionStatus.isNotBlank() -> " $connectionStatus"
                    else -> "Idle"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
