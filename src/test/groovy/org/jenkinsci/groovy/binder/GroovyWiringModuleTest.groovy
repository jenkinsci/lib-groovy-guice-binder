package org.jenkinsci.groovy.binder

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import javax.inject.Inject
import javax.inject.Named
import javax.xml.parsers.SAXParser

import static com.google.inject.Guice.createInjector

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

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void zoo() {
        def m = new GroovyWiringModule(getClass().getResource("zoo.conf"));
        m.addStarImports("javax.xml.parsers");

        def zoo = createInjector(m);
        zoo.injectMembers(this);

        assert dog=="Dog"
        assert price==20
        assert saxParserClassName==SAXParser.class.getName()
    }

    /**
     * Tests the "allOf" method and its overriding semantics.
     */
    @Test
    public void allOf() {
        def dir = tmp.newFolder();
        new File(dir,"script1.groovy").text = "a=1"
        new File(dir,"script2.groovy").text = "a=2"

        def i = createInjector(GroovyWiringModule.allOf(dir))

        assert i.getInstance(Key.get(int.class, Names.named("a")))==2;
    }

    @Test
    public void dsl() {
        def m = new GroovyWiringModule(getClass().getResource("dsl.conf"))
        m.addStarImports(Animal.class.package.name)
        def i = createInjector(m);
        assert i.getInstance(Animal.class) instanceof Dog
    }
}
