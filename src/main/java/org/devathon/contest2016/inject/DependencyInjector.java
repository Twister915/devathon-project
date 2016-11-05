package org.devathon.contest2016.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unchecked")
public final class DependencyInjector {
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Class<?>> providerTypes = new HashMap<>();

    public DependencyInjector() {
        bind(DependencyInjector.class, this);
    }

    public <T> T getInstance(Class<T> type) {
        boolean isSingleton = type.isAnnotationPresent(Singleton.class);
        if (isSingleton) {
            Object val = singletons.get(type);
            if (val != null)
                return (T) val;
        }

        T inst = doConstruction(type);
        doFieldInjection(type, inst);

        if (isSingleton)
            singletons.put(type, inst);

        return inst;
    }

    private <T> Class<? extends T> getConcreteType(Class<T> type) {
        Class<? extends T> concreteType = (Class<? extends T>) providerTypes.getOrDefault(type, type);
        if (concreteType == null)
            throw new IllegalStateException("The type " + type.getSimpleName() + " could not be injected.");
        assert type.isAssignableFrom(concreteType);
        return concreteType;
    }

    private <T> T doConstruction(Class<?> type) {
        Class<?> concreteType = getConcreteType(type);
        Set<Exception> constructionAttempts = new HashSet<>();
        Constructor<?>[] constructors = concreteType.getDeclaredConstructors();

        T inst = null;
        CONSTRUCTORS: for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[constructor.getParameterCount()];
            for (int i = 0; i < paramTypes.length; i++) {
                try {
                    params[i] = getInstance(paramTypes[i]);
                } catch (Exception e) {
                    constructionAttempts.add(new IllegalStateException("Failed to get instance for constructor argument of type " + paramTypes[i] + " on class " + concreteType.getSimpleName(), e));
                    continue CONSTRUCTORS;
                }
            }

            constructor.setAccessible(true);

            try {
                inst = (T) constructor.newInstance(params);
                break;
            } catch (Exception e) {
                constructionAttempts.add(new IllegalStateException("Could not create instance of class " + concreteType.getSimpleName(), e));
            }
            throw new CompositeException(constructionAttempts);
        }

        if (inst == null)
            throw new IllegalStateException("There's no constructors?");

        return inst;
    }

    private void doFieldInjection(Class<?> type, Object inst) {
        Class<?> concreteType = inst.getClass();
        Iterator<Field> fieldIterator = allFields(concreteType);
        while (fieldIterator.hasNext()) {
            Field next = fieldIterator.next();
            Class<?> declaringClass = next.getDeclaringClass(); //can't cache since we're walking up inheritance tree :(
            if (!declaringClass.isAnnotationPresent(Inject.class) && !next.isAnnotationPresent(Inject.class))
                continue;

            if (!next.isAccessible())
                next.setAccessible(true);

            try {
                next.set(inst, getInstance(type));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not inject at field " + next.getName() + " of " + concreteType.getSimpleName(), e);
            }
        }
    }

    private static <T> Iterator<Field> allFields(Class<T> clazz) {
        return new Iterator<Field>() {
            private Class<? super T> type = clazz;
            private Field[] fields;
            private int i;

            @Override
            public boolean hasNext() {
                if (fields == null)
                    fields = type.getDeclaredFields();

                if (i == fields.length) {
                    i = 0;
                    fields = null;
                    type = type.getSuperclass();
                }

                return type != Object.class;
            }

            @Override
            public Field next() {
                return fields[i++];
            }
        };
    }

    public DependencyInjector bind(Class<?> type, Class<?> provider) {
        testType(type, provider);
        Class<?> concreteType = getConcreteType(provider);
        Singleton annotation = concreteType.getAnnotation(Singleton.class);
        if (annotation != null && annotation.eager())
            bind(type, getInstance(type));
        else
            providerTypes.put(type, provider);
        return this;
    }

    public DependencyInjector bind(Class<?> type, Object instance) {
        testType(type, instance.getClass());
        doFieldInjection(type, instance);
        singletons.put(type, instance);
        return this;
    }

    public DependencyInjector bind(Class<?> type) {
        bind(type, type);
        return this;
    }

    public DependencyInjector bind(Object instance) {
        bind(instance.getClass(), instance);
        return this;
    }

    private static void testType(Class<?> type1, Class<?> type2) {
        if (!type1.isAssignableFrom(type2))
            throw new IllegalArgumentException("Cannot assign " + type1.getSimpleName() + " from " + type2.getSimpleName());
    }
}
