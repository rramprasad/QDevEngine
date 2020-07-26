package com.rramprasad.qdevengine.utils

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class CameraPreview(context: Context, private val previewCallback : Camera.PreviewCallback) : SurfaceView(context),SurfaceHolder.Callback {

    private var mCamera: Camera? = null

    init {
        holder.addCallback(this)
    }

    private fun getCameraInstance(): Camera? {
        var camera: Camera? = null
        try {
            camera = Camera.open(0)
        } catch (e: Exception) {
            Log.d("CameraPreview", "camera is not available")
        }
        return camera
    }


    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        holder.removeCallback(this)
        mCamera?.setPreviewCallback(null)
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        mCamera = getCameraInstance()
        try {
            mCamera?.setPreviewCallback(previewCallback)
            var parameters = mCamera?.getParameters()
            if (parameters == null) {
                parameters = mCamera?.getParameters()
            }
            parameters?.setPreviewFormat(ImageFormat.NV21)
            parameters?.setPreviewSize(1280, 720)
            mCamera?.setParameters(parameters)

            mCamera?.setPreviewDisplay(holder)
            mCamera?.startPreview()
            mCamera?.let {
                CameraUtils().getCameraOptimalVideoSize(it,this)
            }
        } catch (e: IOException) {
            Log.d("CameraPreview", "Error setting camera preview: " + e.message)
        }
    }
}