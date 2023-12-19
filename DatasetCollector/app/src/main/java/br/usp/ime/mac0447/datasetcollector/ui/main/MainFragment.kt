package br.usp.ime.mac0447.datasetcollector.ui.main

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import br.usp.ime.mac0447.datasetcollector.R
import br.usp.ime.mac0447.datasetcollector.databinding.FragmentMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class MainFragment : Fragment() {

    private val SAVE_IMAGE_REQUEST_CODE: Int = 2122
    private val CAMERA_REQUEST_CODE: Int = 2120
    val CAMERA_PERMISSION_REQUEST_CODE: Int = 2121
    private lateinit var currentPhotoPath: String
    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        viewModel.objTypes.observe(viewLifecycleOwner, Observer {
            objTypes -> binding.actObjTypeName.setAdapter(ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, objTypes))
        })
        binding.btnAddPhoto.setOnClickListener {
            prepTakePhoto()
        }
    }

    private fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            val permissionRequest = arrayOf(android.Manifest.permission.CAMERA)
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(requireContext(), "Unable to take photo without permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(requireContext().packageManager)
            if (takePictureIntent == null) {
                Toast.makeText(requireContext(), "Unable to save photo", Toast.LENGTH_LONG).show()
            } else {
                val photoFile: File = createImageFile()
                photoFile?.also {
                    val photoUri = FileProvider.getUriForFile(requireActivity().applicationContext, "br.usp.ime.mac0447.datasetcollector.android.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, SAVE_IMAGE_REQUEST_CODE)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SAVE_IMAGE_REQUEST_CODE) {
                Toast.makeText(requireContext(), "Image saved", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val objType: String = binding.actObjTypeName.text.toString()
        val objName: String = binding.txtObjName.text.toString()
        val bgColor: String =
            if (binding.radioFundo.checkedRadioButtonId == R.id.radioClaro) "Claro"
            else "Escuro"

        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("${objType}_${objName}_${bgColor}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}