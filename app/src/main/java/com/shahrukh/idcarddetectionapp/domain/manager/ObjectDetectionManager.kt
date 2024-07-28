package com.shahrukh.idcarddetectionapp.domain.manager

import android.graphics.Bitmap
import com.shahrukh.idcarddetectionapp.domain.model.Detection


/**
 * Interface responsible for managing object detection operations.
 */
interface ObjectDetectionManager {
    fun detectObjectsInCurrentFrame(
        bitmap: Bitmap,
        rotation: Int,
        confidenceThreshold: Float
    ): List<Detection>
}