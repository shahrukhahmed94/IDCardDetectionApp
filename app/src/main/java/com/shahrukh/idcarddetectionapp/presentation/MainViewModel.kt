package com.shahrukh.idcarddetectionapp.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahrukh.idcarddetectionapp.domain.manager.ObjectDetectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MainViewModel@Inject constructor(
    objectDetectionManager: ObjectDetectionManager
):  ViewModel() {
    companion object {
        private val TAG: String? = MainViewModel::class.simpleName
    }


    init {

    }
}