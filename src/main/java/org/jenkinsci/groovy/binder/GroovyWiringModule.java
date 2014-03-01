package org.jenkinsci.groovy.binder;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.*;
import static java.util.Collections.*;

/**
 * Loads Groovy binder DSL as {@link Module}.
 *
 * @author Kohsuke Kawaguchi
 */
public class GroovyWiringModule extends AbstractModule {
    private final Collection<URL> scripts;
    private final ImportCustomizer importCustomizer = new ImportCustomizer();

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private Level level = Level.FINE;

    public GroovyWiringModule(URL... scripts) {
        this(asList(scripts));
    }

    public GroovyWiringModule(Collection<URL> scripts) {
        this.scripts = scripts;
    }

    /**
     * Tweaks the logging level to report which scripts are loaded.
     *
     * On the server-side use, it tends to be preferrable to set this to {@link Level#INFO}
     * so that the log indicates precisely what's loaded.
     */
    public GroovyWiringModule withLogLevel(Level l) {
        this.level = l;
        return this;
    }

    /**
     * Sets the class loader that determines what classes Groovy wiring script will see.
     */
    public GroovyWiringModule withClassLoader(ClassLoader cl) {
        this.classLoader = cl;
        return this;
    }

    public GroovyWiringModule addImports(String... imports) {
        importCustomizer.addImports(imports);
        return this;
    }

    public GroovyWiringModule addStarImports(String... packages) {
        importCustomizer.addStarImports(packages);
        return this;
    }

    public GroovyWiringModule addImports(Class... classes) {
        for (Class c : classes)
            importCustomizer.addImports(c.getName());
        return this;
    }

    @Override
    protected void configure() {
        Module m = Modules.EMPTY_MODULE;

        final GroovyShell shell = createShell();
        for (final URL url : scripts) {
            m = Modules.override(m).with(new AbstractModule() {
                @Override
                protected void configure() {
                    try {
                        LOGGER.log(level, "Loading " + url);
                        BinderClosureScript s = (BinderClosureScript)shell.parse(new GroovyCodeSource(url));
                        s.setBinder(binder());
                        s.run();
                    } catch (IOException e) {
                        throw new Error("Failed to configure via "+url,e);
                    }
                }
            });
        }

        m.configure(binder());
    }

    /**
     * Creates {@llink GroovyWiringModule} that loads all the specified script files.
     *
     * @param scriptsFiles
     *      If this is a file, that single file is loaded. If this is a directory,
     *      all the "*.groovy" files in this directory gets loaded (in their
     *      lexicographical order to guarantee consistency.)
     */
    public static GroovyWiringModule allOf(File scriptsFiles) throws IOException {
        if (scriptsFiles==null)   return new GroovyWiringModule();

        if (!scriptsFiles.exists())
            throw new Error(scriptsFiles+" doesn't exist");

        List<File> scripts;

        if (scriptsFiles.isFile()) {
            scripts = singletonList(scriptsFiles);
        } else {
            // if it's a directory, list all the files in there
            scripts = asList(scriptsFiles.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".groovy");
                }
            }));
        }

        scripts = new ArrayList<File>(scripts);

        // sort to apply them in consistent ordering
        Collections.sort(scripts);

        List<URL> urls = new ArrayList<URL>();
        for (File script : scripts) {
            urls.add(script.toURI().toURL());
        }

        return new GroovyWiringModule(urls);
    }

    /**
     * Creates {@link GroovyShell} that controls how scripts are loaded.
     */
    protected GroovyShell createShell() {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setScriptBaseClass(BinderClosureScript.class.getName());
        cc.addCompilationCustomizers(importCustomizer);
        return new GroovyShell(classLoader, new Binding(),cc);
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyWiringModule.class.getName());
}
