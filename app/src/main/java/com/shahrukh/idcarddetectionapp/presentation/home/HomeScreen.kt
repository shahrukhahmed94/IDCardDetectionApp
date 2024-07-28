package com.shahrukh.idcarddetectionapp.presentation.home

import android.graphics.RectF
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shahrukh.idcarddetectionapp.R
import com.shahrukh.idcarddetectionapp.data.manager.ObjectDetectionManagerImpl
import com.shahrukh.idcarddetectionapp.domain.model.Detection
import com.shahrukh.idcarddetectionapp.presentation.common.ImageButton
import com.shahrukh.idcarddetectionapp.presentation.home.components.CameraOverlay
import com.shahrukh.idcarddetectionapp.presentation.home.components.CameraPreview
import com.shahrukh.idcarddetectionapp.presentation.home.components.ObjectCounter
import com.shahrukh.idcarddetectionapp.presentation.home.components.RequestPermissions
import com.shahrukh.idcarddetectionapp.presentation.utils.CameraFrameAnalyzer
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants.capturedImageBit
import com.shahrukh.idcarddetectionapp.presentation.utils.Dimens
import com.shahrukh.idcarddetectionapp.presentation.utils.ImageScalingUtils

/**
 * The primary screen for the application. It combines camera functionalities,
 * real-time object detection, and UI elements to interact with the camera and settings.
 */
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()



    // Requesting necessary permissions
    RequestPermissions()

    // Observing the state for whether an image is saved successfully
    val isImageSavedStateFlow by viewModel.isImageSavedStateFlow.collectAsState()

    // State to keep track of the preview size of the camera feed
    val previewSizeState = remember { mutableStateOf(IntSize(0, 0)) }

    // List to hold bounding box coordinates for detected objects
    val boundingBoxCoordinatesState = remember { mutableStateListOf<RectF>() }

    // State for the confidence score, influenced by the user through a slider
    val confidenceScoreState = remember { mutableFloatStateOf(Constants.INITIAL_CONFIDENCE_SCORE) }

    // Scale factors to translate coordinates from the detected image to the preview size
    var scaleFactorX = 1f
    var scaleFactorY = 1f

    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels * 1f
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels * 1f





        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // A state-backed list to store detected objects
            var detections by remember {
                mutableStateOf(emptyList<Detection>())
            }

            // Calling it to automatically re-invoke Composable(s) when state of 'detections' changes
            LaunchedEffect(detections) {}

            // Preparing Image Analyzer
            val cameraFrameAnalyzer = remember {
                CameraFrameAnalyzer(
                    objectDetectionManager = ObjectDetectionManagerImpl(
                        context = context
                    ),
                    onObjectDetectionResults = {
                        detections = it

                        // Clear the previous RectFs and add all new ones
                        boundingBoxCoordinatesState.clear()
                        detections.forEach { detection ->
                            boundingBoxCoordinatesState.add(detection.boundingBox)
                        }
                    },
                    confidenceScoreState = confidenceScoreState
                )
            }

            // Prepare Camera Controller
            val cameraController = remember {
                viewModel.prepareCameraController(
                    context,
                    cameraFrameAnalyzer
                )
            }

            // Combined Column for Camera Preview, CameraOverlay & Bottom UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.gray_900)),
            ) {
                // Camera Preview Column with CameraOverlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                ) {
                    // Camera Preview
                    CameraPreview(
                        controller = remember {
                            cameraController
                        },
                        modifier = Modifier.fillMaxSize(),
                        onPreviewSizeChanged = { newSize ->
                            previewSizeState.value = newSize

                            // Get Scale-Factors along X and Y depending on size of camera-preview
                            val scaleFactors = ImageScalingUtils.getScaleFactors(
                                newSize.width,
                                newSize.height
                            )

                            scaleFactorX = scaleFactors[0]
                            scaleFactorY = scaleFactors[1]

                            Log.d(
                                "HomeViewModel",
                                "HomeScreen() called with: newSize = $scaleFactorX & scaleFactorY = $scaleFactorY"
                            )
                        }
                    )

                    // Add CameraOverlay here so it overlays on top of CameraPreview
                    CameraOverlay(detections = detections)
                }

                // Bottom column with Capture-Image and Threshold Level Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .padding(top = Dimens.Padding8dp),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    ImageButton(
                        drawableResourceId = R.drawable.ic_capture,
                        contentDescriptionResourceId = R.string.capture_button_description,
                        modifier = Modifier
                            .size(Dimens.CaptureButtonSize)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                // Capture and Saves Photo
                                viewModel.capturePhoto(
                                    context = context,
                                    navController = navController,
                                    cameraController = cameraController,
                                    screenWidth,
                                    screenHeight,
                                    detections
                                )

                                // Show toast of Save State
                                if (isImageSavedStateFlow) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Image saved successfully!",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "Error saving image",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                    )


                }
            }

            // Column with rotate-camera and detected object count Composable (Overlapping UI)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .padding(top = Dimens.Padding32dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {


                    // Detected Object Count Composable
                    ObjectCounter(objectCount = detections.size)
                }
            }
        }

    }
