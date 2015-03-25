The instructions to create a groovy based mojo are here: http://groovy.codehaus.org/GMaven+-+Implementing+Maven+Plugins

They developed a archetype to help you generate groovy mojos as follows:
```
mvn archetype:generate -DarchetypeGroupId=org.codehaus.groovy.maven.archetypes -DarchetypeArtifactId=gmaven-archetype-mojo -DarchetypeVersion=1.0-rc-2

```


<<< Rick wonders what would happen if he left off the version... would he get the latest... >>>

I ran it without version and got the latest (1.0-rc-3)
```
$ mvn archetype:generate -DarchetypeGroupId=org.codehaus.groovy.maven.archetypes -DarchetypeArtifactId=gmaven-archetype-mojo 
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'archetype'.
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Default Project
[INFO]    task-segment: [archetype:generate] (aggregator-style)
[INFO] ------------------------------------------------------------------------
[INFO] Preparing archetype:generate
[INFO] No goals needed for project - skipping
[INFO] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[INFO] Setting property: velocimacro.messages.on => 'false'.
[INFO] Setting property: resource.loader => 'classpath'.
[INFO] Setting property: resource.manager.logwhenfound => 'false'.
[INFO] [archetype:generate]
[INFO] Generating project in Interactive mode
[INFO] Archetype [org.codehaus.groovy.maven.archetypes:gmaven-archetype-mojo:1.0-rc-3] found in catalog internal
Downloading: http://repo1.maven.org/maven2/org/codehaus/groovy/maven/archetypes/gmaven-archetype-mojo/1.0-rc-3/gmaven-archetype-mojo-1.0-rc-3.jar
7K downloaded
Define value for groupId: : 

```

<<< Rick is such a rebel for not following instructions... may he live to regret it >>>

It asks a bunch of questions which I fill out as follows:

```
[INFO] Archetype [org.codehaus.groovy.maven.archetypes:gmaven-archetype-mojo:1.0-rc-3] found in catalog internal
Define value for groupId: : org.crank
Define value for artifactId: : p2c-codegen-mojo
Define value for version:  1.0-SNAPSHOT: : 
Define value for package:  org.crank: : 
Confirm properties configuration:
name: Example Maven Plugin
groupId: org.crank
artifactId: p2c-codegen-mojo
version: 1.0-SNAPSHOT
package: org.crank
 Y: : 
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/src/main/groovy/HelloMojo.groovy [line 37,column 31] : ${message} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/src/main/groovy/HelloMojo.groovy [line 42,column 18] : ${message} is not a valid reference.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1 minute 15 seconds
[INFO] Finished at: Thu Jan 15 18:11:59 PST 2009
[INFO] Final Memory: 14M/490M
[INFO] ------------------------------------------------------------------------
richard-hightowers-macbook-pro:crank-archetypes-mojos richardhightower$ 
```

Will this project that was generated build? Let's see:

```
$ ls
p2c-codegen-mojo

$ cd p2c-codegen-mojo/
$ ls
pom.xml	src

$ mvn clean install
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building Example Maven Plugin
[INFO]    task-segment: [clean, install]
[INFO] ------------------------------------------------------------------------
[INFO] [clean:clean]

<<< Many downloads ensue whilst Rick drinks his Orange and Spice tea >>>
[INFO] [plugin:descriptor]
[INFO] Using 2 extractors.
[INFO] Applying extractor for language: java
[INFO] Extractor for language: java found 1 mojo descriptors.
[INFO] Applying extractor for language: bsh
[INFO] Extractor for language: bsh found 0 mojo descriptors.
[INFO] [resources:resources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [compiler:compile]
[INFO] Compiling 1 source file to /Users/richardhightower/projects/crank-archetypes-mojos/p2c-codegen-mojo/target/classes
[INFO] [groovy:compile {execution: default}]
[INFO]  Compiled 1 Groovy class
[INFO] [groovy:generateTestStubs {execution: default}]
[INFO]  No sources found for Java stub generation
[INFO] [resources:testResources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [compiler:testCompile]
[INFO] Nothing to compile - all classes are up to date
[INFO] [groovy:testCompile {execution: default}]
[INFO]  No sources found to compile
[INFO] [surefire:test]
[INFO] No tests to run.
[INFO] [jar:jar]
[INFO] Building jar: /Users/richardhightower/projects/crank-archetypes-mojos/p2c-codegen-mojo/target/p2c-codegen-mojo-1.0-SNAPSHOT.jar
[INFO] [plugin:addPluginArtifactMetadata]
[INFO] [install:install]
[INFO] Installing /Users/richardhightower/projects/crank-archetypes-mojos/p2c-codegen-mojo/target/p2c-codegen-mojo-1.0-SNAPSHOT.jar to /Users/richardhightower/.m2/repository/org/crank/p2c-codegen-mojo/1.0-SNAPSHOT/p2c-codegen-mojo-1.0-SNAPSHOT.jar
[INFO] [plugin:updateRegistry]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 15 seconds
[INFO] Finished at: Thu Jan 15 18:13:43 PST 2009
[INFO] Final Memory: 35M/490M
[INFO] ------------------------------------------------------------------------
richard-hightowers-macbook-pro:p2c-codegen-mojo richardhightower$
```

