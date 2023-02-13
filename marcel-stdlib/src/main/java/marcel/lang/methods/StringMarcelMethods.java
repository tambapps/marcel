package marcel.lang.methods;

import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.collections.lists.CharacterList;

public class StringMarcelMethods {

  public static char getAt(String self, int i) {
    return self.charAt(i);
  }

  public static CharacterList toCharList(String self) {
    return CharacterArrayList.wrap(self.toCharArray());
  }

  public static int toInt(String self) {
    return Integer.parseInt(self);
  }
  public static long toLong(String self) {
    return Long.parseLong(self);
  }
  public static float toFloat(String self) {
    return Float.parseFloat(self);
  }
  public static double toDouble(String self) {
    return Double.parseDouble(self);
  }

}
