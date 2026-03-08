package com.cardscanner.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.NavHostFragment
import com.cardscanner.app.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentPhotoUri: Uri? = null

    val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoUri != null) navigateToPreview(currentPhotoUri!!)
    }
    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { navigateToPreview(it) }
    }
    val cameraPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun requestCameraOrLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) launchCamera()
        else cameraPermLauncher.launch(Manifest.permission.CAMERA)
    }

    fun launchCamera() {
        val f = File.createTempFile("card_", ".jpg", cacheDir)
        currentPhotoUri = FileProvider.getUriForFile(this, "${packageName}.provider", f)
        takePictureLauncher.launch(currentPhotoUri)
    }

    fun launchGallery() = pickImageLauncher.launch("image/*")

    private fun navigateToPreview(uri: Uri) {
        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment).navController
        nav.navigate(R.id.action_home_to_preview,
            Bundle().apply { putString("imageUri", uri.toString()) })
    }
}
