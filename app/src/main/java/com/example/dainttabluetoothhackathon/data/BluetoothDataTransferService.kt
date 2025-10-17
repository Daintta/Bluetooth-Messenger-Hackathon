package com.example.dainttabluetoothhackathon.data

import android.bluetooth.BluetoothSocket
import com.example.dainttabluetoothhackathon.data.BluetoothMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(private val socket: BluetoothSocket) {

    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw IOException("Failed to read from input stream")
                }

                emit(
                    BluetoothMessage(
                        message = String(buffer, 0, byteCount),
                        senderName = socket.remoteDevice.name ?: "Unknown device",
                        isFromLocalUser = false
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(message.toByteArray())
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
}
