package xh.zero.magicpen.ble

import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.Button
import android.widget.TextView
import xh.zero.magicpen.Configs
import xh.zero.magicpen.PlainListAdapter
import xh.zero.magicpen.R

class BleDeviceAdapter(
    val items: ArrayList<BluetoothDevice>,
    private val onItemClick: (device: BluetoothDevice) -> Unit
) : PlainListAdapter<BluetoothDevice>(items) {
    override fun itemLayoutId(): Int = R.layout.list_item_ble_device

    override fun bindView(v: View, item: BluetoothDevice, position: Int) {
        val tv = v.findViewById<TextView>(R.id.tv_ble_info)
        if (item.address == Configs.DEVICE_MAC_ADDRESS) {
            tv.text = "${item.name} - ${item.address}"
        }

        v.findViewById<Button>(R.id.btn_connect).setOnClickListener {
            onItemClick(item)
        }
    }

    fun addDevice(device: BluetoothDevice) {
        items.add(device)
        notifyDataSetChanged()
    }
}