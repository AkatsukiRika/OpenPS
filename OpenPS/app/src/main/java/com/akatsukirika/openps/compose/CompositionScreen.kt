package com.akatsukirika.openps.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.R
import com.akatsukirika.openps.utils.clickableNoIndication
import com.akatsukirika.openps.viewmodel.CompositionViewModel
import com.akatsukirika.openps.viewmodel.FlipEvent
import com.akatsukirika.openps.viewmodel.MirrorEvent
import kotlinx.coroutines.launch

enum class CompositionTab(val index: Int) {
    CROP(0),
    ROTATE(1),
    PERSPECTIVE(2)
}

enum class CropOptions(
    val index: Int,
    @DrawableRes val iconRes: Int = R.drawable.ic_freeform,
    @StringRes val nameRes: Int = R.string.freeform,
    val ratio: Float = 0f
) {
    CUSTOM(0, iconRes = R.drawable.ic_freeform, nameRes = R.string.freeform),
    ORIGINAL(1, iconRes = R.drawable.ic_original, nameRes = R.string.filter_original),
    RATIO_1_1(2, iconRes = R.drawable.ic_ratio_1_1, nameRes = R.string.ratio_1_1, ratio = 1f),
    RATIO_2_3(3, iconRes = R.drawable.ic_ratio_2_3, nameRes = R.string.ratio_2_3, ratio = 2 / 3f),
    RATIO_3_2(4, iconRes = R.drawable.ic_ratio_3_2, nameRes = R.string.ratio_3_2, ratio = 3 / 2f),
    RATIO_3_4(5, iconRes = R.drawable.ic_ratio_3_4, nameRes = R.string.ratio_3_4, ratio = 3 / 4f),
    RATIO_4_3(6, iconRes = R.drawable.ic_ratio_4_3, nameRes = R.string.ratio_4_3, ratio = 4 / 3f),
    RATIO_9_16(7, iconRes = R.drawable.ic_ratio_9_16, nameRes = R.string.ratio_9_16, ratio = 9 / 16f),
    RATIO_16_9(8, iconRes = R.drawable.ic_ratio_16_9, nameRes = R.string.ratio_16_9, ratio = 16 / 9f),
}

@Composable
fun CompositionScreen(viewModel: CompositionViewModel, visible: Boolean) {
    val selectedTab = viewModel.currentTab.collectAsState().value
    val selectedCropOption = viewModel.currentCropOptions.collectAsState().value
    val canSave = viewModel.canSave.collectAsState().value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.padding(bottom = 8.dp),
            shape = RoundedCornerShape(100.dp),
            enabled = canSave
        ) {
            Text(text = stringResource(id = R.string.save_changes))
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(AppColors.DarkBG)
            .onSizeChanged {
                val height = it.height
                if (visible) {
                    viewModel.bottomScreenHeight.value = height.toFloat()
                } else {
                    viewModel.bottomScreenHeight.value = 0f
                }
            }
        ) {
            when (selectedTab) {
                CompositionTab.CROP -> {
                    CropOptionList(
                        modifier = Modifier.height(84.dp),
                        selected = selectedCropOption,
                        onSelect = {
                            viewModel.currentCropOptions.value = it
                        }
                    )
                }

                CompositionTab.ROTATE -> {
                    RotateOptionList(modifier = Modifier.height(84.dp), viewModel)
                }

                CompositionTab.PERSPECTIVE -> {
                    PerspectiveOptionList(modifier = Modifier.height(84.dp))
                }
            }

            BottomTabRow(selectedTab = selectedTab, onSelect = {
                viewModel.currentTab.value = it
            })
        }
    }
}

@Composable
private fun CropOptionList(
    modifier: Modifier = Modifier,
    selected: CropOptions,
    onSelect: (CropOptions) -> Unit
) {
    LazyRow(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item {
            Spacer(modifier = Modifier.width(8.dp))
        }

        items(CropOptions.entries) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(70.dp)
                    .clickableNoIndication {
                        onSelect(it)
                    }
            ) {
                Icon(
                    painter = painterResource(id = it.iconRes),
                    contentDescription = null,
                    tint = if (it == selected) AppColors.Green200 else Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(id = it.nameRes),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (it == selected) AppColors.Green200 else Color.White
                )
            }
        }

        item {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun RotateOptionList(modifier: Modifier = Modifier, viewModel: CompositionViewModel) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .width(70.dp)
                .fillMaxHeight()
                .clickable {}
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_left),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.rotate_left),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .width(70.dp)
                .fillMaxHeight()
                .clickable {}
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_left),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = -1f
                    },
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.rotate_right),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .width(70.dp)
                .fillMaxHeight()
                .clickable {
                    scope.launch {
                        viewModel.mirrorEvent.emit(MirrorEvent)
                    }
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mirror),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.rotate_mirror),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .clickable {
                    scope.launch {
                        viewModel.flipEvent.emit(FlipEvent)
                    }
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mirror),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(90f),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.rotate_flip),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PerspectiveOptionList(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .width(70.dp)
                .fillMaxHeight()
                .clickable {}
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_perspective_vertical),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.perspective_horizontal),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .clickable {}
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_perspective_vertical),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(-90f),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.perspective_vertical),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BottomTabRow(
    modifier: Modifier = Modifier,
    selectedTab: CompositionTab,
    onSelect: (CompositionTab) -> Unit
) {
    TabRow(modifier = modifier, selectedTabIndex = selectedTab.index, backgroundColor = AppColors.DarkBG) {
        Tab(selected = selectedTab == CompositionTab.CROP, onClick = {
            onSelect(CompositionTab.CROP)
        }) {
            Text(
                text = stringResource(id = R.string.crop),
                color = if (selectedTab == CompositionTab.CROP) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Tab(selected = selectedTab == CompositionTab.ROTATE, onClick = {
            onSelect(CompositionTab.ROTATE)
        }) {
            Text(
                text = stringResource(id = R.string.rotate),
                color = if (selectedTab == CompositionTab.ROTATE) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Tab(selected = selectedTab == CompositionTab.PERSPECTIVE, onClick = {
            onSelect(CompositionTab.PERSPECTIVE)
        }) {
            Text(
                text = stringResource(id = R.string.perspective),
                color = if (selectedTab == CompositionTab.PERSPECTIVE) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}