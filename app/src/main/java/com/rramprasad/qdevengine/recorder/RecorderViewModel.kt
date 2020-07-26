package com.rramprasad.qdevengine.recorder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class RecorderViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val previewFramesQueue: Queue<ByteArray> = LinkedList()
    private val saveVideoResponse = MutableLiveData<String>()
    private val recorderRepository : RecorderRepository = RecorderRepository()

    fun addPreviewFrame(data: ByteArray) {
        previewFramesQueue.add(data)
    }

    fun saveVideo(outputFilePath : String) : MutableLiveData<String> {
        val tempFramesQueue: Queue<ByteArray> = LinkedList()
        previewFramesQueue.toCollection(tempFramesQueue)

        previewFramesQueue.clear()

        viewModelScope.launch {
            val videoSaved = recorderRepository.saveVideo(tempFramesQueue,outputFilePath)
            if(videoSaved){
                tempFramesQueue.clear()
                saveVideoResponse.postValue("Video Saved successfully")
            }
            else{
                saveVideoResponse.postValue("Video not saved")
            }
        }

        return saveVideoResponse
    }

}
