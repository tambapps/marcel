package marcel.lang.lambda;

public interface Lambda0<R> extends Lambda1<Object, R> {

  R invoke();

  @Override
  default R invoke(Object arg0) {
    return invoke();
  }
}