Ok... now to open this up in IntelliJ and take a look around.

I get this class

```
//
// Generated from archetype; please customize.
//

package org.crank

import org.codehaus.groovy.maven.mojo.GroovyMojo

/**
 * Example Maven2 Groovy Mojo.
 *
 * @goal hello
 */
class HelloMojo
    extends GroovyMojo
{
    /**
     * The hello message to display.
     *
     * @parameter expression="${message}" default-value="Hello World"
     */
    String message
    
    void execute() {
        println "${message}"
    }
}

```

And this pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<!--
    Generated from archetype; please customize.
-->

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.crank</groupId>
    <artifactId>p2c-codegen-mojo</artifactId>
    <name>Example Maven Plugin</name>
    <version>1.0-SNAPSHOT</version>
    <packaging>maven-plugin</packaging>
    
    <dependencies>
        <dependency>
            <groupId>org.codehaus.groovy.maven</groupId>
            <artifactId>gmaven-mojo</artifactId>
            <version>1.0-rc-3</version>
        </dependency>
        
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.groovy.maven</groupId>
                <artifactId>gmaven-plugin</artifactId>
                <version>1.0-rc-3</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generateStubs</goal>
                            <goal>compile</goal>
                            <goal>generateTestStubs</goal>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>

```

I need to add the codegen as an dependency.

Now I notice whilst installing codegen (mvn clean install) that I have a failed unit test. Mental note... fix that....

It also occurs to me since I am past the prototyping stage that I need to change the package and group name of code gen to org.crank instead of com.arcmind. Mental note... fix that....

Here is the current pom info for codegen
```
    <groupId>arcmind</groupId>
    <artifactId>codegen</artifactId>
    <version>1.0-SNAPSHOT</version>
```

I added codegen as a dependency and then started on the mojo, here is what I have so far... (updated a bit)

```

package org.crank

import org.codehaus.groovy.maven.mojo.GroovyMojo
import com.arcmind.codegen.*
import org.apache.maven.project.MavenProject

/**
 * Example Maven2 Groovy Mojo.
 *
 * @goal codegen
 */
class CodegenMojo extends GroovyMojo {

    /**
     * JDBC Url
     *
     * @required
     * @parameter expression="${codegen.jdbc.url}"
     */
    String url
    
    
    /**
     * JDBC User Name
     *
     * @required
     * @parameter expression="${codegen.jdbc.userName}"
     */
    String userName

    /**
     * JDBC Password
     *
     * @required
     * @parameter expression="${codegen.jdbc.password}"
     */
    String password

    /**
     * JDBC Driver
     *
     * @required
     * @parameter expression="${codegen.jdbc.driver}"
     */
    String driver

    /**
     * Table names
     *
     * 
     * @parameter expression="${codegen.tableNames}"
     */
    String tableNames
    
    /**
     * Default package name
     *
     * 
     * @parameter expression="${codegen.packageName}"  default-value="com.mycompany"
     */
    String packageName

    /**
     * Default package name
     *
     * 
     * @parameter expression="${codegen.packageName}" default-value="."
     */
    String rootDir

    
    /**
     * App configuration directory.
     *
     * 
     * @parameter expression="${codegen.appConfigDir}" default-value="./codegen"
     */
    String appConfigDir
    
    /**
     * xmlFileName
     *
     * 
     * @parameter expression="${codegen.xmlFileName}" default-value="codegen.xml"
     */
    String xmlFileName


