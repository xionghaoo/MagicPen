package xh.zero.magicpen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xh.zero.magicpen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenCvTest.setOnClickListener {
            startActivity(Intent(this, ShapeDetectActivity::class.java))
        }
    }
}