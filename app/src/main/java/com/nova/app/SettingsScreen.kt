package com.nova.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(state: NovaState) {
    val ctx = LocalContext.current
    val s = state.settings
    val c = MaterialTheme.colorScheme
    val notifOk by rememberNotificationStatus()
    var confirmReset by remember { mutableStateOf(false) }
    val gate = rememberNotifGate {
        s.notificationsEnabled = true
        state.log.add("Notifications enabled")
    }
    val pInfo = remember { ctx.packageManager.getPackageInfo(ctx.packageName, 0) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Settings", "Make Nova yours")

        // THEME
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Theme", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Accents.forEachIndexed { i, a ->
                    val sel = s.accent == i
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(a.primary, a.secondary)))
                            .border(if (sel) 3.dp else 1.dp, if (sel) Color.White else Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable {
                                s.accent = i
                                state.log.add("Theme changed to ${a.name}")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (sel) Icon(Icons.Default.Check, contentDescription = a.name + " selected", tint = Color.Black.copy(alpha = 0.8f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            SwitchRow("Pure black (AMOLED)", "Saves battery on OLED screens", s.amoled) { s.amoled = it }
            SwitchRow("Animations", "Turn off for a calmer, faster interface", s.animations) { s.animations = it }
        }

        // TOUCH INDICATOR
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Touch indicator", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            SwitchRow("Show Touch Indicator", "A green dotted circle follows your finger inside Nova", s.touchEnabled) {
                s.touchEnabled = it
                state.log.add(if (it) "Touch indicator enabled" else "Touch indicator disabled")
            }
            LabeledSlider("Circle size", "${s.touchSize.roundToInt()} dp", s.touchSize, 32f..140f, s.touchEnabled) { s.touchSize = it }
            LabeledSlider("Opacity", "${(s.touchOpacity * 100).roundToInt()}%", s.touchOpacity, 0.2f..1f, s.touchEnabled) { s.touchOpacity = it }
            LabeledSlider("Animation speed", "${(s.touchSpeed * 100).roundToInt()}%", s.touchSpeed, 0f..1f, s.touchEnabled) { s.touchSpeed = it }
            Spacer(Modifier.height(6.dp))
            Text(
                "Touch anywhere in Nova to preview. No overlay permission is needed because the indicator is drawn by Nova itself and only in Nova. It never reads other apps or your screen. To show touches in every app, use Android's Show taps in Developer options (Tools tab).",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
        }

        // NOTIFICATIONS
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Notifications", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            SwitchRow("Allow Nova notifications", "Only used for alerts you trigger", s.notificationsEnabled && notifOk) { on ->
                if (on) gate() else {
                    s.notificationsEnabled = false
                    state.log.add("Notifications disabled")
                }
            }
            Spacer(Modifier.height(8.dp))
            GhostButton("System notification settings", { openNotificationSettings(ctx) }, Modifier.fillMaxWidth())
        }

        // PERMISSION MANAGER
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Permission manager", style = MaterialTheme.typography.titleLarge)
            Text(
                "Nova asks only when a feature needs it. Everything else is never requested.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            PermRow("Notifications", "Used to show alerts you ask for.", if (notifOk) "Allowed" else "Not allowed", notifOk)
            PermRow("Camera", "Nova has no scanning feature. The flashlight needs no camera permission.", "Not used", true)
            PermRow("Microphone", "Nova has no voice or audio feature.", "Not used", true)
            PermRow("Location", "Nova has no location feature.", "Not used", true)
            PermRow("Storage and media", "Nova does not read or save your files.", "Not used", true)
            PermRow("Display over other apps", "Not needed. The touch indicator stays inside Nova.", "Not used", true)
            PermRow("Accessibility service", "Nova does not use one.", "Not used", true)
            PermRow("Battery optimization", "Nova runs nothing in the background.", "Not used", true)
            Spacer(Modifier.height(12.dp))
            GhostButton("Open Android app permissions", { openAppSettings(ctx) }, Modifier.fillMaxWidth())
        }

        // PRIVACY
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Nova has no account, no ads, no analytics and no internet permission. Your settings and activity list are stored only on this phone. Nova never collects passwords, messages, codes or anything from other apps. Uninstalling Nova removes all of it.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            GhostButton("Clear activity history", { state.log.clear(); ctx.toast("Activity cleared") }, Modifier.fillMaxWidth())
        }

        // APP INFO
        GlassCard(Modifier.fillMaxWidth()) {
            Text("App information", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            InfoRow("Version", pInfo.versionName ?: "1.0")
            InfoRow("Package", ctx.packageName)
        }

        GlowButton("Reset all settings", { confirmReset = true }, Modifier.fillMaxWidth())
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset settings?") },
            text = { Text("Theme, touch indicator and notification preferences go back to their defaults. Your activity history is kept.") },
            confirmButton = {
                TextButton(onClick = {
                    s.reset()
                    state.log.add("Settings reset")
                    confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(top = 6.dp)) {
        Row {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(valueText, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, enabled = enabled)
    }
}

@Composable
private fun PermRow(name: String, note: String, status: String, ok: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            StatusChip(status, ok)
        }
        Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
