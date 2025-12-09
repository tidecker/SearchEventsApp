package com.example.eventsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eventsearch.ui.theme.EventSearchTheme
import com.example.eventsearch.ui.theme.screen.home.FavoriteEventsScreen
import com.example.eventsearch.ui.theme.screen.home.SearchScreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.eventsearch.data.fetchAutoLocation
import com.example.eventsearch.data.geocodeLocation
import com.example.eventsearch.data.model.SearchEvent
import com.example.eventsearch.data.parseSearchEvents
import com.example.eventsearch.data.remote.EventsApi
import com.example.eventsearch.ui.theme.screen.home.EventDetailsScreen
import kotlinx.coroutines.launch


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
            EventSearchTheme {
                val scope = rememberCoroutineScope()

                var showSearch by remember { mutableStateOf(false) }
                val submittedQuery = remember { SearchParameters() }
                var searchError by remember { mutableStateOf<String?>(null) }

                var searchResults by remember { mutableStateOf<List<SearchEvent>>(emptyList()) }

                var selectedEvent by remember { mutableStateOf<SearchEvent?>(null) }

                Scaffold(
                    topBar = {
                        if (selectedEvent != null) {
                            // --- EVENT DETAILS TOP BAR ---
                            TopAppBar(
                                title = { Text("Event Details") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        selectedEvent = null   // go back to search results
                                        showSearch = true
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
                            // --- YOUR EXISTING SEARCH TOP BAR (UNCHANGED) ---
                            TopAppBar(
                                modifier = Modifier.height(120.dp),
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
                                                onValueChange = {
                                                    submittedQuery.keywordParam = it
                                                    searchError = null
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
                                actions = {
                                    Box(
                                        modifier = Modifier.padding(2.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        IconButton(onClick = {
                                            if (!showSearch) {
                                                showSearch = true
                                                submittedQuery.keywordParam = ""
                                                submittedQuery.locationParam = ""
                                                submittedQuery.distanceParam = "10"
                                                submittedQuery.categoryParam = ""
                                                searchError = null
                                            } else {
                                                if (submittedQuery.keywordParam.isBlank()) {
                                                    searchError = "Keyword is required"
                                                } else {
                                                    searchError = null
                                                    scope.launch {
                                                        try {
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
                                                        }
                                                    }
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
                    when {
                        selectedEvent != null -> {
                            // DETAILS SCREEN
                            EventDetailsScreen(
                                event = selectedEvent!!,
                                onBack = { selectedEvent = null }
                            )
                        }
                        showSearch -> {
                            // SEARCH SCREEN
                            SearchScreen(
                                submittedQuery = submittedQuery,
                                searchResults = searchResults,
                                onEventClick = { selectedEvent = it },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            // HOME / FAVORITES
                            FavoriteEventsScreen(Modifier.padding(innerPadding))
                        }
                    }
                    }
            }
        }
    }
}

