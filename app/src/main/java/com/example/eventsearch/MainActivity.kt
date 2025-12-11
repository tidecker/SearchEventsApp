package com.example.eventsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eventsearch.ui.theme.EventSearchTheme
import com.example.eventsearch.ui.theme.screen.home.FavoriteEventsScreen
import com.example.eventsearch.ui.theme.screen.home.SearchScreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.example.eventsearch.data.fetchAutoLocation
import com.example.eventsearch.data.geocodeLocation
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.model.SearchEvent
import com.example.eventsearch.data.model.Seatmap
import com.example.eventsearch.data.parseSearchEvents
import com.example.eventsearch.data.remote.EventsApi
import com.example.eventsearch.data.remote.FavoritesApi
import com.example.eventsearch.data.remote.RemoveFavoriteRequest
import com.example.eventsearch.ui.theme.screen.home.EventDetailsScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class SearchParameters(
    initialKeyword: String = "",
    initialLocation: String = "",
    initialDistance: String = "10",
    initialCategory: String = ""
)
 {
    var keywordParam by mutableStateOf(initialKeyword)
    var locationParam by mutableStateOf(initialLocation)
    var distanceParam by mutableStateOf(initialDistance)
    var categoryParam by mutableStateOf(initialCategory)
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isSearchLoading by remember { mutableStateOf(false) }

            EventSearchTheme {
                val scope = rememberCoroutineScope()
                var showSearch by remember { mutableStateOf(false) }
                val submittedQuery = remember { SearchParameters() }
                var searchError by remember { mutableStateOf<String?>(null) }
                var searchResults by remember { mutableStateOf<List<SearchEvent>>(emptyList()) }
                var selectedEvent by remember { mutableStateOf<SearchEvent?>(null) }
                var favoritesList by remember { mutableStateOf<List<FavoriteEvent>>(emptyList()) }
                var statusMessage by remember { mutableStateOf("Loading...") }
                var cameFromFavorites by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()
                var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current


                LaunchedEffect(Unit) {
                    statusMessage = "Loading..."
                    try {
                        favoritesList = FavoritesApi.retrofitService.getFavorites()
                        statusMessage = ""
                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                    }
                }

                LaunchedEffect(showSearch) {
                    if (showSearch && selectedEvent == null) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }

                // favorite api call
                /******************************************************************************
                 * TOGGLE FAVORITES
                 ******************************************************************************/
                fun isFavorite(event: SearchEvent): Boolean =
                    favoritesList.any { it.eventId == event.id }   // use the matching id fields
                fun toggleFavorite(event: SearchEvent) {
                    scope.launch {
                        try {
                            if (isFavorite(event)) {
                                // remove on server
                                FavoritesApi.retrofitService.removeFavorite(
                                    RemoveFavoriteRequest(event.id)
                                )
                            } else {
                                // add on server
                                val favorite = FavoriteEvent(
                                    id = null,
                                    eventId = event.id,
                                    name = event.name,
                                    date = event.dateTimeLabel,
                                    genre = event.categoryLabel,
                                    imageUrl = event.imageUrl,
                                    venue = event.venueName,
                                    isFavorite = true
                                )
                                FavoritesApi.retrofitService.addFavorite(favorite)
                            }

                            // always refresh local list from server
                            favoritesList = FavoritesApi.retrofitService.getFavorites()
                            statusMessage = ""
                        } catch (e: Exception) {
                            statusMessage = "Error updating favorite: ${e.message}"
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        if (selectedEvent != null) {
                            /**
                            * --- EVENT DETAILS TOP BAR ---
                            **/
                            TopAppBar(
                                /******************************************************************************
                                 * TOP BAR TITLE/TEXT (EVENT NAME)
                                 ******************************************************************************/
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedEvent?.name ?: "",
                                            maxLines = 1,
                                            overflow = TextOverflow.Visible,
                                            modifier = Modifier
                                                .weight(1f)
                                                .basicMarquee(),
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        IconButton(onClick = { toggleFavorite(selectedEvent!!)   }) {
                                            Icon(
                                                if (isFavorite(selectedEvent!!)) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                contentDescription = "Favorite"
                                            )
                                        }
                                    }
                                },
                                /******************************************************************************
                                 * BACK BUTTON (GO BACK TO SEARCH RESULTS)
                                 ******************************************************************************/
                                navigationIcon = {
                                    IconButton(onClick = {
                                        selectedEvent = null

                                        if (cameFromFavorites) {
                                            // go back to favorites screen
                                            showSearch = false
                                        } else {
                                            // go back to search screen
                                            showSearch = true
                                        }
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )

                        } else {
                            /**
                             * --- SEARCH EVENT TOP BAR ---
                             **/
                            TopAppBar(
                                modifier = Modifier.height(120.dp),
                                /******************************************************************************
                                 * TOP BAR TITLE/TEXT
                                 ******************************************************************************/
                                title = {
                                    if (showSearch) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            BasicTextField(
                                                value = submittedQuery.keywordParam,
                                                onValueChange = { new ->
                                                    submittedQuery.keywordParam = new
                                                    searchError = null

                                                    if (new.length < 1) {
                                                        suggestions = emptyList()
                                                    } else {
                                                        coroutineScope.launch {
                                                            try {
                                                                val json = EventsApi.retrofitService.getSuggestions(new)
                                                                suggestions = Json.decodeFromString<List<String>>(json)
                                                            } catch (e: Exception) {
                                                                suggestions = emptyList()
                                                            }
                                                        }
                                                    }
                                                },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                decorationBox = { innerTextField ->
                                                    if (submittedQuery.keywordParam.isEmpty()) {
                                                        Text(
                                                            "Search events...",
                                                            style = MaterialTheme.typography.titleLarge,
                                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                    innerTextField()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 11.dp)
                                                    .height(30.dp)
                                                    .focusRequester(focusRequester)
                                            )

                                            if (searchError != null) {
                                                Text(
                                                    text = searchError!!,
                                                    color = Color.Red,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.padding(top = 12.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text("Event Search")
                                        }
                                    }
                                },
                                /******************************************************************************
                                 * BACK BUTTON
                                 ******************************************************************************/
                                navigationIcon = {
                                    if (showSearch) {
                                        IconButton(
                                            modifier = Modifier.padding(top = 2.dp),
                                            onClick = {
                                                showSearch = false
                                                submittedQuery.keywordParam = ""
                                                submittedQuery.locationParam = ""
                                                submittedQuery.distanceParam = "10"
                                                submittedQuery.categoryParam = ""
                                                searchError = null
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                },
                                /******************************************************************************
                                 * SEARCH BUTTON
                                 ******************************************************************************/
                                actions = {
                                    Box(
                                        modifier = Modifier.padding(2.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        IconButton(onClick = {
                                            suggestions = emptyList()

                                            // 1) First click just opens the search bar
                                            if (!showSearch) {
                                                showSearch = true
                                                submittedQuery.keywordParam = ""
                                                submittedQuery.locationParam = ""
                                                submittedQuery.distanceParam = "10"
                                                submittedQuery.categoryParam = ""
                                                searchError = null
                                                return@IconButton
                                            }

                                            // 2) If keyword is blank, show red error and DO NOT search
                                            if (submittedQuery.keywordParam.isBlank()) {
                                                searchError = "Keyword is required"
                                                // optional: clear results when invalid
                                                // searchResults = emptyList()
                                                return@IconButton
                                            }

                                            // 3) Otherwise run the search
                                            searchError = null
                                            // 3) Otherwise run the search
                                            searchError = null
                                            scope.launch {
                                                try {
                                                    isSearchLoading = true          // <--- start spinner
                                                    searchResults = emptyList()     // <--- clear old results

                                                    val (lat, lng) =
                                                        if (submittedQuery.locationParam.isBlank()) {
                                                            fetchAutoLocation()
                                                        } else {
                                                            geocodeLocation(submittedQuery.locationParam)
                                                        }

                                                    val json = EventsApi.retrofitService.searchEvents(
                                                        keyword = submittedQuery.keywordParam,
                                                        category = "all",
                                                        distance = submittedQuery.distanceParam.ifBlank { "10" },
                                                        lat = lat,
                                                        lng = lng
                                                    )

                                                    searchResults = parseSearchEvents(json)
                                                } catch (e: Exception) {
                                                    searchError = "Search failed: ${e.message}"
                                                } finally {
                                                    isSearchLoading = false         // <--- stop spinner (success or error)
                                                }
                                            }

                                        }) {
                                            Icon(Icons.Default.Search, contentDescription = "Search")
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {

                        /******************************************************************************
                         * SCREEN SWITCHING
                         ******************************************************************************/
                        when {
                            selectedEvent != null -> {
                                // DETAILS SCREEN
                                EventDetailsScreen(
                                    event = selectedEvent!!,
                                    onBack = { selectedEvent = null },
                                    favoritesList = favoritesList,
                                )
                            }
                            showSearch -> {
                                // SEARCH SCREEN
                                SearchScreen(
                                    submittedQuery = submittedQuery,
                                    searchResults = searchResults,
                                    onEventClick = {
                                        selectedEvent = it
                                        cameFromFavorites = false
                                    },
                                    toggleFavorite = { toggleFavorite(it) },
                                    isFavorite = { isFavorite(it) },
                                    modifier = Modifier.padding(innerPadding),
                                    isLoading = isSearchLoading   // <--- add this
                                )
                            }
                            else -> {
                                // HOME / FAVORITES
                                FavoriteEventsScreen(
                                    favoritesList = favoritesList,
                                    statusMessage = statusMessage,
                                    onFavoriteClick = { fav ->
                                        selectedEvent = SearchEvent(
                                            id = fav.eventId,
                                            name = fav.name,
                                            categoryLabel = fav.genre,
                                            dateTimeLabel = fav.date,
                                            imageUrl = fav.imageUrl,
                                            venueName = fav.venue,
                                            sortKey = null,                     // we don't have this in favorites
                                            seatmap = Seatmap(staticUrl = "")   // placeholder; or real URL if you have it
                                        )
                                        cameFromFavorites = true
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        /******************************************************************************
                         * SEARCH SUGGESTIONS
                         ******************************************************************************/
                        if (showSearch && suggestions.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(1f)          // whole overlay on top
                            ) {
                                // transparent background to detect outside taps
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            suggestions = emptyList()
                                        }
                                )


                                // your existing dropdown card
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 49.dp)
                                        .offset(y = 72.dp)
                                ) {
                                    Column {
                                        suggestions.take(5).forEach { term ->
                                            Text(
                                                text = term,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        submittedQuery.keywordParam = term
                                                        suggestions = emptyList()
                                                    }
                                                    .padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionsDropdown(
    suggestions: List<String>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            suggestions.forEach { term ->
                Text(
                    text = term,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(term) }
                        .padding(16.dp)
                )
            }
        }
    }
}

