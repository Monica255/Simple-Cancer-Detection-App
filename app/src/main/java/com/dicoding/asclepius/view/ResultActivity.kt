package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.Result
import java.text.NumberFormat

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageClassifierHelper = ImageClassifierHelper(context = this)
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))

        imageUri?.let {uri->
            binding.resultImage.setImageURI(uri)

            when(val results = imageClassifierHelper.classifyStaticImage(uri)){
                is Result.Success ->{
                    results.data?.let { it ->
                        if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                            val sortedCategories =
                                it[0].categories.sortedByDescending { it?.score }
                            val displayResult ="${sortedCategories[0].label} " + NumberFormat.getPercentInstance()
                                .format(sortedCategories[0].score).trim()
                            binding.resultText.text = displayResult
                        } else {
                            binding.resultText.text = ""
                        }
                    }?:showToast(getString(R.string.error_no_data))
                }
                is Result.Error ->{
                    results.message?.let { showToast(it) }
                }
            }

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val EXTRA_IMAGE_URI = "image_uri"
    }
}