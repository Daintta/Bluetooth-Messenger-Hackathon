package com.example.dainttabluetoothhackathon

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dainttabluetoothhackathon.data.AndroidBluetoothController
import com.example.dainttabluetoothhackathon.ui.BluetoothViewModel
import com.example.dainttabluetoothhackathon.ui.ConversationScreen
import com.example.dainttabluetoothhackathon.ui.MenuScreen
import com.example.dainttabluetoothhackathon.ui.theme.ChatTheme

class MainActivity : ComponentActivity() {

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Not needed */ }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if(canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        setContent {
            ChatTheme {
                val navController = rememberNavController()
                val viewModel = viewModel<BluetoothViewModel>(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return BluetoothViewModel(AndroidBluetoothController(applicationContext)) as T
                        }
                    }
                )
                val state by viewModel.state.collectAsState()

                if (!isBluetoothEnabled) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Bluetooth is disabled") },
                        text = { Text("Please enable Bluetooth to use this app.") },
                        confirmButton = {
                            Button(onClick = {
                                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            }) {
                                Text("Enable")
                            }
                        }
                    )
                }

                LaunchedEffect(key1 = state.isConnected) {
                    if(state.isConnected) {
                        navController.navigate("chat")
                    }
                }


                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(
                        navController = navController,
                        startDestination = "menu" // 👈 first screen shown
                    ) {
                        composable("menu") { MenuScreen(
                            navController = navController,
                            state = state,
                            onStartScan = viewModel::startScan,
                            onStopScan = viewModel::stopScan,
                            onStartServer = viewModel::waitForIncomingConnections,
                            onDeviceClick = viewModel::connectToDevice
                        ) }
                        composable("chat") { ConversationScreen(
                            navController = navController,
                            state = state,
                            onSendMessage = viewModel::sendMessage,
                            onDisconnect = viewModel::disconnectFromDevice
                        ) }
                    }
                }
            }
        }
    }
}
