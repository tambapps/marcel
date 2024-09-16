package marcel.lang.dynamic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.*;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class DynamicMethod {

    public static DynamicMethod of(Lambda lambda, Class<?>... parameters) {
        if (Arrays.stream(parameters).anyMatch(Class::isPrimitive)) {
            throw new IllegalArgumentException("Parameter types must not be primitive types");
        }
        return new DynamicMethod(lambda, List.of(parameters));
    }

    private final Lambda lambda;
    @Getter
    private final List<Class<?>> parameters;

    public boolean matches(Object[] args) {
        if (args.length != parameters.size()) {
            return false;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            if (!parameters.get(i).isInstance(args[i])) return false;
        }
        return true;
    }

    public DynamicObject invoke(Object[] args) {
        return DynamicObject.of(doInvoke(args));
    }

    private Object doInvoke(Object[] args) {
        if (lambda instanceof Lambda0) {
            return ((Lambda0) lambda).invoke();
        } else if (lambda instanceof Lambda1) {
            return ((Lambda1) lambda).invoke(args[0]);
        } else if (lambda instanceof Lambda2) {
            return ((Lambda2) lambda).invoke(args[0], args[1]);
        } else if (lambda instanceof Lambda3) {
            return ((Lambda3) lambda).invoke(args[0], args[1], args[2]);
        } else if (lambda instanceof Lambda4) {
            return ((Lambda4) lambda).invoke(args[0], args[1], args[2], args[3]);
        } else if (lambda instanceof Lambda5) {
            return ((Lambda5) lambda).invoke(args[0], args[1], args[2], args[3], args[4]);
        } else if (lambda instanceof Lambda6) {
            return ((Lambda6) lambda).invoke(args[0], args[1], args[2], args[3], args[4], args[5]);
        } else if (lambda instanceof Lambda7) {
            return ((Lambda7) lambda).invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        } else if (lambda instanceof Lambda8) {
            return ((Lambda8) lambda).invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
        } else if (lambda instanceof Lambda9) {
            return ((Lambda9) lambda).invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
        } else if (lambda instanceof Lambda10) {
            return ((Lambda10) lambda).invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
        } else {
            throw new IllegalStateException("Unsupported lambda type: " + lambda.getClass().getName());
        }
    }
}
