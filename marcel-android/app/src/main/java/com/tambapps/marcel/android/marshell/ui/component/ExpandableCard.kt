package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle

val EXPANDABLE_CARD_ANIMATION_SPEC: FiniteAnimationSpec<IntSize> = tween(
    durationMillis = 300,
    easing = LinearOutSlowInEasing
)

@Composable
fun ExpandableCard(
    expanded: MutableState<Boolean>,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    logoTint: Color = MaterialTheme.colorScheme.primary,
    additionalLogos: @Composable (RowScope.() -> Unit)? = null,
    expandedContent: @Composable (ColumnScope.() -> Unit)
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded.value) 180f else 0f, label = "Arrow Animation"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = EXPANDABLE_CARD_ANIMATION_SPEC)
            .clickable { expanded.value = !expanded.value },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(6f),
                    text = title,
                    style = shellTextStyle,
                    color = titleColor,
                    maxLines = 1,
                )

                additionalLogos?.invoke(this)

                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .rotate(rotation)
                        .size(36.dp),
                    onClick = { expanded.value = !expanded.value }) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow",
                        tint = logoTint,
                    )
                }
            }
            if (expanded.value) {
                expandedContent.invoke(this)
            }
        }
    }
}
