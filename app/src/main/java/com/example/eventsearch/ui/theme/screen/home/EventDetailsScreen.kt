package com.example.eventsearch.ui.theme.screen.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.eventsearch.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventsearch.data.model.EventDetails
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.model.SearchEvent
import com.example.eventsearch.data.model.SpotifyAlbum
import com.example.eventsearch.data.model.SpotifyDetails
import com.example.eventsearch.data.parseEventDetails
import com.example.eventsearch.data.parseSpotifyInfo
import com.example.eventsearch.data.remote.EventsApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventDetailsScreen(
    event: SearchEvent,
    onBack: () -> Unit,
    favoritesList: List<FavoriteEvent>,
) {
    val tabs = listOf("Details", "Artist", "Venue")
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()
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
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> DetailsTab(details)
                            1 -> ArtistTab(details)
                            2 -> VenueTab(details)
                        }
                    }
                }
            }

        }
    }
}

/* --- Tabs --- */
@Composable
private fun DetailsTab(event: EventDetails, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
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
                    val rawStatus = event.dates?.status?.code?.uppercase() ?: ""
                    val isOffSale = rawStatus == "OFFSALE"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Event",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (!isOffSale) {
                            Row {
                                // Open in browser
                                IconButton(onClick = {
                                    val url = event.ticketmasterUrl ?: event.url
                                    if (!url.isNullOrBlank()) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                        contentDescription = "Open in browser"
                                    )
                                }

                                // Share
                                IconButton(onClick = {
                                    val url = event.ticketmasterUrl ?: event.url
                                    if (!url.isNullOrBlank()) {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, url)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Share"
                                    )
                                }
                            }
                        }
                    }


                    HorizontalDivider(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 12.dp)
                    )

                    DetailRow(
                        label = "Date",
                        value = run {
                            val date = event.dates?.start?.localDate ?: return@run "N/A"
                            val time = event.dates?.start?.localTime

                            val localDate = java.time.LocalDate.parse(date)

                            if (time != null) {
                                val localTime = java.time.LocalTime.parse(time)
                                val dt = java.time.LocalDateTime.of(localDate, localTime)
                                dt.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a"))
                            } else {
                                localDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))

                    val artists = event.embedded?.attractions
                        ?.mapNotNull { it.name }
                        ?.joinToString(", ")

                    if (!artists.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        DetailRow(label = "Artists", value = artists)
                    }


                    val venueName = event.embedded?.venues?.firstOrNull()?.name

                    if (!venueName.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        DetailRow(label = "Venue", value = venueName)
                    }


                    val genreList = event.classifications
                        ?.firstOrNull()
                        ?.let { c ->
                            listOfNotNull(
                                c.segment?.name,
                                c.genre?.name,
                                c.subGenre?.name,
                                c.type?.name,
                                c.subType?.name
                            )
                        }
                        ?.filter { it.isNotBlank() && it != "Undefined" }
                        ?.distinct()
                        ?: emptyList()

                    if (genreList.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Genres",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(Modifier.height(4.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            genreList.forEach { g ->
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            g,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))


                    Text(
                        text = "Ticket Status",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(4.dp))

                    val statusText = when (rawStatus) {
                        "ONSALE" -> "On Sale"
                        "OFFSALE" -> "Off Sale"
                        "CANCELED" -> "Cancelled"
                        else -> rawStatus.lowercase().replaceFirstChar { it.uppercase() }
                    }

                    val containerColor = when (rawStatus) {
                        "ONSALE" -> MaterialTheme.colorScheme.primary
                        "OFFSALE" -> MaterialTheme.colorScheme.secondary
                        "CANCELED" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val labelColor = when (rawStatus) {
                        "ONSALE", "OFFSALE", "CANCELED" ->
                            MaterialTheme.colorScheme.onPrimary
                        else ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }


                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = statusText,
                                color = labelColor
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = containerColor
                        )
                    )
                }
            }

            val seatmapUrl = event.seatmap?.staticUrl

            if (!seatmapUrl.isNullOrBlank()) {
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
                            model = seatmapUrl,
                            contentDescription = "Seatmap",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                .heightIn(max = 360.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
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
    val artistName = event.embedded?.attractions?.firstOrNull()?.name

    Log.d("ArtistTab", "COMPOSE ArtistTab, artistName = $artistName")

    if (artistName == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text("No artist data")
        }
        return
    }

    val context = LocalContext.current

    var spotifyInfo by remember { mutableStateOf<SpotifyDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(artistName) {
        Log.d("ArtistTab", "LaunchedEffect start for $artistName")
        try {
            val json = EventsApi.retrofitService.getSpotifyData(artistName)
            Log.d("ArtistTab", "Spotify JSON: $json")
            spotifyInfo = parseSpotifyInfo(json, artistName)
            Log.d("ArtistTab", "Parsed spotifyInfo = $spotifyInfo")
        } catch (e: Exception) {
            Log.e("ArtistTab", "Spotify error", e)
            error = "No artist data"
            spotifyInfo = null
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                CircularProgressIndicator()
            }
        }

        error != null || spotifyInfo == null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text("No artist data")
            }
        }

        else -> {
            val artistInfo = spotifyInfo?.artist

            Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                // --- Top artist card ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 8.dp),
                    ) {
                        AsyncImage(
                            model = artistInfo?.imageUrl ?: event.images.firstOrNull()?.url,
                            contentDescription = artistInfo?.name,
                            modifier = Modifier
                                .size(72.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            // Name + external icon INLINE
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = artistInfo?.name!!,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        artistInfo.spotifyUrl?.let { url ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                        contentDescription = "Open in Spotify"
                                    )
                                }
                            }

                            // Followers + popularity
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "Followers: ${"%,d".format(artistInfo?.followers)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Popularity: ${artistInfo?.popularity}%",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        artistInfo?.genres?.forEach { g ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        g,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                }

                Spacer(Modifier.height(24.dp))

                // --- Albums header ---
                Text(
                    text = "Albums",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))

                // two-per-row grid
                spotifyInfo?.albums
                    ?.sortedByDescending { it.releaseDate }    // NEW: sort newest → oldest
                    ?.chunked(2)
                    ?.forEach { rowAlbums ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowAlbums.forEach { album ->
                                AlbumCard(
                                    album = album,
                                    onClick = {
                                        album.spotifyUrl?.let { url ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowAlbums.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

            }
        }
    }
}

@Composable
private fun AlbumCard(album: SpotifyAlbum, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            AsyncImage(
                model = album.imageUrl,
                contentDescription = album.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = album.name!!,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                val formattedDate =
                    album.releaseDate?.let { raw ->
                        try {
                            val parsed = java.time.LocalDate.parse(raw)
                            parsed.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        } catch (e: Exception) {
                            raw // fallback to original
                        }
                    } ?: ""

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall
                )
                if (album.totalTracks != null) {
                    Text(
                        text = "${album.totalTracks} tracks",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueTab(event: EventDetails, modifier: Modifier = Modifier) {
    val venue = event.embedded?.venues?.firstOrNull()

    if (venue == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No venue information for this event.")
        }
        return
    }

    val context = LocalContext.current
    val imageUrl = venue.images?.firstOrNull()?.url
        //?: event.images.firstOrNull()?.url

    val addressLine = buildString {
        venue.address?.line1?.let { append(it) }
        val city = venue.city?.name
        val state = venue.state?.stateCode
        val countryName = venue.country?.name
        val country = when (countryName) {
            "United States Of America" -> "US"
            else -> countryName
        }
        val cityStateCountry = listOfNotNull(city, state, country).joinToString(", ")
        if (cityStateCountry.isNotEmpty()) {
            if (isNotEmpty()) append(", ")
            append(cityStateCountry)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()       // whole pager page
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)     // fixed at top
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(top= 8.dp, start = 16.dp, end = 16.dp)) {

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = venue.name ?: "Venue image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = venue.name ?: "Unknown venue",
                            style = MaterialTheme.typography.titleLarge
                        )

                        if (addressLine.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = addressLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = {
                        val url = venue.url ?: event.ticketmasterUrl ?: event.url
                        if (!url.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open venue in browser"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
