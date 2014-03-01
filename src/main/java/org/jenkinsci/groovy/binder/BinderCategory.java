package org.jenkinsci.groovy.binder;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

/**
 * Mix-in for DSL.
 *
 * @author Kohsuke Kawaguchi
 */
public class BinderCategory {
    public static <T> ScopedBindingBuilder to(Class<T> type, Class<? extends T> impl) {
        return BINDER.get().bind(type).to(impl);
    }

    public static <T> void toInstance(Class<T> type, T instance) {
        BINDER.get().bind(type).toInstance(instance);
    }

    public static <T> ScopedBindingBuilder to(Class<T> type, Provider<? extends T> impl) {
        return BINDER.get().bind(type).toProvider(impl);
    }

    public static <T> LinkedBindingBuilder<T> named(AnnotatedBindingBuilder<T> builder, String name) {
        return builder.annotatedWith(Names.named(name));
    }

    /*package*/ static final ThreadLocal<Binder> BINDER = new ThreadLocal<Binder>();
}
