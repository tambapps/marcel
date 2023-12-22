package marcel.lang.extensions;

import marcel.io.Files;
import marcel.lang.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// TODO delete me
public class IoMarcelMethods {

  public static LineIterator lineIterator(File self) throws IOException {
    return new LineIterator(self);
  }

  public static String readText(File self) throws IOException {
    return Files.readText(self.toPath(), StandardCharsets.UTF_8);
  }

  public static String readText(File self, Charset charset) throws IOException {
    return Files.readText(self.toPath(), charset);
  }

}
