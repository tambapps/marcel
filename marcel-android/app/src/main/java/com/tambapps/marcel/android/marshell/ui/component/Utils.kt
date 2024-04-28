package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
fun IconButton(size: Dp, modifier: Modifier = Modifier, onClick: () -> Unit, imageVector: ImageVector) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        ) {
        Icon(
            modifier = Modifier.size(size),
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
fun TopBarIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    drawable: Int,
    contentDescription: String
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            painter = painterResource(id = drawable),
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else Color.Gray
        )
    }
}


fun shellIconModifier(horizontalPadding: Dp = 6.dp) = Modifier
    .size(TopBarIconSize)
    .padding(horizontal = horizontalPadding)