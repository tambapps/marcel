package marcel.lang.lambda;

public interface FloatLambda1<R> extends Lambda1<Float, R> {

  R invoke(float i);

  @Override
  default R invoke(Float arg0) {
    return invoke((float) arg0);
  }
}
