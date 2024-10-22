package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.viewmodel.GalleryViewModel

@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))

            AlbumLazyRow(viewModel = viewModel)
        }
    }
}

@Composable
private fun AlbumLazyRow(modifier: Modifier = Modifier, viewModel: GalleryViewModel) {
    val albumList = viewModel.albumList.collectAsState(initial = emptyList()).value
    val selectedAlbum = viewModel.selectedAlbum.collectAsState(initial = null).value

    LazyRow(modifier = modifier) {
        item {
            Spacer(modifier = Modifier.width(16.dp))
        }

        items(albumList) {
            Box(modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .height(36.dp)
                .background(if (it != selectedAlbum) AppColors.DarkBG else AppColors.Green500)
                .border(1.dp, Color.Gray, RoundedCornerShape(24.dp))
                .clickable {
                    viewModel.updateSelectedAlbum(it)
                }
            ) {
                Text(
                    text = it.albumName,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        item {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}