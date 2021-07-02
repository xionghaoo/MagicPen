package xh.zero.magicpen

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File

/**
 * 扩展方法
 *
 * @author yidong
 * @date 2020/6/24
 */

fun ImageView.showMat(source: Mat) {
    val bitmap = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(source, bitmap)
    setImageBitmap(bitmap)
}

fun Context.getBgrFromResId(@DrawableRes resId: Int): Mat {
    return Utils.loadResource(this, resId)
}

fun View.setVisible() {
    this.visibility = View.VISIBLE
}

fun View.setInvisible() {
    this.visibility = View.INVISIBLE
}

fun Mat.toRgb(): Mat {
    val rgb = Mat()
    Imgproc.cvtColor(this, rgb, Imgproc.COLOR_BGR2RGB)
    return rgb
}

fun Mat.toGray(): Mat {
    val gray = Mat()
    Imgproc.cvtColor(this, gray, Imgproc.COLOR_BGR2GRAY)
    return gray
}

//fun Activity.wrapCoroutine(before: () -> Unit, method: () -> Any, after: () -> Unit) {
//    GlobalScope.launch(Dispatchers.Main) {
//        before()
//        withContext(Dispatchers.IO) {
//            method()
//        }
//        after()
//    }
//}

fun Context.copyFromAssets(@RawRes resId: Int, targetDir:String, targetFileName:String): String {
    val targetDirFile = getDir(targetDir, Context.MODE_PRIVATE)
    val targetFile = File(targetDirFile, targetFileName)
    targetFile.outputStream().use {
        resources.openRawResource(resId).copyTo(it)
    }
    return targetFile.absolutePath
}