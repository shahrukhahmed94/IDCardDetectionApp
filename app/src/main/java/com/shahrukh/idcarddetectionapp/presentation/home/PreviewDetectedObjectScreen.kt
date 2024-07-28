package com.shahrukh.idcarddetectionapp.presentation.home

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahrukh.idcarddetectionapp.presentation.utils.Constants.capturedImageBit

@Composable
fun PreviewDetectedObjectScreen(
    //croppedImage: Bitmap
) {


    println("Preview Detected Object Screen Called")


  //  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {


        Text(text = "Screen Called!")

        Spacer(modifier = Modifier.height(20.dp))
        (capturedImageBit?.asImageBitmap() )?.let {

            Log.i("Captured Image Bitmap", capturedImageBit?.asImageBitmap().toString())
            Image(
                // bitmap = croppedImage.asImageBitmap(),
                bitmap = it,
                contentDescription = "Detected Object",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
