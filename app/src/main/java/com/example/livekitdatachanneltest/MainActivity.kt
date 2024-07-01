@file:OptIn(Beta::class)

package com.example.livekitdatachanneltest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.livekitdatachanneltest.ui.theme.LiveKitDataChannelTestTheme
import io.livekit.android.LiveKit
import io.livekit.android.annotations.Beta
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.util.LoggingLevel
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {

    lateinit var room: Room

    val url = "wss://example.com"
    val token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        room = LiveKit.create(this.applicationContext)
        LiveKit.loggingLevel = LoggingLevel.DEBUG

        setContent {
            LaunchedEffect(key1 = Unit) {
                room.connect(url, token)

                room.events.collect {
                    Log.e("LiveKitEvent", "events: $it")

                    when (it) {
                        is RoomEvent.TranscriptionReceived, is RoomEvent.DataReceived -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Received data event: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {}
                    }
                }
            }

            LiveKitDataChannelTestTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val connectionState by room::state.flow.collectAsState(initial = Room.State.CONNECTING)
                    val participants by room::remoteParticipants.flow
                        .map { it.values.toList().plus(room.localParticipant) }
                        .collectAsState(initial = emptyList())

                    LazyColumn {
                        item {
                            Text(text = "Connection state: $connectionState")
                        }
                        item {
                            Text(text = "Participants: ${participants.count()}")
                        }
                        items(participants) {
                            Text(text = it::identity.flow.collectAsState().value?.value ?: "")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LiveKitDataChannelTestTheme {
        Greeting("Android")
    }
}