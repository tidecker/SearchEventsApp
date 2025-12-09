package com.example.eventsearch.ui.theme.screen.home

import android.content.Intent
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.remote.FavoritesApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.lang.Integer.parseInt
import java.time.LocalDateTime
import java.util.Date

@Composable
fun FavoriteEventsScreen(
    favoritesList: List<FavoriteEvent>,
    statusMessage: String,
    onFavoriteClick: (FavoriteEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(currentDate.format(formatter))
        Spacer(Modifier.height(24.dp))
        Text("Favorites", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        when {
            statusMessage.isNotEmpty() -> {
                Text(statusMessage)
            }
            favoritesList.isEmpty() -> {
                NoFavoritesCard()
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoritesList) { event ->
                        FavoriteEventRow(
                            event = event,
                            onFavoriteClick = onFavoriteClick
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current

            Text(
                text = "Powered by TicketMaster",
                fontStyle = FontStyle.Italic,
                modifier = Modifier.clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://www.ticketmaster.com".toUri()
                    )
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun FavoriteEventRow(
    event: FavoriteEvent,
    onFavoriteClick: (FavoriteEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFavoriteClick(event) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: event image
        AsyncImage(
            model = event.imageUrl,
            contentDescription = event.name,
            modifier = Modifier
                .size(56.dp)
                .padding(end = 12.dp)
        )

        // Middle: title + date
        Column(
            modifier = Modifier.weight(1f)   // this gives the middle column the width
        ) {
            Text(
                text = event.name,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .fillMaxWidth()          // constrain horizontally
                    .basicMarquee()          // now marquee can run
            )
            Text(
                text = event.date,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(  // this gives the middle column the width
        ) {
            RelativeTimeText(event.id!!)
        }

        // Right: chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open details"
        )
    }
}

fun formatElapsed(ms: Long): String {
    val sec = ms / 1000
    val min = ms / (60_000)
    val hr  = ms / (3_600_000)
    val day = ms / (86_400_000)

    return when {
        sec < 60      -> "$sec seconds ago"
        min == 1L     -> "a minute ago"
        min < 60      -> "$min minutes ago"
        hr == 1L      -> "an hour ago"
        hr < 24       -> "$hr hours ago"
        day == 1L     -> "a day ago"
        else          -> "$day days ago"
    }
}

@Composable
fun RelativeTimeText(eventId: String) {
    // compute once
    val createdAtMs = remember(eventId) {
        val hex = eventId.substring(0, 8)
        val seconds = hex.toLong(16)
        seconds * 1000
    }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(createdAtMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)   // update every second
        }
    }

    val elapsed = formatElapsed(now - createdAtMs)

    Text(
        text = elapsed,
        style = MaterialTheme.typography.bodySmall
    )
}


