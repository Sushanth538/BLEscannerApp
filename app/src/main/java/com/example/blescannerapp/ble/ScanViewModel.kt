// Purpose-> Acts as a ViewModel that talks to the BLEScanner and manages connection logic and data reception.

package com.example.blescannerapp.ble

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    //Initializes a custom BLEScanner (defined elsewhere).
    private val scanner = BLEScanner(application.applicationContext)

    //scanResults: list of nearby BLE devices.
    val scanResults: StateFlow<List<ScanResult>> = scanner.scanResults

    //isScanning: true/false if scanning is active.
    val isScanning: StateFlow<Boolean> = scanner.isScanning

    private var bluetoothGatt: BluetoothGatt? = null

    //sensorData holds the latest received BLE data (from notification or read).
    //It's nullable because data might not be available initially.
    private val _sensorData = MutableStateFlow<ByteArray?>(null)
    val sensorData: StateFlow<ByteArray?> = _sensorData

    // Connection status tracking
    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus

    //These start/stop the BLE scan by calling your custom BLEScanner.
    fun startScan() {
        scanner.startScan()
    }

    fun stopScan() {
        scanner.stopScan()
    }

    //Connects to the selected BLE device.
    //Saves the session in bluetoothGatt.
    //Sets a custom BluetoothGattCallback to handle events.
    @Suppress("MissingPermission")
    fun connectToDevice(context: Context, device: BluetoothDevice) {
        _connectionStatus.value = "Connecting to ${device.name ?: "Unknown"}..."
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    // NEW: Safe public method to initiate connection from UI
    fun connectAndMonitorStatus(context: Context, device: BluetoothDevice) {
        _connectionStatus.value = "Connecting to ${device.name ?: "Unknown"}..."
        connectToDevice(context, device)
    }

    //Handles all Bluetooth events: connection, service discovery, data receiving.
    private val gattCallback = object : BluetoothGattCallback() {

        //If connected: Starts service discovery.
        //If disconnected: Cleans up.
        //Else: Logs other state transitions.
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionStatus.value = "Connected to ${gatt?.device?.name ?: "Unknown"}"
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionStatus.value = "Disconnected"
                    Log.d("BLE", "Disconnected from GATT server.")
                }

                else -> {
                    _connectionStatus.value = "Connection state changed: $newState"
                }
            }
        }

        //Logs all available services and characteristics.
        //Enables notifications on characteristics that support it.
        //Reads values of readable characteristics.
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.services?.forEach { service ->
                Log.d("BLE", "Service UUID: ${service.uuid}")

                service.characteristics.forEach { characteristic ->
                    Log.d("BLE", "Characteristic UUID: ${characteristic.uuid}")

                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                        gatt.setCharacteristicNotification(characteristic, true)

                        val descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        )
                        descriptor?.let {
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(it)
                        }
                    }

                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                        gatt.readCharacteristic(characteristic)
                    }
                }
            }
        }
        //Triggered when a BLE device pushes data via notifications.
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val data = characteristic?.value
            Log.d("BLE", "Notification: ${data?.joinToString()}")
            _sensorData.value = data
        }

        //Triggered when the app manually reads a characteristic.
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            val data = characteristic?.value
            Log.d("BLE", "Read data: ${data?.joinToString()}")
            _sensorData.value = data
        }
    }

    //Ensures GATT connection is closed when ViewModel is destroyed (avoids memory leaks).
    override fun onCleared() {
        super.onCleared()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
