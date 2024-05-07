package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriodUnit
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Collections


@Composable
fun PickerExample(
  amountState: MutableIntState,
  unitState: MutableState<WorkPeriodUnit>,
  valuesRange: List<Int> = (1..24).toList(),
  title: @Composable (() -> Unit)? = null,
  footer: @Composable (() -> Unit)? = null,
  ) {
  Surface(modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxSize()
    ) {

      val amountValues = remember {
        if (valuesRange.first() != amountState.intValue) valuesRange.toMutableList().let {
          Collections.rotate(it, - it.indexOf(amountState.intValue))
          it
        }else valuesRange
      }
      val unitValues = remember {
        val unitValues = WorkPeriodUnit.entries
        if (unitValues.first() != unitState.value) unitValues.toMutableList().let {
          Collections.rotate(it, - it.indexOf(unitState.value))
          it
        }else unitValues
      }

      title?.invoke()
      Row(modifier = Modifier.fillMaxWidth()) {
        Picker(
          selectedItemState = amountState,
          items = amountValues,
          visibleItemsCount = 3,
          modifier = Modifier.weight(0.3f),
          textModifier = Modifier.padding(8.dp),
          textStyle = TextStyle(fontSize = 32.sp),
        )
        Picker(
          selectedItemState = unitState,
          items = unitValues,
          visibleItemsCount = 3,
          modifier = Modifier.weight(0.7f),
          textModifier = Modifier.padding(8.dp),
          textStyle = TextStyle(fontSize = 32.sp),
          converter = { it.name.lowercase() }
        )
      }
      footer?.invoke()
    }
  }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> Picker(
  items: List<T>,
  selectedItemState: MutableState<T>,
  modifier: Modifier = Modifier,
  startIndex: Int = 0,
  visibleItemsCount: Int = 3,
  textModifier: Modifier = Modifier,
  textStyle: TextStyle = LocalTextStyle.current,
  dividerColor: Color = LocalContentColor.current,
  converter: ((T) -> String)? = null
) {

  val visibleItemsMiddle = visibleItemsCount / 2
  val listScrollCount = Integer.MAX_VALUE
  val listScrollMiddle = listScrollCount / 2
  val listStartIndex = listScrollMiddle - listScrollMiddle % items.size - visibleItemsMiddle + startIndex

  fun getItem(index: Int) = items[index % items.size]

  val listState = rememberLazyListState(initialFirstVisibleItemIndex = listStartIndex)
  val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

  val itemHeightPixels = remember { mutableIntStateOf(0) }
  val itemHeightDp = pixelsToDp(itemHeightPixels.value)

  val fadingEdgeGradient = remember {
    Brush.verticalGradient(
      0f to Color.Transparent,
      0.5f to Color.Black,
      1f to Color.Transparent
    )
  }

  LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
      .map { index -> getItem(index + visibleItemsMiddle) }
      .distinctUntilChanged()
      .collect { item -> selectedItemState.value = item }
  }

  Box(modifier = modifier) {

    LazyColumn(
      state = listState,
      flingBehavior = flingBehavior,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .height(itemHeightDp * visibleItemsCount)
        .fadingEdge(fadingEdgeGradient)
    ) {
      items(listScrollCount) { index ->
        Text(
          text = getItem(index).let { converter?.invoke(it) ?: it.toString() },
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = textStyle,
          modifier = Modifier
            .onSizeChanged { size -> itemHeightPixels.intValue = size.height }
            .then(textModifier)
        )
      }
    }

    HorizontalDivider(
      color = dividerColor,
      modifier = Modifier.offset(y = itemHeightDp * visibleItemsMiddle)
    )

    HorizontalDivider(
      color = dividerColor,
      modifier = Modifier.offset(y = itemHeightDp * (visibleItemsMiddle + 1))
    )

  }

}

private fun Modifier.fadingEdge(brush: Brush) = this
  .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
  .drawWithContent {
    drawContent()
    drawRect(brush = brush, blendMode = BlendMode.DstIn)
  }


@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }