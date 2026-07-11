package com.connect.watch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun WatchfaceStoreScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Premium Watchfaces")
        Spacer(modifier = Modifier.height(16.dp))
        // Example Watchface Card
        AsyncImage(
            model = "https://example.com/watchface.png",
            contentDescription = "Watchface Preview",
            modifier = Modifier.size(150.dp)
        )
        Text("Author: Connect Premium")
    }
}
