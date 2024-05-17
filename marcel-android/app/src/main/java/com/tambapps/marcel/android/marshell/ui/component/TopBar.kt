package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize

@Composable
fun TopBarLayout(
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  content: @Composable RowScope.() -> Unit) {
  Row(modifier = Modifier.fillMaxWidth()
    .height(TopBarHeight),
    horizontalArrangement = horizontalArrangement,
    verticalAlignment = Alignment.CenterVertically, content = content)
}

@Composable
fun TopBarIconButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  enabled: Boolean = true,
  drawable: Int,
  contentDescription: String
) {
  IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
  ) {
    Icon(
      painter = painterResource(id = drawable),
      contentDescription = contentDescription,
      tint = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
    )
  }
}

@Composable
fun TopBarIconButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  enabled: Boolean = true,
  icon: ImageVector,
  contentDescription: String
) {
  IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
    )
  }
}


fun shellIconModifier(horizontalPadding: Dp = 6.dp) = Modifier
  .size(TopBarIconSize)
  .padding(horizontal = horizontalPadding)