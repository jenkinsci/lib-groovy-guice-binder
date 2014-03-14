package org.jenkinsci.groovy.binder;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.inject.Named;
import java.util.Map;

/**
 * {@link Script} that provides DSL for Guice binder.
 *
 * <h2>Named primitives</h2>
 *
 * A simple binding of {@link Named} values can be done as an assignment.
 * <pre>
 * foo = 5
 * </pre>
 *
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BinderClosureScript extends Script {
    private Binder binder;
    private MetaClass metaClass;

    public BinderClosureScript() {
    }

    public BinderClosureScript(Binding binding) {
        super(binding);
    }

    /**
     * Convenience method to bind simple properties
     */
    @Override
    public void setProperty(String property, Object value) {
        // set it to binding so that the script can read them back like local variables,
        // as if they are defined like "def x = y"
        super.setProperty(property,value);

        // expose this to Binder as well
        Binder binder = getBinder();
        binder.bind((Class)value.getClass()).annotatedWith(Names.named(property)).toInstance(value);
    }

    /**
     * Convenient access to system properties.
     */
    public Map<String,String> getProperties() {
        return (Map)System.getProperties();
    }

    /**
     * Convenient access to environment variables.
     */
    public Map<String,String> getEnvs() {
        return System.getenv();
    }

    /**
     * Sets the delegation target.
     */
    public void setBinder(Binder binder) {
        this.binder = binder;
        this.metaClass = InvokerHelper.getMetaClass(binder.getClass());
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return metaClass.invokeMethod(binder,name,args);
        } catch (MissingMethodException mme) {
            return super.invokeMethod(name, args);
        }
    }

    @Override
    public Object getProperty(String property) {
        try {
            return metaClass.getProperty(binder,property);
        } catch (MissingPropertyException e) {
            return super.getProperty(property);
        }
    }

    /**
     * Expose {@link Binder} to the script.
     */
    public Binder getBinder() {
        return binder;
    }

    /**
     * Given a closure that uses Groovy binder DSL, wrap that into a Guice {@link Module}.
     *
     * <p>
     * To reuse the DSL methods defined on {@link BinderClosureScript} and its super types,
     * while we call the closure we temporarily swap {@link Binder} object.
     */
    public Module module(final Closure c) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                final Binder old = getBinder();
                try {
                    c.setDelegate(binder());
                    c.call();
                } finally {
                    setBinder(old);
                }
            }
        };
    }
}
