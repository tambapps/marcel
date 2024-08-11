package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import org.commonmark.node.Block
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
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

class MarkdownComposer(
  private val highlighter: SpannableHighlighter
) {

  private var listBlockDepth = 0

  @Composable
  fun Markdown(node: Node) {
    when (node) {
      is ListItem -> ListItem(node = node)
      is Paragraph -> Paragraph(node = node)
      is Heading -> Heading(node = node)
      is Text -> Text(node = node)
      is FencedCodeBlock -> FencedCodeBlock(node = node)
      is Block -> Block(node = node)
    }
  }

  @Composable
  fun Block(node: Block) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
      if (node is ListBlock) {
        listBlockDepth++
      }
      node.forEach { child ->
        Markdown(child)
      }
      if (node is ListBlock) {
        listBlockDepth--
      }
    }
  }

  @Composable
  fun ListItem(node: ListItem) {
    Row {
      Text(text ="\t".repeat(listBlockDepth) + "- ")
      node.firstChild?.let { Markdown(it) }
    }
  }

  @Composable
  fun Paragraph(node: Paragraph) {
    Text(text = buildParagraph(node, MaterialTheme.typography.shellTextStyle), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
  }

  @Composable
  fun FencedCodeBlock(node: FencedCodeBlock) {
    val corner = 8.dp
    Box(
      modifier = Modifier
        .padding(vertical = 16.dp)
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(corner, corner, corner, corner))
        .background(Color.DarkGray)
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        text = if ("marcel" == node.info) highlighter.highlight(node.literal) else AnnotatedString(node.literal),
        style = MaterialTheme.typography.shellTextStyle.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
      )
    }
  }

  @Composable
  fun Text(node: Text) {
    Text(text = node.literal, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
  }

  @Composable
  fun Heading(node: Heading) {
    val (style, padding) = when(node.level) {
      1 -> Pair(MaterialTheme.typography.titleLarge, 16.dp)
      2 -> Pair(MaterialTheme.typography.titleMedium, 8.dp)
      else -> Pair(MaterialTheme.typography.titleSmall, 4.dp)
    }
    Text(text = buildParagraph(node, MaterialTheme.typography.shellTextStyle), style = style, modifier = Modifier.padding(top = padding, bottom = padding))
  }
}

private fun buildParagraph(p: Block, shellTextStyle: TextStyle): AnnotatedString {
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
        is Code -> withStyle(style = SpanStyle(background = Color.DarkGray, fontFamily = shellTextStyle.fontFamily)) {
          append(n.literal)
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
