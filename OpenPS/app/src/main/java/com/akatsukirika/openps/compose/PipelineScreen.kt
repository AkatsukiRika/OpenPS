package com.akatsukirika.openps.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akatsukirika.openps.model.debug.DebugProgramItem
import com.akatsukirika.openps.viewmodel.PipelineViewModel

@Composable
fun PipelineScreen(viewModel: PipelineViewModel) {
    val programItemList = viewModel.programItemList.collectAsState().value

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))

        ProgramItemRow(debugItemList = programItemList)
    }
}

@Composable
fun ProgramItemRow(debugItemList: List<DebugProgramItem>) {
    Box(modifier = Modifier
        .fillMaxWidth()
    ) {
        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White)
            .padding(16.dp)
        ) {
            items(debugItemList) {
                ProgramItem(it)

                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Text(
            text = "Programs",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            modifier = Modifier
                .offset(y = (-8).dp, x = 8.dp)
                .background(AppColors.DarkBG)
        )
    }
}

@Preview
@Composable
fun PreviewProgramItemRow() {
    ProgramItemRow(debugItemList = listOf(
        DebugProgramItem(id = 1, filterName = "Filter 1", isActive = true),
        DebugProgramItem(id = 2, filterName = "Filter 2", isActive = false),
        DebugProgramItem(id = 3, filterName = "Filter 3", isActive = true)
    ))
}

@Composable
fun ProgramItem(debugItem: DebugProgramItem) {
    Column(modifier = Modifier
        .background(Color.Black)
        .alpha(if (debugItem.isActive) 1f else 0.5f)
        .border(1.dp, Color.White)
        .padding(all = 8.dp)
    ) {
        Text(
            text = "ID: ${debugItem.id}",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        )

        Text(
            text = "Filter: ${debugItem.filterName}",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Normal, fontSize = 14.sp)
        )

        Text(
            text = "Status: ${if (debugItem.isActive) "Active" else "Inactive"}",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Normal, fontSize = 14.sp)
        )
    }
}

@Preview
@Composable
fun PreviewProgramItem() {
    ProgramItem(debugItem = DebugProgramItem())
}