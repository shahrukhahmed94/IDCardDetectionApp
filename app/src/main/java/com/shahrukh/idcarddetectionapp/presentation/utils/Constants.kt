package com.shahrukh.idcarddetectionapp.presentation.utils

import android.graphics.Bitmap

object Constants {
    // Preference Datastore related
    const val USER_CONFIG_DATASTORE_NAME = "userConfigDatastore"
    const val USER_CONFIG = "userConfig"

    // Values
    const val INITIAL_CONFIDENCE_SCORE = 0.5f

    const val ORIGINAL_IMAGE_WIDTH = 320f
    const val ORIGINAL_IMAGE_HEIGHT = 320f

    // TensorFlow Lite
    const val MODEL_MAX_RESULTS_COUNT = 10
    const val MODEL_PATH = "detect_quant_metadata.tflite"


 var capturedImageBit: Bitmap? = null

}