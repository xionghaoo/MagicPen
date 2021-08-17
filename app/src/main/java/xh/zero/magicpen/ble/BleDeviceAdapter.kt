package xh.zero.magicpen.ble

import android.view.View
import android.widget.TextView
import xh.zero.magicpen.PlainListAdapter
import xh.zero.magicpen.R

class BleDeviceAdapter(val items: ArrayList<BleDevice>) : PlainListAdapter<BleDevice>(items) {
    override fun itemLayoutId(): Int = R.layout.list_item_ble_device

    override fun bindView(v: View, item: BleDevice, position: Int) {
        val tv = v as TextView
        tv.text = "${item.name} - ${item.macAddr}"
    }

    fun addDevice(device: BleDevice) {
        items.add(device)
        notifyDataSetChanged()
    }
}