package marcel.io.clargs;

class Parsers {

  static int parseInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new OptionParserException(e);
    }
  }

  static long parseLong(String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      throw new OptionParserException(e);
    }
  }

  static double parseDouble(String s) {
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      throw new OptionParserException(e);
    }
  }

  static float parseFloat(String s) {
    try {
      return Float.parseFloat(s);
    } catch (NumberFormatException e) {
      throw new OptionParserException(e);
    }
  }
}
