package marcel.lang.lambda;

public interface LongLambda1<R> extends Lambda1<Long, R> {

  R invoke(long i);

  @Override
  default R invoke(Long arg0) {
    return invoke((long) arg0);
  }
}
