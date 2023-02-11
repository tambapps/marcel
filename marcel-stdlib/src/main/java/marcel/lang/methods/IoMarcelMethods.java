package marcel.lang.methods;

import marcel.lang.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class IoMarcelMethods {

  public static LineIterator lineIterator(File self) throws IOException {
    return new LineIterator(self);
  }


}