    /**
     * xmlDataSourceFileName
     *
     * 
     * @parameter expression="${codegen.xmlDataSourceFileName}" default-value="datasource.xml"
     */
    String xmlDataSourceFileName

    /**
     * Configuration file propertiesFile
     *
     *
     * @parameter expression="${codegen.propertiesFile}" default-value="config.properties"
     */
    String propertiesFile

    /**
     *
     *
     * @parameter expression="${codegen.codeGenPackage}" default-value="com.arcmind.codegen"
     */
    String codeGenPackage

    /**
     *
     *
     * @parameter expression="${codegen.generators}" default-value="FacesConfigCodeGen,JPACodeGenerator,SpringJavaConfigCodeGen,XHTMLCodeGenerator"
     */
    String generators

    /**
     *  @parameter expression="${codegen.actions}" default-value="all"
     */
    String actions


    /*
     * @parameter expression="${codegen.debug}" 
     */
    String debug

    /*
     *  @parameter expression="${codegen.usePom}" default-value="true"
     */
    boolean usePom

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project

    /*
     * @parameter expression="${codegen.debug}" default-value="true"
     */
    boolean useGUI

    private static String PROPS_TO_COPY = "url,userName,password,driver,tableNames,packageName,rootDir,appConfigDir,xmlFileName,xmlDataSourceFileName,propertiesFile,codeGenPackage,generators,debug"

    void execute() {

        def build = project.build


        log.info "project groupId ${project.groupId} artifactId ${artifactId}"
        log.info "build.sourceDirectory = ${build.sourceDirectory}"
        log.info "build.outputDirectory = ${build.outputDirectory}"

        build.resources.each {
           log.info "--------- dump resource ----------"
           it.dump()
        }

        build.testResources  {
            log.info "--------- test resource ----------"
            it.dump()
        }

        log.info "DUMP BUILD"
        build.dump()


        CodeGenMain main
        if (useGUI) {
            GeneratorSwingApp generatorSwingApp = new GeneratorSwingApp()
            main = generatorSwingApp.main
            configure(main)
            generatorSwingApp.show()

        } else {
            main = new CodeGenMain()
            main.actions.addAll(actions.split(","))
            configure(main)
            main.run()
        }

    }

    def configure(CodeGenMain main) {
        List<String> props = PROPS_TO_COPY.split(",")
        for (String prop : props) {
            main[prop]=this[prop]
            info.log "property set as follows: ${prop}=${main[prop]}"
        }
    }
}

```

I'd like to make codegen more maven aware (like reading the pom/project to get that actual locations of src/main/java and src/main/webapp). Not sure 100% how to do that yet (thus the copious print statements).


I set the goal prefix in the mojo pom as follows:

```
    <build>
        <plugins>
            ...
            <plugin>
              <artifactId>maven-plugin-plugin</artifactId>
              <version>2.3</version>
              <configuration>
                <goalPrefix>codegen</goalPrefix>
              </configuration>
            </plugin>
        </plugins>
    </build>

```


I then try to configure the plugin into the sample app's pom as follows:
```
	<build>
		<finalName>crank</finalName>
		<resources>
			<resource>
				<directory>src/main/resources</directory>
			</resource>
			<resource>
				<directory>src/main/java</directory>
			</resource>
		</resources>
		<plugins>
			<plugin>
				<groupId>org.crank</groupId>
				<artifactId>p2c-codegen-mojo</artifactId>
				<version>1.0-SNAPSHOT</version>
				<configuration>
					<codegen>
						<usePom>true</usePom>
						<packageName>com.somecompany.employeetask</packageName>
						<jdbc>
							<url>jdbc:mysql://localhost:3306/presto2</url>
							<userName>presto2</userName>
							<password>presto2</password>
							<driver>com.mysql.jdbc.Driver</driver>	
						</jdbc>
					</codegen>
				</configuration>
			</plugin>

