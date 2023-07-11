package com.kdroid.blescanapp.bluetoothscanner.data

/**
 * A class that represents a bluetooth list and attribute of the bluetooth.
 */
data class BluetoothDevices(
    val name: String, val address: String, val rssi: Int,

    ) {

    companion object {
        fun createBluetoothDevicesList(): MutableList<BluetoothDevices> {
            return mutableListOf()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        val otherDevice = other as BluetoothDevices

        return address == otherDevice.address
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }
}