package com.rramprasad.qdevengine.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.VideoCapture.OnVideoSavedCallback
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.rramprasad.qdevengine.R
import kotlinx.android.synthetic.main.fragment_recorder.*
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class RecorderFragment : Fragment() {

    private lateinit var mVideoCapture: VideoCapture
    private val viewModel: RecorderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_recorder, container, false)
        Log.d(TAG, "onCreateView: ")
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (allPermissionsGranted()) {
            startCameraPreview()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        Log.d(TAG, "allPermissionsGranted: ")
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: ")
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCameraPreview()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startCameraPreview() {
        Log.d(TAG, "startCameraPreview: ")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            Log.d(TAG, "startCameraPreview: cameraProviderFutureListener")
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), VideoFrameAnalyzer())
                }

            // Select back camera
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                preview.setSurfaceProvider(previewview_finder.createSurfaceProvider())

                // Create a configuration object for the video use case
                /*mVideoCapture = VideoCaptureConfig.Builder().apply {
                    setTargetRotation(previewview_finder.display.rotation)

                }.build()*/

                mVideoCapture = VideoCapture.Builder().apply {
                    setTargetRotation(previewview_finder.display.rotation)
                    setAudioRecordSource(MediaRecorder.AudioSource.MIC)
                }.build()

                val file = File(requireActivity().filesDir,"clip_0")
                mVideoCapture.startRecording(file, Executors.newSingleThreadExecutor(), object : VideoCapture.OnVideoSavedCallback {
                    override fun onVideoSaved(file: File) {

                        val msg = "Video capture succeeded: ${file.absolutePath}"
                        previewview_finder.post {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                        val msg = "Video capture failed: $message"
                        previewview_finder.post {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        }
                    }
                })

                cameraProvider.bindToLifecycle(this, cameraSelector,preview, imageAnalyzer, mVideoCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        /*mVideoCapture.let{

        }
        mVideoCapture.stopRecording()*/
    }
}

private class VideoFrameAnalyzer() : ImageAnalysis.Analyzer {
    init {

    }

    override fun analyze(image: ImageProxy) {
        Log.d("VideoFrameAnalyzer", "analyze: ${image.format}")
        Log.d("VideoFrameAnalyzer", "analyze: ${image.planes}")

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        Log.d("VideoFrameAnalyzer", "analyze: ${data.size}")
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

}
