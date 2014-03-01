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
}
