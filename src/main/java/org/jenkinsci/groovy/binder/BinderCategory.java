package org.jenkinsci.groovy.binder;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.binder.ScopedBindingBuilder;

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

    /*package*/ static final ThreadLocal<Binder> BINDER = new ThreadLocal<Binder>();
}
