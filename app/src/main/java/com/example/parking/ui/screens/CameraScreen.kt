package com.example.parking.ui.screens

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build()
                val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

                preview.setSurfaceProvider(previewView.surfaceProvider)

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        })

        Button(
            onClick = {
                val photoFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "${System.currentTimeMillis()}.jpg"
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        Log.d("CameraScreen", "Photo captured: $savedUri")
                        onImageCaptured(savedUri) // Pass the latest image URI.
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                    }
                })

            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Ta Bild")
        }
    }
}
