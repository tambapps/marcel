package marcel.lang.lambda;

public interface CharLambda1<R> extends Lambda1<Character, R> {

  R invoke(char i);

  @Override
  default R invoke(Character arg0) {
    return invoke((char) arg0);
  }
}
