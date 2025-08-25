package tools.keystroke.mono.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import tools.keystroke.mono.components.AppContextMenu
import tools.keystroke.mono.components.ContextMenuItem
import tools.keystroke.mono.ui.theme.interFamily
import tools.keystroke.mono.utils.AppInfo
import tools.keystroke.mono.utils.AppManager
import java.util.Locale

enum class SearchPosition { Top, Bottom }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    navController: NavController,
    searchPosition: SearchPosition = SearchPosition.Top,
    horizontalPadding: Dp = 16.dp,
    fontSize: Int = 30,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allApps by remember { mutableStateOf(AppManager.getInstalledApps(context)) }

    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }
    val apps = remember(allApps, searchQuery) {
        val query = searchQuery.text.trim().lowercase(Locale.getDefault())
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

    fun onExit() = navController.popBackStack()

    fun updateAppsList() {
        allApps = AppManager.getInstalledApps(context)
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
                    value = searchQuery,
                    onQueryChange = { searchQuery = it },
                    horizontalPadding = horizontalPadding,
                    onExit = { onExit() },
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
                            item { Spacer(modifier = Modifier.padding(2.dp)) }
                        }

                        itemsIndexed(apps, key = { _, item -> item.packageName }) { idx, app ->
                            var showContextMenu by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                            AppManager.launchApp(
                                                context, app.packageName
                                            )
                                        }, onLongClick = {
                                            showContextMenu = true
                                        }, onLongClickLabel = "App options"
                                        )
                                        .padding(vertical = 10.dp, horizontal = horizontalPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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

                                AppContextMenu(
                                    expanded = showContextMenu,
                                    onDismissRequest = { showContextMenu = false },
                                    items = buildList {
                                        add(
                                            ContextMenuItem(
                                                title = "Open",
                                                onClick = {
                                                    AppManager.launchApp(
                                                        context, app.packageName
                                                    )
                                                },
                                            )
                                        )
                                        add(
                                            ContextMenuItem(
                                                title = "App info",
                                                onClick = {
                                                    AppManager.openAppInfoSettings(
                                                        context, app.packageName
                                                    )
                                                },
                                            )
                                        )
                                        add(
                                            ContextMenuItem(
                                                title = "Uninstall", onClick = {
                                                    if (AppManager.requestUninstall(
                                                            context, app.packageName
                                                        ).isSuccess
                                                    ) {
                                                        updateAppsList()
                                                    }
                                                }, disabled = app.isSystemApp
                                            )
                                        )
                                    })
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
                    value = searchQuery,
                    onQueryChange = { searchQuery = it },
                    horizontalPadding = horizontalPadding,
                    onExit = { onExit() },
                )

                Spacer(modifier = Modifier.padding(10.dp))
            }
        }

    }
}

@Composable
fun SquareIconButton(
    onClick: () -> Unit,
    size: Dp = 72.dp,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    contentDescription: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RectangleShape,
        tonalElevation = 2.dp,
        modifier = Modifier.size(size),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    value: TextFieldValue,
    horizontalPadding: Dp = 16.dp,
    onQueryChange: (TextFieldValue) -> Unit,
    onExit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .padding(end = horizontalPadding), verticalAlignment = Alignment.CenterVertically
    ) {
        SquareIconButton(onClick = onExit)

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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
    }
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
