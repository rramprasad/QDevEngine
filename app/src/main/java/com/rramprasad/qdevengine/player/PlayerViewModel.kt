package com.rramprasad.qdevengine.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PlayerViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val userId : String = savedStateHandle["uid"] ?: ""
}
