package com.kdroid.blescanapp.bluetoothscanner.manager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import com.kdroid.blescanapp.bluetoothscanner.callbacks.BluetoothScannerCallback
import timber.log.Timber

/**
 * Manager for bluetooth LE scanning
 */
class BluetoothScanManager(
    btManager: BluetoothManager,
    private val scanPeriod: Long = DEFAULT_SCAN_PERIOD,
    private val scanSettings: ScanSettings,
    private val scanFilter: List<ScanFilter>,
    private val scanCallback: BluetoothScannerCallback = BluetoothScannerCallback(),
) {
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner

    var beforeScanActions: MutableList<() -> Unit> = mutableListOf()
    var afterScanActions: MutableList<() -> Unit> = mutableListOf()

    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())

    /**
     * BluetoothScanner is responsible for scanning Bluetooth LE devices for a specified duration.
     * The scan will automatically stop after [scanPeriod] seconds.
     * Note: This class does not handle permission checks. Ensure that the required permissions are granted prior to usage.
     */

    @SuppressLint("MissingPermission")
    fun scanBleDevices() {

        fun stopScan() {
            Timber.d("scan stop")
            scanning = false
            bleScanner.stopScan(scanCallback)


            executeAfterScanActions()
        }

        // scans for bluetooth LE devices
        if (scanning) {
            stopScan()
        } else {
            // stops scanning after scanPeriod
            handler.postDelayed({ stopScan() }, scanPeriod)

            executeBeforeScanActions()

            // starts scanning
            Timber.d("scan start")
            scanning = true
            bleScanner.startScan(scanFilter,scanSettings,scanCallback)
        }
    }

    private fun executeBeforeScanActions() {
        executeListOfFunctions(beforeScanActions)
    }

    private fun executeAfterScanActions() {
        executeListOfFunctions(afterScanActions)
    }

    companion object {

        /**
         * Constant holding the default maximum scan period time, in milliseconds.
         * This represents the maximum duration for which scanning will be performed.
         */

        const val DEFAULT_SCAN_PERIOD: Long = 12000

        /**
         * This Function is executes a list of functions.
         */
        private fun executeListOfFunctions(toExecute: List<() -> Unit>) {
            toExecute.forEach {
                it()
            }
        }
    }
}