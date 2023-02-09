package marcel.lang.io;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class LineIterator implements Iterator<String> {

  private final BufferedReader reader;
  private String currentLine;


  public LineIterator(File file) throws IOException {
    this(new BufferedReader(new FileReader(file)));
  }

  public LineIterator(BufferedReader reader) throws IOException {
    this.reader = reader;
    currentLine = reader.readLine();
  }

    @Override
  public boolean hasNext() {
    return currentLine != null;
  }

  @SneakyThrows
  @Override
  public String next() {
    String lineToReturn = this.currentLine;
    this.currentLine = reader.readLine();
    return lineToReturn;
  }
}
