package xh.zero.magicpen

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import xh.zero.magicpen.ml.FlowerModel
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TensorflowTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TensorflowTestActivity"
        private const val MAX_RESULT_DISPLAY = 3 // Maximum number of results displayed

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tensorflow_test)

        findViewById<Button>(R.id.btn_tensorflow_test).setOnClickListener {
            test()
        }
    }

    private fun test() {
        Thread {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_flower_rose)
            val tfImage: TensorImage = TensorImage.fromBitmap(bitmap)
            val width = bitmap.width
            val height = bitmap.height

            val outputs = flowerModel.process(tfImage)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }.take(MAX_RESULT_DISPLAY) // take the top results

            val items = mutableListOf<Recognition>()
            for (output in outputs) {
                val recognition = Recognition(output.label, output.score)
                items.add(recognition)
                Log.d(TAG, "out: ${recognition.label} ---- ${recognition.probabilityString}")
            }
            runOnUiThread {
                val tvResult = findViewById<TextView>(R.id.tv_recognize_result)
                val txt = StringBuilder()
                items.forEach { item ->
                    txt.append("识别结果: ${item.label} 的可能性为 ${item.probabilityString}").append("\n")
                }
                tvResult.text = txt.toString()
            }
        }.start()
    }

    private val flowerModel: FlowerModel by lazy{

        // TODO 6. Optional GPU acceleration
//        val compatList = CompatibilityList()

//        val options = if(compatList.isDelegateSupportedOnThisDevice) {
//            Log.d(TAG, "This device is GPU Compatible ")
//            Model.Options.Builder().setDevice(Model.Device.GPU).build()
//        } else {
//            Log.d(TAG, "This device is GPU Incompatible ")
//            Model.Options.Builder().setNumThreads(4).build()
//        }

        // Initialize the Flower Model
        FlowerModel.newInstance(this)
    }


}