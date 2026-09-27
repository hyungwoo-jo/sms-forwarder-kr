package cn.ppps.forwarder.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
object BluetoothUtils {

    /**
     */
    fun hasBluetoothPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
    }

    /**
     */
    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     */
    fun hasBluetoothCapability(context: Context): Boolean {
        if (!hasBluetoothPermission(context)) {
            Log.e("BluetoothUtils", "hasBluetoothCapability: no bluetooth permission")
            return false
        }

        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }
}
