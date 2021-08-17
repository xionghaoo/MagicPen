package xh.zero.magicpen.ble

import android.Manifest
import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import xh.zero.magicpen.Configs
import xh.zero.magicpen.R
import xh.zero.magicpen.ToastUtil
import xh.zero.magicpen.databinding.ActivityBleBluetoothBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " + bluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = ACTION_GATT_DISCONNECTED
//                    connectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

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
            bluetoothGatt = device.connectGatt(this, true, gattCallback, TRANSPORT_LE)
            scanLeDevice(false)
        }
        binding.rcDevices.adapter = bleDeviceAdapter


        binding.btnScan.setOnClickListener {
            scanTask()
        }

        binding.btnClose.setOnClickListener {
            close()
        }
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
            scanLeDevice(true)
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

    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter?.stopLeScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                bluetoothAdapter?.startLeScan(leScanCallback)
            }
            else -> {
                mScanning = false
                bluetoothAdapter?.stopLeScan(leScanCallback)
            }
        }
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
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

}