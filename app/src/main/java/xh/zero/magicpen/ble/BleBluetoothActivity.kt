package xh.zero.magicpen.ble

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import xh.zero.magicpen.Configs
import xh.zero.magicpen.DrawView
import xh.zero.magicpen.ToastUtil
import xh.zero.magicpen.databinding.ActivityBleBluetoothBinding
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and

class BleBluetoothActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val RC_PERMISSION_LOCATION = 2
        private const val SCAN_PERIOD = 120_000L

        private const val TAG = "BleBluetoothActivity"
    }

    private lateinit var binding: ActivityBleBluetoothBinding

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var mScanning: Boolean = false

    private val handler = Handler()
    private lateinit var bleDeviceAdapter: BleDeviceAdapter

    var bluetoothGatt: BluetoothGatt? = null

    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            // 扫描结果
            if (device.address == Configs.DEVICE_MAC_ADDRESS) {
                binding.tvStatus.text = "状态：发现设备 ${device.address}"
                bleDeviceAdapter.addDevice(device)
            }
        }
    }

//    private var connectionState = STATE_DISCONNECTED

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = ACTION_GATT_CONNECTED
//                    connectionState = STATE_CONNECTED
//                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " + bluetoothGatt?.discoverServices())
                    runOnUiThread {
                        binding.tvStatus.text = "状态：已连接到GATT服务器，开启服务发现"
                        refreshConnectedDevice()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = ACTION_GATT_DISCONNECTED
//                    connectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
//                    broadcastUpdate(intentAction)

                    runOnUiThread {
                        AlertDialog.Builder(this@BleBluetoothActivity)
                            .setMessage("设备已断开连接！")
                            .show()
                        binding.tvStatus.text = "状态：GATT服务器连接已断开"
                        close()
                    }

                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            runOnUiThread {
                binding.tvStatus.text = "状态：已连接, 发现服务列表"
            }

            Log.d(TAG, "onServicesDiscovered: ${gatt.services.size}, ${status}")
            gatt.services.forEach { service ->
                Log.d(TAG, "uuid: ${service.uuid}, characteristic: ${service.characteristics.size}, ${service.characteristics}")
            }
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }

//            gatt.writeCharacteristic(BluetoothGattCharacteristic(, , BluetoothGattCharacteristic.PROPERTY_NOTIFY))
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d(TAG, "onCharacteristicRead: characteristic, ${characteristic.uuid}")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }

        // Characteristic notification
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val result = bytesToHex(characteristic.value)
            Log.d(TAG, "特征值: ${result}")
            runOnUiThread {
                binding.tvStatus.text = "状态：收到特征值 $result"
                when(result) {
                    Configs.NOTIFY_ONCE_CLICK -> {
                        Log.d(TAG, "单次点击")
                        binding.tvResult.text = "识别结果：单次点击"
                    }
                    Configs.NOTIFY_DOUBLE_CLICK -> {
                        Log.d(TAG, "双击")
                        binding.tvResult.text = "识别结果：双击"
                    }
                    Configs.NOTIFY_GESTURE_DOWN -> {
                        Log.d(TAG, "向下")
                        binding.tvResult.text = "识别结果：向下"
                        binding.vDrawResult.drawArrow(DrawView.ArrowDirection.BOTTOM)
                    }
                    Configs.NOTIFY_GESTURE_UP -> {
                        Log.d(TAG, "向上")
                        binding.tvResult.text = "识别结果：向上"
                        binding.vDrawResult.drawArrow(DrawView.ArrowDirection.TOP)
                    }
                    Configs.NOTIFY_GESTURE_LEFT -> {
                        Log.d(TAG, "向左")
                        binding.tvResult.text = "识别结果：向左"
                        binding.vDrawResult.drawArrow(DrawView.ArrowDirection.LEFT)
                    }
                    Configs.NOTIFY_GESTURE_RIGHT -> {
                        Log.d(TAG, "向右")
                        binding.tvResult.text = "识别结果：向右"
                        binding.vDrawResult.drawArrow(DrawView.ArrowDirection.RIGHT)
                    }
                    Configs.NOTIFY_GESTURE_CLOCKWISE -> {
                        Log.d(TAG, "顺时针")
                        binding.tvResult.text = "识别结果：顺时针"
                    }
                    Configs.NOTIFY_GESTURE_ANTICLOCKWISE -> {
                        Log.d(TAG, "逆时针")
                        binding.tvResult.text = "识别结果：逆时针"
                    }
                }
            }


