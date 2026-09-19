package com.takeawaypro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.takeawaypro.app.Product

/**
 * Shows the product's real photo (picked by the admin in [AdminProductFormScreen])
 * when available, falling back to its emoji icon otherwise. Used everywhere a
 * product appears so a photo added in the admin console shows up for customers too.
 */
@Composable
fun ProductThumbnail(
    product: Product,
    size: Dp = 44.dp,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (product.imageUri != null) {
            AsyncImage(
                model = product.imageUri,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Text(product.emoji, fontSize = (size.value / 1.8f).sp)
        }
    }
}
