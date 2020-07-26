package com.rramprasad.qdevengine.recorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*

class VideoEncoder(val outputFilePath : String){

    private lateinit var mMediaMuxer: MediaMuxer
    private lateinit var mMediaCodec: MediaCodec
    private var isMuxFinish = true

    init {
        initializeMediaCodec()
    }

    private fun initializeMediaCodec(){
        //Encoding format, AVC corresponds to H264
        val mediaFormat: MediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1280, 720)
        //YUV 420  Corresponding to the image color sampling format
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        //Bit rate
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 3000000)
        //Frame rate
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        //I frame interval
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

            //Create and generate MP4 initialization object
            mMediaMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Enter the configuration state
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        //Perform life cycle execution status
        mMediaCodec.start()
    }

    fun encodeVideo(previewFramesQueue: Queue<ByteArray>) {
        var pts: Long = 0
        var generateIndex: Long = 0
        var mTrackIndex: Int = 0

        while (!isMuxFinish) {
            // get the subscript of the free input buffer
            val inputBufferId: Int = mMediaCodec.dequeueInputBuffer(-1)
            if (inputBufferId >= 0) {
                pts = computePresentationTime(generateIndex)
                //Effective empty buffer area
                val inputBuffer: ByteBuffer? = mMediaCodec.getInputBuffer(inputBufferId)
                val tempByte: ByteArray = previewFramesQueue.poll()
                if (isMuxFinish) {
                    break
                }
                inputBuffer?.put(tempByte!!)
                //Put the data into the encoding queue
                mMediaCodec.queueInputBuffer(inputBufferId, 0, tempByte.size, pts, 0)
                generateIndex += 1
            }
            val bufferInfo = MediaCodec.BufferInfo()
            //Get the out buffer Id output after successful encoding
            val outputBufferId: Int = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferId >= 0) {
                val outputBuffer: ByteBuffer? = mMediaCodec.getOutputBuffer(outputBufferId)
                val out = ByteArray(bufferInfo.size)
                outputBuffer?.get(out)
                writeBytesToFile(out)
                outputBuffer?.position(bufferInfo.offset)
                outputBuffer?.limit(bufferInfo.offset + bufferInfo.size)
                // Write the encoded data to the MP4 multiplexer
                if (outputBuffer != null) {
                    mMediaMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo)
                }
                //Release output buffer
                mMediaCodec.releaseOutputBuffer(outputBufferId, false)
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val mediaFormat: MediaFormat = mMediaCodec.getOutputFormat()
                mTrackIndex = mMediaMuxer.addTrack(mediaFormat)
                mMediaMuxer.start()
            }
        }
    }

    private fun writeBytesToFile(out: ByteArray) {
        var fos: FileOutputStream? = null

        try {
            fos = FileOutputStream(outputFilePath)
            fos.write(out)
        } catch (e: FileNotFoundException) {
            Log.d("VideoEncoder", "writeBytesToFile: $e")
        } catch (ioe: IOException) {
            Log.d("VideoEncoder", "writeBytesToFile: $ioe")
        } finally {
            try {
                fos?.close()
            } catch (ioe: IOException) {
                Log.d("VideoEncoder", "writeBytesToFile: $ioe")
            }
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private fun computePresentationTime(frameIndex: Long): Long {
        return 132 + frameIndex * 1000000 / 20
    }

    private fun NV21ToNV12(
        nv21: ByteArray?,
        nv12: ByteArray?,
        width: Int,
        height: Int
    ) {
        if (nv21 == null || nv12 == null) {
            return
        }
        val framesize = width * height
        var i: Int
        var j: Int
        System.arraycopy(nv21, 0, nv12, 0, framesize)
        i = 0
        while (i < framesize) {
            nv12[i] = nv21[i]
            i++
        }
        j = 0
        while (j < framesize / 2) {
            nv12[framesize + j - 1] = nv21[j + framesize]
            j += 2
        }
        j = 0
        while (j < framesize / 2) {
            nv12[framesize + j] = nv21[j + framesize - 1]
            j += 2
        }
    }


}