//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBleBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            ToastUtil.showToast(this, "设备不支持BLE")
            finish()
        }

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        binding.rcDevices.layoutManager = LinearLayoutManager(this)
        bleDeviceAdapter = BleDeviceAdapter(ArrayList()) { device ->
            Log.d(TAG, "开始连接设备")
            binding.tvStatus.text = "状态：正在连接设备。。。"
            bluetoothGatt = device.connectGatt(this, false, gattCallback, TRANSPORT_LE)
            scanLeDevice(false)
        }
        binding.rcDevices.adapter = bleDeviceAdapter

        refreshConnectedDevice()

        binding.btnScan.text = "开始扫描"
        binding.btnScan.setOnClickListener {
            scanTask()
        }

        binding.btnClose.setOnClickListener {
            close()
        }

        binding.btnGestureMode.setOnClickListener {
            enterGestureMode()
        }
        binding.btnGestureNotify.setOnClickListener {
            enableNotify()
        }

        binding.vDrawResult.setOnTouchListener { v, event -> true }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(RC_PERMISSION_LOCATION)
    fun scanTask() {
        if (hasLocationPermission()) {
            scanLeDevice(!mScanning)
        } else {
            EasyPermissions.requestPermissions(
                this,
                "App需要位置权限，请授予",
                RC_PERMISSION_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission() : Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun refreshConnectedDevice() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).filter { it.type == BluetoothDevice.DEVICE_TYPE_LE }
        if (devices.isNotEmpty()) {
            val device = devices.first()
            binding.tvConnectedDevice.text = "${device.name} - ${device.address}"
            binding.btnConnectedDevice.visibility = View.VISIBLE
            binding.btnConnectedDevice.setOnClickListener {
                bluetoothGatt = device.connectGatt(this, false, gattCallback, TRANSPORT_LE)
            }
        }
    }

    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                binding.tvStatus.text = "状态：开始设备扫描。。。"
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter?.stopLeScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                binding.btnScan.text = "停止扫描"
                bluetoothAdapter?.startLeScan(leScanCallback)
            }
            else -> {
                mScanning = false
                binding.btnScan.text = "开始扫描"
                bluetoothAdapter?.stopLeScan(leScanCallback)
            }
        }
    }

    private fun enterGestureMode() {
        val characteristicWrite = bluetoothGatt?.getService(Configs.SERVICE_GESTURE)?.getCharacteristic(Configs.CHARACTERISTIC_WRITE_CMD)
        if (characteristicWrite != null) {
//            val value = byteArrayOf(0xaa553300.toByte())
            val value = hexStringToByteArray("aa553300")
            Log.d(TAG, "write bytes: $value")
            characteristicWrite.value = value
            val status = bluetoothGatt?.writeCharacteristic(characteristicWrite)
            Log.d(TAG, "写入状态: $status")
            binding.tvGestureMode.text = "手势模式: ${if (status == true) "已开启" else "已关闭"}"
        }

    }

    private fun enableNotify() {
        val characteristicNotify = bluetoothGatt?.getService(Configs.SERVICE_GESTURE)?.getCharacteristic(Configs.CHARACTERISTIC_PEN_NOTIFY)
        if (characteristicNotify != null) {
            for (descriptor in characteristicNotify.getDescriptors()) {
                Log.d(TAG, "BluetoothGattDescriptor: " + descriptor.uuid.toString())
            }

            bluetoothGatt?.setCharacteristicNotification(characteristicNotify, true)
            val descriptor = characteristicNotify.getDescriptor(Configs.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID).apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
            val status = bluetoothGatt?.writeDescriptor(descriptor)
            Log.d(TAG, "写入状态: $status")
            binding.tvGestureNotify.text = "手势通知: ${if (status == true) "已开启" else "已关闭"}"
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray? {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun bytesToHex(bytes: ByteArray): String? {
        val result = CharArray(bytes.size * 2)
        for (index in bytes.indices) {
            val v = bytes[index].toInt()
            val upper = v ushr 4 and 0xF
            result[index * 2] = (upper + if (upper < 10) 48 else 65 - 10).toChar()
            val lower = v and 0xF
            result[index * 2 + 1] = (lower + if (lower < 10) 48 else 65 - 10).toChar()
        }
        return String(result)
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        Log.d(TAG, "broadcastUpdate: uuid = ${characteristic.uuid}")
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        when (characteristic.uuid) {
            UUID.fromString("0000EEE0-0000-1000-8000-00805F9B34FB") -> {
                // 五、	APP跟遥控器的命令交互
                val flag = characteristic.properties
                Log.d(TAG, "控制命令： ${flag}")
//                val format = when (flag and 0x01) {
//                    0x01 -> {
//                        Log.d(TAG, "Heart rate format UINT16.")
//                        BluetoothGattCharacteristic.FORMAT_UINT16
//                    }
//                    else -> {
//                        Log.d(TAG, "Heart rate format UINT8.")
//                        BluetoothGattCharacteristic.FORMAT_UINT8
//                    }
//                }
//                val heartRate = characteristic.getIntValue(format, 1)
//                Log.d(TAG, String.format("Received heart rate: %d", heartRate))
//                intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                Log.d(TAG, "其他特征值： ${characteristic}")

//                val data: ByteArray? = characteristic.value
//                if (data?.isNotEmpty() == true) {
//                    val hexString: String = data.joinToString(separator = " ") {
//                        String.format("%02X", it)
//                    }
//                    intent.putExtra(EXTRA_DATA, "$data\n$hexString")
//                }
            }

        }
        sendBroadcast(intent)
    }

    fun close() {
        scanLeDevice(false)
        bleDeviceAdapter.items.clear()
        bleDeviceAdapter.notifyDataSetChanged()
        bluetoothGatt?.close()
        bluetoothGatt = null
        binding.tvGestureMode.text = "手势模式：已关闭"
        binding.tvGestureNotify.text = "手势通知：已关闭"
        binding.vDrawResult.clear()

        binding.tvResult.text = "识别结果"
    }

}