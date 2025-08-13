package com.example.blescannerapp.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

@SuppressLint("MissingPermission")
class BLEManager(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null

    // ✅ Track connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Existing ByteArray flow for other usage
    private val _dataFromDevice = MutableStateFlow<ByteArray?>(null)
    val dataFromDevice: StateFlow<ByteArray?> = _dataFromDevice.asStateFlow()

    // ✅ Float array flow for plotting waveform
    private val _floatDataFlow = MutableStateFlow<List<Float>>(emptyList())
    val floatDataFlow: StateFlow<List<Float>> = _floatDataFlow.asStateFlow()

    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BLE", "Connected to GATT server.")
                    _isConnected.value = true
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BLE", "Disconnected from GATT server.")
                    _isConnected.value = false
                    gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.services.firstOrNull() // Replace with specific UUID if needed
                val characteristic = service?.characteristics?.firstOrNull() // Replace with specific UUID if needed

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    Log.d("BLE", "Notifications enabled")

                    // Optional: Enable notifications via descriptor
                    // val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
                    // descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    // gatt.writeDescriptor(descriptor)
                }
            } else {
                Log.w("BLE", "Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value
            _dataFromDevice.value = value

            if (value != null) {
                val floatList = mutableListOf<Float>()
                val buffer = java.nio.ByteBuffer.wrap(value).order(java.nio.ByteOrder.LITTLE_ENDIAN)

                while (buffer.remaining() >= 4) {
                    floatList.add(buffer.float)
                }

                Log.d("BLE", "Received float array: $floatList")
                _floatDataFlow.value = floatList
            }
        }
    }
}
