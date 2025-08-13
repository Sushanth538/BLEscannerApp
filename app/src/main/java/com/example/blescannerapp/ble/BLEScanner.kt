package com.example.blescannerapp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BLEScanner(context: Context) {

    // Gets the bluetoothAdapter from system --> required to perform ble operation.
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    //actual scanner used to search for BLE device.
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    //A StateFlow to hold the list of discovered BLE devices in real time.
    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    //Used to auto-stop scanning after 10 seconds.
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    private val scanTimeout: Long = 10000L // scan duration: 10 seconds
    private val handler = Handler(Looper.getMainLooper())

    //Whenever a new device is found, it is added to the list only if it’s not already present.
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val currentList = _scanResults.value.toMutableList()
            if (currentList.none { it.device.address == result.device.address }) {
                currentList.add(result)
                _scanResults.value = currentList
            }
        }
    }

    @SuppressLint("MissingPermission")
    //Starts scanning and auto stops after scanTimeout.
    fun startScan() {
        if (!_isScanning.value) {
            bluetoothLeScanner?.startScan(scanCallback)
            _isScanning.value = true
            handler.postDelayed({ stopScan() }, scanTimeout)
        }
    }

    @SuppressLint("MissingPermission")

    //Manually stops scanning.
    fun stopScan() {
        if (_isScanning.value) {
            bluetoothLeScanner?.stopScan(scanCallback)
            _isScanning.value = false
        }
    }
}
