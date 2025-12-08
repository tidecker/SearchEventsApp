package com.example.eventsearch.ui.theme.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.eventsearch.R
import com.example.eventsearch.SearchParameters

@Composable
fun SearchScreen(submittedQuery: SearchParameters, modifier: Modifier = Modifier) {

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

    Column( modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().weight(0.05f).background(MaterialTheme.colorScheme.primary)) {
            Column(modifier = Modifier.weight(0.66f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = submittedQuery.locationParam,
                            onValueChange = { submittedQuery.locationParam = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (submittedQuery.locationParam.isEmpty()) {
                                    Text(
                                        text = "Current Location",
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onPrimary)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(200.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = "Location",
                                            modifier = Modifier.size(28.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Current Location")
                                    }
                                },
                                onClick = {
                                    submittedQuery.locationParam = ""
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
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
                            if (new.all { it.isDigit() }) {
                                submittedQuery.distanceParam = new        // you can type to change distance
                            }
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
        Row(modifier = Modifier.fillMaxWidth().weight(0.06f).background(MaterialTheme.colorScheme.primary)){
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
                                    .background(color = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(2.dp))
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
                            submittedQuery.categoryParam = categories[index]
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
        Row(modifier = Modifier.fillMaxWidth().weight(0.89f)) {
            Text(
                text = submittedQuery.keywordParam,
                modifier = Modifier
                    .padding(top = 40.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
