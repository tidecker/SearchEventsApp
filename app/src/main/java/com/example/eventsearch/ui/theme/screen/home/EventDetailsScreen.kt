package com.example.eventsearch.ui.theme.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.model.SearchEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: SearchEvent,
    onBack: () -> Unit,
    favoritesList: List<FavoriteEvent>,
) {
    val tabs = listOf("Details", "Artist", "Venue")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> DetailsTab(
                event = event,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
            1 -> ArtistTab(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
            2 -> VenueTab(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}

/* --- Tabs --- */
@Composable
private fun DetailsTab(event: SearchEvent, modifier: Modifier = Modifier) {
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
                        text = "Event",
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

                DetailRow(label = "Date", value = event.dateTimeLabel)
                Spacer(Modifier.height(8.dp))

                DetailRow(
                    label = "Artists",
                    // TODO: replace with real artists field from event
                    value = "Artist info coming soon"
                )
                Spacer(Modifier.height(8.dp))

                DetailRow(label = "Venue", value = event.venueName)
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { },
                        label = { Text(event.categoryLabel.ifBlank { "Music" }) }
                    )
                    // Add more chips here if you have multiple genres
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Ticket Status",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))

                AssistChip(
                    onClick = { },
                    label = { Text("On Sale") }, // TODO: bind real status
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        labelColor = MaterialTheme.colorScheme.primary
                    )
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
                    model = event.seatmap.staticUrl,
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
private fun ArtistTab(modifier: Modifier = Modifier) {
    // TODO: replace with real Artist content
    Box(modifier = modifier.padding(16.dp)) {
        Text("Artist tab")
    }
}

@Composable
private fun VenueTab(modifier: Modifier = Modifier) {
    // TODO: replace with real Venue content
    Box(modifier = modifier.padding(16.dp)) {
        Text("Venue tab")
    }
}
