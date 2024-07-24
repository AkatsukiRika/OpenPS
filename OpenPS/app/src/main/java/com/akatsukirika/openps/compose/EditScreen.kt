package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
}

const val INDEX_SMOOTH = 0
const val INDEX_WHITE = 1

@Composable
fun EditScreen(callback: EditScreenCallback) {
    val context = LocalContext.current
    val itemList = remember {
        listOf(
            FunctionItem(index = INDEX_SMOOTH, icon = R.drawable.ic_smooth, name = context.getString(R.string.smooth)),
            FunctionItem(index = INDEX_WHITE, icon = R.drawable.ic_white, name = context.getString(R.string.white))
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

        FunctionList(
            modifier = Modifier.padding(vertical = 16.dp),
            itemList = itemList,
            selectedIndex = selectedFunctionIndex,
            onSelect = {
                selectedFunctionIndex = if (selectedFunctionIndex == -1 || it != selectedFunctionIndex) it else -1
                if (selectedFunctionIndex != -1) {
                    currentLevel = levelMap[selectedFunctionIndex] ?: 0f
                }
            }
        )
    }
}

@Composable
private fun FunctionList(
    modifier: Modifier = Modifier,
    itemList: List<FunctionItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(modifier = modifier) {
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

data class FunctionItem(
    val index: Int,
    val icon: Int,
    val name: String
)