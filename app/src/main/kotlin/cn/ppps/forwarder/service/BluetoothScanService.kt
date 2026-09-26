package cn.ppps.forwarder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Disabled compatibility component; this edition never scans Bluetooth. */
class BluetoothScanService : Service() {
    companion object { var isRunning = false }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = false
        stopSelf()
        return START_NOT_STICKY
    }
}
