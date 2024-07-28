package marcel.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CharUnaryOperator {

  /**
   * Applies this operator to the given operand.
   *
   * @param operand the operand
   * @return the operator result
   */
  char applyAsChar(char operand);

  /**
   * Returns a composed operator that first applies the {@code before}
   * operator to its input, and then applies this operator to the result.
   * If evaluation of either operator throws an exception, it is relayed to
   * the caller of the composed operator.
   *
   * @param before the operator to apply before this operator is applied
   * @return a composed operator that first applies the {@code before}
   * operator and then applies this operator
   * @throws NullPointerException if before is null
   *
   * @see #andThen(CharUnaryOperator)
   */
  default CharUnaryOperator compose(CharUnaryOperator before) {
    Objects.requireNonNull(before);
    return (char v) -> applyAsChar(before.applyAsChar(v));
  }

  /**
   * Returns a composed operator that first applies this operator to
   * its input, and then applies the {@code after} operator to the result.
   * If evaluation of either operator throws an exception, it is relayed to
   * the caller of the composed operator.
   *
   * @param after the operator to apply after this operator is applied
   * @return a composed operator that first applies this operator and then
   * applies the {@code after} operator
   * @throws NullPointerException if after is null
   *
   * @see #compose(CharUnaryOperator)
   */
  default CharUnaryOperator andThen(CharUnaryOperator after) {
    Objects.requireNonNull(after);
    return (char t) -> after.applyAsChar(applyAsChar(t));
  }

  /**
   * Returns a unary operator that always returns its input argument.
   *
   * @return a unary operator that always returns its input argument
   */
  static CharUnaryOperator identity() {
    return t -> t;
  }
}
