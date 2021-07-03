package xh.zero.magicpen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import xh.zero.magicpen.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var contours: List<MatOfPoint>
    private lateinit var srcMat: Mat
    private lateinit var hsvMat: Mat
    private var resultBitmap: Bitmap? = null
    private lateinit var contours2f: MatOfPoint2f
    private lateinit var approxCurve: MatOfPoint2f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openCVInit()

        binding.btnOpenCvTest.setOnClickListener {
            startActivity(Intent(this, ShapeDetectActivity::class.java))
        }

        binding.drawView.setOnDrawListener(object : DrawView.OnDrawListener {
            override fun onCompleted(path: Path) {
                binding.drawViewBg.setPath(path)
                val bitmap = loadBitmapFromView(binding.drawViewBg)
//                binding.drawViewBak.setImageBitmap(bitmap)
                handlePath(bitmap)
            }
        })
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

//            val shape: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.contourpoly)
//            val bmp32: Bitmap = shape.copy(Bitmap.Config.ARGB_8888, true)
//            Utils.bitmapToMat(shape, srcMat)
//            srcMat = Utils.loadResource(this, R.drawable.contourpoly)

//            srcMat = Mat(image.height, image.width, CvType.CV_8UC4)
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
        Imgproc.drawContours(resultMat, contours, -1, Scalar(0.0, 0.0, 0.0), 10)
        if (contours.isNotEmpty()) {
            // 对轮廓进行多边形拟合
            contours2f = MatOfPoint2f(*contours[0].toArray())
            // 近似精度的参数，值越小精度越高
            val epsilon = 0.01 * Imgproc.arcLength(contours2f, true)
            Log.d(TAG, "epsilon: ${epsilon}")
            approxCurve = MatOfPoint2f()
            // 拟合后的顶点集合approxCurve
            Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true)

            // 还原图片
//            Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR)
//            Utils.matToBitmap(resultMat, resultBitmap)
//            binding.drawViewBak.setImageBitmap(resultBitmap)

            Log.d(TAG, "顶点数：${approxCurve.rows()}")

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