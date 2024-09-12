package marcel.lang;

import androidx.annotation.NonNull;

import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.List;

/**
 * Class used to render a string as markdown in Marshell
 */
public class Markdown {

  public static final Parser PARSER = Parser.builder().extensions(List.of(TablesExtension.create())).build();
  private final String source;
  private final Node node;

  public static Markdown of(String source) {
    Node node = PARSER.parse(source);
    return new Markdown(source, node);
  }

  private Markdown(String source, Node node) {
    this.source = source;
    this.node = node;
  }

  public String getSource() {
    return source;
  }

  public Node getNode() {
    return node;
  }

  @NonNull
  @Override
  public String toString() {
    return source;
  }
}
