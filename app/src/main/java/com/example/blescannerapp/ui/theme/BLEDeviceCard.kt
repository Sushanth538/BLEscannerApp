package com.example.blescannerapp.ui

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable //Marks a function that creates UI using Compose
//result: A ScanResult object from BLE scanning. Contains device info.
//
//onClick: Lambda function that gets called when the card is tapped. You pass this from the parent ScanScreen.
fun BLEDeviceCard(result: ScanResult, onClick: (ScanResult) -> Unit) {
    val name = result.device.name ?: "Unnamed"
    val address = result.device.address
    val rssi = result.rssi

    //ElevatedCard: A material-style card with elevation (i.e. shadow).
    //
    //fillMaxWidth(): Expands to full screen width.
    //
    //clickable { onClick(result) }: When tapped, notifies the ScanScreen which device was selected.
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(result) },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: $name", style = MaterialTheme.typography.titleMedium)
            Text("Address: $address", style = MaterialTheme.typography.bodyMedium)
            Text("RSSI: $rssi", style = MaterialTheme.typography.bodySmall)

            //Uses Column to stack device info vertically.
            //Applies padding of 16.dp for spacing inside the card.
            //Displays the three main pieces of information:
            //Name: from result.device.name or "Unnamed" if null.
            //MAC Address: from result.device.address
            //RSSI (signal strength): higher (less negative) = stronger signal.
        }
    }
}
