package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.commonmark.node.Block
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text


@Composable
fun Markdown(
  node: Node,
  listItemContent: ((ListItem, Int) -> @Composable RowScope.() -> Unit)? = null,
  listBlockDepth: Int = 0
  ) {
  when (node) {
    is ListItem -> {

      Row {
        if (listItemContent != null) {
          listItemContent.invoke(node, listBlockDepth)
        } else {
          Text(text = "- ")
          node.firstChild?.let { Markdown(it, null, listBlockDepth + 1) }
        }
     }
    }
    is Paragraph -> {
      Text(text = buildParagraph(node), textAlign = TextAlign.Justify)
    }
    is Heading -> {
      val (style, padding) = when(node.level) {
        1 -> Pair(MaterialTheme.typography.titleLarge, 16.dp)
        2 -> Pair(MaterialTheme.typography.titleMedium, 8.dp)
        else -> Pair(MaterialTheme.typography.titleSmall, 4.dp)
      }
      Text(text = buildParagraph(node), style = style, modifier = Modifier.padding(top = padding, bottom = padding))
    }
    is Block -> {
      Column(modifier = Modifier.padding(bottom = 8.dp)) {
        val newDepth = if (node is ListBlock) listBlockDepth + 1 else listBlockDepth
        node.forEach { child ->
          Markdown(child, listItemContent, newDepth)
        }
      }
    }
    is Text -> {
      Text(text = node.literal, style = MaterialTheme.typography.bodyMedium)
    }
  }

}


private fun buildParagraph(p: Block): AnnotatedString {
  return buildAnnotatedString {
    p.forEach { n ->
      when(n) {
        is Text -> append(n.literal)
        is HardLineBreak -> append("\n\n")
        // TODO make color a constant. it is used elsewhere
        is Link -> withStyle(style = SpanStyle(color = Color(0xFF2196F3))) {
          append((n.firstChild as? Text)?.literal ?: n.destination)
        }
        is HtmlBlock -> append("\n\n") // it's usually for <br/>
        is StrongEmphasis -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
          append((n.firstChild as? Text)?.literal ?: "???")
        }
      }
      append(" ")
    }
  }
}


private inline fun Block.forEach(consumer: (Node) -> Unit) {
  var n: Node? = firstChild
  while (n != null) {
    while (n is Text && n.next is SoftLineBreak && n.next?.next is Text) {
      // merge  consecutive text, because in source code I sometimes lr one time, which generates a SoftLineBreak
      val secondText = n.next.next as Text
      n = Text(n.literal + " " + secondText.literal).apply {
        secondText.next?.insertBefore(this)
      }
    }
    consumer.invoke(n!!)
    n = n.next
  }
}
