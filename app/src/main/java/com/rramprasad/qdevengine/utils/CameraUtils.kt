package com.rramprasad.qdevengine.utils

import android.hardware.Camera
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.util.Log


class CameraUtils {

    private var mOptVideoWidth = 1920
    private var mOptVideoHeight = 1080

     fun getCameraOptimalVideoSize(camera : Camera, cameraPreview : CameraPreview) {
        try {
            val parameters: Camera.Parameters = camera.getParameters()
            val mSupportedPreviewSizes =
                parameters.supportedPreviewSizes
            val mSupportedVideoSizes =
                parameters.supportedVideoSizes
            val optimalSize: Camera.Size? = getOptimalVideoSize(
                mSupportedVideoSizes,
                mSupportedPreviewSizes, cameraPreview.width, cameraPreview.height
            )
            if (optimalSize != null) {
                mOptVideoWidth = optimalSize.width
                mOptVideoHeight = optimalSize.height
            }
        } catch (e: Exception) {
            Log.e("CameraUtils", "getCameraOptimalVideoSize: ", e)
        }
    }


    fun getOptimalVideoSize(
        supportedVideoSizes: List<Camera.Size>,
        previewSizes: List<Camera.Size>, w: Int, h: Int
    ): Camera.Size? {
        // Use a very small tolerance because we want an exact match.
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h

        // Supported video sizes list might be null, it means that we are allowed to use the preview
        // sizes
        val videoSizes: List<Camera.Size>
        videoSizes = supportedVideoSizes ?: previewSizes
        var optimalSize: Camera.Size? = null

        // Start with max value and refine as we iterate over available video sizes. This is the
        // minimum difference between view and camera height.
        var minDiff = Double.MAX_VALUE

        // Target view height

        // Try to find a video size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (size in videoSizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff && previewSizes.contains(
                    size
                )
            ) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        // Cannot find video size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in videoSizes) {
                if (Math.abs(size.height - h) < minDiff && previewSizes.contains(
                        size
                    )
                ) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }
}




