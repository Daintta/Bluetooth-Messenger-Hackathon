package com.example.dainttabluetoothhackathon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dainttabluetoothhackathon.data.BluetoothDevice
import com.example.dainttabluetoothhackathon.data.BluetoothMessage
import com.example.dainttabluetoothhackathon.data.BluetoothUIState
import com.example.dainttabluetoothhackathon.domain.BluetoothController
import com.example.dainttabluetoothhackathon.domain.ConnectionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUIState())
    val state: StateFlow<BluetoothUIState>
        get() = _state.asStateFlow()

    private var deviceConnectionJob: Job? = null

    init {
        bluetoothController.scannedDevices.onEach { devices ->
            _state.update { it.copy(scannedDevices = devices) }
        }.launchIn(viewModelScope)

        bluetoothController.pairedDevices.onEach { devices ->
            _state.update { it.copy(pairedDevices = devices) }
        }.launchIn(viewModelScope)

        bluetoothController.isScanning.onEach { isScanning ->
            _state.update { it.copy(isScanning = isScanning) }
        }.launchIn(viewModelScope)

        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update { it.copy(errorMessage = error) }
        }.launchIn(viewModelScope)

        bluetoothController.incomingMessages.onEach { message ->
            _state.update { it.copy(messages = it.messages + message) }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: BluetoothDevice) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy(isConnecting = false, isConnected = false) }
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true, isServerStarted = true) }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.sendMessage(message)
            if (bluetoothMessage) {
                _state.update {
                    it.copy(
                        messages = it.messages + BluetoothMessage(
                            message = message,
                            senderName = "Me",
                            isFromLocalUser = true
                        )
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                is ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}
