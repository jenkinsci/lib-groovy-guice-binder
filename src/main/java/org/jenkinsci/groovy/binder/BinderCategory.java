package org.jenkinsci.groovy.binder;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

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

    /**
     * Allow modules to be combined like "m1+m2"
     */
    public static Module plus(Module m1, Module m2) {
        return Modules.combine(m1,m2);
    }

    public static Module overrideWith(Module m1, Module m2) {
        return Modules.override(m1).with(m2);
    }

    /*package*/ static final ThreadLocal<Binder> BINDER = new ThreadLocal<Binder>();
}
