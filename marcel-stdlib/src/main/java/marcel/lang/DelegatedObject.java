package marcel.lang;

// TODO delete me (once legacy is gone)
/**
 * Interface used by the compiler to specify a Marcel class that should be delegated
 * @param <T> the type of the delegate object
 */
@Deprecated
public interface DelegatedObject<T> {

  T getDelegate();

}
