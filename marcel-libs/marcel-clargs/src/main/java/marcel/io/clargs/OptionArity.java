package marcel.io.clargs;

import lombok.AllArgsConstructor;
import marcel.lang.IntRange;
import marcel.lang.IntRanges;

interface OptionArity {

  boolean respects(int n);

  default boolean isConstant() {
    return false;
  }

  default int getN() {
    throw new UnsupportedOperationException();
  }

  static OptionArity of(String arity) {
    if (arity.equals("*")) {
      return new Any();
    } else if (arity.contains("..")) {
      // range arity
      String[] split = arity.split("\\.\\.");
      if (split.length != 2) {
        throw new ArityParsingException(arity);
      }
      if (split[0].equals("*")) {
        return new AtMost(parseInt(split[1]));
      } else if (split[1].equals("*")) {
        return new AtLeast(parseInt(split[0]));
      } else {
        return new Between(IntRanges.of(parseInt(split[0]), parseInt(split[1])));
      }
    } else if (arity.endsWith("+")) {
      return new AtLeast(Integer.parseInt(arity.substring(0, arity.length() - 1)));
    } else {
      return new ConstantArity(Integer.parseInt(arity));
    }
  }

  static int parseInt(String s) {
    try {
      int i = Integer.parseInt(s);
      if (i < 0) {
        throw new ArityParsingException(s);
      }
      return i;
    } catch (NumberFormatException e) {
      throw new ArityParsingException(s);
    }
  }

  @AllArgsConstructor
  class ConstantArity implements OptionArity {
    private final int n;

    @Override
    public boolean respects(int n) {
      return this.n == n;
    }

    @Override
    public String toString() {
      return String.valueOf(n);
    }

    @Override
    public boolean isConstant() {
      return true;
    }

    @Override
    public int getN() {
      return n;
    }
  }

  class Any implements OptionArity {
    @Override
    public boolean respects(int n) {
      return true;
    }
  }

  @AllArgsConstructor
  class Between implements OptionArity {
    private final IntRange range;

    @Override
    public boolean respects(int n) {
      return range.contains(n);
    }

    @Override
    public String toString() {
      return "between " + range.getFrom() + " and " + range.getTo();
    }
  }

  @AllArgsConstructor
  class AtLeast implements OptionArity {
    private final int min;

    @Override
    public boolean respects(int n) {
      return n >= min;
    }

    @Override
    public String toString() {
      return "at least " + min;
    }
  }

  @AllArgsConstructor
  class AtMost implements OptionArity {
    private final int max;

    @Override
    public boolean respects(int n) {
      return n <= max;
    }

    @Override
    public String toString() {
      return "at most " + max;
    }
  }

  class ArityParsingException extends OptionParserException {
    public ArityParsingException(String arity) {
      super("Invalid arity \"" + arity + "\"");
    }
  }
}
