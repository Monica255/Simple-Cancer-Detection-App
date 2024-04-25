package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.InputStream


class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    private val context: Context,
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }
    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            Log.e(TAG, e.message.toString())
        }    }

    fun classifyStaticImage(image: Uri): Result<List<Classifications>?> {
        try {
            if (imageClassifier == null) {
                setupImageClassifier()
            }

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(CastOp(DataType.UINT8))
                .build()

            val tensorImage =
                imageProcessor.process(TensorImage.fromBitmap(uriToBitmap(context, image)))

            val imageProcessingOptions = ImageProcessingOptions.builder()
                .setOrientation(ImageProcessingOptions.Orientation.TOP_LEFT)
                .build()

            return Result.Success(imageClassifier?.classify(tensorImage, imageProcessingOptions))
        }catch (e:Exception){
            return Result.Error(context.getString(R.string.error_classifying_image))
        }

    }


    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

}

sealed class Result<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Result<T>(data)
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
}