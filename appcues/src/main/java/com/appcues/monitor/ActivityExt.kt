package com.appcues.monitor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

internal fun Activity.intentActionFinish(): String {
    return componentName.className + ".ACTION.FINISH"
}

internal fun Activity.sendLocalBroadcast(intent: Intent) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
}

internal fun Activity.registerLocalReceiver(broadcastReceiver: BroadcastReceiver, intentFilter: IntentFilter) {
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
}

internal fun Activity.unregisterLocalReceiver(broadcastReceiver: BroadcastReceiver) {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
}
