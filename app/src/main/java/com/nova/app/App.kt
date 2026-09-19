package com.nova.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NovaApp(state: NovaState) {
    val s = state.settings
    CompositionLocalProvider(LocalAnimations provides s.animations) {
        NovaTheme(s) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                TouchIndicatorHost(s) { NovaScaffold(state) }
            }
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
private fun NovaScaffold(state: NovaState) {
    val s = state.settings
    val c = MaterialTheme.colorScheme
    val anim = LocalAnimations.current
    var tab by rememberSaveable { mutableStateOf(0) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(c.background, if (s.amoled) Color(0xFF05070D) else Color(0xFF111A33))))
    ) {
        // Soft glowing orbs in the background
        Canvas(Modifier.fillMaxSize()) {
            val a = Offset(size.width * 0.9f, size.height * 0.08f)
            val b = Offset(size.width * 0.05f, size.height * 0.85f)
            drawCircle(
                brush = Brush.radialGradient(listOf(c.primary.copy(alpha = 0.20f), Color.Transparent), center = a, radius = size.width * 0.8f),
                radius = size.width * 0.8f,
                center = a,
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(c.secondary.copy(alpha = 0.18f), Color.Transparent), center = b, radius = size.width * 0.8f),
                radius = size.width * 0.8f,
                center = b,
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = c.onBackground,
            floatingActionButton = { QuickTouchFab(state) },
            bottomBar = { NovaNavBar(tab) { tab = it } },
        ) { pad ->
            AnimatedContent(
                targetState = tab,
                modifier = Modifier.padding(pad),
                label = "pages",
                transitionSpec = {
                    if (anim) {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(tween(320)) { w -> w / 6 * dir } + fadeIn(tween(320))) togetherWith
                            (slideOutHorizontally(tween(260)) { w -> -w / 6 * dir } + fadeOut(tween(200)))
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                },
            ) { t ->
                when (t) {
                    0 -> HomeScreen(state) { tab = it }
                    1 -> ToolsScreen(state)
                    2 -> ActivityScreen(state)
                    else -> SettingsScreen(state)
                }
            }
        }
    }
}

@Composable
private fun QuickTouchFab(state: NovaState) {
    val s = state.settings
    ExtendedFloatingActionButton(
        onClick = {
            s.touchEnabled = !s.touchEnabled
            state.log.add(if (s.touchEnabled) "Touch indicator enabled" else "Touch indicator disabled")
        },
        containerColor = if (s.touchEnabled) TouchGreen else MaterialTheme.colorScheme.primary,
        contentColor = Color(0xFF04121A),
        shape = RoundedCornerShape(20.dp),
        icon = { Icon(if (s.touchEnabled) Icons.Default.Check else Icons.Default.Add, contentDescription = null) },
        text = { Text(if (s.touchEnabled) "Touch: ON" else "Touch: OFF", fontWeight = FontWeight.SemiBold) },
    )
}

@Composable
private fun NovaNavBar(selected: Int, onSelect: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    val items = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Tools", Icons.Default.Build),
        NavItem("Activity", Icons.AutoMirrored.Filled.List),
        NavItem("Settings", Icons.Default.Settings),
    )
    NavigationBar(containerColor = Color.Black.copy(alpha = 0.55f), contentColor = c.onBackground) {
        items.forEachIndexed { i, item ->
            NavigationBarItem(
                selected = selected == i,
                onClick = { onSelect(i) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = c.primary,
                    selectedTextColor = c.primary,
                    indicatorColor = c.primary.copy(alpha = 0.18f),
                    unselectedIconColor = c.onSurfaceVariant,
                    unselectedTextColor = c.onSurfaceVariant,
                ),
            )
        }
    }
}