```

Then after dutifully building everything (mvn clean install), I try running the plugin:

```
$ mvn codegen:codegen
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'codegen'.
[INFO] ------------------------------------------------------------------------
[ERROR] BUILD ERROR
[INFO] ------------------------------------------------------------------------
[INFO] The plugin 'org.apache.maven.plugins:maven-codegen-plugin' does not exist or no valid version could be found
[INFO] ------------------------------------------------------------------------
[INFO] For more information, run Maven with the -e switch
[INFO] ------------------------------------------------------------------------
[INFO] Total time: < 1 second
[INFO] Finished at: Fri Jan 16 12:16:12 PST 2009
[INFO] Final Memory: 6M/490M
[INFO] ------------------------------------------------------------------------
```

Ummm... Err... Can't find the plugin eh. I seem to remember something about mojo registration in that maven book.

Scanning for 20 minutes...


Here it is...
http://books.sonatype.com/maven-book/reference/writing-plugins-sect-plugin-prefix.html

I read it a few times. Not the most clearly written part of the book. Cross my eyes. Focus. Focus. Focus. Huh?

Ok I think after reading the above a few times, that all I need to do is add an entry to settings.xml....

```
$ pwd
/Users/richardhightower
$ cd .m2
$ open settings.xml

<?xml version="1.0"?>
<settings>
    <pluginGroups>
       <pluginGroup>org.crank</pluginGroup>
    </pluginGroups>

```

Let me try again....

```
$ mvn codegen:codegen
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'codegen'.
[INFO] org.crank: checking for updates from central
[INFO] ------------------------------------------------------------------------
[ERROR] BUILD ERROR
[INFO] ------------------------------------------------------------------------
[INFO] The plugin 'org.apache.maven.plugins:maven-codegen-plugin' does not exist or no valid version could be found
[INFO] ------------------------------------------------------------------------
[INFO] For more information, run Maven with the -e switch
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 15 seconds
[INFO] Finished at: Fri Jan 16 12:22:39 PST 2009
[INFO] Final Memory: 7M/490M
[INFO] ------------------------------------------------------------------------
```

That looks vaguely familiar. Like my first girlfriend told me: "that's not it".

It did mention editing some other file (here is what the books says):

> "You can use the plugin prefix jar and turn that command-line into mvn jar:jar.
> > How does Maven resolve something like jar:jar to
> > org.apache.mven.plugins:maven-jar:2.3? Maven looks at a file in
> > the Maven repository to obtain a list of plugins for a specific groupId.
> > By default, Maven is configured to look for plugins in two groups:
> > org.apache.maven.plugins and org.codehaus.mojo."

How does it look in a group?


> "When you specify a new plugin prefix like mvn hibernate3:hbm2ddl,
> Maven is going to scan the repository metadata for the appropriate plugin prefix."

Where, in the groups?

> "First, Maven is going to scan the org.apache.maven.plugins group for the plugin
> > prefix hibernate3."

So there is a file inside of this group?



> "If it doesn't find the plugin prefix hibernate3 in the org.apache.maven.plugins
> group it will scan the metadata for the org.codehaus.mojo group."

Ok, so what file do I have to edit to make this happen for codegen?

> "When Maven scans the metadata for a particular groupId,
> > it is retrieving an XML file from the Maven repository which captures
> > metadata about the artifacts contained in a group."

Ah ha there is an XML file involved....


> "This XML file is specific for each repository referenced, if you are not
> using a custom Maven repository, you will be able to see the Maven metadata
> for the org.apache.maven.plugins group in your local Maven repository
> (~/.m2/repository) under org/apache/maven/plugins/maven-metadata-central.xml"

Now we are cooking with bacon!

> "Example 17.4, “Maven Metadata for the Maven Plugin Group” shows a
> snippet of the maven-metadata-central.xml file from the
> org.apache.maven.plugin group."

Based on the above....

```
$ pwd
/Users/richardhightower/.m2

