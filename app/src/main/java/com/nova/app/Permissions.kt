package com.nova.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

object Notifier {
    private const val CHANNEL = "nova_alerts"

    @SuppressLint("MissingPermission")
    fun send(ctx: Context, title: String, text: String): Boolean {
        val nm = NotificationManagerCompat.from(ctx)
        if (!nm.areNotificationsEnabled()) return false
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Nova alerts")
                .setDescription("Test alerts and updates from Nova")
                .build()
        )
        val open = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_nova)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(1001, n)
        return true
    }
}

fun openNotificationSettings(ctx: Context) {
    try {
        ctx.startActivity(
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        )
    } catch (e: Exception) {
        openAppSettings(ctx)
    }
}

fun openAppSettings(ctx: Context) {
    try {
        ctx.startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.packageName))
        )
    } catch (e: Exception) {
        ctx.toast("Could not open settings")
    }
}

fun openDeveloperOptions(ctx: Context) {
    try {
        ctx.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
    } catch (e: Exception) {
        ctx.toast("Turn on Developer options first: Settings > About phone > tap Build number 7 times")
    }
}

/** Live notification permission status; refreshes when the user returns to the app. */
@Composable
fun rememberNotificationStatus(): State<Boolean> {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val status = remember { mutableStateOf(NotificationManagerCompat.from(ctx).areNotificationsEnabled()) }
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                status.value = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    return status
}

/**
 * Returns a function that runs [onGranted] once notifications are allowed.
 * If permission is missing, it first explains why, then shows the Android permission prompt.
 */
@Composable
fun rememberNotifGate(onGranted: () -> Unit): () -> Unit {
    val ctx = LocalContext.current
    var showWhy by remember { mutableStateOf(false) }
    val latest by rememberUpdatedState(onGranted)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) latest() else ctx.toast("Notifications stay off. You can allow them later in Settings > Permission manager.")
    }

    if (showWhy) {
        AlertDialog(
            onDismissRequest = { showWhy = false },
            title = { Text("Allow notifications?") },
            text = { Text("Nova needs notification access to show you alerts, like the test alert you just asked for. It does not read your other notifications. You can turn this off any time.") },
            confirmButton = {
                TextButton(onClick = {
                    showWhy = false
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showWhy = false }) { Text("Not now") } },
        )
    }

    return {
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            latest()
        } else if (Build.VERSION.SDK_INT >= 33) {
            showWhy = true
        } else {
            ctx.toast("Notifications are blocked for Nova in system settings")
            openNotificationSettings(ctx)
        }
    }
}

@Composable
fun rememberSendTestNotification(state: NovaState): () -> Unit {
    val ctx = LocalContext.current
    val gate = rememberNotifGate {
        if (Notifier.send(ctx, "Nova", "Systems online. Notifications are working.")) {
            state.log.add("Test notification sent")
        }
    }
    return {
        if (!state.settings.notificationsEnabled) {
            ctx.toast("Notifications are switched off in Nova settings")
        } else {
            gate()
        }
    }
}
