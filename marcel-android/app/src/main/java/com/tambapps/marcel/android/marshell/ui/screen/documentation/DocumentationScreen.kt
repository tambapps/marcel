package com.tambapps.marcel.android.marshell.ui.screen.documentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.Document
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.Text as MarkText

// TODO need a non-navigational drawer. Maybe put this in a new activity for that?
@Composable
fun DocumentationScreen(viewModel: DocumentationViewModel = hiltViewModel()) {
  LaunchedEffect(Unit) {
    viewModel.fetchPage()
  }
  Column(modifier = Modifier
    .fillMaxSize()
    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
    TopBar()
    viewModel.node?.let { DocumentationNode(node = it) }
  }
}

@Composable
private fun DocumentationNode(node: Node) {
  when (node) {
    is ListItem -> {
      Row {
        Text(text = "- ")
        node.firstChild?.let { DocumentationNode(it) }
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
        node.forEach { child ->
          DocumentationNode(child)
        }
      }
    }
    is MarkText -> {
      Text(text = node.literal, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

private fun buildParagraph(p: Block): AnnotatedString {
  return buildAnnotatedString {
    p.forEach { n ->
      when(n) {
        is MarkText -> append(n.literal)
        is HardLineBreak -> append("\n\n")
        // TODO make color a constant. it is used elsewhere
        is Link -> withStyle(style = SpanStyle(color = Color(0xFF2196F3))) {
          append((n.firstChild as? MarkText)?.literal ?: n.destination)
        }
        is HtmlBlock -> append("\n\n") // it's usually for <br/>
        is StrongEmphasis -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
          append((n.firstChild as? MarkText)?.literal ?: "???")
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

@Composable
private fun TopBar() {
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(TopBarHeight)) {
    Text(text = "Documentation", fontSize = 20.sp,
      modifier = Modifier.align(Alignment.Center), color = Color.White)
  }
}