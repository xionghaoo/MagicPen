package xh.zero.magicpen

import android.app.Application

class MagicPenApp : Application() {
    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("native_lib")
    }
}