package marcel.util.concurrent;

import lombok.Getter;

import java.util.List;

public class CompositeException extends RuntimeException {

  @Getter
  private List<Throwable> throwables;

  public CompositeException(List<Throwable> throwables) {
    super("%d errors occurred during execution of tasks".formatted(throwables.size()));
    this.throwables = throwables;
  }
}
