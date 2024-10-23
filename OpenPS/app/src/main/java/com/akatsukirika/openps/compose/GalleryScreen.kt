package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.akatsukirika.openps.model.GalleryImage
import com.akatsukirika.openps.viewmodel.GalleryViewModel

@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))

            AlbumLazyRow(viewModel = viewModel)

            Spacer(modifier = Modifier.height(12.dp))
            
            ImagesGrid(viewModel = viewModel)
        }
    }
}

@Composable
private fun AlbumLazyRow(modifier: Modifier = Modifier, viewModel: GalleryViewModel) {
    val context = LocalContext.current
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
                    viewModel.updateSelectedAlbum(context, it)
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

@Composable
private fun ImagesGrid(modifier: Modifier = Modifier, viewModel: GalleryViewModel) {
    val selectedAlbum = viewModel.selectedAlbum.collectAsState(initial = null).value

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (selectedAlbum != null) {
            items(
                items = selectedAlbum.images,
                key = {
                    it.uri.toString()
                }
            ) {
                ImageGridItem(image = it)
            }
        }
    }
}

@Composable
private fun ImageGridItem(image: GalleryImage) {
    val context = LocalContext.current
    
    Box(modifier = Modifier
        .aspectRatio(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image.uri)
                .crossfade(true)
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}