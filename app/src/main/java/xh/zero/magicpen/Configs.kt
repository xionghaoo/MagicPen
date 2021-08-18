package xh.zero.magicpen

import java.util.*

class Configs {
    companion object {
        const val DEVICE_MAC_ADDRESS = "FC:CF:CD:00:00:06"

        val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


        // 设备信息
        const val SERVICE_DEVICE_INFO = "0000180A-0000-1000-8000-00805F9B34FB"
        const val SERVICE_SN = "00002A25-0000-1000-8000-00805F9B34FB"
        const val SERVICE_FIRMWARE = "00002A2B-0000-1000-8000-00805F9B34FB"
        val SERVICE_GESTURE = UUID.fromString("0000EEE0-0000-1000-8000-00805F9B34FB")

        // App写命令
        val CHARACTERISTIC_WRITE_CMD = UUID.fromString("0000EEE1-0000-1000-8000-00805F9B34FB")
        // 魔法笔上报
        val CHARACTERISTIC_PEN_NOTIFY = UUID.fromString("0000EEE2-0000-1000-8000-00805F9B34FB")

        // 手势模式
        const val CMD_GESTURE = "0xaa553300"
        // 陀螺仪校准
        const val CMD_GYRO = "0xaa554000"

        // 手势
        const val NOTIFY_ONCE_CLICK = "55AA0100"
        const val NOTIFY_DOUBLE_CLICK = "55AA0200"
        const val NOTIFY_GESTURE_UP = "55AA5200"
        const val NOTIFY_GESTURE_DOWN = "55AA5100"
        const val NOTIFY_GESTURE_LEFT = "55AA5000"
        const val NOTIFY_GESTURE_RIGHT = "55AA4F00"
        const val NOTIFY_GESTURE_CLOCKWISE = "55AAC000"
        const val NOTIFY_GESTURE_ANTICLOCKWISE = "55AAB000"


    }
}