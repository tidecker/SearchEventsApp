package com.example.eventsearch.ui.theme.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.eventsearch.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventsearch.data.model.EventDetails
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.model.SearchEvent
import com.example.eventsearch.data.parseEventDetails
import com.example.eventsearch.data.remote.EventsApi
import com.example.eventsearch.data.remote.FavoritesApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: SearchEvent,
    onBack: () -> Unit,
    favoritesList: List<FavoriteEvent>,
) {
    val tabs = listOf("Details", "Artist", "Venue")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var eventDetails by remember { mutableStateOf(null as EventDetails?) }

// get event details using event.id
    LaunchedEffect(event.id) {
        try {
            val json = EventsApi.retrofitService.getEventDetails(event.id)
            eventDetails = parseEventDetails(json)
        } catch (e: Exception) {
            // you might want to show an error state here instead of just swallowing it
            eventDetails = null
        }
    }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            Spacer(modifier = Modifier.height(70.dp))

            val tabIcons = listOf(
                painterResource(id = R.drawable.data_info_alert_24px),
                painterResource(id = R.drawable.artist_24px),
                painterResource(id = R.drawable.stadium_24px)
            )
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                painter = tabIcons[index],
                                contentDescription = title
                            )
                        }
                    )
                }
            }
            val details = eventDetails

            when {
                details == null -> {
                    CircularProgressIndicator()
                }
                else -> {
                    when (selectedTabIndex) {
                        0 -> DetailsTab(details)
                        1 -> ArtistTab(details)
                        2 -> VenueTab(details)
                    }
                }
            }
        }
    }
}

/* --- Tabs --- */
@Composable
private fun DetailsTab(event: EventDetails, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // --- Event info card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.name!!,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        IconButton(onClick = { /* TODO open in browser */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = "Open in browser"
                            )
                        }
                        IconButton(onClick = { /* TODO share */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share"
                            )
                        }
                    }
                }

                Divider(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 12.dp)
                )

                DetailRow(
                    label = "Date",
                    value = buildString {
                        event.dates?.start?.localDate?.let { append(it) }
                        event.dates?.start?.localTime?.let { append(" $it") }
                    }
                )
                Spacer(Modifier.height(8.dp))

                val artists = event.embedded?.attractions
                    ?.mapNotNull { it.name }
                    ?.joinToString(", ")
                    ?: "N/A"

                DetailRow(label = "Artists", value = artists)

                Spacer(Modifier.height(8.dp))

                val venueName = event.embedded?.venues?.firstOrNull()?.name ?: "N/A"

                DetailRow(label = "Venue", value = venueName)

                Spacer(Modifier.height(12.dp))

                val genres = event.classifications?.firstOrNull()?.let { c ->
                    listOfNotNull(
                        c.segment?.name,
                        c.genre?.name,
                        c.subGenre?.name,
                        c.type?.name,
                        c.subType?.name
                    ).joinToString(" • ")
                } ?: "N/A"

                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(genres) }
                    )
                    // Add more chips here if you have multiple genres
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Ticket Status",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))

                val statusText = event.dates?.status?.code?.uppercase() ?: "N/A"

                AssistChip(
                    onClick = {},
                    label = { Text(statusText) }
                )
            }
        }

        // --- Seatmap card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                Text(
                    text = "Seatmap",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                )

                AsyncImage(
                    model = event.seatmap?.staticUrl,
                    contentDescription = "Seatmap",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Composable
private fun ArtistTab(event: EventDetails, modifier: Modifier = Modifier) {
    // TODO: replace with real Artist content
    Box(modifier = modifier.padding(16.dp)) {
        Text("Artist tab")
    }
}

@Composable
private fun VenueTab(event: EventDetails,modifier: Modifier = Modifier) {
    // TODO: replace with real Venue content
    Box(modifier = modifier.padding(16.dp)) {
        Text("Venue tab")
    }
}
