package marcel.lang;

// TODO document this and that delegated getter is automatically generated when delegate variable is present
/**
 * Interface used by the compiler to specify a Marcel class that should be delegated
 * @param <T> the type of the delegate object
 */
public interface DelegatedObject<T> {

  T getDelegate();

}
