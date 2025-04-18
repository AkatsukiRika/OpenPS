package com.akatsukirika.openps.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.R
import com.akatsukirika.openps.model.FunctionItem
import com.akatsukirika.openps.repo.INDEX_ORIGINAL
import com.akatsukirika.openps.utils.clickableNoIndication
import com.akatsukirika.openps.viewmodel.EditViewModel
import kotlin.math.roundToInt

@Composable
fun ImageEffectScreen(viewModel: EditViewModel, modifier: Modifier = Modifier) {
    val currentLevel = viewModel.currentLevel.collectAsState(initial = 0f).value
    val selectedFunctionIndex = viewModel.selectedFunctionIndex.collectAsState(initial = -1).value
    val selectedFilterIndex = viewModel.selectedFilterIndex.collectAsState(initial = -1).value
    val selectedTabIndex = viewModel.selectedTabIndex.collectAsState(initial = 0).value
    val beautifyLevelMap = viewModel.beautifyLevelMap.collectAsState(initial = emptyMap()).value
    val adjustLevelMap = viewModel.adjustLevelMap.collectAsState(initial = emptyMap()).value
    val itemList = viewModel.itemList.collectAsState(initial = emptyList()).value
    val loadStatus = viewModel.loadStatus.collectAsState(initial = STATUS_IDLE).value

    Column(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .background(AppColors.DarkBG)
    ) {
        val selectedItem = if (selectedTabIndex == TAB_FILTER) {
            itemList.getOrNull(selectedFilterIndex)
        } else {
            itemList.getOrNull(selectedFunctionIndex)
        }
        if (selectedItem != null && !selectedItem.isOriginal) {
            if (selectedItem.hasTwoWaySlider) {
                BidirectionalSliderLayout(viewModel, currentLevel)
            } else {
                SliderLayout(viewModel, currentLevel)
            }
        }

        Box {
            Column {
                if (selectedTabIndex == TAB_FILTER) {
                    FilterList(
                        modifier = Modifier.height(108.dp),
                        itemList = itemList,
                        selectedIndex = selectedFilterIndex,
                        onSelect = {
                            viewModel.onSelect(it)
                        }
                    )
                } else {
                    FunctionList(
                        modifier = Modifier.height(84.dp),
                        itemList = itemList,
                        levelMap = if (selectedTabIndex == TAB_BEAUTIFY) beautifyLevelMap else adjustLevelMap,
                        selectedIndex = selectedFunctionIndex,
                        onSelect = {
                            viewModel.onSelect(it)
                        }
                    )
                }

                BottomTabRow(
                    modifier = Modifier.height(28.dp),
                    selectedIndex = selectedTabIndex,
                    loadStatus = loadStatus,
                    onSelect = {
                        viewModel.updateSelectedTab(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun BidirectionalSliderLayout(viewModel: EditViewModel, currentLevel: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 24.dp)
    ) {
        Box {
            Text(
                text = if (currentLevel >= 0.01f) "+${(currentLevel * 100).toInt()}" else "${(currentLevel * 100).toInt()}",
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            Text(
                text = "+100",
                color = Color.Transparent,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        BidirectionalSlider(
            value = currentLevel,
            onValueChange = {
                viewModel.onValueChange((it * 100).roundToInt() / 100f)
            },
            onValueChangeFinished = {
                viewModel.onValueChangeFinished()
            },
            modifier = Modifier.weight(1f),
            trackColor = AppColors.Green500.copy(alpha = SliderDefaults.InactiveTrackAlpha),
            highlightColor = AppColors.Green500
        )
    }
}

@Composable
private fun SliderLayout(viewModel: EditViewModel, currentLevel: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 24.dp)
    ) {
        Text(
            text = "${(currentLevel * 100).toInt()}",
            color = Color.White,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(16.dp))

        BidirectionalSlider(
            value = currentLevel,
            onValueChange = {
                viewModel.onValueChange((it * 100).roundToInt() / 100f)
            },
            onValueChangeFinished = {
                viewModel.onValueChangeFinished()
            },
            modifier = Modifier.weight(1f),
            trackColor = AppColors.Green500.copy(alpha = SliderDefaults.InactiveTrackAlpha),
            highlightColor = AppColors.Green500,
            valueRange = 0f..1f
        )
    }
}

@Composable
private fun BottomTabRow(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    loadStatus: Int,
    onSelect: (Int) -> Unit
) {
    TabRow(modifier = modifier, selectedTabIndex = selectedIndex, backgroundColor = AppColors.DarkBG) {
        Tab(selected = selectedIndex == TAB_BEAUTIFY, onClick = {
            onSelect(TAB_BEAUTIFY)
        }, enabled = loadStatus != STATUS_ERROR) {
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

        Tab(selected = selectedIndex == TAB_FILTER, onClick = {
            onSelect(TAB_FILTER)
        }) {
            Text(
                text = stringResource(id = R.string.filter),
                color = if (selectedIndex == TAB_FILTER) Color.White else Color.Gray,
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
    levelMap: Map<Int, Float>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item {
            Spacer(modifier = Modifier.width(8.dp))
        }

        items(itemList) { item ->
            val isUsed = levelMap.containsKey(item.index) && levelMap[item.index] != 0f

            FunctionListItem(item, isSelected = item.index == selectedIndex, isUsed = isUsed, onClick = {
                onSelect(item.index)
            })
        }

        item {
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
private fun FilterList(
    modifier: Modifier = Modifier,
    itemList: List<FunctionItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item {
            Spacer(modifier = Modifier.width(8.dp))
        }

        item {
            Spacer(modifier = Modifier.width(8.dp))
        }

        items(itemList) { item ->
            if (item.index == INDEX_ORIGINAL) {
                OriginalFilterItem(item, isSelected = selectedIndex == INDEX_ORIGINAL, onClick = {
                    onSelect(INDEX_ORIGINAL)
                })
            } else {
                FilterListItem(item, isSelected = item.index == selectedIndex, onClick = {
                    onSelect(item.index)
                })
            }
        }

        item {
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
private fun FunctionListItem(item: FunctionItem, isSelected: Boolean, isUsed: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickableNoIndication {
                onClick()
            }
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = null,
            tint = if (isSelected) AppColors.Green200 else Color.White,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = item.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) AppColors.Green200 else Color.White
        )

        Box(modifier = Modifier
            .alpha(if (isUsed) 1f else 0f)
            .clip(CircleShape)
            .background(if (isSelected) AppColors.Green200 else Color.White)
            .size(4.dp)
        )
    }
}

@Composable
private fun OriginalFilterItem(item: FunctionItem, isSelected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilterListItem(item, isSelected, onClick)

        Box(modifier = Modifier
            .width(1.dp)
            .height(64.dp)
            .background(Color.DarkGray)
        )

        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
private fun FilterListItem(item: FunctionItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(end = 8.dp)
        .width(64.dp)
        .height(80.dp)
        .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = item.icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isSelected) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(item.labelBgColor).copy(0.75f))
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_tick),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(32.dp)
            )
        }

        Text(
            text = item.name,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(item.labelBgColor))
        )
    }
}