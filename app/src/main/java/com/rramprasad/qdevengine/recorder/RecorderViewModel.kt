package com.rramprasad.qdevengine.recorder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class RecorderViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val userId : String = savedStateHandle["uid"] ?: ""
}
