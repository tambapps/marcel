package marcel.lang.util.function;

import java.util.Objects;

@FunctionalInterface
public interface FloatUnaryOperator {

  /**
   * Applies this operator to the given operand.
   *
   * @param operand the operand
   * @return the operator result
   */
  float applyAsFloat(float operand);

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
   * @see #andThen(FloatUnaryOperator)
   */
  default FloatUnaryOperator compose(FloatUnaryOperator before) {
    Objects.requireNonNull(before);
    return (float v) -> applyAsFloat(before.applyAsFloat(v));
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
   * @see #compose(FloatUnaryOperator)
   */
  default FloatUnaryOperator andThen(FloatUnaryOperator after) {
    Objects.requireNonNull(after);
    return (float t) -> after.applyAsFloat(applyAsFloat(t));
  }

  /**
   * Returns a unary operator that always returns its input argument.
   *
   * @return a unary operator that always returns its input argument
   */
  static FloatUnaryOperator identity() {
    return t -> t;
  }
}
