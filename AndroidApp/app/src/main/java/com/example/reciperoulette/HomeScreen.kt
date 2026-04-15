package com.reciperoulette.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onStartClick: () -> Unit) {

    // Spinning dice animation
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diceRotation"
    )

    // Pulsing button animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Background decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-200).dp)
                .clip(CircleShape)
                .background(Color(0xFFE94560).copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 100.dp, y = 250.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F3460).copy(alpha = 0.3f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {

            // Spinning dice icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE94560), Color(0xFFFF6B6B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Casino,
                    contentDescription = "Dice",
                    tint = Color.White,
                    modifier = Modifier
                        .size(60.dp)
                        .rotate(rotation)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Recipe\nRoulette",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp,
                letterSpacing = (-1).sp
            )

            // Subtitle
            Text(
                text = "Open your fridge.\nSpin the wheel.\nEat something wild. 🎲",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Button
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "📸  Scan My Fridge",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Footer hint
            Text(
                text = "Powered by Google Gemini Vision",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}