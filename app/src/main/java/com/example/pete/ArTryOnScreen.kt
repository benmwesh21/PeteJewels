package com.example.pete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pete.ui.theme.LuxuryGold
import com.example.pete.ui.theme.PeteTheme

@Composable
fun ArTryOnScreen(
    item: JewelryItem,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Placeholder for AR View (e.g., SceneView or ARCore Surface)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = LuxuryGold)
            Spacer(Modifier.height(16.dp))
            Text(
                "Initializing AR Experience...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Calibrating for ${stringResource(item.nameRes)}",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // UI Overlays
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.4f), shape = MaterialTheme.shapes.medium)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(item.nameRes),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { /* Switch Camera */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Cameraswitch, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Flip", color = Color.White)
                }
                Button(
                    onClick = { /* Capture Screenshot */ },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                ) {
                    Text("Capture", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
fun ArTryOnPreview() {
    PeteTheme {
        ArTryOnScreen(item = jewelryList[0], onClose = {})
    }
}
