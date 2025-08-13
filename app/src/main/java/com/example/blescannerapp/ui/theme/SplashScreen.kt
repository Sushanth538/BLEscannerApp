package com.example.blescannerapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.blescannerapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Wait 2 seconds then call onTimeout
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    // Gradient background
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF616161), // Light black / dark gray
            Color(0xFF616161) // Light black / dark gray
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // your uploaded image name
            contentDescription = null,
            modifier = Modifier.size(450.dp)
        )
    }
}
