package org.jenkinsci.groovy.binder

import org.junit.Test

import javax.inject.Inject
import javax.inject.Named

import static com.google.inject.Guice.createInjector

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class CategoryTest {
    @Inject @Named("customer")
    Payment customer

    @Inject @Named("internal")
    Payment internal

    @Test
    public void test() {
        def m = new GroovyWiringModule(getClass().getResource("category.conf"))
        m.addStarImports(this.class.package.name)
        def i = createInjector(m);
        i.injectMembers(this)

        assert customer instanceof Visa
        assert internal instanceof MasterCard
    }
}
