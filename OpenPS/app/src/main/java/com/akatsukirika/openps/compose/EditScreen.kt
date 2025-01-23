package com.akatsukirika.openps.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.R
import com.akatsukirika.openps.viewmodel.CompositionViewModel
import com.akatsukirika.openps.viewmodel.EditViewModel
import com.akatsukirika.openps.viewmodel.EliminateViewModel

// 处理状态
const val STATUS_IDLE = 10
const val STATUS_CHECKING = 11
const val STATUS_LOADING = 12
const val STATUS_SUCCESS = 13
const val STATUS_ERROR = 14
// 一级TAB
const val TAB_BEAUTIFY = 0
const val TAB_ADJUST = 1
const val TAB_FILTER = 2
// 模块
const val MODULE_NONE = -1
const val MODULE_COMPOSITION = 0
const val MODULE_ELIMINATE_PEN = 1
const val MODULE_IMAGE_EFFECT = 2

@Composable
fun EditScreen(
    viewModel: EditViewModel,
    eliminateViewModel: EliminateViewModel,
    compositionViewModel: CompositionViewModel
) {
    MaterialTheme {
        val selectedModule = viewModel.selectedModule.collectAsState(initial = MODULE_NONE).value

        Column(modifier = Modifier.fillMaxWidth()) {
            OperationRow(
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.End)
            )

            AnimatedVisibility(visible = selectedModule == MODULE_NONE) {
                ModuleSelectLayout(viewModel)
            }

            AnimatedVisibility(visible = selectedModule == MODULE_COMPOSITION) {
                CompositionScreen(compositionViewModel)
            }

            AnimatedVisibility(visible = selectedModule == MODULE_ELIMINATE_PEN) {
                EliminatePenScreen(object : EliminatePenCallback {
                    override fun onCancel() {
                        viewModel.selectedModule.value = MODULE_NONE
                    }

                    override fun onConfirm() {
                        viewModel.selectedModule.value = MODULE_NONE
                    }
                }, eliminateViewModel)
            }

            AnimatedVisibility(visible = selectedModule == MODULE_IMAGE_EFFECT) {
                ImageEffectScreen(viewModel)
            }
        }
    }
}

@Composable
private fun ModuleSelectLayout(viewModel: EditViewModel) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .background(AppColors.DarkBG),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable {
                    viewModel.selectedModule.value = MODULE_COMPOSITION
                }
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_composition),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = stringResource(id = R.string.composition),
                color = Color.White,
                fontSize = 11.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable {
                    viewModel.selectedModule.value = MODULE_ELIMINATE_PEN
                }
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_eliminate_pen),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = stringResource(id = R.string.eliminate_pen),
                color = Color.White,
                fontSize = 11.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable {
                    viewModel.selectedModule.value = MODULE_IMAGE_EFFECT
                }
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_image_effect),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = stringResource(id = R.string.image_effect),
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun OperationRow(modifier: Modifier = Modifier, viewModel: EditViewModel) {
    val canUndo = viewModel.canUndo.collectAsState(initial = false).value
    val canRedo = viewModel.canRedo.collectAsState(initial = false).value

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        IconButton(onClick = {
            viewModel.undo()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo),
                contentDescription = null,
                tint = if (canUndo) Color.White else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = {
            viewModel.redo()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_redo),
                contentDescription = null,
                tint = if (canRedo) Color.White else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = R.drawable.ic_compare),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(bottom = 16.dp, end = 16.dp)
                .size(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        viewModel.helper?.onCompareBegin()
                        tryAwaitRelease()
                        viewModel.helper?.onCompareEnd()
                    })
                }
        )
    }
}
