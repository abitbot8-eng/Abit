package com.nova.app

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun formatTime(t: Long): String = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(t))

@Composable
fun HomeScreen(state: NovaState, go: (Int) -> Unit) {
    val ctx = LocalContext.current
    val s = state.settings
    val c = MaterialTheme.colorScheme
    val notifOk by rememberNotificationStatus()
    val sendTest = rememberSendTestNotification(state)
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Nova", greeting)

        GlassCard(Modifier.fillMaxWidth(), glow = c.primary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Orb()
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("All systems ready", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Everything runs on this device. Nothing leaves your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    StatusChip(if (s.touchEnabled) "Touch indicator on" else "Touch indicator off", s.touchEnabled)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassCard(Modifier.weight(1f)) {
                Text("Alerts", style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(if (notifOk && s.notificationsEnabled) "Ready" else "Off", style = MaterialTheme.typography.titleLarge)
            }
            GlassCard(Modifier.weight(1f)) {
                Text("Animations", style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(if (s.animations) "On" else "Off", style = MaterialTheme.typography.titleLarge)
            }
        }

        SectionTitle("Quick actions")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(Icons.Default.Notifications, "Test alert", "Send a notification", Modifier.weight(1f)) { sendTest() }
            ActionTile(
                Icons.Default.Star,
                "Flashlight",
                if (state.torchOn) "Tap to turn off" else "Tap to turn on",
                Modifier.weight(1f),
            ) {
                if (!state.toggleTorch(ctx)) ctx.toast("Flashlight is not available on this device")
            }
        }
        ActionTile(Icons.Default.Place, "Touch playground", "Try the green touch indicator", Modifier.fillMaxWidth()) { go(1) }

        SectionTitle("Recent activity")
        GlassCard(Modifier.fillMaxWidth()) {
            val recent = state.log.entries.take(3)
            if (recent.isEmpty()) {
                Text("Nothing yet. Try a quick action above.", style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
            } else {
                recent.forEachIndexed { i, e ->
                    if (i > 0) Spacer(Modifier.height(10.dp))
                    Text(e.text, style = MaterialTheme.typography.titleMedium)
                    Text(formatTime(e.time), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            GhostButton("See all activity", { go(2) }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ToolsScreen(state: NovaState) {
    val ctx = LocalContext.current
    val c = MaterialTheme.colorScheme
    val sendTest = rememberSendTestNotification(state)
    var taps by rememberSaveable { mutableStateOf(0) }
    val battery = remember {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Tools", "Everything Nova can do right now")

        GlassCard(Modifier.fillMaxWidth()) {
            CardHeader(Icons.Default.Notifications, "Notification test", "Send yourself a test alert")
            Text(
                "Android will ask for notification permission only when you press the button below.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            GlowButton("Send test notification", { sendTest() }, Modifier.fillMaxWidth())
        }

        GlassCard(Modifier.fillMaxWidth()) {
            CardHeader(Icons.Default.Star, "Flashlight", "Uses the camera LED. No camera permission needed.")
            GlowButton(
                if (state.torchOn) "Turn flashlight off" else "Turn flashlight on",
                { if (!state.toggleTorch(ctx)) ctx.toast("Flashlight is not available on this device") },
                Modifier.fillMaxWidth(),
            )
        }

        GlassCard(Modifier.fillMaxWidth()) {
            CardHeader(Icons.Default.Place, "Touch playground", "Turn on the touch indicator, then tap and drag")
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, c.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) { detectTapGestures { taps++ } },
                contentAlignment = Alignment.Center,
            ) {
                Text("Tap or drag here\nTaps: $taps", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "The indicator works inside Nova and needs no permission. To see touches in every app, use Android's built-in Show taps option.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            GhostButton("Open Developer options", { openDeveloperOptions(ctx) }, Modifier.fillMaxWidth())
        }

        GlassCard(Modifier.fillMaxWidth()) {
            CardHeader(Icons.Default.Info, "This device", "Read-only information")
            InfoRow("Model", "${Build.MANUFACTURER} ${Build.MODEL}")
            InfoRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            InfoRow("Battery", "$battery%")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActivityScreen(state: NovaState) {
    val c = MaterialTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScreenTitle("Activity", "What you did in Nova. Stored only on this phone.")
                if (state.log.entries.isNotEmpty()) {
                    GhostButton("Clear activity", { state.log.clear() })
                }
            }
        }
        if (state.log.entries.isEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("No activity yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Use a tool or change a setting and it will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.log.entries) { e ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(c.primary))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(e.text, style = MaterialTheme.typography.titleMedium)
                            Text(formatTime(e.time), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
