package com.kdroid.blescanapp.bluetoothscanner.callbacks

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import timber.log.Timber

class BluetoothScannerCallback(
    private val onScanResultAction: (ScanResult?) -> Unit = {},
    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {},
    private val onScanFailedAction: (Int) -> Unit = {},
) : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        Timber.d("BluetoothScannerCallback - onScanResults called $result" )
        onScanResultAction(result)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        Timber.d("BluetoothScannerCallback - onBatchScanResults called $results")
        onBatchScanResultAction(results)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Timber.e("BluetoothScannerCallback - scan failed with error '$errorCode'")
        onScanFailedAction(errorCode)
    }
}