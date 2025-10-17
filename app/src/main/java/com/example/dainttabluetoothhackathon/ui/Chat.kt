package com.example.dainttabluetoothhackathon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.dainttabluetoothhackathon.ui.theme.ChatTheme
import java.time.LocalDateTime.now
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/* ---------- Models ---------- */

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sentAt: LocalDateTime = now(),
    val isMe: Boolean // TRUE is user
)

/* ---------- Main chat screen ---------- */

@Composable // Jetpack Compose
fun ConversationScreen() {
    val messages = remember { //'remember' keeps in memory
        mutableStateListOf( //compose auto updates UI when list changes
            Message(text = "Hey! Ready to test Bluetooth chat?", isMe = true), // Example
            Message(text = "Yep, let's do it 👋", isMe = false), // Example
        )
    }

    val listState = rememberLazyListState() //controls scroll position
    val scope = rememberCoroutineScope() //runs asynchronous tasks (i.e. animations)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.lastIndex) }
        }
    }

    Scaffold( // layout helper
        bottomBar = { //dock to bottom of screen
            InputBar(onSend = { text -> //onSend callback, text param
                if (text.isNotBlank()) {
                    //send msg
                    messages += Message(text = text.trim(), isMe = true)
                }
            })
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
            items(messages, key = { it -> it.id }) { msg ->
                //defined below in composable sections
                MessageRow(message = msg)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/* ---------- Composables ---------- */

@Composable
fun MessageRow(message: Message) {
    val bubbleColor =
        if (message.isMe) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondary

    val textColor =
        if (message.isMe) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant

    val shape =
        if (message.isMe)
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        else
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Clip
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = message.sentAt.format(DateTimeFormatter.ofPattern("HH:mm")),
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
            textStyle = TextStyle(
                fontSize = 18.sp,
            ),
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
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}

/* ---------- Preview ---------- */

@Preview(showBackground = true)
@Composable
fun PreviewConversation() {
    ChatTheme{
        ConversationScreen()
    }
}