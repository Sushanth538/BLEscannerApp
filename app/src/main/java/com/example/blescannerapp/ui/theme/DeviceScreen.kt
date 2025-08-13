package com.example.blescannerapp.ui

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.blescannerapp.ble.BLEManager
import kotlinx.coroutines.launch

// Add import for the waveform graph composable
import com.example.blescannerapp.ui.WaveformGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    scanResult: ScanResult,
    onBack: () -> Unit,
    onConnect: (ScanResult) -> Unit // Keep this for future use if needed
) {
    val context = LocalContext.current
    val bleManager = remember { BLEManager(context) }
    val scope = rememberCoroutineScope()

    val bleData by bleManager.dataFromDevice.collectAsState()
    val floatData by bleManager.floatDataFlow.collectAsState() // <-- New

    val isConnected by bleManager.isConnected.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val deviceName = scanResult.device.name ?: scanResult.scanRecord?.deviceName ?: "Unnamed"
            Text(text = "Device Name: $deviceName")
            Text(text = "MAC Address: ${scanResult.device.address}")
            Text(text = "RSSI: ${scanResult.rssi}")

            Button(
                onClick = {
                    bleManager.connectToDevice(scanResult.device)
                },
                enabled = !isConnected
            ) {
                Text(if (isConnected) "Connected" else "Connect")
            }

            Text(text = "Received Data: ${bleData?.joinToString() ?: "No data yet"}")

            Spacer(modifier = Modifier.height(8.dp))

            // Show the waveform graph using the float array
            WaveformGraph(data = floatData)
        }
    }
}
