package com.reciperoulette.ui.camera

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandler(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        content()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFFE94560),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Camera Permission Needed",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Recipe Roulette needs camera access to scan your fridge and detect ingredients.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                ) {
                    Text("Grant Camera Access", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onBack) {
                    Text("Go Back", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}