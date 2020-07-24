package com.rramprasad.qdevengine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val userId : String = savedStateHandle["uid"] ?: ""
}
