I have been fairly steady working on the CodeGen project. Due to the wonders of open source, I have received some help in improving the Swing GUI. Thanks Alex.

I had some issues creating the archetype. I had to rename the packages of the sample app (sample app is at svn https://krank.googlecode.com/svn/crank-examples/crank-example) so that they would not collide with crank package names, otherwise, the maven archetype tool mangled the living snot out of my library imports.

I've created the following generators (since last I wrote about this):

  * **FacesConfigCodeGen** (edits an faces-config to add entries for CRUD pages)
  * **SpringJavaConfigCodeGen** (edits a Spring Java Config file to add CRUD controllers)
  * **XHTMLCodeGenerator** (generates XHTML/Facelets pages for Listings and forms)
  * **JPACodeGenerator** (Generates JPA Model classes)

The above all have unit tests, but only **JPACodeGenerator** generated code and been tested in a running application. **FacesConfigCodeGen**, **SpringJavaConfigCodeGen**, and **XHTMLCodeGenerator** have not been wired into the Swing GUI yet. Each took about a day to write and test.

They all work with ManyToOne, OneToMany, and ManyToMany relationships, in otherwords, they generate all the GUI needed for editing/managing these relationships. I have not added support for OneToOne (Crank/Presto2 supports it, but the generation does not).

I also greatly improved the JPACodeGenerator. It took quite a few passes to get it right, but I was going for perfection. (I hate crappily generated code with a lot of spurious unused imports and weird formatting)

The plan for today is to create an archetype, change the code generator to be aware of a maven project structure. Then use the archetype to create a new project. Point the code generator at the project directory. Configure the code generators programatically to handle the maven project structure. Then integration test the generated code by generating code and testing the webapp.

You can find the code for the code generator at: https://krank.googlecode.com/svn/crank-codegen. Just check it out with svn and provide comments....

```
svn co http://krank.googlecode.com/svn/crank-codegen codegen
```

#### Project notes on using maven archetype support ####
The best guide I've seen for creating a maven archetype based on a previous maven project is here: [Nino Martinez' guide to making a maven archetype](http://ninomartinez.wordpress.com/2008/09/03/making-a-maven-archetype/)

Here I repeat it....

  * Build a project that will act as a template for the archetype
  * run mvn archetype:create-from-project , from the project folder
  * Edit the generated archetype ( placed in target/generated-sources/archetype ), it might not pickup on everything that needs to be substituted.
  * From target/generated-sources/archetype of the project template run mvn install
  * Try the archetype yourself by running mvn archetype:generate -DarchetypeCatalog=local from a fresh directory
  * Repeat the process until satisfied

Before I started this, I made crank-example (svn http://krank.googlecode.com/svn/crank-examples/crank-example) a standalone sample (it does not inherit from any crank pom).

Here is step 1:
```
richard-hightowers-macbook-pro:crank-example richardhightower$ mvn clean
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building Crank :: Crud :: Master Example project
[INFO]    task-segment: [clean]
[INFO] ------------------------------------------------------------------------
[INFO] [clean:clean]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1 second
[INFO] Finished at: Wed Jan 14 13:17:45 PST 2009
[INFO] Final Memory: 8M/490M
[INFO] ------------------------------------------------------------------------
richard-hightowers-macbook-pro:crank-example richardhightower$ mvn archetype:create-from-project
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'archetype'.
[INFO] ------------------------------------------------------------------------
[INFO] Building Crank :: Crud :: Master Example project
[INFO]    task-segment: [archetype:create-from-project] (aggregator-style)
[INFO] ------------------------------------------------------------------------
[INFO] Preparing archetype:create-from-project
[INFO] ------------------------------------------------------------------------
[INFO] Building Crank :: Crud :: Master Example project
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[INFO] Setting property: velocimacro.messages.on => 'false'.
[INFO] Setting property: resource.loader => 'classpath'.
[INFO] Setting property: resource.manager.logwhenfound => 'false'.
[INFO] [archetype:create-from-project]
[INFO] Setting default groupId: crank-examples
[INFO] Setting default artifactId: crank-example
[INFO] Setting default version: 1.0.4-SNAPSHOT
[INFO] Setting default package: com.mycompany
[INFO] Archetype created in target/generated-sources/archetype
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4 seconds
[INFO] Finished at: Wed Jan 14 13:18:03 PST 2009
[INFO] Final Memory: 15M/490M
[INFO] ------------------------------------------------------------------------
richard-hightowers-macbook-pro:crank-example richardhightower$ 
```

Then I copy over to my archetypes folder where I can edit it and inspect it:

```
$ pwd
/Users/richardhightower/projects/crank-examples/crank-example

$ mv target/generated-sources/archetype ~/projects/crank-archetypes/crank-example-archetype

```

Next I need to build the archetype so I can use it (done in ~/projects/crank-archetypes/crank-example-archetype):

```
$ pwd 
/Users/richardhightower/projects/crank-archetypes/crank-example-archetype

$ mvn clean install
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building crank-example-archetype
[INFO]    task-segment: [clean, install]
[INFO] ------------------------------------------------------------------------
[INFO] [clean:clean]
[INFO] Deleting directory /Users/richardhightower/projects/crank-archetypes/crank-example-archetype/target
[INFO] [resources:resources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [resources:testResources]
[INFO] Using default encoding to copy filtered resources.
[INFO] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[INFO] Setting property: velocimacro.messages.on => 'false'.
[INFO] Setting property: resource.loader => 'classpath'.
[INFO] Setting property: resource.manager.logwhenfound => 'false'.
[INFO] [archetype:jar]
[INFO] [archetype:add-archetype-metadata]
[INFO] [archetype:integration-test]
[INFO] [install:install]
[INFO] Installing /Users/richardhightower/projects/crank-archetypes/crank-example-archetype/target/crank-example-archetype-1.0.4-SNAPSHOT.jar to /Users/richardhightower/.m2/repository/crank-examples/crank-example-archetype/1.0.4-SNAPSHOT/crank-example-archetype-1.0.4-SNAPSHOT.jar
[INFO] [archetype:update-local-catalog]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3 seconds
[INFO] Finished at: Wed Jan 14 13:24:32 PST 2009
[INFO] Final Memory: 18M/490M
[INFO] ------------------------------------------------------------------------

```

Now I want to use the archetype to generate a project, and then test the project as follows:

```
$ pwd
/Users/richardhightower

$ cd projects/
$ mkdir generateprojects
$ cd generateprojects/
$ mvn archetype:generate -DarchetypeCatalog=local
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
[INFO] No archetype defined. Using maven-archetype-quickstart (org.apache.maven.archetypes:maven-archetype-quickstart:1.0)
Choose archetype:
1: local -> crank-example-archetype (crank-example-archetype)
Choose a number:  (1): 1
Define value for groupId: : testingArchetype1
Define value for artifactId: : employeeTaskTestApp
Define value for version:  1.0-SNAPSHOT: : 
Define value for package:  testingArchetype1: : com.somecompany.coolproject
Confirm properties configuration:
groupId: testingArchetype1
artifactId: employeeTaskTestApp
version: 1.0-SNAPSHOT
package: com.somecompany.coolproject
 Y: : Y
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 58 seconds
[INFO] Finished at: Wed Jan 14 13:28:31 PST 2009
[INFO] Final Memory: 14M/490M
[INFO] ------------------------------------------------------------------------

```

After you run mvn archetype:generate -DarchetypeCatalog=local, maven prompts you information about the project you want to create (which you fill out via the console).

Now to verify that the project is actually there and that it builds:

```
$ ls
employeeTaskTestApp

$ cd employeeTaskTestApp/

$ ls
log4j.xml	pom.xml		runjetty.sh	src

$ mvn clean install

[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building Crank :: Crud :: Master Example project
[INFO]    task-segment: [clean, install]
[INFO] ------------------------------------------------------------------------
[INFO] [clean:clean]
[INFO] [resources:resources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [compiler:compile]
[INFO] Compiling 5 source files to /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/target/classes
[INFO] [resources:testResources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [compiler:testCompile]
[INFO] No sources to compile
[INFO] [surefire:test]
[INFO] No tests to run.
[INFO] [war:war]
[INFO] Packaging webapp
[INFO] Assembling webapp[employeeTaskTestApp] in [/Users/richardhightower/projects/generateprojects/employeeTaskTestApp/target/crank]
[INFO] Processing war project
[INFO] Webapp assembled in[2786 msecs]
[INFO] Building war: /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/target/crank.war
[INFO] [install:install]
[INFO] Installing /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/target/crank.war to /Users/richardhightower/.m2/repository/testingArchetype1/employeeTaskTestApp/1.0-SNAPSHOT/employeeTaskTestApp-1.0-SNAPSHOT.war
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 19 seconds
[INFO] Finished at: Wed Jan 14 13:31:58 PST 2009
[INFO] Final Memory: 33M/490M
[INFO] ------------------------------------------------------------------------

```

So far so good... now let's see if it will run....

I've added a runjetty.sh script that uses the jetty plugin for maven as follows:

```
$ cat runjetty.sh 

#!/bin/sh
MAVEN_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009 -ea"
export MAVEN_OPTS
echo $MAVEN_OPTS
mvn -Ddb=mysql -Dlog4j.configuration=file:./log4j.xml  jetty:run -o

$ chmod +x runjetty.sh 

$ runjetty.sh 

-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009 -ea
Listening for transport dt_socket at address: 9009
[INFO] 
NOTE: Maven is executing in offline mode. Any artifacts not already in your local
repository will be inaccessible.

[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'jetty'.
[INFO] ------------------------------------------------------------------------
[INFO] Building Crank :: Crud :: Master Example project
[INFO]    task-segment: [jetty:run]
[INFO] ------------------------------------------------------------------------
[INFO] Preparing jetty:run
[INFO] [resources:resources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [compiler:compile]
[INFO] Nothing to compile - all classes are up to date
[INFO] [resources:testResources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [compiler:testCompile]
[INFO] No sources to compile
[INFO] [jetty:run]
[INFO] Configuring Jetty for project: Crank :: Crud :: Master Example project
[INFO] Webapp source directory = /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/src/main/webapp
[INFO] web.xml file = /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/src/main/webapp/WEB-INF/web.xml
[INFO] Classes = /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/target/classes
2009-01-14 13:33:38.532::INFO:  Logging to STDERR via org.mortbay.log.StdErrLog
[INFO] Context path = /employeeTaskTestApp
[INFO] Tmp directory = /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/target/work
[INFO] Web defaults =  jetty default
[INFO] Web overrides =  none
[INFO] Webapp directory = /Users/richardhightower/projects/generateprojects/employeeTaskTestApp/src/main/webapp
[INFO] Starting jetty 6.1.5 ...
2009-01-14 13:33:38.667::INFO:  jetty-6.1.5
log4j:WARN Continuable parsing error 109 and column 23
log4j:WARN The content of element type "log4j:configuration" must match "(renderer*,appender*,(category|logger)*,root?,categoryFactory?)".
2009-01-14 13:33:39.156::WARN:  Unknown realm: default
2009-01-14 13:33:39.177::INFO:  No Transaction manager found - if your webapp requires one, please configure one.
2009-01-14 13:33:40.884:/employeeTaskTestApp:INFO:  Initializing Spring root WebApplicationContext
Jan 14, 2009 1:33:41 PM com.sun.faces.config.ConfigureListener contextInitialized
INFO: Initializing Sun's JavaServer Faces implementation (1.2_04-b10-p01) for context '/employeeTaskTestApp'
2009-01-14 13:33:45.894::INFO:  Started SelectChannelConnector@0.0.0.0:8080
[INFO] Started Jetty Server

```

It seems to startup the webapp without any catastrophic errors.

I opened up the page and it looks correct to... The app works. Life is good:

![http://krank.googlecode.com/svn/wiki/img/codegen/archetype_success.png](http://krank.googlecode.com/svn/wiki/img/codegen/archetype_success.png)

When things go well, I love maven.

#### Next steps ####
I figured I would right down the next steps before I break for lunch:

  * Edit sample app, add comments that code generator needs to edit files
  * Recreate archetype (with new comments)
  * Change codegen to recognize maven project structure and configure code generators accordingly
  * Recreate sample project with needed codegen comments
  * Point codegen to sample project folder
  * Generate artifacts (xListing.xhtml, xForm.xhtml, entries in faces-config.xml, entries in Spring Java Config)
  * Test app and see if it runs the generated listings
  * Repeat as needed until codegen is beat into submission

#### Random musing ####
My Groovy is staring to look as readable as Perl.

```
	public List<Class> loadClassesForGenerators () {
		if (this.generators==null || "".equals(this.generators.trim())) {
			codeGenPackage = "com.arcmind.codegen"
			generators="FacesConfigCodeGen,JPACodeGenerator,SpringJavaConfigCodeGen,XHTMLCodeGenerator"
		}
		List<String> classNames = this.generators.split(",").findAll{String className -> className != ""}
		return classNames.collect{String className-> className.contains(".") ? Class.forName(className) : Class.forName("${codeGenPackage}.${className}")}
	}
```

I am enjoying writing but who is going to be able to maintain this...

#### Refactoring CodeGenerators ####

I decided that I need to refactor the code generators so codegen can polymorphicaly handle multiple code generators. This will also make it possible for other developers to add custom code generators.

The codegen utility (code generator for crank and presto2) has the following code generators:

  * **FacesConfigCodeGen** (edits an faces-config to add entries for CRUD pages)
  * **SpringJavaConfigCodeGen** (edits a Spring Java Config file to add CRUD controllers)
  * **XHTMLCodeGenerator** (generates XHTML/Facelets pages for Listings and forms)
  * **JPACodeGenerator** (Generates JPA Model classes)

These were changed to implement this new interface as follows:

##### CodeGenerator.groovy interface #####
```

package com.arcmind.codegen


public interface CodeGenerator{
	boolean isUse()
	void setUse(boolean use)
	void process()
	void setDebug(boolean debug)
	void setClasses(List<JavaClass> classes)
	void setRootDir(File file)
	void setPackageName(String packageName)
}

```

Then I changed CodeGenMain as follows:

Added a property that accepts a comma delimited list of code generator names (and a default package for code generators) as follows:
```
public class CodeGenMain{
        ...
	String codeGenPackage
	String generators

```

It also has a list of code generator instances as follows:

```

public class CodeGenMain{
        ...
        List<CodeGenerator> codeGenerators = []
```

The two string properties mentioned above get populate by the command line arguments or entries in the config.properties file as follows:

##### config.properties #####
```

codeGenPackage=com.arcmind.codegen
# You can implement your own code generator by implementing the CodeGenerator interface
# generators are a list of class names, if they are not fully qualified the codeGenPackage is used
generators=FacesConfigCodeGen,JPACodeGenerator,SpringJavaConfigCodeGen,XHTMLCodeGenerator
```

You can see that the ones I wrote are already configured. If you wanted to add your own, it would be easy enough just to add another entry. The entry can be a fully qualified class name or just the short name if the class is in the codeGenPackage already.

The **loadClassesForGenerators** method was added to CodeGenMain to load the code generators dynamically.

```
public class CodeGenMain{
        ...

	public List<Class> loadClassesForGenerators () {
		if (this.generators==null || "".equals(this.generators.trim())) {
			codeGenPackage = "com.arcmind.codegen"
			generators="FacesConfigCodeGen,JPACodeGenerator,SpringJavaConfigCodeGen,XHTMLCodeGenerator"
		}
		List<String> classNames = this.generators.split(",").findAll{String className -> 
			className != ""
		}
		return classNames.collect{String className-> 
			className.contains(".") ? Class.forName(className) : Class.forName("${codeGenPackage}.${className}")
		}
	}

```


Then lastly we configure the code generator instances (as well as the code gen's classes) in the configureCollaborators as follows:

```
	public boolean configureCollaborators() {
                ...
		List<Class> codeGenClasses = loadClassesForGenerators()

		if (debug) {
                        ...
                        /* Override the println method so its output goes to the GUI */
			codeGenClasses.each{Class cls-> 
                             cls.metaClass.println = printlnClosure
                        }
		}

                ...
		collaborators = [jdbcUtils, reader, modelGen, ...]
                
                /* Instantiate the instances of code generators */
		codeGenerators = codeGenClasses.collect{Class cls -> cls.newInstance()}
		collaborators.addAll(codeGenerators)
		
		
		/* Collaborator debug configuration. */
		for (collaborator in collaborators) {
			collaborator.debug = debug == null ? false :Boolean.valueOf(debug)
		}

                /* Configure the actual code generators. */
		codeGenerators.each{CodeGenerator codeGen ->
			if (this.packageName != null) {
				codeGen.setPackageName(this.packageName)
			}
			if (this.rootDir != null) {
				codeGen.setRootDir(new File(this.rootDir))
			}
		}
                ....

```

Notice that we added a new property called rootDir. This is the rootDir of the project that we are generating artifacts for.

Now when we actually invoke these code generators we do it polymorphicaly as follows:

```
	def generateArtifacts() {
		if (debug) println "Generating artifacts"
		this.codeGenerators.each {CodeGenerator codeGen ->
			/* Output the generated classes. */
			codeGen.classes = modelGen.classes
			if (debug) println "Generating artifacts for ${codeGen.classes} with ${codeGen.class.name}"
			codeGen.process()
		}
	}

```

This should make it easy to extend the codegen utility to add additional code generators.

#### The code generation passes integration testing ####

For integration testing, I generated a project from the archetype, pointed the codegen swing app to it. Generated the artifacts and then ran the results with jetty.

Now I need to do the following:

  * Check the archetype into subversion
  * Figure out how to create a maven plugin (groovy based)
  * Get codegen to launch as a maven plugin

Seems there is a site that explains how to do this...

http://groovy.codehaus.org/GMaven+-+Implementing+Maven+Plugins

Reading now....

Here is the general guide to Maven MOJO (maven plugin development)

http://maven.apache.org/guides/plugin/guide-java-plugin-development.html

Ok... after reading a bit more the vision becomes clearer.
I will build the mojo which will include the codegen project as a dependency.

I will configure the sample app to use the cp2-codgen mojo then I will create an archetype based on this newly updated project. This way I can preconfigure the mojo as part of the artifact so it is completely configured.

I have this book at home, but since I am sitting here drinking my third cup of coffee for the day.... Here is the maven book from Orielly (the free version)
http://books.sonatype.com/maven-book/index.html

I opted for the HTML version so I can "bookmark it" here.
http://books.sonatype.com/maven-book/reference/writing-plugins.html

I am going to read this as the official docs were barely a snack and since I am going to spend sometime developing this plugin... might as make sure that I am doing this right.

I also read the section of using Groovy as the plugin bits.

Ok... I read it... I think I am going to change my strategy a bit. I am going to write the plugin first, then configure it in the sample app, then re-generate the archetype (so I don't have to check it in twice only to wipe it out and recheck it in).

I will do this in a new page.