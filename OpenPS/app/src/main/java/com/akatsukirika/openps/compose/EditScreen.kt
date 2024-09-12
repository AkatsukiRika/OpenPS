package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.R
import com.akatsukirika.openps.viewmodel.EditViewModel

// 人像美颜
const val INDEX_SMOOTH = 0
const val INDEX_WHITE = 1
const val INDEX_LIPSTICK = 2
const val INDEX_BLUSHER = 3
const val INDEX_EYE_ZOOM = 4
const val INDEX_FACE_SLIM = 5
// 编辑小项
const val INDEX_CONTRAST = 0
const val INDEX_EXPOSURE = 1
// 处理状态
const val STATUS_IDLE = 10
const val STATUS_LOADING = 11
const val STATUS_SUCCESS = 12
const val STATUS_ERROR = 13
// 一级TAB
const val TAB_BEAUTIFY = 0
const val TAB_ADJUST = 1

@Composable
fun EditScreen(viewModel: EditViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_compare),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        viewModel.helper?.onCompareBegin()
                        tryAwaitRelease()
                        viewModel.helper?.onCompareEnd()
                    })
                }
        )

        MainColumn(viewModel)
    }
}

@Composable
private fun MainColumn(viewModel: EditViewModel) {
    val currentLevel = viewModel.currentLevel.collectAsState(initial = 0f).value
    val selectedFunctionIndex = viewModel.selectedFunctionIndex.collectAsState(initial = -1).value
    val selectedTabIndex = viewModel.selectedTabIndex.collectAsState(initial = 0).value
    val itemList = viewModel.itemList.collectAsState(initial = emptyList()).value
    val loadStatus = viewModel.loadStatus.collectAsState(initial = STATUS_IDLE).value

    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .background(AppColors.DarkBG)
    ) {
        val selectedItem = itemList.getOrNull(selectedFunctionIndex)
        if (selectedItem != null) {
            if (selectedItem.hasTwoWaySlider) {
                BidirectionalSlider(
                    value = currentLevel,
                    onValueChange = {
                        viewModel.onValueChange(it)
                    },
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                    trackColor = AppColors.Green500.copy(alpha = SliderDefaults.InactiveTrackAlpha),
                    highlightColor = AppColors.Green500
                )
            } else {
                Slider(
                    value = currentLevel,
                    onValueChange = {
                        viewModel.onValueChange(it)
                    },
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Green500,
                        activeTrackColor = AppColors.Green500
                    )
                )
            }
        }

        Box {
            Column {
                FunctionList(
                    modifier = Modifier.height(84.dp),
                    itemList = itemList,
                    selectedIndex = selectedFunctionIndex,
                    onSelect = {
                        viewModel.onSelect(it)
                    }
                )

                BottomTabRow(
                    modifier = Modifier.height(28.dp),
                    selectedIndex = selectedTabIndex,
                    onSelect = {
                        viewModel.updateSelectedTab(it)
                    }
                )
            }

            if (loadStatus == STATUS_LOADING) {
                LoadingMask(modifier = Modifier
                    .fillMaxWidth()
                    .height((84 + 28).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                )
            }
        }
    }
}

@Composable
private fun BottomTabRow(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    TabRow(modifier = modifier, selectedTabIndex = selectedIndex, backgroundColor = AppColors.DarkBG) {
        Tab(selected = selectedIndex == TAB_BEAUTIFY, onClick = {
            onSelect(TAB_BEAUTIFY)
        }) {
            Text(
                text = stringResource(id = R.string.beautify),
                color = if (selectedIndex == TAB_BEAUTIFY) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Tab(selected = selectedIndex == TAB_ADJUST, onClick = {
            onSelect(TAB_ADJUST)
        }) {
            Text(
                text = stringResource(id = R.string.adjust),
                color = if (selectedIndex == TAB_ADJUST) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FunctionList(
    modifier: Modifier = Modifier,
    itemList: List<FunctionItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item {
            Spacer(modifier = Modifier.width(8.dp))
        }

        items(itemList) { item ->
            FunctionListItem(item, isSelected = item.index == selectedIndex, onClick = {
                onSelect(item.index)
            })
        }

        item {
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
private fun FunctionListItem(item: FunctionItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable {
                onClick()
            }
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = null,
            tint = if (isSelected) AppColors.Green200 else Color.White,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) AppColors.Green200 else Color.White
        )
    }
}

@Composable
private fun LoadingMask(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.5f))) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = AppColors.Green500
        )
    }
}

data class FunctionItem(
    val index: Int,
    val icon: Int,
    val name: String,
    val hasTwoWaySlider: Boolean = false
)