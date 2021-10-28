package xh.zero.magicpen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import xh.zero.magicpen.ble.BleBluetoothActivity
import xh.zero.magicpen.databinding.ActivityMainBinding
import java.io.File
import java.io.FileWriter
import java.io.FilterWriter
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQ_STORAGE_CODE = 5
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var contours: List<MatOfPoint>
    private lateinit var srcMat: Mat
    private lateinit var hsvMat: Mat
    private var resultBitmap: Bitmap? = null
    private lateinit var contours2f: MatOfPoint2f
    private lateinit var approxCurve: MatOfPoint2f
    private var currentSaveFileName: String? = null
    private var saveFileDir: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openCVInit()

        binding.btnOpenCvTest.setOnClickListener {
//            startActivity(Intent(this, ShapeDetectActivity::class.java))
//            startActivity(Intent(this, TensorflowTestActivity::class.java))
            startActivity(Intent(this, BleBluetoothActivity::class.java))
        }

        binding.drawView.setOnDrawListener(object : DrawView.OnDrawListener {
            override fun onTouchStart(x: Float, y: Float) {
                binding.tvCoordinate.text = "x: $x, y: $y"
                currentSaveFileName = "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}.txt"
                saveCoordinate(x, y, currentSaveFileName!!)
                binding.tvFileLocation.text = "本次绘制坐标保存位置: $saveFileDir"
            }

            override fun onTouchMove(x: Float, y: Float) {
                binding.tvCoordinate.text = "x: $x, y: $y"
                saveCoordinate(x, y, currentSaveFileName!!)
            }

            override fun onCompleted(path: Path) {
                binding.drawViewBg.setPath(path)
                val bitmap = loadBitmapFromView(binding.drawViewBg)
//                binding.drawViewBak.setImageBitmap(bitmap)
                handlePath(bitmap)
            }
        })

        binding.drawViewFixed.setOnTouchListener { v, event -> true }

        requestPermissionTask()
    }

    private fun saveCoordinate(x: Float, y: Float, fileName: String) {
        if (hasStoragePermission()) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "magic_pen")
            if (!dir.exists()) {
                dir.mkdir()
            }
            val record = File(dir, fileName)
            saveFileDir = record.absolutePath
            try {
                val writer = FileWriter(record, true)
                writer.appendLine("x: $x, y: $y")
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @AfterPermissionGranted(REQ_STORAGE_CODE)
    private fun requestPermissionTask() {
        if (hasStoragePermission()) {

        } else {
            EasyPermissions.requestPermissions(
                this,
                "App需要存储权限",
                REQ_STORAGE_CODE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(
            v.width,
            v.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    private fun handlePath(image: Bitmap?) {
        if (image == null) return
        srcMat = Mat()
        try {
            Utils.bitmapToMat(image, srcMat)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        contours = ArrayList()
        val hierarchy = Mat()
        val binaryMat = Mat()
        val resultMat: Mat = srcMat.clone()
        hsvMat = Mat()
        // 将BGR颜色转成HSV
        Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV)
        // 指定颜色阈值内区域变为白色，其他变为黑色
        Core.inRange(hsvMat, Scalar(156.0, 43.0, 46.0), Scalar(180.0, 255.0, 255.0), binaryMat)
//        Core.inRange(hsvMat, Scalar(26.0, 43.0, 46.0), Scalar(34.0, 255.0, 255.0), binaryMat)
        // 生成二值化图片
        resultBitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888)

        // 灰度图
//        val tmp = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(binaryMat, tmp)
//        binding.drawViewBak.setImageBitmap(tmp)

        // 找到轮廓，并存入contours
        Imgproc.findContours(
            binaryMat,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        // 将contours轮廓在resultMat(srcMat)上用粗细为10的黑色线条画出
//        Imgproc.drawContours(resultMat, contours, -1, Scalar(0.0, 0.0, 0.0), 10)
        if (contours.isNotEmpty()) {
            // 对轮廓进行多边形拟合
            contours2f = MatOfPoint2f(*contours[0].toArray())
            // 近似精度的参数，值越小精度越高
            var epsilon = 0.03 * Imgproc.arcLength(contours2f, true)
            if (epsilon <= 5) {
                epsilon = 5.0
            }
            Log.d(TAG, "epsilon: ${epsilon}")
            approxCurve = MatOfPoint2f()
            // 拟合后的顶点集合approxCurve
            Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true)

            // 还原图片
//            Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR)
//            Utils.matToBitmap(resultMat, resultBitmap)
//            binding.drawViewBak.setImageBitmap(resultBitmap)

            Log.d(TAG, "顶点数：${approxCurve.rows()}")
            val num = approxCurve.rows()
            if (num == 0) {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show()
                binding.drawViewFixed.clear()
                return
            }
            if (num == 2 || num == 1) {
                // 绘制箭头
                binding.drawViewFixed.drawArrow(binding.drawView.getTouchDirection())
            } else if (num <= 4 || num == 10) {
                val path = Path()
                approxCurve.toList().forEachIndexed { index, point ->
                    if (index == 0) {
                        path.moveTo(point.x.toFloat(), point.y.toFloat())
                    } else {
                        path.lineTo(point.x.toFloat(), point.y.toFloat())
                    }
                }
                path.close()
                binding.drawViewFixed.setPath(path)
            } else {
                // 画圆
                binding.drawViewFixed.setCircle()
            }


        }

    }

    private fun openCVInit() {
        val success = OpenCVLoader.initDebug()
        if (success) {
            Log.d(TAG, "OpenCV Lode success")
        } else {
            Log.d(TAG, "OpenCV Lode failed ")
        }
    }

}