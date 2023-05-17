package marcel.lang.methods;

import marcel.lang.io.LineIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

public class IoMarcelMethods {

  public static LineIterator lineIterator(File self) throws IOException {
    return new LineIterator(self);
  }

  // use readText() instead
  @Deprecated
  public static String getText(File self) throws IOException {
    return readText(self);
  }

  public static String readText(File self) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(self))) {
      return reader.lines()
              .collect(Collectors.joining("\n"));
    }
  }

}
