# Groovy Guice Binder

This little library defines a Groovy DSL for [Guice Binder](https://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/Binder.html)
and a mechanism to load it as a Guice `Module` by parsing and evaluating Groovy script at runtime.

The primary use case of this library is to define application configuration files directly as Guice wiring
script, thereby simplifying the configuration process. Your application no longer has to have code to
load property/XML/YAML files and influence the Guice wiring process.


## Usage
`GroovyWiringModule` class is a `Module` implementation that encapsulates the logic of loading Groovy scripts:

    // loads all *.groovy under the configuration directory and turn that into a module
    Module config = GroovyWiringModule.allOf(new File("/etc/myapp.d"));
    // allow config scripts to rewire what's already programmatically wired
    Injector i = Guice.createInjector(Modules.override( ... my application's modules ...).with(config))

You can also just load one script at a time, for example from a central server:

    GroovyWiringModule config = new GroovyWiringModule(new URL("http://brain.cloudbees.com/mansion.conf"));

The specified scripts are evaluated later when `Guice.createInjector` is called, and therefore `GroovyWiringModule`
provides various decorator methods to customize its behaviours:

    // import all the classes in these packages so that config script won't have to have import statements
    config.addStarImports("com.example.model", "com.example.utils")


## DSL
The following DSL features are available to Groovy scripts that get loaded by `GroovyWiringModule`:

### Named binding
Assignment to undeclared variables become `@Named` instance binding

    // in Groovy
    a = 5;
    b = new Foo(...);

    // equivalent binding in Java
    bind(int.class).named("a").toInstance(5);
    bind(Foo.class).named("b").toInstance(new Foo(...));

### Direct access to the `Binder` methods
All the methods on the `Binder` class are directly accessible, so you can use all Guice Binding EDSL as-is.

    // in Groovy
    bind(Payment).to(VisaPayment)

    // equivalent binding in Java
    bind(Payment.class).to(VisaPayment.class)

### Access to system properties and environment variables
System properties and Environment variables can be accessed readily:

    // in Groovy
    src = properties['java.home']
    dst = envs['JENKINS_HOME']

    // equivalent binding in Java
    bind(String.class).named("src").toInstance(System.getProperty("java.home"))
    bind(String.class).named("dst").toInstance(System.getenv("JENKINS_HOME"))
