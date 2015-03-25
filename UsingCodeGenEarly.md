# Introduction #

Crank/Presto2 provides a code generator that reverse engineer an existing database and create a starter project. This feature is very nascent, and we are looking for people to try it out.

# Status #
It now works with Windows thanks to Alex (Oleg) and I. It was breaking due to case sensitivity issues (that were MySQL 5 and Windows specific). The next step is to get it to work with Oracle. (It currently works with MySQL and HSQL).

# Details #

Using Codegen code generator with Crank. The codegen is a generic code generator that has templates written for Crank and Presto2.

This is a first attempt at writing a guide on how to create a new project with crank using the crank maven archetype and the codegen maven mojo. (A mojo is a maven plugin.)

Since there is no binary release of these yet, you need to check them out and build them.

  1. Check out crank and build it
  1. Check out codegen and build it
  1. Check out presto2/crank mojo and build it
  1. Check out archetype build it
  1. Setup codegen mojo in local maven repo
  1. Use Archetype
  1. Use codegen maven plugin to reverse engineer a database
  1. Run webapp with jetty

Now the first 5 steps are needed because Crank does not have any recent binary distributions. When Crank gets an official release and gets added to the public maven repo then they will not be needed.

## Status of instructions ##


## Step One: Check out crank and build it ##
First check out the latest version of Crank (binary distribution to ensue)

```
$ svn co http://krank.googlecode.com/svn/trunk crank
$ cd crank
```

Then build crank
```
$ mvn clean install
```

If you have problems any build problems (let me know), then build using this:
```
$ mvn clean install -Dtest=none -DfailIfNoTests=false
```

Generally if you have a problem with any of the builds, just add  -Dtest=none -DfailIfNoTests=false and then notify me of the problem you had.

You now have Crank installed into you local maven repository.

## Step 2: Check out codegen and build it ##

Next check out codegen and build it.
```
$ svn co https://krank.googlecode.com/svn/crank-codegen codegen
$ cd codegen
```

Then build it
```
$ mvn clean install
```

If there are problems building try with -Dtest=none -DfailIfNoTests=false and notify me (Rick Hightower email: RichardHightower AT gmail DOT com).

## Step 3:  Check out presto2/crank mojo and build it ##

Check it out:
```
$ svn co https://krank.googlecode.com/svn/mojosAndArchetypes/codegenMojo codegen-mojo
$ cd codegen-mojo
```

Then build:
```
$ mvn clean install
```

If there are problems building try with -Dtest=none -DfailIfNoTests=false and notify me (Rick Hightower email: RichardHightower AT gmail DOT com).

## Step 4: Check out archetype build it ##

Check it out:
```
$ svn co https://krank.googlecode.com/svn/mojosAndArchetypes/crank-example-archetype crank-archetype
$ cd crank-archetype
```

Then build:
```
$ mvn clean install
```

If there are problems building try with -Dtest=none -DfailIfNoTests=false and notify me (Rick Hightower email: RichardHightower AT gmail DOT com).

## Step 5: Setup codegen mojo in local maven repo ##
There does not seem to be an easy way to automatically register a new mojo with a short name. I consulted the Maven Reference from Orielly and the world renown maven expert Carlos Sanchez.

It seems you need to register yourself in the xml files.
This would need to get setup in your local maven repo.
The first step to set this up is to edit the settings.xml as follows:

Mac OSX:
```
$ open ~/.m2/settings.xml
```

Windows:
```
$ notepad "%USERPROFILE%\.m2\settings.xml"
```

Add the following entry to this file:

#### settings.xml ####
```

<?xml version="1.0"?>
<settings>
    <pluginGroups>
       <pluginGroup>org.crank</pluginGroup>
    </pluginGroups>
    ...
</settings>
```


Next you need to create the short name by editing the maven-metadata-central.xml file for the org.crank package as follows:

Mac OSX:
```
open ~/.m2/repository/org/crank/maven-metadata-central.xml
```

Windows
```
notepad "%USERPROFILE%\.m2\repository\org\crank\maven-metadata-central.xml"
```

Edit the file
#### org/crank/maven-metadata-central.xml ####
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

Steps 1-5 only ever need to be done once, after that you can use the project.

## Step 6: Use Archetype ##
Now that the code generator and archetype is build, we can use them. First we want to use the archetype to generate a stub project as follows:

```
$ mvn archetype:generate -DarchetypeCatalog=local
```

Then it will prompt us as follows:

```
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
```

Pick the one associated with the archetype. Above I had only one to pick from so I just typed 1.

Then it will ask you for the groupId, artifactId, version, and package (here is what I filled out):

```
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
```


## Step 7: Use codegen maven plugin to reverse engineer a database ##

Before you can perform this step, you have to have a database. The first time you run the example, I suggest that you use the sample database that we use for testing. Currently, we have only tested codegen with MySQL using INNODB and HSQL. For instructions on how to build/create the sample DB go to CreatingSampleDatabaseForCodegenCrank.


Edit the pom.xml. Notice that the codegen-mojo is already configured.
Change the package name to the one you picked in step 6 (com.somecompany.coolproject) and make it match the following:

#### pom.xml ####
```

			<plugin>
				<groupId>org.crank</groupId>
				<artifactId>p2c-codegen-mojo</artifactId>
				<version>1.0-SNAPSHOT</version>
				<configuration>
					<useGUI>true</useGUI>
					<usePom>true</usePom>
					<packageName>com.exfoo</packageName>
					<url>jdbc:mysql://localhost:3306/presto2</url>
					<userName>presto2</userName>
					<password>presto2</password>
					<driver>com.mysql.jdbc.Driver</driver>
					<tableNames>DEPARTMENT,EMPLOYEE,ROLE</tableNames>
					<debug>true</debug>
					<trace>true</trace>
					<propertiesFile>./codegen/config.properties</propertiesFile>
					<appConfigDir>./codegen</appConfigDir>
				</configuration>
			</plugin>

```


Go into the directory of the project you created in step 6 and type:

```
$ mvn codegen:codegen
```

A swing gui will popup, click the "Modify Properties" button and ensure that the package name is correct.

Hit the Reverse DB button.
Hit the Generate Code button.
You are done.

## Step 8: Run webapp with jetty ##
Run the webapp with the maven jetty plugin
$ mvn -Ddb=mysql -Dlog4j.configuration=file:./log4j.xml  jetty:run

Go to http://localhost:8080 in your browser and click the link associated with this app. Try adding employees.

TBD add screen shots.