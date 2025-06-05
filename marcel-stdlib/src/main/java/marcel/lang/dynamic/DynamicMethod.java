package marcel.lang.dynamic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class DynamicMethod {

    public static DynamicMethod of(Lambda lambda, MethodParameter... parameters) {
        if (Arrays.stream(parameters).map(MethodParameter::getType).anyMatch(Class::isPrimitive)) {
            throw new IllegalArgumentException("Parameter types must not be primitive types");
        }
        return new DynamicMethod(lambda, List.of(parameters));
    }

    private final Lambda lambda;
    @Getter
    private final List<MethodParameter> parameters;

    public boolean matches(Map<String, Object> namedArgs, Object[] args) {
        if (args.length + namedArgs.size() > parameters.size()) {
            return false;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            if (!matches(parameters.get(i), args[i])) return false;
        }
        /*
        if (namedArgs.isEmpty()) {
            for (int i = args.length; i < parameters.size(); i++) {
                if (!parameters.get(i).hasDefaultValue()) return false;
            }
            return true;
        }
         */
        List<MethodParameter> remainingParameters = parameters.subList(args.length, parameters.size());
        for (MethodParameter parameter : remainingParameters) {
            if (namedArgs.entrySet().stream().noneMatch(namedParameter -> matches(parameter, namedParameter.getValue()) && Objects.equals(namedParameter.getKey(), parameter.getName()))) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(MethodParameter parameter, Object value) {
        return value == null || parameter.getType().isInstance(value);
    }

    public DynamicObject invoke(Map<String, Object> namedArgs, Object[] args) {
        return DynamicObject.of(doInvoke(arrangedArgs(namedArgs, args)));
    }

    private Object[] arrangedArgs(Map<String, Object> namedArgs, Object[] args) {
        if (namedArgs.isEmpty()) {
            return args;
        }
        Object[] arrangedArgs = new Object[args.length + namedArgs.size()];
        System.arraycopy(args, 0, arrangedArgs, 0, args.length);
        int i = args.length;
        for (MethodParameter remainingParameter : parameters.subList(args.length, parameters.size())) {
            arrangedArgs[i++] = namedArgs.entrySet().stream().filter(entry -> entry.getKey().equals(remainingParameter.getName())).findFirst().map(Map.Entry::getValue).orElse(null);
        }
        return arrangedArgs;
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
