package com.example.dainttabluetoothhackathon.domain

import com.example.dainttabluetoothhackathon.data.BluetoothDevice
import com.example.dainttabluetoothhackathon.data.BluetoothMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val isScanning: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: Flow<String>
    val incomingMessages: Flow<BluetoothMessage>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    suspend fun sendMessage(message: String): Boolean

    fun closeConnection()
    fun release()
}
