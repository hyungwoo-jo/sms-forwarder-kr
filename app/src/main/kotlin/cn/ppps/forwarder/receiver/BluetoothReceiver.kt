package cn.ppps.forwarder.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import cn.ppps.forwarder.App
import cn.ppps.forwarder.service.BluetoothScanService
import cn.ppps.forwarder.utils.ACTION_RESTART
import cn.ppps.forwarder.utils.ACTION_START
import cn.ppps.forwarder.utils.ACTION_STOP
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.TASK_CONDITION_BLUETOOTH
import cn.ppps.forwarder.utils.TaskWorker
import cn.ppps.forwarder.utils.task.TaskUtils
import cn.ppps.forwarder.workers.BluetoothWorker

@Suppress("PrivatePropertyName", "DEPRECATION")
@SuppressLint("MissingPermission")
class BluetoothReceiver : BroadcastReceiver() {

    private val TAG: String = BluetoothReceiver::class.java.simpleName
    private val handler = Handler()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        try {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> handleActionFound(intent)
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> handleDiscoveryFinished(context)
                BluetoothAdapter.ACTION_STATE_CHANGED -> handleStateChanged(context, intent)
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> handleScanModeChanged()
                BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED -> handleLocalNameChanged()
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> handleConnectionStateChanged()
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> handleBondStateChanged()
                BluetoothDevice.ACTION_ACL_CONNECTED -> handleAclConnected(context, intent)
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> handleAclDisconnected(context, intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling Bluetooth action: ${intent.action}", e)
        }
    }

    private fun handleActionFound(intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
        if (ActivityCompat.checkSelfPermission(App.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        if (SettingUtils.bluetoothIgnoreAnonymous && device.name.isNullOrEmpty()) return

        Log.d(TAG, "Discovered device: ${device.name} - ${device.address}")
        val discoveredDevices = TaskUtils.discoveredDevices
        discoveredDevices[device.address] = device.name ?: ""
        TaskUtils.discoveredDevices = discoveredDevices
    }

    private fun handleDiscoveryFinished(context: Context) {
        Log.d(TAG, "Bluetooth scan finished, discoveredDevices: ${TaskUtils.discoveredDevices}")
        if (TaskUtils.discoveredDevices.isNotEmpty()) {
            handleWorkRequest(context, BluetoothAdapter.ACTION_DISCOVERY_FINISHED, Gson().toJson(TaskUtils.discoveredDevices))
        }

        restartBluetoothService(ACTION_STOP)
        if (SettingUtils.enableBluetooth) {
            Log.d(TAG, "Bluetooth scan finished, restart in ${SettingUtils.bluetoothScanInterval}ms")
            handler.postDelayed({
                restartBluetoothService(ACTION_START)
            }, SettingUtils.bluetoothScanInterval)
        }
    }

    private fun handleStateChanged(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        handleBluetoothStateChanged(state)
        handleWorkRequest(context, BluetoothAdapter.ACTION_STATE_CHANGED, state.toString())
    }

    private fun handleScanModeChanged() {
        if (SettingUtils.enableBluetooth) {
            restartBluetoothService()
        }
    }

    private fun handleLocalNameChanged() {
        // handle local name changed logic
    }

    private fun handleConnectionStateChanged() {
        // handle connection state changed logic
    }

    private fun handleBondStateChanged() {
        // handle bond state changed logic
    }

    private fun handleAclConnected(context: Context, intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
        Log.d(TAG, "Connected device: ${device.name} - ${device.address}")
        TaskUtils.connectedDevices[device.address] = device.name
        handleWorkRequest(context, BluetoothDevice.ACTION_ACL_CONNECTED, Gson().toJson(mutableMapOf(device.address to device.name)))
    }

    private fun handleAclDisconnected(context: Context, intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
        Log.d(TAG, "Disconnected device: ${device.name} - ${device.address}")
        TaskUtils.connectedDevices.remove(device.address)
        handleWorkRequest(context, BluetoothDevice.ACTION_ACL_DISCONNECTED, Gson().toJson(mutableMapOf(device.address to device.name)))
    }

    private fun handleBluetoothStateChanged(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_OFF -> {
                Log.d(TAG, "BluetoothAdapter.STATE_OFF")
                TaskUtils.bluetoothState = state
                restartBluetoothService(ACTION_STOP)
                handler.removeCallbacksAndMessages(null)
            }

            BluetoothAdapter.STATE_ON -> {
                Log.d(TAG, "BluetoothAdapter.STATE_ON")
                TaskUtils.bluetoothState = state
                if (SettingUtils.enableBluetooth) {
                    restartBluetoothService(ACTION_START)
                }
            }

            BluetoothAdapter.STATE_TURNING_ON -> {
                Log.d(TAG, "BluetoothAdapter.STATE_TURNING_ON")
            }

            BluetoothAdapter.STATE_TURNING_OFF -> {
                Log.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF")
            }

            BluetoothAdapter.STATE_CONNECTING -> {
                Log.d(TAG, "BluetoothAdapter.STATE_CONNECTING")
            }

            BluetoothAdapter.STATE_CONNECTED -> {
                Log.d(TAG, "BluetoothAdapter.STATE_CONNECTED")
            }

            BluetoothAdapter.STATE_DISCONNECTING -> {
                Log.d(TAG, "BluetoothAdapter.STATE_DISCONNECTING")
            }

            BluetoothAdapter.STATE_DISCONNECTED -> {
                Log.d(TAG, "BluetoothAdapter.STATE_DISCONNECTED")
            }
        }
    }

    private fun restartBluetoothService(action: String = ACTION_RESTART) {
        Log.d(TAG, "restartBluetoothService, action: $action")
        val serviceIntent = Intent(App.context, BluetoothScanService::class.java)
        serviceIntent.action = action
        App.context.startService(serviceIntent)
    }

    private fun handleWorkRequest(context: Context, action: String, msg: String) {
        val request = OneTimeWorkRequestBuilder<BluetoothWorker>()
            .setInputData(
                workDataOf(
                    TaskWorker.CONDITION_TYPE to TASK_CONDITION_BLUETOOTH,
                    TaskWorker.ACTION to action,
                    TaskWorker.MSG to msg,
                )
            ).build()
        WorkManager.getInstance(context).enqueue(request)
    }
}