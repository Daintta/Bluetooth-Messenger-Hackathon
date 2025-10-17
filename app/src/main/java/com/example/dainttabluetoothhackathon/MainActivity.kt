package com.example.dainttabluetoothhackathon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.dainttabluetoothhackathon.ui.ConversationScreen
import com.example.dainttabluetoothhackathon.ui.theme.ChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatTheme {
                Surface(color = MaterialTheme.colorScheme.background){
                    ConversationScreen()
                }
            }
        }
    }
}