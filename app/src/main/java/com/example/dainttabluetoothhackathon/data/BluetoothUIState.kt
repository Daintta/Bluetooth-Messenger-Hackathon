package com.example.dainttabluetoothhackathon.data

data class BluetoothUIState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isServerStarted: Boolean = false,
    val isScanning: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = listOf(
        BluetoothMessage(
            message = "Hey! Ready to test Bluetooth chat?",
            senderName = "Me",
            isFromLocalUser = true
        ),
        BluetoothMessage(
            message = "Yep, let's do it 👋",
            senderName = "Other",
            isFromLocalUser = false
        )
    )
)
