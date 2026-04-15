package com.reciperoulette.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reciperoulette.viewmodel.RecipeViewModel
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onPhotoTaken: (Bitmap) -> Unit,
    onBack: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                setupCamera(ctx, lifecycleOwner, previewView) { capture ->
                    imageCapture = capture
                }
                previewView
            }
        )

        // Top bar overlay
        TopAppBar(
            title = {
                Text(
                    "Point at your fridge 🥶",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.5f)
            ),
            modifier = Modifier.statusBarsPadding()
        )

        // Viewfinder frame hint
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.75f)
                .align(Alignment.Center)
                .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        )

        // Bottom capture controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Make sure the fridge contents are visible",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )

            // Capture button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(3.dp, Color.White, CircleShape)
            ) {
                IconButton(
                    onClick = {
                        if (!isCapturing) {
                            isCapturing = true
                            takePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                executor = cameraExecutor,
                                onSuccess = { bitmap, filePath ->
                                    recipeViewModel.setCapturedBitmap(bitmap)
                                    recipeViewModel.analyzeImage(bitmap)
                                    onPhotoTaken(bitmap)
                                    isCapturing = false
                                },
                                onError = { isCapturing = false }
                            )
                        }
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            Icons.Filled.Camera,
                            contentDescription = "Capture",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    executor: java.util.concurrent.Executor,
    onSuccess: (Bitmap, String) -> Unit,
    onError: () -> Unit
) {
    imageCapture ?: run { onError(); return }

    val photoFile = File(context.cacheDir, "fridge_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                // Resize to save on Gemini tokens
                val resized = resizeBitmap(bitmap, 1024)
                onSuccess(resized, photoFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                onError()
            }
        }
    )
}

private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val ratio = maxDimension.toFloat() / maxOf(width, height)
    if (ratio >= 1f) return bitmap
    val matrix = Matrix()
    matrix.postScale(ratio, ratio)
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
}