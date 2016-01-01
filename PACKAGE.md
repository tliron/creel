Creel Packaging
===============

Packages are collections of artifacts. They are defined using special tags in standard JVM resource manifests. Additionally, packages support special install/uninstall hooks for calling arbitrary entry points, allowing for custom behavior. Indeed, a package can include no artifacts, and only implement these hooks.

Packages allow you to work around various limitations in repositories such as Maven, in which the smallest deployable unit is a Jar. The package specification allows you to include as many files as you need in a single Jar, greatly simplifying your deployment scheme.

Note that two different ways are supported for specifying artifacts: they can specified as files, thus referring to actual zipped entries with the Jar file in which the manifest resides, or that can be specified as general resources, in which case they will be general resource URLs to be loaded by the classloader, and thus they can reside anywhere in the classpath.

Also note what "volatile" means in this context: a "volatile" artifact is one that should be installed once and only once. This means that subsequent attempts to install the package, beyond the first, should ignore these artifacts. This is useful for marking configuration files, example files, and other files that the user should be allow to delete without worrying that they would reappear on every change to the dependency structure.


Specification
-------------

Supported manifest tags:

* `Package-Files`: a comma separated list of file paths within this Jar.
* `Package-Folders`: a comma separated list of folder paths within this Jar. Specifies all artifacts under these folders, recursively.
* `Package-Resources`: a comma separated list of resource paths to be retrieved via the classloader.
* `Package-Volatile-Files`: all these artifacts will be marked as volatile.
* `Package-Volatile-Folders`: all artifacts under these paths will be marked as volatile.
* `Package-Installer`: specifies a class name which has a main() entry point. Simple string arguments can be optionally appended, separated by spaces. The installer will be called when the package is to be installed, after all artifacts have been unpacked. Any thrown exception would cause installation to fail.
* `Package-Uninstaller`: specifies a class name which has a main() entry point. Simple string arguments can be optionally appended, separated by spaces. The uninstaller will be called when the package is to be uninstalled.

For example, here is a simple `/META-INF/MANIFEST.MF` file:

    Manifest-Version: 1.0
    Package-Folders: resources,classes
    Package-Volatile-Files: resources/config.properties
    Package-Installer: org.myorg.app.Install

All packaged files would be expected under the `/package/` directory inside the Jar.


Building with Ant
-----------------

Easy. Just nest a manifest inside a [jar task](https://ant.apache.org/manual/Tasks/jar.html):

    <target name="package" description="Create package">
        <jar destfile="mypackage.jar" basedir="files">
            <manifest>
                <attribute name="Package-Folders" value="resources,classes" />
                <attribute name="Package-Volatile-Files" value="resources/config.properties" />
                <attribute name="Package-Installer" value="org.myorg.app.Install" />
            </manifest>
        </jar>
    </target>


Building with Maven
-------------------

As usual, everything is complicated with Maven.

You can use the [assembly plugin](http://maven.apache.org/plugins/maven-assembly-plugin/). Here's a section in `pom.xml`:

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>2.6</version>
                <executions>
                    <execution>
                        <id>jar</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <configuration>
                            <appendAssemblyId>false</appendAssemblyId>
                            <archive>
                                <manifestEntries>
                                    <Package-Folders>resources,classes</Package-Folders>
                                    <Package-Volatile-Files>resources/config.properties</Package-Volatile-Files>
                                    <Package-Installer>org.myorg.app.Install</Package-Installer>
                                </manifestEntries>
                            </archive>
                            <descriptors>
                                <descriptor>package.xml</descriptor>
                            </descriptors>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

You will also need a `package.xml` file:

    <?xml version="1.0" encoding="UTF-8"?>
    <assembly
        xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.3"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.3 http://maven.apache.org/xsd/assembly-1.1.3.xsd">
    
        <id>jar</id>
        <formats>
            <format>jar</format>
        </formats>
        <baseDirectory>files</baseDirectory>
        <fileSets>
            <fileSet>
                <directory>resources</directory>
                <outputDirectory>.</outputDirectory>
                <includes>
                    <include>**</include>
                </includes>
            </fileSet>
        </fileSets>
    </assembly>
