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
                var showSearch by remember { mutableStateOf(false) }
                val submittedQuery = remember { SearchParameters() }
                var searchError by remember { mutableStateOf<String?>(null) }
                Scaffold(
                    topBar = {
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
                                                .height(30.dp)   // you control exact size
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
                                    Box(modifier = Modifier.padding(top = 12.dp), contentAlignment = Alignment.CenterStart) {
                                        Text(
                                            text = "Event Search"
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                if (showSearch) {
                                    IconButton(
                                        modifier = Modifier.padding(top = 2.dp),
                                        onClick = {
                                            showSearch = false
                                            // Reset parameters
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
                                Box(modifier = Modifier.padding(2.dp), contentAlignment = Alignment.CenterEnd) {
                                    IconButton(onClick = {
                                        if (!showSearch) {
                                            // First click: open search UI
                                            showSearch = true
                                            // Reset parameters on open
                                            submittedQuery.keywordParam = ""
                                            submittedQuery.locationParam = ""
                                            submittedQuery.distanceParam = "10"
                                            submittedQuery.categoryParam = ""
                                            searchError = null
                                        } else {
                                            // Search UI already visible
                                            if (submittedQuery.keywordParam.isBlank()) {
                                                // Second click with empty text: show error
                                                searchError = "Keyword is required"
                                            } else {
                                                // Valid: clear error and perform search
                                                searchError = null
                                                // make fetch using submittedQuery
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
                ) { innerPadding ->
                    if (showSearch) {
                        SearchScreen(submittedQuery, Modifier.padding(innerPadding))
                    } else {
                        FavoriteEventsScreen(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

