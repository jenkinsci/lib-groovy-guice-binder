package org.jenkinsci.groovy.binder;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import groovy.lang.Binding;
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
    private Object delegate;
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
        Binder binder = getBinder();
        binder.bind((Class)value.getClass()).annotatedWith(Names.named(property)).toInstance(value);
    }

    /**
     * Expose {@link Binder} to the script.
     */
    public Binder getBinder() {
        return (Binder)getDelegate();
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
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
        this.metaClass = InvokerHelper.getMetaClass(delegate.getClass());
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return metaClass.invokeMethod(delegate,name,args);
        } catch (MissingMethodException mme) {
            return super.invokeMethod(name, args);
        }
    }

    @Override
    public Object getProperty(String property) {
        try {
            return metaClass.getProperty(delegate,property);
        } catch (MissingPropertyException e) {
            return super.getProperty(property);
        }
    }

    public Object getDelegate() {
        return delegate;
    }
}
