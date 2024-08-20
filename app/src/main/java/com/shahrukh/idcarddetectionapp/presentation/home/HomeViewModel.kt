package com.shahrukh.idcarddetectionapp.presentation.home

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.graphics.Paint
import android.util.DisplayMetrics
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.navigation.NavController

import com.shahrukh.idcarddetectionapp.domain.manager.ObjectDetectionManager
import com.shahrukh.idcarddetectionapp.domain.model.Detection
import com.shahrukh.idcarddetectionapp.presentation.utils.CameraFrameAnalyzer
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants.ORIGINAL_IMAGE_HEIGHT
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants.ORIGINAL_IMAGE_WIDTH
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants.capturedImageBit
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants.originalImageBitmap
import com.shahrukh.idcarddetectionapp.presentation.utils.Routes
import kotlinx.coroutines.CompletableDeferred
import java.lang.Float.max
import java.lang.Float.min
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val objectDetectionManager: ObjectDetectionManager
): ViewModel() {
    companion object {
        private val TAG: String? = HomeViewModel::class.simpleName
    }

    private val _isImageSavedStateFlow = MutableStateFlow(true)
    val isImageSavedStateFlow = _isImageSavedStateFlow.asStateFlow()

    var isObjDetected =  MutableStateFlow(false)





    /**
     * Initializes and returns a `LifecycleCameraController` instance with the specified use cases.
     * This function sets up the camera controller for image analysis and image capture.
     *
     * @param context context used for managing the lifecycle of the camera controller.
     * @return Returns a fully initialized `LifecycleCameraController` instance.
     */
    fun prepareCameraController(
        context: Context,
        cameraFrameAnalyzer: CameraFrameAnalyzer
    ): LifecycleCameraController {




        Log.d(TAG, "prepareCameraController() called")
        return LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS or
                        CameraController.IMAGE_CAPTURE
            )
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                cameraFrameAnalyzer
            )
        }
    }

    /**
     * Retrieves the selected camera (front or back) based on the current camera selection of the provided camera controller.
     *
     * @param cameraController The controller managing the camera operations.
     * @return Returns [CameraSelector.DEFAULT_FRONT_CAMERA] if the current camera is the back camera,
     *         and [CameraSelector.DEFAULT_BACK_CAMERA] if it's the front camera.
     */
    fun getSelectedCamera(cameraController: LifecycleCameraController): CameraSelector {
        Log.d(TAG, "getSelectedCamera() called")
        return if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun cropImage(bitmap: Bitmap, boundingBox: RectF, tensorImageWidth: Int, tensorImageHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height


        // Calculate scaling factors
        val scaleX = originalWidth.toFloat() / tensorImageWidth
        val scaleY = originalHeight.toFloat() / tensorImageHeight


        // Convert bounding box coordinates to original image dimensions
        val left = (boundingBox.left * scaleX).toInt()
        val top = (boundingBox.top * scaleY).toInt()
        val right = (boundingBox.right * scaleX).toInt()
        val bottom = (boundingBox.bottom * scaleY).toInt()


        // Ensure dimensions are within bounds
        val width = (right - left).coerceAtLeast(1)
        val height = (bottom - top).coerceAtLeast(1)

        // Ensure the cropping rectangle is within the bitmap bounds
        val adjustedLeft = left.coerceIn(0, originalWidth - 1)
        val adjustedTop = top.coerceIn(0, originalHeight - 1)
        val adjustedRight = (adjustedLeft + width).coerceAtMost(originalWidth)
        val adjustedBottom = (adjustedTop + height).coerceAtMost(originalHeight)


        // Crop the image
        return Bitmap.createBitmap(
            bitmap,
            adjustedLeft,
            adjustedTop,
            adjustedRight - adjustedLeft,
            adjustedBottom - adjustedTop
        )


    }



    /**
     * Captures a photo using the provided camera controller and processes the captured image.
     *
     * This function initiates a photo capture and once successful, it rotates the image based on
     * its rotation degrees. Also, if the image is captured using the front camera, it
     * inverts the image along the X-axis. After processing the image, the resulting bitmap is saved
     * by calling the 'saveBitmapToDevice' private method,
     *
     * @param context The application's context.
     * @param cameraController The lifecycle-aware camera controller to manage the photo capture.
     */
    fun capturePhoto(
        context: Context,
        navController: NavController,
        cameraController: LifecycleCameraController,
        screenWidth: Float,
        screenHeight: Float,
        detections: List<Detection>
    ) {




        cameraController.takePicture(
            ContextCompat.getMainExecutor(context),
            object: ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    Log.d(TAG, "onCaptureSuccess() called for capturePhoto")

                    // Rotating the image by transforming it via Matrix using rotationDegrees
                    val rotatedImageMatrix: Matrix =
                        Matrix().apply {
                            postRotate(image.imageInfo.rotationDegrees.toFloat())


                        }

                    // Creating a new Bitmap via createBitmap using 'rotatedImageMatrix'
                    val rotatedBitmap: Bitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,

                        rotatedImageMatrix,
                        true
                    )

                    val combinedBitmap = overlayDetectionsOnBitmap(
                        rotatedBitmap,
                        detections,
                        screenWidth,
                        screenHeight
                    )

                    // Save the Image-Bitmap to Device
                   // saveBitmapToDevice(
                     //   context = context,
                       // capturedImageBitmap = combinedBitmap
                    //)

                   // val uri = saveBitmapToDevice(
                     //   context = context,
                       // capturedImageBitmap = combinedBitmap
                    //)







                     //  isObjDetected.value = true



                //    navController.navigate(
                  //      Routes.getPreviewDetectedObjectRoute(
                    //        uri.toString()
                      //  ))

                    val detection = detections.firstOrNull()
                    if (detection != null) {

                        val croppedBitmap = cropImage(rotatedBitmap, detection.boundingBox,detection.tensorImageWidth,detection.tensorImageHeight)

                        viewModelScope.launch {
                            val savedBitmap = saveBitmapToDevice(context, croppedBitmap)
                            val originalBitmap = saveBitmapToDevice(context,combinedBitmap)
                            if (savedBitmap != null) {

                                capturedImageBit = savedBitmap
                                originalImageBitmap  = originalBitmap
                                navController.navigate(Routes.ROUTE_PREVIEW_DETECTED_OBJECT_SCREEN)
                                Log.i("Saved Bitmap is",savedBitmap.toString())
                                // Handle the successfully saved bitmap
                                Log.d(TAG, "Image saved successfully")
                            } else {
                                // Handle the failure
                                Log.e(TAG, "Failed to save image")
                            }
                        }
                        //val uri = saveBitmapToDevice(context, croppedBitmap)

                       // navController.navigate(Screen.Preview.createRoute(uri.toString()))
                       // navController.navigate(PreviewDetectedObjectScreen(croppedImage = uri.toString()))
                    }



                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e(TAG, "onError() called for capturePhoto with: exception = $exception")
                    isPhotoSuccessfullySaved(false)
                }
            }
        )
    }


    /**
     * Saves the provided bitmap to the device's external storage.
     *
     * This function saves the bitmap in the device's picture directory and gives it a unique name based on the current system time.
     * For devices running Android API 29 and above, the bitmap is saved using the MediaStore API which ensures that the saved image
     * is immediately visible in gallery apps without the need for any additional scanning.
     *
     * @param context The application context used for content resolution.
     * @param capturedImageBitmap The bitmap image to be saved.
     */



    /**fun saveBitmapToDevice  (
        context: Context,
        capturedImageBitmap: Bitmap
    )  {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "saveBitmapToDevice() called for Version = ${Build.VERSION.SDK_INT}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, generateImageName())
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    val uri: Uri? = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it).use { outputStream ->
                            if (outputStream != null) {
                                capturedImageBitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    outputStream
                                )

                                capturedImageBit = capturedImageBitmap
                            }
                            outputStream?.flush()
                            outputStream?.close()
                        }
                    }





                    // Update _isImageSavedStateFlow value to true
                    isPhotoSuccessfullySaved(true)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "saveBitmapToDevice() called with: exception = $exception")
                isPhotoSuccessfullySaved(false)
            }

        }
    }*/

    suspend fun saveBitmapToDevice(
        context: Context,
        capturedImageBitmap: Bitmap
    ): Bitmap? {
        val deferredResult = CompletableDeferred<Bitmap?>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "saveBitmapToDevice() called for Version = ${Build.VERSION.SDK_INT}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, generateImageName())
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    val uri: Uri? = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it).use { outputStream ->
                            if (outputStream != null) {
                                capturedImageBitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    outputStream
                                )
                                outputStream.flush()
                                outputStream.close()
                                deferredResult.complete(capturedImageBitmap)
                            } else {
                                deferredResult.complete(null)
                            }
                        }
                    } ?: deferredResult.complete(null)

                    // Update _isImageSavedStateFlow value to true
                    isPhotoSuccessfullySaved(true)
                } else {
                    deferredResult.complete(null)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "saveBitmapToDevice() called with: exception = $exception")
                isPhotoSuccessfullySaved(false)
                deferredResult.complete(null)
            }
        }
        return deferredResult.await()
    }


    /**
     * Updates the state flow with the status of whether the photo has been successfully saved or not.
     *
     * @param isSaved Boolean flag indicating if the photo was successfully saved.
     */
    private fun isPhotoSuccessfullySaved(isSaved: Boolean) {
        Log.d(TAG, "isPhotoSuccessfullySaved() called with: isSaved Flag = $isSaved")
        _isImageSavedStateFlow.value = isSaved
    }


    /**
     * Returns the current system time in a formatted string, prepended with "IMG_".
     * The returned string is in the format "IMG_YYYYMMDD_HHMMSS", which represents the current system time.
     *
     * @return A formatted string representing the current system time, prepended with "IMG_".
     */
    private fun generateImageName(): String {
        Log.d(TAG, "generateImageName() called")
        val currentDateTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDateTime)
        return "IMG_$formattedDate"
    }

    /**
     * Overlays detection boxes on the provided bitmap image.
     *
     * @param bitmap The original bitmap image where detections will be overlaid.
     * @param detections A list of [Detection] objects representing the detected items.
     * @param screenWidth The width of the screen where the image will be displayed.
     * @param screenHeight The height of the screen where the image will be displayed.
     * @return A new [Bitmap] with overlaid detections.
     */
    fun overlayDetectionsOnBitmap(
        bitmap: Bitmap,
        detections: List<Detection>,
        screenWidth: Float,
        screenHeight: Float
    ): Bitmap {
        val overlayBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(overlayBitmap)

        // Draw the captured image onto the new bitmap
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Draw detections on the canvas (i.e., on top of the captured image)
        detections.forEach { detection ->
            drawDetectionBox(
                detection = detection,
                originalBitmap = overlayBitmap,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        }
        return overlayBitmap
    }


    /**
     * Draws a detection box around a detected object on a bitmap.
     *
     * @param detection The [Detection] object containing the details of what was detected.
     * @param originalBitmap The bitmap on which the detection box is to be drawn.
     * @param screenWidth The width of the screen for scaling the detection box.
     * @param screenHeight The height of the screen for scaling the detection box.
     * @return The original bitmap with a detection box drawn on it.
     */
    private fun drawDetectionBox(
        detection: Detection,
        originalBitmap: Bitmap,
        screenWidth: Float,
        screenHeight: Float
    ): Bitmap {
        // Prepare a Paint object for drawing
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = getColorForLabel(detection.detectedObjectName)
        }

        val scaleFactor = min(
            screenWidth * 1f / detection.tensorImageWidth,
            screenHeight * 1f / detection.tensorImageHeight
        )

        // Scaling adaptively depending on DPI of the device
        val adaptiveScaleFactor: Float = getDeviceDensityValue()

        val scaledBox = RectF(
            detection.boundingBox.left * adaptiveScaleFactor,
            detection.boundingBox.top * adaptiveScaleFactor,
            detection.boundingBox.right * adaptiveScaleFactor,
            detection.boundingBox.bottom * adaptiveScaleFactor
        ).also {
            it.left = it.left.coerceAtLeast(0f)
            it.top = it.top.coerceAtLeast(0f)
            it.right = it.right.coerceAtMost(screenWidth)
            it.bottom = it.bottom.coerceAtMost(screenHeight)
        }

        // Clone the original bitmap to draw onto
        val canvas = Canvas(originalBitmap)

        // Draw the rectangle on the canvas
        canvas.drawRect(scaledBox, paint)

        val text = "${detection.detectedObjectName} ${(detection.confidenceScore * 100).toInt()}%"
        val textPaint = Paint().apply {
            color = paint.color
            textSize = 20f
        }

        // Draw the text on the canvas
        canvas.drawText(text, scaledBox.left, scaledBox.top - 10, textPaint)

        return originalBitmap
    }

    /**
     * Retrieves the device's screen density factor as a float value.
     * This value is used to scale pixel dimensions to match the current screen density.
     *
     * @return A float representing the density factor of the display (e.g., 0.75 for low, 1.0 for medium, etc.).
     * The default return value is 1.0f, corresponding to the baseline screen density (mdpi).
     */
    private fun getDeviceDensityValue(): Float {
        return when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> 0.75f
            DisplayMetrics.DENSITY_MEDIUM -> 1.0f
            DisplayMetrics.DENSITY_HIGH -> 1.5f
            DisplayMetrics.DENSITY_XHIGH -> 2.0f
            DisplayMetrics.DENSITY_XXHIGH -> 3.0f
            DisplayMetrics.DENSITY_XXXHIGH -> 4.0f
            else -> 1.0f
        }
    }

    private val labelColorMap = mutableMapOf<String, Int>()

    /**
     * Gets a color associated with a particular label. If a color is not already assigned,
     * it generates a random color and associates it with the label for consistent coloring.
     *
     * @param label The label for which a color is required.
     * @return The color associated with the given label.
     */
    private fun getColorForLabel(label: String): Int {
        return labelColorMap.getOrPut(label) {
            // Generates a random color for the label if it doesn't exist in the map.
            Random.nextInt()
        }
    }

}