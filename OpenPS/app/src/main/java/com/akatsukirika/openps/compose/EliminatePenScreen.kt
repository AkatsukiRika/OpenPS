package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.R
import com.akatsukirika.openps.utils.clickableNoIndication
import com.akatsukirika.openps.viewmodel.EliminateViewModel

interface EliminatePenCallback {
    fun onCancel()
    fun onConfirm()
}

const val MODE_PAINT = 0
const val MODE_LARIAT = 1
const val MODE_ERASER = 2
const val MODE_GENERATE = 3

@Composable
fun EliminatePenScreen(callback: EliminatePenCallback, viewModel: EliminateViewModel, modifier: Modifier = Modifier) {
    val mode = viewModel.mode.collectAsState().value
    val readyToGenerate = viewModel.readyToGenerate.collectAsState().value

    Column(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .background(AppColors.DarkBG),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mode != MODE_LARIAT && mode != MODE_GENERATE) {
            SliderLayout(viewModel)
        }

        Row(modifier = Modifier.padding(vertical = 10.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(64.dp)
                    .clickableNoIndication {
                        viewModel.mode.value = MODE_PAINT
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_paint),
                    contentDescription = null,
                    tint = if (mode == MODE_PAINT) AppColors.Green200 else Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = stringResource(id = R.string.paint),
                    color = if (mode == MODE_PAINT) AppColors.Green200 else Color.White,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(64.dp)
                    .clickableNoIndication {
                        viewModel.mode.value = MODE_LARIAT
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lariat),
                    contentDescription = null,
                    tint = if (mode == MODE_LARIAT) AppColors.Green200 else Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = stringResource(id = R.string.lariat),
                    color = if (mode == MODE_LARIAT) AppColors.Green200 else Color.White,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(64.dp)
                    .clickableNoIndication {
                        viewModel.mode.value = MODE_ERASER
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eraser),
                    contentDescription = null,
                    tint = if (mode == MODE_ERASER) AppColors.Green200 else Color.White,
                    modifier = Modifier.size(26.dp)
                )

                Text(
                    text = stringResource(id = R.string.eraser),
                    color = if (mode == MODE_ERASER) AppColors.Green200 else Color.White,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(64.dp)
                    .clickableNoIndication(enabled = readyToGenerate) {
                        viewModel.mode.value = MODE_GENERATE
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_generate),
                    contentDescription = null,
                    tint = if (mode == MODE_GENERATE) AppColors.Green200 else if (readyToGenerate) Color.White else Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = stringResource(id = R.string.generate),
                    color = if (mode == MODE_GENERATE) AppColors.Green200 else if (readyToGenerate) Color.White else Color.DarkGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SliderLayout(viewModel: EliminateViewModel) {
    val currentLevel = viewModel.size.collectAsState().value

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
                viewModel.size.value = it
            },
            onValueChangeFinished = {},
            modifier = Modifier.weight(1f),
            trackColor = AppColors.Green500.copy(alpha = SliderDefaults.InactiveTrackAlpha),
            highlightColor = AppColors.Green500,
            valueRange = 0f..1f
        )
    }
}