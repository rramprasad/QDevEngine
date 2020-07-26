package com.rramprasad.qdevengine.recorder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class RecorderRepository {

    suspend fun saveVideo(previewFramesQueue: Queue<ByteArray>,outputFilePath : String) = withContext(Dispatchers.IO){
        val videoEncoder = VideoEncoder(outputFilePath)
        videoEncoder.encodeVideo(previewFramesQueue)
        return@withContext false
    }
}