package com.example.fragmentsnavigation.presentation.camera

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fragmentsnavigation.R
import com.example.fragmentsnavigation.databinding.FragmentCameraBinding
import java.io.File

private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)


class CameraFragment: Fragment(R.layout.fragment_camera) {
    private lateinit var fragmentCameraBinding: FragmentCameraBinding

    private val cameraXHelper by lazy {
        CameraXHelper(
            caller = this,
            previewView = fragmentCameraBinding.previewView,
            textView = fragmentCameraBinding.resultTextView,
            onPictureTaken = { file, uri ->
                Log.e("", "Picture taken ${file.absolutePath}, uri=$uri")
            },
            onError = {Log.e("", "Got error $it")},
            onBarcodeFound = {
                fragmentCameraBinding.resultTextView.text = it
            },

            //builderPreview = Preview.Builder().setTargetResolution(Size(200, 200)),
            builderImageCapture = ImageCapture.Builder().setTargetResolution(Size(200, 200)),
            filesDirectory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "camerax_sample"
            )
        )
    }

    private val permissionsRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                Log.e("", "All permissions granted")
            }
            else {
                Log.e("","No permissions granted")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        if (hasPermissions(requireContext())){

            fragmentCameraBinding.btnScan.setOnClickListener{
                Log.d("", "btn scan clicked")
                val b = bundleOf("orderName" to fragmentCameraBinding.resultTextView.text)
                findNavController().navigate(R.id.action_cameraFragment_to_orderDetailsFragment, b)
            }

            fragmentCameraBinding.btnEnterName.setOnClickListener {
                showDialog{ orderName ->
                    val b = bundleOf("orderName" to orderName)
                    findNavController().navigate(R.id.action_cameraFragment_to_orderDetailsFragment, b)
                }
            }

        }
        else {
            permissionsRequestLauncher.launch(
                PERMISSIONS_REQUIRED
            )
        }

        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraXHelper.start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cameraXHelper.start()
    }

    private fun showDialog(onAccepted: (String) -> Unit){
        activity?.apply {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Введите номер заказа")
            val inputText = EditText(this)
            inputText.hint = "Номер заказа"
            inputText.inputType = InputType.TYPE_CLASS_TEXT
            dialogBuilder.setView(inputText)

            dialogBuilder.setPositiveButton(
                "Ok",
                DialogInterface.OnClickListener { dialog, id ->
                    val orderName = inputText.text.toString()
                    Log.d("", "Need to go to next screen with order name: $orderName")
                    onAccepted(orderName)
                }
            )
            dialogBuilder.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                }
            )
            dialogBuilder.show()
        } ?: throw IllegalStateException("No activity")

    }

    companion object {
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}