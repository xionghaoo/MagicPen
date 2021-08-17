package xh.zero.magicpen

import android.content.Context
import android.view.Gravity
import android.widget.Toast

class ToastUtil {
    companion object {
        fun showToast(context: Context, message: String) {
            val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }
}