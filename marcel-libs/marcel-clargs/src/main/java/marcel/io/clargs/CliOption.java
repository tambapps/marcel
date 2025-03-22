package marcel.io.clargs;

import lombok.Getter;
import org.apache.commons.cli.Converter;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;

public class CliOption extends Option {

  @Getter
  private final String defaultValue;
  // need to redeclare it here as there are no setters
  private final DeprecatedAttributes deprecatedAttributes;

  public CliOption(Builder builder, String defaultValue) {
    this(builder.build(), defaultValue);
  }

  public CliOption(Option option, String defaultValue) {
    super(option.getOpt(), option.getLongOpt(), option.hasArg(), option.getDescription());
    setArgName(option.getArgName());
    setArgs(option.getArgs());
    setOptionalArg(option.hasOptionalArg());
    setRequired(option.isRequired());
    setType((Class<?>) option.getType());
    setValueSeparator(option.getValueSeparator());
    setConverter(option.getConverter());
    this.defaultValue = defaultValue;
    this.deprecatedAttributes = option.getDeprecated();
  }

  @Override
  public Converter<?, ?> getConverter() {
    return super.getConverter();
  }

  @Override
  public DeprecatedAttributes getDeprecated() {
    return deprecatedAttributes;
  }

  @Override
  public boolean isDeprecated() {
    return deprecatedAttributes != null;
  }

  String toDeprecatedString() {
    if (!isDeprecated()) {
      return "";
    }
    final char APOS = '\'';
    // @formatter:off
    final StringBuilder buf = new StringBuilder()
        .append("Option '")
        .append(getOpt())
        .append(APOS);
    // @formatter:on
    if (getLongOpt() != null) {
      buf.append(APOS).append(getLongOpt()).append(APOS);
    }
    buf.append(": ").append(deprecatedAttributes);
    return buf.toString();
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder().append("[ ");
    buf.append("Option ");
    buf.append(getOpt());
    final char SP = ' ';
    if (getLongOpt() != null) {
      buf.append(SP).append(getLongOpt());
    }
    if (isDeprecated()) {
      buf.append(SP);
      buf.append(deprecatedAttributes.toString());
    }
    if (hasArgs()) {
      buf.append("[ARG...]");
    } else if (hasArg()) {
      buf.append(" [ARG]");
    }
    // @formatter:off
    return buf.append(" :: ")
        .append(getDescription())
        .append(" :: ")
        .append(getType())
        .append(" ]")
        .toString();
    // @formatter:on
  }
}
