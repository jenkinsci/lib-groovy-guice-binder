package org.jenkinsci.groovy.binder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.SAXParser;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyWiringModuleTest extends Assert {
    @Inject @Named("dog")
    String dog;

    @Inject
    @Named("admission")
    int price;

    @Inject @Named("saxParserClassName")
    String saxParserClassName;

    @Test
    public void zoo() throws Exception {
        GroovyWiringModule m = new GroovyWiringModule(getClass().getResource("zoo.conf"));
        m.addStarImports("javax.xml.parsers");

        Injector zoo = Guice.createInjector(m);
        zoo.injectMembers(this);

        assertThat(dog, is("Dog"));
        assertThat(price, is(20));
        assertThat(saxParserClassName,is(SAXParser.class.getName()));
    }
}
