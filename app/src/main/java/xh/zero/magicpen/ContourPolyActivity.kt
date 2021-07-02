package xh.zero.magicpen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import xh.zero.magicpen.databinding.ActivityContourPolyBinding

/**
 * 轮廓外接多边形
 * author: yidong
 * 2020/10/7
 */
class ContourPolyActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ContourPolyActivity"
    }

    private lateinit var binding: ActivityContourPolyBinding
    private var mSource: Mat = Mat()
    private var mGray: Mat = Mat()
    private var mBinary: Mat = Mat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContourPolyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bgr = Utils.loadResource(this, R.drawable.contourpoly)
        Imgproc.cvtColor(bgr, mSource, Imgproc.COLOR_BGR2RGB)
        Imgproc.cvtColor(bgr, mGray, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(mGray, mGray, Size(5.0, 5.0), 2.0, 2.0)
        Imgproc.threshold(
            mGray,
            mBinary,
            20.0,
            255.0,
            Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU
        )
        binding.ivLena.showMat(mBinary)

        binding.rectMaxOuter.setOnClickListener {
            findRect(0)
        }
        binding.rectMinOuter.setOnClickListener {
            findRect(1)
        }
        binding.rectPolygon.setOnClickListener {
            findRect(2)
        }

    }

    fun findRect(flag: Int) {
        val tmp = mSource.clone()
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            mBinary,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        for (i in 0 until contours.size) {
            when (flag) {
                0 -> {
                    title = "最大外接矩形"
                    val rect = Imgproc.boundingRect(contours[i])
                    Imgproc.rectangle(tmp, rect, Scalar(255.0, 255.0, 0.0), 4, Imgproc.LINE_8)
                }
                1 -> {
                    title = "最小外接矩形"
                    val source = MatOfPoint2f()
                    source.fromList(contours[i].toList())
                    val rect = Imgproc.minAreaRect(source)
                    val points = arrayOfNulls<Point>(4)
                    val center = rect.center
                    rect.points(points)
                    Log.d(TAG, "RotateRect: ${points.toList()}, Center：$center")
                    for (j in 0..3) {
                        Imgproc.line(
                            tmp,
                            points[j % 4],
                            points[(j + 1) % 4],
                            Scalar(255.0, 255.0, 0.0),
                            4,
                            Imgproc.LINE_8
                        )
                    }
                }
                else -> {
                    title = "轮廓多边形"
                    val result = MatOfPoint2f()
                    val source = MatOfPoint2f()
                    source.fromList(contours[i].toList())
                    Imgproc.approxPolyDP(source, result, 4.0, true)
                    Log.d(TAG, "Poly: ${result.dump()}")
                    val points = result.toArray()
                    for (j in points.indices) {
                        Imgproc.line(
                            tmp,
                            points[j % points.size],
                            points[(j + 1) % points.size],
                            Scalar(255.0, 255.0, 0.0),
                            4,
                            Imgproc.LINE_8
                        )
                    }
                }
            }
        }
        binding.ivResult.showMat(tmp)
        tmp.release()
        hierarchy.release()
    }


    override fun onDestroy() {
        mSource.release()
        mGray.release()
        mBinary.release()
        super.onDestroy()
    }
}