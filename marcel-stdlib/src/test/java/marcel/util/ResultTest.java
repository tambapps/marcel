package marcel.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultTest {

  private static final Throwable EXCEPTION = new RuntimeException();
  private static final Integer VALUE = 1;

  @Test
  public void testMapSuccess() {
    assertSuccess(VALUE + 1, Result.success(VALUE).map((v) -> v + 1));
  }

  @Test
  public void testMapFailure() {
    Result<Object> result = Result.failure(EXCEPTION).map(Object::toString);
    assertFailure(EXCEPTION, result);
  }

  @Test
  public void testFlatMapSuccess() {
    assertSuccess(VALUE + 1, Result.success(VALUE).flatMap((v) -> Result.success(v + 1)));
  }

  @Test
  public void testFlatMapFailure() {
    Result<Object> result = Result.failure(EXCEPTION).flatMap((v) -> Result.success(v.toString()));
    assertFailure(EXCEPTION, result);
  }

  @Test
  public void testThenSuccess() {
    assertSuccess(VALUE + 1, Result.success(VALUE).then(Result.success(VALUE + 1)));
  }

  @Test
  public void testThenFailure() {
    Result<Object> result = Result.failure(EXCEPTION).then(Result.success(VALUE + 1));
    assertFailure(EXCEPTION, result);
  }

  @Test
  public void testRecoverSuccess() {
    assertSuccess(VALUE, Result.<Object>success(VALUE).recover((e) -> (VALUE + 1)));
  }

  @Test
  public void testRecoverFailure() {
    Result<Object> result = Result.failure(EXCEPTION).recover((e) -> VALUE);
    assertSuccess(VALUE, result);
  }

  @Test
  public void testSuccess() {
    Result<Object> result = Result.success(VALUE);
    assertSuccess(VALUE, result);
  }

  @Test
  public void testFailure() {
    Result<Object> result = Result.failure(EXCEPTION);
    assertFailure(EXCEPTION, result);
  }

  @Test
  public void testOfSuccess() {
    var v = 1;
    Result<Object> result = Result.of(() -> v);
    assertSuccess(v, result);
  }

  @Test
  public void testOfFailure() {
    var e = new RuntimeException();
    Result<Object> result = Result.of(() -> { throw e; });
    assertFailure(e, result);
  }

  private void assertFailure(Throwable expectedException, Result<Object> result) {
    assertTrue(result.isFailure());
    assertFalse(result.isSuccess());
    assertEquals(expectedException, result.getExceptionOrNull());
    assertNull(result.getOrNull());
    assertEquals(1, result.getOrDefault(1));
    assertThrows(expectedException.getClass(), result::getOrThrow);
  }

  private void assertSuccess(Object expectedValue, Result<Object> result) {
    assertFalse(result.isFailure());
    assertTrue(result.isSuccess());
    assertNull(result.getExceptionOrNull());
    assertEquals(expectedValue, result.getOrNull());
    assertEquals(expectedValue, result.getOrDefault(2));
    assertEquals(expectedValue, result.getOrThrow());

  }
}
