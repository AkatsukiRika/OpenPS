package com.akatsukirika.openps.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.akatsukirika.openps.R
import com.akatsukirika.openps.model.GalleryImage
import com.akatsukirika.openps.utils.clickableNoIndication
import com.akatsukirika.openps.viewmodel.GalleryViewModel

private const val LABEL_ANIMATED_CONTENT = "animated_content"
private const val SHARED_ELEMENT_KEY_IMAGE = "image"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    val previewImage = viewModel.previewImage.collectAsState(null).value

    MaterialTheme {
        SharedTransitionLayout {
            AnimatedContent(targetState = previewImage, label = LABEL_ANIMATED_CONTENT) { targetState ->
                if (targetState == null) {
                    GalleryLayout(
                        viewModel = viewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                } else {
                    PreviewLayout(
                        viewModel = viewModel,
                        previewImage = targetState,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
        }

        BackHandler(enabled = previewImage != null) {
            viewModel.updatePreviewImage(null)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GalleryLayout(
    viewModel: GalleryViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))

        AlbumLazyRow(viewModel = viewModel)

        Spacer(modifier = Modifier.height(12.dp))

        ImagesGrid(
            viewModel = viewModel,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PreviewLayout(
    viewModel: GalleryViewModel,
    previewImage: GalleryImage,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Box(modifier = Modifier.fillMaxSize()) {
        with(sharedTransitionScope) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(previewImage.uri)
                    .crossfade(false)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = SHARED_ELEMENT_KEY_IMAGE + "_" + previewImage.uri),
                        animatedVisibilityScope
                    ),
                contentScale = ContentScale.Fit
            )
        }

        IconButton(onClick = {
            viewModel.updatePreviewImage(null)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cancel),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp)
                    .size(20.dp)
            )
        }

        Text(
            text = "${previewImage.format.displayName} | ${previewImage.width} x ${previewImage.height} | ${previewImage.getHumanizedSize()}",
            color = Color.White.copy(0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            fontSize = 14.sp
        )
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ImagesGrid(
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
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
                ImageGridItem(it, viewModel, sharedTransitionScope, animatedVisibilityScope)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ImageGridItem(
    image: GalleryImage,
    viewModel: GalleryViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    
    Box(modifier = Modifier
        .aspectRatio(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .clickable {
            viewModel.selectImage(image.uri)
        }
    ) {
        with(sharedTransitionScope) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(image.uri)
                    .crossfade(true)
                    .size(GalleryViewModel.THUMBNAIL_SIZE)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = SHARED_ELEMENT_KEY_IMAGE + "_" + image.uri),
                        animatedVisibilityScope
                    ),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.DarkGray)
            )
        }

        MagnifyButton(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 2.dp, end = 2.dp),
            image, viewModel
        )
    }
}

@Composable
private fun MagnifyButton(modifier: Modifier = Modifier, image: GalleryImage, viewModel: GalleryViewModel) {
    Box(modifier = modifier
        .size(24.dp)
        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        .clickableNoIndication {
            viewModel.updatePreviewImage(image)
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_preview_magnify),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(4.dp)
        )
    }
}