$ find . -name "maven-metadata-central.xml"
./repository/cache-base/cache-service/1.0-SNAPSHOT/maven-metadata-central.xml
./repository/cache-base/cache-service-impl/1.0-SNAPSHOT/maven-metadata-central.xml
./repository/com/lowagie/itext/maven-metadata-central.xml
./repository/commons-collections/commons-collections/maven-metadata-central.xml
./repository/commons-digester/commons-digester/maven-metadata-central.xml
./repository/commons-logging/commons-logging/maven-metadata-central.xml
./repository/eclipse/jdtcore/maven-metadata-central.xml
./repository/jfree/jcommon/maven-metadata-central.xml
./repository/jfree/jfreechart/maven-metadata-central.xml
./repository/net/sf/mgp/maven-gwt-plugin/maven-metadata-central.xml
./repository/org/apache/maven/archetypes/maven-archetype-quickstart/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-antrun-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-archetype-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-checkstyle-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-clean-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-codegen-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-eclipse-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-help-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-idea-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-install-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-intellij-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-jar-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-javadoc-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-jetty-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-netbeans-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-pmd-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-release-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-resources-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-site-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-source-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-surefire-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-test-plugin/maven-metadata-central.xml
./repository/org/apache/maven/plugins/maven-war-plugin/maven-metadata-central.xml
./repository/org/codehaus/mojo/jboss-maven-plugin/maven-metadata-central.xml
./repository/org/codehaus/mojo/maven-metadata-central.xml
./repository/org/codehaus/mojo/tomcat-maven-plugin/maven-metadata-central.xml
./repository/org/crank/crank-core/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/crank-crud/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/crank-jsf-support/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/crank-jsf-validation/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/crank-springmvc-validation/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/crank-test-support/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/crank-validation/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/example/employee-jpa-model/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/example/security-jpa-model/1.0.1-SNAPSHOT/maven-metadata-central.xml
./repository/org/crank/maven-metadata-central.xml
./repository/org/mortbay/jetty/maven-jetty-plugin/maven-metadata-central.xml
```

Ok... I was expecting one file and I got... umm, err, a lot of files.

It seems like I am doing the right thing (or close):

Let's just see what is in the group for this plugin:
```
$ open ./repository/org/crank/maven-metadata-central.xml
<?xml version="1.0" encoding="UTF-8"?><metadata />
```

Ummm.. Err.. That file is empty so when I configured the settings to search this group. This group was empty. Nice. I thought the maven-plugin-plugin mojo that I configured would register this. Hmmm... Exactly what does the maven-plugin-plugin do?

No matter let me edit this above....

The example in the book is as follows:

```
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <plugins>
    <plugin>
      <name>Maven Clean Plugin</name>
      <prefix>clean</prefix>
      <artifactId>maven-clean-plugin</artifactId>
    </plugin>
    <plugin>
      <name>Maven Compiler Plugin</name>
      <prefix>compiler</prefix>
      <artifactId>maven-compiler-plugin</artifactId>
    </plugin>
    <plugin>
      <name>Maven Surefire Plugin</name>
      <prefix>surefire</prefix>
      <artifactId>maven-surefire-plugin</artifactId>
    </plugin>
    ...
  </plugins>
</metadata>
```

Based on that my guess is:

```
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <plugins>
    <plugin>
      <name>Codgen code generator for Crank and Presto2</name>
      <prefix>codegen</prefix>
      <artifactId>p2c-codegen-mojo</artifactId>
    </plugin>
  </plugins>
</metadata>
```

Ok... that seemed to cause something to happen. The book needs an example with some step by step instructions. I think I got it through reading and trial & error.

Now I have a new error message:
```
richard-hightowers-macbook-pro:crank-example richardhightower$ mvn codegen:codegen
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'codegen'.
[INFO] org.crank: checking for updates from central
[INFO] ------------------------------------------------------------------------
[ERROR] BUILD ERROR
[INFO] ------------------------------------------------------------------------
[INFO] The plugin 'org.apache.maven.plugins:maven-codegen-plugin' does not exist or no valid version could be found
[INFO] ------------------------------------------------------------------------
[INFO] For more information, run Maven with the -e switch
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 15 seconds
[INFO] Finished at: Fri Jan 16 12:22:39 PST 2009
[INFO] Final Memory: 7M/490M
[INFO] ------------------------------------------------------------------------
richard-hightowers-macbook-pro:crank-example richardhightower$ mvn codegen:codegen
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'codegen'.
[INFO] ------------------------------------------------------------------------
[INFO] Building Crank :: Crud :: Master Example project
[INFO]    task-segment: [codegen:codegen]
[INFO] ------------------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[ERROR] BUILD ERROR
[INFO] ------------------------------------------------------------------------
[INFO] One or more required plugin parameters are invalid/missing for 'codegen:codegen'

[0] Inside the definition for plugin 'p2c-codegen-mojo' specify the following:

<configuration>
  ...
  <driver>VALUE</driver>
</configuration>

-OR-

on the command line, specify: '-Dcodegen.jdbc.driver=VALUE'

[1] Inside the definition for plugin 'p2c-codegen-mojo' specify the following:

<configuration>
  ...
  <password>VALUE</password>
</configuration>

-OR-

on the command line, specify: '-Dcodegen.jdbc.password=VALUE'

[2] Inside the definition for plugin 'p2c-codegen-mojo' specify the following:

<configuration>
  ...
  <url>VALUE</url>
