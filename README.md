
Creel
=====

Creel is a lightweight and lightning-fast library for resolving and downloading JVM dependencies from [Maven](https://maven.apache.org/) repositories.

It can be run as a command line tool, via an [Ant](http://ant.apache.org/) task, or embedded into any program.

Features:

* _No dependencies._ Just one tiny jar (135Kb). Requires just JVM 7+.
* Very fast multi-threaded, multi-part downloads. (The downloader class is general purpose and you can use it independently of Creel.)
* Optimizes even further when using a filesystem-based ("file:" URL) repository, using fast copies and memory-mapped loading.
* Resolves version conflicts according to policy, your choice of newest (cutting-edge) or oldest (conservative).
* Define your own rules for rewriting versions, names, exclusions, etc. Create your own custom rule code if necessary.
* Elegantly handles upgrades and other changes: keeps a tiny database of previously installed dependencies, and will remove them if they are no longer needed.
* Supports Maven-style ("m2" a.k.a. "ibiblio") repositories out of the box, but is easily extensible if you'd like to support others. (Ivy, or your own custom technology.)
* SHA-1 and MD5 validation of all downloaded files (and also pom.xml and maven-metadata.xml while processing them).
* Extends the Maven spec with convenient pattern globbing ("*" and "?") and also supports Ivy's "+" suffix.
* Supports an innovative, straightforward [packaging format](PACKAGE.md) that allows you to easily distribute arbitrary files (not just Jars) in a Maven repo.
* Colorful, animated ANSI terminal feedback where supported. See your downloads woosh!

How does Creel compare to [Gradle](http://gradle.org/)? Well, Creel is _much_ lighter and _much_ faster, but it's also a one-trick pony designed only for resolving and downloading dependencies, while Gradle can be used for complete project development and management. Note that Creel could be a great choice as a library with which to build your own Gradle-like tool.

And, actually, that's precisely the context in which Creel was created: it was spun off as an independent library out of [Sincerity](http://threecrickets.com/sincerity/). Sincerity is a terrific tool for managing and bootstrapping application containers. Check it out!

How does Creel compare to [Ivy](http://ant.apache.org/ivy/)? Well, Ivy can also be used to download Maven dependencies and integrates with Ant, but it's heavier and much more complex. We actually used Ivy for a long time, but found it too cumbersome to embed and extend. Creel was conceived as a lighter, faster, and simpler replacement for Ivy.  


Embedded
--------

It's easy to embed Creel into your Java (or Groovy, Clojure, Scala, etc.) application. A simple [EventHandler interface](components/creel/source/com/threecrickets/creel/event) can allow you to integrate Creel activity notifications as appropriate.

Here's a simple example in JavaScript, `creel.js`, using the excellent Nashorn engine that comes with JVM 8: 

    var engine = new com.threecrickets.creel.Engine()
    engine.eventHandler.add(new com.threecrickets.creel.event.ConsoleEventHandler(true, false))
    
    engine.modules = [
        {group: 'com.github.sommeri', name: 'less4j', version: '(,1.15.2)'},
        {group: 'org.jsoup', name: 'jsoup', version: '1.8.1'},
        {group: 'com.fasterxml.jackson', name: 'jackson'},
        {group: 'com.threecrickets.prudence', name: 'prudence'},
        {group: 'jsslutils', name: 'jsslutils'}]
    
    var local = false
    engine.repositories = [
        local ? {id: '3c', url: 'file:/Depot/Repository/'} : {id: '3c', url: 'http://repository.threecrickets.com/maven'},
        {id: 'restlet', url: 'http://maven.restlet.com', all: false},
        {id: 'central', url: 'https://repo1.maven.org/maven2/'}]
    
    engine.rules = [
        {type: 'exclude', name: '*annotations*'},
        {type: 'excludeDependencies', group: 'org.apache.commons', name: 'commons-beanutils'},
        {type: 'rewriteVersion', group: 'com.beust', name: '*c?mmand*', newVersion: '1.35+'},
        {type: 'repositories', group: 'jsslutils', repositories: 'restlet'}]
    
    engine.rootDir = 'lib'
    engine.run()

To run it:

    jjs -cp creel.jar creel.js


Command Line
------------

If you really don't want to or can't use scripting, then there's a basic command line:

    java -jar creel.jar

By default it will look for a file called `creel.properties` in the current directory. Here's a simple example:

    destination=lib/jars

    module.1.group=com.github.sommeri
    module.1.name=less4j
    module.1.version=(,1.15.2)
    
    module.2.group=com.fasterxml.jackson
    module.2.name=jackson
    
    repository.1.url=file:/Depot/Repository/
    repository.2.url=https://repo1.maven.org/maven2/
    
    rule.1.type=exclude
    rule.1.name=*annotations*

Use `--help` to get a list of command line options.

Note that the properties file can define all of the same attributes we used in the JavaScript example above, but we omitted them here for brevity. You may also set command line options in the properties file, such as `destination=` above. Command line options would override these. 


Ant Task
--------

Use Creel to download your dependency Jars and include them in the classpath. Here's a complete Ant `build.xml`:

    <?xml version="1.0"?>
    <project name="Testing Creel" default="compile" xmlns:creel="antlib:com.threecrickets.creel.ant">
        <taskdef uri="antlib:com.threecrickets.creel.ant" resource="com/threecrickets/creel/ant/antlib.xml" classpath="creel.jar" />
        <target name="dependencies">
            <creel:run destdir="lib" pathid="my.dependencies.classpath">
                <module group="com.github.sommeri" name="less4j" version="(,1.15.2)"/>
                <module group="org.jsoup" name="jsoup" version="1.8.1"/>
                <module group="com.fasterxml.jackson" name="jackson"/>
                <module group="com.threecrickets.prudence" name="prudence"/>
                <module group="jsslutils" name="jsslutils"/>
                <repository id="3c" url="http://repository.threecrickets.com/maven"/>
                <repository id="restlet" url="http://maven.restlet.com" all="false"/>
                <repository id="central" url="https://repo1.maven.org/maven2/"/>
                <rule type="exclude" name="*annotations*"/>
            </creel:run>
        </target>
        <target name="compile" depends="dependencies">
            <javac srcdir="." classpathref="my.dependencies.classpath">
                <include name="Test.java" />
            </javac>
        </target>
    </project>

Note how lovely it is that you can include everything in your single `build.xml` file. (Still, if you prefer a separate file, the task supports loading a `creel.properties` file.)

See the online documentation for all Ant task attributes.


Rules
-----

The following rules are built in:

##### exclude

Excludes modules from installation. Note that their dependencies will be excluded from identification, but can still be pulled in by other modules.

To match, set `group`, `name`, and optionally `version`. You can use globs and version ranges.

##### excludeDependencies

Excludes modules' dependencies from installation. (In Ivy these are called "intransient" dependencies.)

To match, set `group`, `name`, and optionally `version`. You can use globs and version ranges.

##### rewrite

Rewrites module specifications.

To match, set `group`, `name`, and optionally `version`. You can use globs and version ranges.

Set either or both of `newGroup` and `newName` to the new value.

##### rewriteVersion

Rewrites module versions.

To match, set `group`, `name`, and optionally `version`. You can use globs and version ranges.

Set `newVersion` to the new value.

##### repositories

Look for modules only in specific repositories.

By default, all repositories will be used for all modules. Set `all` to false for repositories that you don't want used this way, in which case you will need to use the `repositories` rule to use them with specific modules.

To match, set `group`, `name`, and optionally `version`. You can use globs and version ranges.

Set `repositories` to a comma-separated list of repository IDs.


Building Creel
--------------

Easily build it with the Ant script in `/build/build/xml`.

And you know what's cool? Creel needs to download Ant as dependency in order to compile its Ant task code. We use Creel in order to download ant.jar. So ... Creel is kinda [self-hosting](https://en.wikipedia.org/wiki/Self-hosting).


What's in a Name?
-----------------

A creel is a basket for fish. And now here's a quote from _Monty Python's The Meaning of Life_ (1983):

> **Man**: I wonder where that fish has gone.  
> **Woman**: You did love it so. You looked after it like a son.  
> **Man**: And it went wherever I did go.  
> **Woman**: Is it in the cupboard?  
> **Audience**: Yes! No!  
> **Woman**: Wouldn't you like to know. It was a lovely little fish.  
> **Man**: And it went wherever I did go.  
> **Man in audience**: It's behind the sofa!  
> _An elephant joins the man and woman._  
> **Woman**: Where can the fish be?  
> **Man in audience**: Have you thought of the drawers in the bureau?  
> **Woman**: It is a most elusive fish.  
> **Man**: And it went wherever I did go!  
> **Woman**: Oh fishy, fishy, fishy, fish.  
> **Man**: Fish, fish, fish, fishy oh!  
> **Woman**: Oh fishy, fishy, fishy fish.  
> **Man**: That went wherever I did go.
