package com.example.eventsearch.ui.theme.screen.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.remote.FavoritesApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri

@Composable
fun FavoriteEventsScreen(modifier: Modifier = Modifier) {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    var statusMessage by remember { mutableStateOf("Loading...") }
    var favoritesList by remember { mutableStateOf<List<FavoriteEvent>>(emptyList()) }

    LaunchedEffect(Unit) {
        statusMessage = "Loading..."
        try {
            favoritesList = FavoritesApi.retrofitService.getFavorites()
            statusMessage = ""
        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
        }
    }

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
                        FavoriteEventCard(event)
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