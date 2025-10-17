package com.example.dainttabluetoothhackathon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.*

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.dainttabluetoothhackathon.ui.theme.ChatTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dainttabluetoothhackathon.data.BluetoothMessage
import com.example.dainttabluetoothhackathon.data.BluetoothUIState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    navController: NavController,
    state: BluetoothUIState,
    onSendMessage: (String) -> Unit,
    onDisconnect: () -> Unit
) {

    val listState = rememberLazyListState() //controls scroll position
    val scope = rememberCoroutineScope() //runs asynchronous tasks (i.e. animations)

    // Auto-scroll to the newest message when a new one is added
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(state.messages.lastIndex) }
        }
    }

    Scaffold( // layout helper
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White        // background
                ),
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onDisconnect()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = { //dock to bottom of screen
            InputBar(onSend = { text -> //onSend callback, text param
                if (text.isNotBlank()) {
                    //send msg
                    onSendMessage(text.trim())
                }
            }
            )
        },
        modifier = Modifier.fillMaxSize() //fill whole screen
    ) { inner -> //content
        // messages list
        LazyColumn(
            //layout
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            //messages
            //msg param per msg in messages
            items(state.messages) { msg ->
                //defined below in composable sections
                MessageRow(message = msg)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/* ---------- Composables ---------- */

@Composable
fun MessageRow(message: BluetoothMessage) {
    val bubbleColor =
        if (message.isFromLocalUser) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondary

    val textColor =
        if (message.isFromLocalUser) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant

    val shape =
        if (message.isFromLocalUser)
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        else
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)

    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val formattedTime = remember(message.time) {
        val instant = Instant.ofEpochMilli(message.time)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        formatter.format(localDateTime)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromLocalUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Clip
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InputBar(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Type your message") },
            singleLine = false,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.rotate(-40f))
        }
    }
}

/* ---------- Preview ---------- */

@Preview(showBackground = true)
@Composable
fun PreviewConversation() {
    ChatTheme{
        val navController = rememberNavController()
        val state = BluetoothUIState(
            messages = listOf(
                BluetoothMessage("Hey! Ready to test Bluetooth chat?", "Me", true),
                BluetoothMessage("Yep, let's do it 👋", "Other", false)
            )
        )
        ConversationScreen(navController, state, {}, {})
    }
}
