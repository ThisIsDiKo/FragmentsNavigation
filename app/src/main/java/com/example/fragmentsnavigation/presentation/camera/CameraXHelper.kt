package com.example.fragmentsnavigation.presentation.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

class CameraXHelper(
    private val caller: Any,
    private val previewView: PreviewView,
    private val textView: TextView,
    private val imageAnalyzer: ImageAnalysis.Analyzer? = null,
    private val filesDirectory: File? = null,
    private val onPictureTaken: ((File, Uri?) -> Unit)? = null,
    private val builderPreview: Preview.Builder? = null,
    private val builderImageCapture: ImageCapture.Builder? = null,
    private val onError: ((Throwable) -> Unit)? = null,
    private val onBarcodeFound: (String) -> Unit
) {
    private val context by lazy {
        when(caller){
            is Activity -> caller
            is Fragment -> caller.activity ?: throw Exception("Fragment not attached to activity")
            else -> throw Exception("Can't get a context from caller")
        }
    }

    private lateinit var imagePreview: Preview
    private lateinit var imageCapture: ImageCapture
    private var imageAnalysis: ImageAnalysis? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val executor = Executors.newSingleThreadExecutor()

    fun start(){
        if (caller !is LifecycleOwner) throw Exception("Caller is not lifecycle owner")
        previewView.post {
            startCamera()
        }
    }

    private fun createImagePreview() =
        (builderPreview ?: Preview.Builder()
            .setTargetAspectRatio(aspectRatio()))
            .setTargetRotation(previewView.display.rotation)
            .build()
            .apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

    private fun createImageAnalysis(): ImageAnalysis{
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

        val analysisUseCase = ImageAnalysis.Builder()
            .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(aspectRatio())
            .build()

        analysisUseCase.setAnalyzer(
            executor,
            ImageAnalysis.Analyzer { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy)
            }
        )

        return analysisUseCase
    }

    private fun createImageCapture() =
        (builderImageCapture ?: ImageCapture.Builder()
            .setTargetAspectRatio(aspectRatio()))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

    fun changeCamera(){
        lensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK
            else CameraSelector.LENS_FACING_FRONT

        startCamera()
    }

    private fun startCamera(){
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val cameraProvideFuture = ProcessCameraProvider.getInstance(context)
        cameraProvideFuture.addListener(Runnable {
            try {
                val cameraProvider: ProcessCameraProvider = cameraProvideFuture.get()
                imagePreview = createImagePreview()
                imagePreview.setSurfaceProvider(previewView.surfaceProvider)

                imageCapture = createImageCapture()
                imageAnalysis = createImageAnalysis()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    caller as LifecycleOwner,
                    cameraSelector,
                    imagePreview,
                    imageCapture,
                    imageAnalysis
                )
            }
            catch (e: Exception){
                onError?.invoke(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun aspectRatio(): Int {
        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun takePicture(){
        val dir = filesDirectory ?: context.cacheDir
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, UUID.randomUUID().toString() + ".jpg")
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file)
            .setMetadata(metadata)
            .build()

        imageCapture.takePicture(
            outputFileOptions,
            executor,
            object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onPictureTaken?.invoke(
                        file,
                        outputFileResults.savedUri
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    onError?.invoke(exception)
                }
            }
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        //Log.e("", "Image size is: ${inputImage.width} ${inputImage.height}")
        //val source = ImageProxyTransformFactory().getOutputTransform(imageProxy)
        //val target = previewView.outputTransform

        // Build the transform from ImageAnalysis to PreviewView

        // Build the transform from ImageAnalysis to PreviewView
        //val coordinateTransform = CoordinateTransform(source, target!!)

        // Detect face in ImageProxy and transform the coordinates to PreviewView.
        // The value of faceBox can be used to highlight the face in PreviewView.

        // Detect face in ImageProxy and transform the coordinates to PreviewView.
        // The value of faceBox can be used to highlight the face in PreviewView.

        textView.post {
            textView.text = ""
        }

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach { barcode ->

                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue
                    //coordinateTransform.mapRect(RectF(bounds))

//                    textView.post {
//                        textView.text = rawValue
//                    }

                    onBarcodeFound(rawValue ?: "")

                    //Log.e(CameraXHelper::class.java.simpleName, "scanned data is: ${rawValue}")
                    //Log.e(CameraXHelper::class.java.simpleName, "bounded box: ${bounds}")

                }
            }
            .addOnFailureListener {
                Log.e(CameraXHelper::class.java.simpleName, it.message ?: it.toString())
            }
            .addOnCompleteListener {
                // When the image is from CameraX analysis use case, must call image.close() on received
                // images when finished using them. Otherwise, new images may not be received or the camera
                // may stall.
                //Log.e(CameraXHelper::class.java.simpleName, "closing imageproxy")
                imageProxy.close()

            }
    }
}