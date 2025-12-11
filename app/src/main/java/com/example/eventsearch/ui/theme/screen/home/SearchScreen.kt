package com.example.eventsearch.ui.theme.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.example.eventsearch.R
import com.example.eventsearch.SearchParameters
import com.example.eventsearch.data.model.FavoriteEvent
import com.example.eventsearch.data.model.SearchEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Composable
fun SearchScreen(
    submittedQuery: SearchParameters,
    searchResults: List<SearchEvent>,
    onEventClick: (SearchEvent) -> Unit,
    toggleFavorite: (SearchEvent) -> Unit,
    isFavorite: (SearchEvent) -> Boolean,
    modifier: Modifier = Modifier,
    isLoading: Boolean           // <--- new param
){

    val categories = listOf(
        "All",
        "Music",
        "Sports",
        "Arts & Theater",
        "Film",
        "Miscellaneous"
    )
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var locationSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLocSuggestLoading by remember { mutableStateOf(false) }

// local text + dropdown visibility for location
    var locationText by remember { mutableStateOf(submittedQuery.locationParam) }
    var showLocDropdown by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            focusManager.clearFocus()
        }) {

        // MAIN CONTENT (unchanged)
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.weight(0.66f)) {
                    /******************************************************************************
                     * LOCATION TEXT FIELD
                     ******************************************************************************/
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            BasicTextField(
                                value = locationText,
                                onValueChange = { new ->
                                    locationText = new
                                    submittedQuery.locationParam = new

                                    // control dropdown visibility
                                    locationSuggestions = emptyList()
                                    showLocDropdown = new.isNotBlank()

                                    if (new.length >= 1) {
                                        isLocSuggestLoading = true
                                        scope.launch {
                                            val result = fetchPlaceSuggestions(new)
                                            delay(100)
                                            locationSuggestions = result
                                            isLocSuggestLoading = false
                                        }
                                    } else {
                                        isLocSuggestLoading = false
                                    }
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (locationText.isEmpty()) {
                                        Text(
                                            text = "Current Location",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        IconButton(onClick = { showLocDropdown = !showLocDropdown }) {
                            Icon(
                                imageVector = if (showLocDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(0.34f)
                        .padding(top = 5.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    /******************************************************************************
                     * DISTANCE TEXT FIELD
                     ******************************************************************************/
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Spacer(modifier = Modifier.width(5.dp))

                        IconButton(
                            onClick = { /* TODO: change distance when icon pressed */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_range_24px),   // double arrow
                                contentDescription = "Distance",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        BasicTextField(
                            value = submittedQuery.distanceParam,
                            onValueChange = { new ->
                                // keep only digits
                                val digits = new.filter { it.isDigit() }

                                // convert to int or fallback to 1
                                val num = digits.toIntOrNull() ?: 1

                                // clamp to range 1–1000
                                val fixed = num.coerceIn(1, 1000)

                                submittedQuery.distanceParam = fixed.toString()
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        Text(
                            text = "mi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }

            }
            /******************************************************************************
             * CATEGORY SELECT FIELD
             ******************************************************************************/
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedCategoryIndex < tabPositions.size) {
                            val currentTab = tabPositions[selectedCategoryIndex]

                            Box( // Wrapper box to handle positioning and centering
                                Modifier
                                    .tabIndicatorOffset(currentTab)
                                    .height(3.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box( // The actual indicator bar
                                    Modifier
                                        .fillMaxWidth(0.5f) // 50% width
                                        .fillMaxHeight()
                                        .background(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f)
                ) {
                    categories.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedCategoryIndex == index,
                            onClick = {
                                selectedCategoryIndex = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        )
                    }
                }
            }

            /******************************************************************************
             * SEARCH RESULTS DISPLAY
             ******************************************************************************/
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val selectedCategory = categories[selectedCategoryIndex]

                val filteredResults = if (selectedCategory == "All") {
                    searchResults
                } else {
                    searchResults.filter { event ->
                        event.categoryLabel == selectedCategory
                    }
                }
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    filteredResults.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No events found")
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredResults) { event ->
                                SearchResultCard(
                                    event = event,
                                    onClick = { onEventClick(event) },
                                    isFavorite = isFavorite(event),
                                    toggleFavorite = toggleFavorite
                                )
                            }
                        }
                    }
                }

            }
        }

        // OVERLAY DROPDOWN – floats over tabs/results, pushed down under TopBar + filters
        if (showLocDropdown && (isLocSuggestLoading || locationSuggestions.isNotEmpty())) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 160.dp, start = 10.dp, end = 100.dp) // move up/down with this
                    .zIndex(10f)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                submittedQuery.locationParam = ""
                                locationText = ""
                                showLocDropdown = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Current Location")
                    }

                    if (isLocSuggestLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 8.dp)
                            )

                            Text("Searching...")
                        }
                    }

                    locationSuggestions
                        .take(5)
                        .forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        submittedQuery.locationParam = suggestion
                                        locationText = suggestion
                                        showLocDropdown = false
                                    }
                                    .padding(12.dp)
                            )
                        }
                }
            }
        }
    }
}

private const val GOOGLE_MAPS_API_KEY =
    "AIzaSyACnOgTCfnLXpO6-eeFAtk4j8ttVomjfjc"

suspend fun fetchPlaceSuggestions(input: String): List<String> =
    withContext(Dispatchers.IO) {
        val encodedInput = URLEncoder.encode(input, "UTF-8")
        val urlStr =
            "https://maps.googleapis.com/maps/api/place/autocomplete/json" +
                    "?input=$encodedInput&types=(cities)&key=$GOOGLE_MAPS_API_KEY"

        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val response = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()

        val json = JSONObject(response)
        val predictions = json.optJSONArray("predictions") ?: return@withContext emptyList()

        val list = mutableListOf<String>()
        for (i in 0 until predictions.length()) {
            val obj = predictions.getJSONObject(i)
            val description = obj.optString("description", "")
            if (description.isNotBlank()) list.add(description)
        }
        list
    }
