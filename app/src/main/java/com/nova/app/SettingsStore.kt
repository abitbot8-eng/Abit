package com.nova.app

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** All user settings. Stored only on this device (SharedPreferences). */
class SettingsStore(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("nova_settings", Context.MODE_PRIVATE)

    private val _accent = mutableIntStateOf(sp.getInt("accent", 0))
    private val _amoled = mutableStateOf(sp.getBoolean("amoled", true))
    private val _animations = mutableStateOf(sp.getBoolean("animations", true))
    private val _touchEnabled = mutableStateOf(sp.getBoolean("touch_enabled", false))
    private val _touchSize = mutableFloatStateOf(sp.getFloat("touch_size", 64f))
    private val _touchOpacity = mutableFloatStateOf(sp.getFloat("touch_opacity", 0.9f))
    private val _touchSpeed = mutableFloatStateOf(sp.getFloat("touch_speed", 0.5f))
    private val _notifications = mutableStateOf(sp.getBoolean("notifications", true))

    var accent: Int
        get() = _accent.intValue
        set(v) { _accent.intValue = v; sp.edit().putInt("accent", v).apply() }

    var amoled: Boolean
        get() = _amoled.value
        set(v) { _amoled.value = v; sp.edit().putBoolean("amoled", v).apply() }

    var animations: Boolean
        get() = _animations.value
        set(v) { _animations.value = v; sp.edit().putBoolean("animations", v).apply() }

    var touchEnabled: Boolean
        get() = _touchEnabled.value
        set(v) { _touchEnabled.value = v; sp.edit().putBoolean("touch_enabled", v).apply() }

    var touchSize: Float
        get() = _touchSize.floatValue
        set(v) { _touchSize.floatValue = v; sp.edit().putFloat("touch_size", v).apply() }

    var touchOpacity: Float
        get() = _touchOpacity.floatValue
        set(v) { _touchOpacity.floatValue = v; sp.edit().putFloat("touch_opacity", v).apply() }

    var touchSpeed: Float
        get() = _touchSpeed.floatValue
        set(v) { _touchSpeed.floatValue = v; sp.edit().putFloat("touch_speed", v).apply() }

    var notificationsEnabled: Boolean
        get() = _notifications.value
        set(v) { _notifications.value = v; sp.edit().putBoolean("notifications", v).apply() }

    fun reset() {
        sp.edit().clear().apply()
        _accent.intValue = 0
        _amoled.value = true
        _animations.value = true
        _touchEnabled.value = false
        _touchSize.floatValue = 64f
        _touchOpacity.floatValue = 0.9f
        _touchSpeed.floatValue = 0.5f
        _notifications.value = true
    }
}

data class LogEntry(val time: Long, val text: String)

/** Simple in-app activity history, stored only on this device. */
class ActivityLog(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("nova_log", Context.MODE_PRIVATE)
    val entries = mutableStateListOf<LogEntry>()

    init {
        val raw = sp.getString("log", "") ?: ""
        raw.split("\n").filter { it.isNotBlank() }.forEach { line ->
            val i = line.indexOf('|')
            if (i > 0) {
                val t = line.substring(0, i).toLongOrNull()
                if (t != null) entries.add(LogEntry(t, line.substring(i + 1)))
            }
        }
    }

    fun add(text: String) {
        val clean = text.replace('\n', ' ').replace('|', '/')
        entries.add(0, LogEntry(System.currentTimeMillis(), clean))
        while (entries.size > 50) entries.removeAt(entries.lastIndex)
        persist()
    }

    fun clear() {
        entries.clear()
        persist()
    }

    private fun persist() {
        val s = entries.joinToString("\n") { "${it.time}|${it.text}" }
        sp.edit().putString("log", s).apply()
    }
}

class NovaState(context: Context) {
    val settings = SettingsStore(context)
    val log = ActivityLog(context)

    var torchOn by mutableStateOf(false)
        private set

    /** Flashlight uses the torch API, which does not need the camera permission. */
    fun toggleTorch(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = cm.cameraIdList.firstOrNull { cid ->
                val ch = cm.getCameraCharacteristics(cid)
                ch.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    ch.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: return false
            val next = !torchOn
            cm.setTorchMode(id, next)
            torchOn = next
            log.add(if (next) "Flashlight turned on" else "Flashlight turned off")
            true
        } catch (e: Exception) {
            false
        }
    }
}
