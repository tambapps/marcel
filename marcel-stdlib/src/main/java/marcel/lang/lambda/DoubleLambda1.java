package marcel.lang.lambda;

public interface DoubleLambda1<R> extends Lambda1<Double, R> {

  R invoke(double i);

  @Override
  default R invoke(Double arg0) {
    return invoke((double) arg0);
  }
}
