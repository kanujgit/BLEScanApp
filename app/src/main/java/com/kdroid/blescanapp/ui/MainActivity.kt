package com.kdroid.blescanapp.ui

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kdroid.blescanapp.R
import com.kdroid.blescanapp.bluetoothscanner.adapter.BluetoothDeviceAdapter
import com.kdroid.blescanapp.bluetoothscanner.callbacks.BluetoothScannerCallback
import com.kdroid.blescanapp.bluetoothscanner.data.BluetoothDevices
import com.kdroid.blescanapp.bluetoothscanner.manager.BluetoothScanManager
import com.kdroid.blescanapp.databinding.ActivityMainBinding
import com.kdroid.blescanapp.utils.LinearLayoutManagerWrapper
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BluetoothScanManager

    private lateinit var foundDevices: MutableList<BluetoothDevices>

    val sortedBleDevices = mutableListOf<BluetoothDevices>()

    // Create a thread-safe set to track unique addresses
    val uniqueAddresses = ConcurrentHashMap<String, Int>()

    private var permissionsCheck = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Request for necessary permission at runtime
        requestPermissions()


        // RecyclerView handling
        foundDevices = BluetoothDevices.createBluetoothDevicesList()
        val bleAdapter = BluetoothDeviceAdapter(foundDevices)
        binding.recBleDevice.apply {
            adapter = bleAdapter
            layoutManager = LinearLayoutManagerWrapper(context, LinearLayoutManager.VERTICAL, false)
        }

        // BleManager creation
        btManager = getSystemService(BluetoothManager::class.java)

        //check if Bluetooth is supported on the device
        if (btManager.adapter == null) {
            showToast(getString(R.string.ble_unsupport_msg))
            finish()
        }
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        val scanFilters = listOf<ScanFilter>()

        bleScanManager = BluetoothScanManager(
            btManager,
            10000,
            scanSettings,
            scanFilters,
            scanCallback = BluetoothScannerCallback({ scanResult ->
                val address = scanResult?.device?.address
                val name = scanResult?.device?.name
                val rssi = scanResult?.rssi ?: 0
                if (name.isNullOrBlank()) return@BluetoothScannerCallback

                val device =
                    BluetoothDevices(address = address.toString(), name = name, rssi = rssi)
                sortedBleDevices.add(device)

                // Use parallel streams for concurrent processing
                sortedBleDevices.parallelStream().forEach { device ->
                    val isUniqueAddress = uniqueAddresses.putIfAbsent(device.address, 1) == null
                    if (isUniqueAddress) {
                        foundDevices.add(device)
                    }
                }
                // Sort the list based on the RSSI
                foundDevices.sortByDescending { it.rssi }
                foundDevices.forEachIndexed { index, device ->
                    Timber.d("Found device at index $index: $device")
                    bleAdapter.notifyItemInserted(index)
                }
            })
        )

        // Adding the actions the manager must do before and after scanning
        bleScanManager.beforeScanActions.add { binding.btnStartScan.isEnabled = false }
        bleScanManager.beforeScanActions.add {
            foundDevices.size.let {
                foundDevices.clear()
                bleAdapter.notifyItemRangeRemoved(0, it)
            }
        }
        bleScanManager.afterScanActions.add { binding.btnStartScan.isEnabled = true }


        // Adding the onclick listener to the start scan button
        binding.btnStartScan.setOnClickListener {
            Timber.i("onClick event")


            val mBluetoothAdapter = btManager.adapter
            if (mBluetoothAdapter == null) {
                showToast("Device does not supported Bluetooth")
            } else if (!mBluetoothAdapter.isEnabled) {
                showToast("Bluetooth is not enabled :)")
            } else {
                if (!permissionsCheck) bleScanManager.scanBleDevices() else requestPermissions()
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    private fun requestPermissions() {

        // Check if all permissions are granted
        val allPermissionsGranted = permissions.all {
            Timber.d(it)
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED

        }

        // Request permissions if not granted
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            // All permissions are already granted
            permissionsCheck = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            permissionsCheck = if (allPermissionsGranted) {
                false
                // All permissions have been granted
            } else {
                // Permission(s) not granted, finish the app
                showToast("Permission is not granted")
                // Permission(s) not granted
                showPermissionGuide()

                true

            }
        }
    }

    private fun showPermissionGuide() {
        // Display a guide or message to the user explaining the need for permissions
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
            .setMessage("Please grant the necessary permissions to use this app.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false).show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1

        private val permissions = arrayOf(
            BLUETOOTH, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, BLUETOOTH_ADMIN
        ).takeIf { Build.VERSION.SDK_INT < Build.VERSION_CODES.S } ?: arrayOf(
            BLUETOOTH_SCAN, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, BLUETOOTH_CONNECT
        )

    }
}