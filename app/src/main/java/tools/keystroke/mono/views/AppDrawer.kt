package tools.keystroke.mono.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tools.keystroke.mono.ui.theme.interFamily
import tools.keystroke.mono.utils.AppInfo
import tools.keystroke.mono.utils.AppManager
import java.util.Locale

enum class SearchPosition { Top, Bottom }

@Composable
fun AppDrawer(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    searchPosition: SearchPosition = SearchPosition.Top,
    horizontalPadding: Dp = 16.dp,
    fontSize: Int = 30,
    showIcons: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allApps = remember { AppManager.getInstalledApps(context) }

    var searchQuery by remember { mutableStateOf("") }
    val apps = remember(allApps, searchQuery) {
        val query = searchQuery.trim().lowercase(Locale.getDefault())
        if (query.isEmpty()) {
            return@remember allApps
        }

        allApps.filter {
            it.label.lowercase(Locale.getDefault()).contains(query) || it.packageName.lowercase(
                Locale.getDefault()
            ).contains(query)
        }
    }

    val listState = rememberLazyListState()
    val indexMap by remember(apps) {
        mutableStateOf(buildLetterIndex(apps))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        Column(Modifier.fillMaxSize()) {
            // MARK: top search bar
            if (searchPosition == SearchPosition.Top) {
                SearchBar(
                    value = TextFieldValue(searchQuery),
                    onQueryChange = { searchQuery = it.text },
                    horizontalPadding = horizontalPadding
                )
            }

            Box(
                modifier = modifier.fillMaxSize()
            ) {
                Column(Modifier.fillMaxSize()) {
                    // MARK: App list
                    LazyColumn(
                        state = listState, modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        if (searchPosition == SearchPosition.Top) {
                            item { Spacer(modifier = Modifier.padding(6.dp)) }
                        }

                        itemsIndexed(apps, key = { _, item -> item.packageName }) { idx, app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { AppManager.launchApp(context, app.packageName) }
                                    .padding(vertical = 10.dp, horizontal = horizontalPadding),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = interFamily,
                                    fontSize = fontSize.sp,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.padding(10.dp)) }
                    }
                }

                // MARK: Jump bar
                JumpBar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onLetterChosen = { letter ->
                        val index = indexMap[letter] ?: return@JumpBar
                        scope.launch {
                            listState.scrollToItem(index)
                        }
                    })
            }

            // MARK: bottom search bar
            if (searchPosition == SearchPosition.Bottom) {
                SearchBar(
                    value = TextFieldValue(searchQuery),
                    onQueryChange = { searchQuery = it.text },
                    horizontalPadding = horizontalPadding
                )

                Spacer(modifier = Modifier.padding(10.dp))
            }
        }

    }
}

@Composable
private fun SearchBar(
    value: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    horizontalPadding: Dp = 16.dp,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = {
            Text(
                "Search apps",
                fontFamily = interFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
    )
}

private val Letters = ('A'..'Z').map { it.toString() }

@Composable
private fun JumpBar(
    modifier: Modifier = Modifier, onLetterChosen: (String) -> Unit
) {
    var boxHeightPx by remember { mutableFloatStateOf(0f) }
    val letterHeightPx by remember(boxHeightPx) {
        mutableFloatStateOf(if (boxHeightPx == 0f) 0f else boxHeightPx / Letters.size)
    }

    Column(
        modifier = modifier
            .width(32.dp)
            .fillMaxHeight()
            .padding(vertical = 32.dp)
            .onGloballyPositioned { boxHeightPx = it.size.height.toFloat() }
            .pointerInput(Unit) {
                // tap to jump
                detectTapGestures { offset ->
                    val index = (offset.y / letterHeightPx).toInt().coerceIn(0, Letters.lastIndex)
                    onLetterChosen(Letters[index])
                }
            }
            .pointerInput(Unit) {
                // drag to scrub
                detectDragGestures { change, _ ->
                    val y = change.position.y.coerceIn(0f, boxHeightPx)
                    val index = (y / letterHeightPx).toInt().coerceIn(0, Letters.lastIndex)
                    onLetterChosen(Letters[index])
                }
            }
            .background(Color.Transparent),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Letters.forEach { letter ->
            Text(
                text = letter,
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

private fun buildLetterIndex(list: List<AppInfo>): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    for ((i, app) in list.withIndex()) {
        val first = app.label.firstOrNull()?.uppercaseChar() ?: '#'
        val key = if (first in 'A'..'Z') first.toString() else "#"
        map.putIfAbsent(key, i)
    }
    // ensure every A–Z maps to something (fallback to closest next/prev)
    var lastSeenIndex: Int? = null
    ('A'..'Z').forEach { c ->
        val k = c.toString()
        if (map[k] != null) lastSeenIndex = map[k]
        else if (lastSeenIndex != null) map[k] = lastSeenIndex!!
    }
    return map
}
