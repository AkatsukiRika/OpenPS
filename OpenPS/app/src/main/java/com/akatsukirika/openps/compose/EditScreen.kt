package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.R

interface EditScreenCallback {
    fun onSetSmoothLevel(level: Float)
    fun onSetWhiteLevel(level: Float)
    fun onSetLipstickLevel(level: Float)
    fun onSetBlusherLevel(level: Float)
}

const val INDEX_SMOOTH = 0
const val INDEX_WHITE = 1
const val INDEX_LIPSTICK = 2
const val INDEX_BLUSHER = 3
const val STATUS_IDLE = 10
const val STATUS_LOADING = 11
const val STATUS_SUCCESS = 12
const val STATUS_ERROR = 13

@Composable
fun EditScreen(callback: EditScreenCallback, loadStatus: Int) {
    val context = LocalContext.current
    val itemList = remember {
        listOf(
            FunctionItem(index = INDEX_SMOOTH, icon = R.drawable.ic_smooth, name = context.getString(R.string.smooth)),
            FunctionItem(index = INDEX_WHITE, icon = R.drawable.ic_white, name = context.getString(R.string.white)),
            FunctionItem(index = INDEX_LIPSTICK, icon = R.drawable.ic_lipstick, name = context.getString(R.string.lipstick)),
            FunctionItem(index = INDEX_BLUSHER, icon = R.drawable.ic_blusher, name = context.getString(R.string.blusher))
        )
    }
    val levelMap = remember { mutableStateMapOf<Int, Float>() }
    var currentLevel by remember { mutableFloatStateOf(0f) }
    var selectedFunctionIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .background(AppColors.DarkBG)
    ) {
        if (selectedFunctionIndex != -1) {
            Slider(
                value = currentLevel,
                onValueChange = {
                    if (selectedFunctionIndex != -1) {
                        currentLevel = it
                        levelMap[selectedFunctionIndex] = currentLevel

                        when (selectedFunctionIndex) {
                            INDEX_SMOOTH -> callback.onSetSmoothLevel(currentLevel)
                            INDEX_WHITE -> callback.onSetWhiteLevel(currentLevel)
                            INDEX_LIPSTICK -> callback.onSetLipstickLevel(currentLevel)
                            INDEX_BLUSHER -> callback.onSetBlusherLevel(currentLevel)
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.Green500,
                    activeTrackColor = AppColors.Green500
                )
            )
        }

        Box {
            FunctionList(
                modifier = Modifier.height(84.dp),
                itemList = itemList,
                selectedIndex = selectedFunctionIndex,
                onSelect = {
                    selectedFunctionIndex = if (selectedFunctionIndex == -1 || it != selectedFunctionIndex) it else -1
                    if (selectedFunctionIndex != -1) {
                        currentLevel = levelMap[selectedFunctionIndex] ?: 0f
                    }
                }
            )

            if (loadStatus == STATUS_LOADING) {
                LoadingMask(modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                )
            }
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
    val name: String
)