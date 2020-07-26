package com.rramprasad.qdevengine.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.rramprasad.qdevengine.R
import com.rramprasad.qdevengine.utils.CameraPreview
import com.rramprasad.qdevengine.utils.CameraUtils
import kotlinx.android.synthetic.main.fragment_recorder.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class RecorderFragment : Fragment(), Camera.PreviewCallback {
    private lateinit var mRunnable: Runnable
    private lateinit var mHandler: Handler
    private lateinit var mCameraPreview: CameraPreview
    private val mCameraUtils: CameraUtils = CameraUtils()
    private val viewModel: RecorderViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recorder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            // Start camera preview
            mCameraPreview = CameraPreview(requireContext(),this)
            surface_view_frame_layout.addView(mCameraPreview)

            startFrequentStoring()

            // Playlist button click event
            playlist_imageview.setOnClickListener {
                playlist_imageview.findNavController().navigate(R.id.action_camera_preview_fragment_to_player_fragment)
            }
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // Start camera preview
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (data != null) {
            viewModel.addPreviewFrame(data)
        }
    }

    private fun startFrequentStoring() {
        mHandler = Handler()
        mRunnable = Runnable {
            val totalFilesCount = requireActivity().filesDir.length()
            if(totalFilesCount >= 2){
                requireActivity().filesDir.listFiles()?.get(0)?.delete()
            }
            val fileName = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss",Locale.getDefault()).format(Date())
            val file = File(requireActivity().filesDir.absolutePath, "$fileName.mp4")
            viewModel.saveVideo(file.absolutePath).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            })
            mHandler.postDelayed(mRunnable,60*1000)
        }
        mHandler.postDelayed(mRunnable, 60*1000)
    }
}
