package marcel.lang.lambda;

// TODO do other primitive types
public interface IntLambda1<R> extends Lambda1<Integer, R> {

  R invoke(int i);

  @Override
  default R invoke(Integer arg0) {
    return invoke((int) arg0);
  }
}
