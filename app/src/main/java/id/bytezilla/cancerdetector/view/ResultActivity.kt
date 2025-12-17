package id.bytezilla.cancerdetector.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import id.bytezilla.cancerdetector.R
import id.bytezilla.cancerdetector.databinding.ActivityResultBinding
import id.bytezilla.cancerdetector.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat

class ResultActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.result)
        binding.toolbar.ibBack.setOnClickListener(this)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
            classifyImage(it)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ib_back -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun classifyImage(imageUri: Uri) {
        val imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        binding.resultText.text = error
                    }
                }

                override fun onResults(results: List<Classifications>?) {
                    runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                val topResult = it[0].categories.sortedByDescending { it?.score }[0]
                                val displayResult = "${topResult.label} ${NumberFormat.getPercentInstance().format(topResult.score)}"
                                binding.resultText.text = displayResult
                            } else {
                                binding.resultText.text = ""
                            }
                        }
                    }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(imageUri)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}