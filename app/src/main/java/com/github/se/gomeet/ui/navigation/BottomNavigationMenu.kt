package com.github.se.gomeet.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.gomeet.ui.theme.Cyan
import com.github.se.gomeet.ui.theme.DarkModeBackground
import com.github.se.gomeet.ui.theme.SomewhatWhite
import com.github.se.gomeet.ui.theme.TranslucentCyan

@Composable
fun BottomNavigationMenu(
    onTabSelect: (String) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding().height(80.dp),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
    )
    {
        tabList.forEach { destination ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = getIconForRoute(destination.route),
                        contentDescription = destination.textId,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(destination.textId) },
                selected = destination.route == selectedItem,
                onClick = { onTabSelect(destination.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Cyan,
                    selectedIconColor = Cyan,
                    indicatorColor = TranslucentCyan
                )
            )
        }
    }
}

@Preview
@Composable
fun PreviewBottomNavigationMenu() {
  BottomNavigationMenu(
      onTabSelect = {},
      tabList = TOP_LEVEL_DESTINATIONS,
      selectedItem = Route.CREATE,
  )
}