</configuration>

-OR-

on the command line, specify: '-Dcodegen.jdbc.url=VALUE'

[3] Inside the definition for plugin 'p2c-codegen-mojo' specify the following:

<configuration>
  ...
  <userName>VALUE</userName>
</configuration>

-OR-

on the command line, specify: '-Dcodegen.jdbc.userName=VALUE'

[INFO] ------------------------------------------------------------------------
[INFO] For more information, run Maven with the -e switch
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1 second
[INFO] Finished at: Fri Jan 16 12:44:10 PST 2009
[INFO] Final Memory: 10M/490M
[INFO] ------------------------------------------------------------------------

```

Seems I misunderstood the docs a bit... I thought having an expression like this:

```
    /**
     * JDBC Url
     *
     * @required
     * @parameter expression="${codegen.jdbc.url}"
     */
    String url

```

Would mean that I could have an entry like this in the mojo config:

```

			<plugin>
				<groupId>org.crank</groupId>
				<artifactId>p2c-codegen-mojo</artifactId>
				<version>1.0-SNAPSHOT</version>
				<configuration>
					<codegen>
						<usePom>true</usePom>
						<packageName>com.somecompany.employeetask</packageName>
						<jdbc>
							<url>jdbc:mysql://localhost:3306/presto2</url>
							<userName>presto2</userName>
							<password>presto2</password>
							<driver>com.mysql.jdbc.Driver</driver>	
						</jdbc>
					</codegen>
				</configuration>
			</plugin>
```

But based on the error message that just applies to the maven properties, not the configuration so it should be like this:

```

			<plugin>
				<groupId>org.crank</groupId>
				<artifactId>p2c-codegen-mojo</artifactId>
				<version>1.0-SNAPSHOT</version>
				<configuration>
					<usePom>true</usePom>
					<packageName>com.somecompany.employeetask</packageName>
					<url>jdbc:mysql://localhost:3306/presto2</url>
					<userName>presto2</userName>
					<password>presto2</password>
					<driver>com.mysql.jdbc.Driver</driver>	
				</configuration>
			</plugin>
```

Eureka! That did it.... It now runs the plugin. Seems like a lot of configuration, I wonder if there is a way to add that mojo meta-data as part of the build process. One of the reason I want to publish this as a plugin is so I can easily distribute it to the whole company (one day, the whole world). Hmmmm....

On a side note, I am getting hungry....

I am stuck at this point now:

```
$ mvn codegen:codegen
[INFO] property set as follows: debug=null
[INFO] ------------------------------------------------------------------------
[ERROR] FATAL ERROR
[INFO] ------------------------------------------------------------------------
[INFO] Cannot invoke method mkdirs() on null object
[INFO] ------------------------------------------------------------------------
[INFO] Trace
java.lang.NullPointerException: Cannot invoke method mkdirs() on null objectdeAdapter.java:198)
	at com.arcmind.codegen.CodeGenMain.writeProperties(CodeGenMain.groovy:355)
	at com.arcmind.codegen.CodeGenMain.this$2$writeProperties(CodeGenMain.groovy)
        at com.arcmind.codegen.CodeGenMain.run(CodeGenMain.groovy:71)
        at com.arcmind.codegen.CodeGenMain.run(CodeGenMain.groovy:71)
...
```

First food, then debugging...

#### Onward and upwards ####

Ok... I am past the mojo issues and now am on my own issues. The failures seem to be in my code. (My code runs standalone, but not so much inside of the mojo). Hmmmm....

Time to debug....

First I wrote this little script so I can connect to the mvn process running my mojo:

```
$ cat rungoal.sh
#!/bin/sh
MAVEN_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009 -ea"
export MAVEN_OPTS
mvn  codegen:codegen
```

![http://krank.googlecode.com/svn/wiki/img/codegen/debugMojo.png](http://krank.googlecode.com/svn/wiki/img/codegen/debugMojo.png)

Ok... the above did not quite work. I tried changing it from attach to listen. I need the gui to stop and wait for me to attach I guess. Listen ignored my breakpoint and attach is too fast for me to attach it. I will need to have it pause so I can attach. I just need to remember how.

Ok... easy enough just change suspend=n to suspend=y

```
#!/bin/sh
MAVEN_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9009 -ea"
export MAVEN_OPTS
echo $MAVEN_OPTS
mvn  codegen:codegen

```

Boyyeah! It hit my break point.