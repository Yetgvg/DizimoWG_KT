package com.example.dizimowg.core.util

import android.content.Context
import android.provider.Settings

fun getAlternativeDeviceId(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown_device"
}
