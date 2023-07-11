package com.kdroid.blescanapp.bluetoothscanner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kdroid.blescanapp.R
import com.kdroid.blescanapp.bluetoothscanner.data.BluetoothDevices

class BluetoothDeviceAdapter(private val devices: List<BluetoothDevices>) :
    RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.item_view_device, parent, false)
        return ViewHolder(deviceView)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: BluetoothDeviceAdapter.ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.tv_device_name)
    }
}