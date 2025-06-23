package marcel.util.function;

public interface FloatBinaryOperator {

  /**
   * Applies this operator to the given operands.
   *
   * @param left the first operand
   * @param right the second operand
   * @return the operator result
   */
  float applyAsFloat(float left, float right);
}
