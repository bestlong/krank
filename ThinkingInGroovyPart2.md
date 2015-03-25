# Introduction #

You may recall [ThinkingInGroovy](http://code.google.com/p/krank/wiki/ThinkingInGroovy) where I shared some experience playing around with Groovy. Well I recently got some funding to work on the code generator again. I dusted off the prototype that I wrote before and start hacking on it again.

This is my fourth code generator. The first code generator that I wrote was at Intel and it was done in JPython. Then a few years later I was at eBlox and wrote a code generator in Jython. Then whilst working at a contract for a big mobile telecom company, I wrote a code generator in Java as a Maven Mojo. (The templates were Velocity.)

So why Groovy.... Well, I really like Groovy. Groovy has what seems to be the best of Python and Java rolled up into a language and syntax that I love. Groovy is what I wished Jython could have been. Note this is a matter of opinion and I am not trying to start a flame war with the Python folks. I still love Python and love programming with it. I just prefer Groovy.

I have poured through the Groovy pragmatic books in hopes to fully utilize the Groovy language and I am doing this write up to see how close I have come to writing "Groovy code".

One area where I have chosen to be less Groovy is in making my methods and closures for the most part strongly typed. I have bad memories of really large Jython/JPython projects that I worked on getting out of hand with regards to typing issues. I like the self documenting nature of declaring methods that take typed arguments. It also helps the IDE give me code completion. So I unapologetically use types in many cases where a more "groovy" programmer would not.

This is a very earlier copy of this project and essentially works well enough to reverse engineer a JPA object model from a database table. I plan on adding Maven Plugin support and a Swing based GUI. Also I plan on adding features like adding classes and relationships from the command line and Swing GUI (even if they do not exist in the database yet).

This code will one day be the basis of code generation support for the Crank and Presto projects.

I have utilized some of the following Groovy language features:

  1. Dynamic property access
  1. XML writing via builder support
  1. XML reading via GPath support
  1. Custom closure creating for IoC for doing code cleanup
  1. AOP style method decoration for logging via Groovy metaClass, and expando support
  1. Category class for adding methods to another class
  1. SimpleTemplateEngine for code generation templates
  1. GDK to simplify working with files, collections
  1. Persisting and reading XML files with MarkupBuilder and XmlSlurper

## Classes in the codegen project ##


I would like to start with a brief overview of the classes involved and then discuss some of the Groovy language features that made this project a joy to write.

## Model classes ##
There are a series of model object to represent Java classes, properties and relationships as well as tables, keys, and columns.

Here are the model objects for database tables and Java classes.

| Class Name | Purpose |
|:-----------|:--------|
|      Table       |    Represents data about a table in a database       |
|      Key       |      Represents data about keys in a table (imported and exported)      |
|      Column       |    Represents data about a column in a table       |
|      JavaClass       |   Represents data about a Java class and how it is mapped to a Table       |
|      JavaProperty       |   Represents data about a Java property and how it is mapped to a database column        |
|      Relationship       |     Represents data about how Java classes are related      |





| Class Name | Purpose |
|:-----------|:--------|
| CodeGenMain           |   The main entry point into the command line version of the codegen application. Processes command line arguments and reads properties file used to configure application. Configure the other classes and performs injection of collaborators.      |
|  JdbcUtils          |  Utility class for working with low-level JDBC connections and ResultSets. This class is used a lot by the DataBaseMetaDataReader class.      |
|  DataBaseMetaDataReader          |   Used to read database metadata and create a hierarchy of Table, Column and Key model objects. The database metadata is read from connection.metaData.getTables, connection.metaData.getColumns, connection.metaData.getPrimaryKeys, connection.metaData.getExportedKeys and connection.metaData.getImportedKeys see Java API docs for JDBC Connection for more detail.      |
|    JavaModelGenerator        |   Takes the table hierarchy produced from DataBaseMetaDataReader or read from an XML file and generates a hierarchy of JavaClass, JavaProperty and Relationship model objects.     |
|    XMLPersister        |    Reads and writes editable XML files. This allows the end user to override what we guessed in the generation process.     |
|    CodeGenerator       | Generates **.java files from JavaClass models objects.**|
|    StringCategory      | Adds methods to the String class that are important for code generation. |

Let's cover the model objects as they are the least interesting and all of the other classes work with them:

```
class Table {
	String name    
	List <Column> columns = []
	Set <String> primaryKeys = []
	List <Key> exportedKeys = []
	List <Key> importedKeys = []
	public String toString() {
		"Table (name=${name}, columns=${columns}, primaryKeys=${primaryKeys} )"
	}
}

public class Key{
	Column primaryKey = new Column()
	Column foriegnKey = new Column()
	short updateRule
	short deleteRule
	String fkName
	String pkName
	short deferrability
	boolean imported
	
	String toString() {
		"Key(primaryKey=${primaryKey} \n     foriegnKey=${foriegnKey})"
	}
}

class Column {
	String name
	int type
	String typeName
	boolean nullable=true
	Table table
	boolean primaryKey
	
	public String toString() {
		"Column( name=${name}, table.name=${table?.name})"
	}
	
}

class JavaClass {

    String name
    String packageName
    boolean primitive
    JavaProperty id
    List<JavaProperty> properties = []
    List<Relationship> relationships = []
    HashMap<String, JavaProperty> columnNameToPropertyMap = [:]
    Table table

    public boolean isNamesMatch() {
        return name == table.name;
    }
    
    String toString() {
        "JavaClass name=${name} packageName=${packageName} primitive=${primitive} properties=${properties}"
    }
    
    

}

class JavaProperty {
    JavaClass javaClass
    JavaClass parentClass
    String name
    Column column
    public boolean isNamesMatch() {
        return name == column.name;
    }
    String toString() {
        "JavaProperty(name=${name} javaClass.name=${javaClass.name})"
    }
}

public class Relationship{
	String name
	RelationshipType type
	Key key
	JavaClass relatedClass
	
	public String toString() {
		"Relationship(name=${name}, cardinality=${cardinality}, \n key=%{key})"
	}
}

public enum RelationshipType{
	ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY, MANY_TO_ONE, JOINED_SUBCLASS_CHILD, JOINED_SUBCLASS_PARENT;
}

```

There is nothing too special about these classes. They are considered POGOs (Plain old Groovy objects). If you don't specify private, protected etc. on a field, it becomes a Java property. (You can't see the getter and setter but they are there). Groovy allows you to use strongly typed properties and I prefer them as they help me document and use the classes that I write. I don't think it makes them less Groovy.

Notice that the **toString** methods all use GStrings which makes defining **toString** methods a breeze. The GStrings work a bit like JSPs and EL. Notice that you don't need a return statement. The last line in a method is the return statement by default and the "return" statement is optional.

The **RelationshipType** was defined in Groovy but it was causing the maven compiler to puke so I redefined it in Java (renamed the **RelationshipType.groovy** to **RelationshipType.java**) and the Groovy maven plugin could handle it. Groovy comes with a joint compiler that can compile both Java classes and Groovy classes.

## The Groovyness of CodeGenMain ##

The **CodeGenMain** uses these Groovy language features:


  1. Dynamic property access
  1. AOP style method decoration for logging via Groovy metaClass, and expando support
  1. Weaves in a Category class for adding methods to another class


### Dynamic property access of CodeGenMain ###
The **CodeGenMain** processes the arguments and properties file using the dynamic property access.

**CodeGenMain** defines these properties:

```
public class CodeGenMain{
	String url
	String userName
	String password
	String driver
	String tableNames
	String packageName
	String outputDir
	String appConfigDir
	String xmlFileName
	String propertiesFile
	String debug
	...
```

It then processes the arguments with the **processArgs** method as follows:

```
	/* Parse the command line arguments. */
	public boolean processArgs (String [] args) {
		boolean invalidArgument = false
		
		String propertyName = null
		String value = null
		/* First pass make sure they passed valid arguments. */
		for (arg in args) {
			if (arg.contains("=")) {
				def command = arg.split("=")
				propertyName = command[0]
				value = command[1]
				if (!this.metaClass.hasProperty(this, propertyName)) {
					invalidArgument = true
					println "Invalid command line argument ${arg}"
				}
				
			}
		}

```

Notice it iterates through the **args** and checks to see if the argument passed (password=foo) and checks to see if **CodeGenMain** defines a property called **password** using the **hasProperty** method of the **metaClass** as follows:

```
this.metaClass.hasProperty(this, propertyName)
```

The **copyPropsFromArgs** method parses the arguments and sets them into **CodeGenMain** using **this\[propertyName\]=value** as follows:
```
	public void copyPropsFromArgs(String[] args) {
		actions = []
		for (String arg : args) {
			if (arg.contains("=")) { 
				def command = arg.split("=")
				String propertyName = command[0]
				String value = command[1]
				this[propertyName]=value
				if (debug) println "Processing property ${propertyName}=${value}"
			} else {
				if (availableActions.contains(arg)) {
					if (debug) println "Found action ${arg}"
					actions << arg
				} else {
					println "Action not understood " + arg
				}
			}
		}
	}

```

**CodeGenMain** writes to properties using the dynamic properties using **this\[key\] = configProperties\[key\]** and **configProperties\[key\] = this\[key\]** as follows:

```
	private void readProperties() {
		configProperties = new Properties()
		File propFile = calculatePropFile()
		if (propFile.exists()) {
			if (debug) println "Found properties file ${propFile}, reading it into application arguments" 
			configProperties.load(new StringReader(propFile.text))
			for (key in configProperties.keySet()) {
				if (configProperties[key]!=null) {
					if (debug) println "overiding values not set: ${key}=${configProperties[key]}"
					this[key] = configProperties[key]
				}
			}
		} else {
			if (debug) println "Properties file not found, so writng new properties file based on arguments passed"
			writeProperties()
		}
	}
	
	private File calculatePropFile () {
		propertiesFile==null ? new File(appConfigDirFile,"config.properties") : new File(propertiesFile) 
	}
	
	private void writeProperties() {
		configProperties = new Properties()
		File propFile = calculatePropFile()
		for (key in this.properties.keySet()) {
			
			Object value = this[key]
			if (value!=null) {
				if (value instanceof String) {
					if (debug ) println "Setting values into properties file ${key} = ${value}"
					configProperties[key] = value
				}
			}
		}
		propFile.parentFile.mkdirs()
		
		propFile.newOutputStream().withStream{stream -> configProperties.store(stream, "prop file") }
	}

```

Notice the ability to read and write a properties file in two lines of code with Groovy love and magic (in readProperties and writeProperties) as follows:

```
configProperties.load(new StringReader(propFile.text)) //read a properties file in one line of code
...
propFile.newOutputStream().withStream{stream -> configProperties.store(stream, "prop file") } //write a properties file in one line of code
```

### Weaves in a Category class for adding methods to another class ###

The **CodeGenMain** uses the **StringCategory** to add additional methods in the String class as follows:

```
	/** This is the main entry point for this program. */
	public static void main (String [] args) {
		use(StringCategory) { 
			CodeGenMain codeGenMain = new CodeGenMain()
			if (codeGenMain.processArgs(args)) {
				codeGenMain.run()
			}
		}
	}
```

For references here is the **StringCategory** class:

```
package com.arcmind.codegen



/** Adds methods to the String class that are important for code generation.
 *  @author richardhightower
 */
public class StringCategory{

    public static String cap(String self) {
        try {
            return self[0].toUpperCase() + self[1..-1]
        }   catch (Exception ex){
            return self
        }
    }

    public static String capAndLower(String self) {
        try {
            return self[0].toUpperCase() + self[1..-1].toLowerCase()
        }   catch (Exception ex){
            return self
        }
    }

	public static String unCap(String self) {
        try {
            return self[0].toLowerCase() + self[1..-1]
        }   catch (Exception ex){
            return self
        }
	}
	
	public static boolean isAllUppers(String self) {

		boolean notAllUpper = false
		self.each {
		    if (!Character.isUpperCase(it.toCharacter())) {
		        notAllUpper = true
		    }
		}

		return !notAllUpper
	}
	
}

```

With the above Category class now the String object has the above methods added to them (even in the code generation templates which are really convenient) as follows:

#### JavaModelGenerator.groovy using new String methods ####
```
    String generateName(dbName) {
    	String name
        if (dbName.contains("_")) {
        	name = dbName.split("_").collect{ String namePart -> namePart.capAndLower() }.join()
        } else {
        	if (dbName.isAllUppers()) {
        		name = dbName.capAndLower()
        	} else {
        		name = dbName.cap()
        	}
        }
    	name
    }

```

### AOP Groovy style ###
Groovy has support for providing AOP style support. We can write code to intercept method calls and debug them. Thus when we put the tool in **debug** mode, we want to log all method calls to all collaborators objects. To do this we define a logging closure as follows:

```
	Closure logClosure = {String methodName, methodArgs->
		def validMethod = delegate.metaClass.getMetaMethod(methodName, methodArgs)
		if (validMethod==null) {
			return delegate.metaClass.invokeMissingMethod(delegate, methodName, methodArgs)
		}  else if (validMethod.name=="println") {
			return  validMethod.invoke(delegate, methodArgs)
		}
		System.out.println "Running ${methodName}(${methodArgs})"
		def result = validMethod.invoke(delegate, methodArgs)
		System.out.println "Completed ${methodName}(${methodArgs})"
		result
	}

```


Then we weave in the closure in the **configureCollaborators** method as follows:

```
	private boolean configureCollaborators() {
		boolean invalidArgument = false
		
		if (debug) {
			DataBaseMetaDataReader.metaClass.invokeMethod = logClosure
			JavaModelGenerator.metaClass.invokeMethod = logClosure
			CodeGenerator.metaClass.invokeMethod = logClosure			
		}
       ...

```

### Complete CodeGenMain ###
Here is the complete **CodeGenMain** class for reference (I am looking for tips on how to make the code more Groovy):

```
package com.arcmind.codegen



/**
 * The main entry point into the command line version of the codegen application. 
 * Processes command line arguments and reads properties file settings. 
 * Configures the other classes and performs injection of collaborators.
 * @author richardhightower
 */
public class CodeGenMain{
	String url
	String userName
	String password
	String driver
	String tableNames
	String packageName
	String outputDir
	String appConfigDir
	String xmlFileName
	String propertiesFile
	String debug
	File appConfigDirFile
	List <String> actions = []
	Set <String> availableActions = ["reverse", "write", "read", "generate", "all", "help"]
	Properties configProperties
	JdbcUtils jdbcUtils
	DataBaseMetaDataReader reader
	JavaModelGenerator modelGen
	CodeGenerator codeGen
	XMLPersister persister
	List collaborators 
	
	Closure logClosure = {String methodName, methodArgs->
		def validMethod = delegate.metaClass.getMetaMethod(methodName, methodArgs)
		if (validMethod==null) {
			return delegate.metaClass.invokeMissingMethod(delegate, methodName, methodArgs)
		}  else if (validMethod.name=="println") {
			return  validMethod.invoke(delegate, methodArgs)
		}
		System.out.println "Running ${methodName}(${methodArgs})"
		def result = validMethod.invoke(delegate, methodArgs)
		System.out.println "Completed ${methodName}(${methodArgs})"
		result
	}
	
	
	
	public void run() {
		
		if (actions.contains("all")) {
			actions << "reverse" 
			actions << "generate"
			actions << "write"
			actions << "saveProps"
		}
		
		if (actions.contains("help") || actions.empty) {
			help()
		}
		
		if (actions.contains("saveProps")) {
			writeProperties()
		}
		
		/* Read the model from the database or the XML file. */
		if (actions.contains("reverse")) {
			if (debug) println "Reverse engineering the database tables"
			/* Process the database tables */
			reader.jdbcUtils = jdbcUtils        
			reader.processDB()			
			/* Convert the tables into JavaClasses. */
			modelGen.tables = reader.tables
			modelGen.convertTablesToJavaClasses()
		} else if (actions.contains("read")) {
			if (debug) println "Reading XML file"
			persister.read()
			reader.tables = persister.tables
			modelGen.classes = persister.classes
		}
		
		if (actions.contains("generate")) {
			if (debug) println "Generating Java classes"
			/* Output the generated classes. */
			codeGen.classes = modelGen.classes
			codeGen.writeClassFiles()			
		}
		
		if (actions.contains("write")) {
			if (debug) println "Writing XML file containing table and classes model"
			/* Write out xml file. */
			persister.classes = modelGen.classes
			persister.tables = reader.tables
			persister.persist()			
		}
		
		if (debug) println "Success!"
	}
	
	/** This is the main entry point for this program. */
	public static void main (String [] args) {
		use(StringCategory) { 
			CodeGenMain codeGenMain = new CodeGenMain()
			if (codeGenMain.processArgs(args)) {
				codeGenMain.run()
			}
		}
	}
	
	/* Parse the command line arguments. */
	public boolean processArgs (String [] args) {
		boolean invalidArgument = false
		
		String propertyName = null
		String value = null
		/* First pass make sure they passed valid arguments. */
		for (arg in args) {
			if (arg.contains("=")) {
				def command = arg.split("=")
				propertyName = command[0]
				value = command[1]
				if (!this.metaClass.hasProperty(this, propertyName)) {
					invalidArgument = true
					println "Invalid command line argument ${arg}"
				}
				
			}
		}
		
		/* If there are errors do not proceed. */
		if (invalidArgument) {
			println "Problems with command line arguments"
			return false;
		}
		
		/* Read properties from the command line and the properties file. */
		copyPropsFromArgs(args)
		appConfigDirFile = appConfigDir == null ? new File("./codegen") : new File(appConfigDir)
		readProperties()
		copyPropsFromArgs(args) //let the command line args overide the properties file
		
		invalidArgument = configureCollaborators()
		
		if (invalidArgument) {
			println "Unable to configure collaborators"
			return false
		}
		
		return true
		
	}
	
	private boolean configureCollaborators() {
		boolean invalidArgument = false
		
		if (debug) {
			DataBaseMetaDataReader.metaClass.invokeMethod = logClosure
			JavaModelGenerator.metaClass.invokeMethod = logClosure
			CodeGenerator.metaClass.invokeMethod = logClosure			
		}
		
		jdbcUtils = new JdbcUtils()
		codeGen = new CodeGenerator()
		reader = new DataBaseMetaDataReader()
		persister = new XMLPersister()
		modelGen = new JavaModelGenerator()
		
		collaborators = [jdbcUtils, reader, modelGen, codeGen, persister]
		
		/* Configure related classes. */
		jdbcUtils.url = url
		jdbcUtils.userName = userName
		jdbcUtils.driver = driver
		jdbcUtils.password = password == null ? "" : password
		
		/* Collaborator debug configuration. */		
		for (collaborator in collaborators) {
			collaborator.debug = debug == null ? false : Boolean.valueOf(debug)			
		}
		
		
		/* configure tablesNames to process. */ 
		try {
			modelGen.tableNames = tableNames != null ? new HashSet(Arrays.asList(tableNames.split(","))) : [] as HashSet;
		} catch (e) {
			e.printStackTrace()
			invalidArgument = true
			println "Unable to parse tableNames argument ${tableNames}."
		}
		
		
		modelGen.packageName = packageName		
		
		/* Configure output dirs by creating dirs if they do not exist. */
		codeGen.outputDir = outputDir == null ? new File("./target") : new File(outputDir)				
		if (!codeGen.outputDir.isDirectory()) {
			codeGen.outputDir.mkdirs()
		}
		persister.outputDir = appConfigDirFile
		if (!persister.outputDir.isDirectory()) {
			persister.outputDir.mkdirs()
		}
		persister.fileName = xmlFileName == null ? "codegen.xml" : xmlFileName
		
		return invalidArgument
		
	}
	
	private void readProperties() {
		configProperties = new Properties()
		File propFile = calculatePropFile()
		if (propFile.exists()) {
			if (debug) println "Found properties file ${propFile}, reading it into application arguments" 
			configProperties.load(new StringReader(propFile.text))
			for (key in configProperties.keySet()) {
				if (configProperties[key]!=null) {
					if (debug) println "overiding values not set: ${key}=${configProperties[key]}"
					this[key] = configProperties[key]
				}
			}
		} else {
			if (debug) println "Properties file not found, so writng new properties file based on arguments passed"
			writeProperties()
		}
	}
	
	private File calculatePropFile () {
		propertiesFile==null ? new File(appConfigDirFile,"config.properties") : new File(propertiesFile) 
	}
	
	private void writeProperties() {
		configProperties = new Properties()
		File propFile = calculatePropFile()
		for (key in this.properties.keySet()) {
			
			Object value = this[key]
			if (value!=null) {
				if (value instanceof String) {
					if (debug ) println "Setting values into properties file ${key} = ${value}"
					configProperties[key] = value
				}
			}
		}
		propFile.parentFile.mkdirs()
		
		propFile.newOutputStream().withStream{stream -> configProperties.store(stream, "prop file") }
	}
	
	public void copyPropsFromArgs(String[] args) {
		actions = []
		for (String arg : args) {
			if (arg.contains("=")) { 
				def command = arg.split("=")
				String propertyName = command[0]
				String value = command[1]
				this[propertyName]=value
				if (debug) println "Processing property ${propertyName}=${value}"
			} else {
				if (availableActions.contains(arg)) {
					if (debug) println "Found action ${arg}"
					actions << arg
				} else {
					println "Action not understood " + arg
				}
			}
		}
	}
	
	public void help () {
		
		
		println """
	codeGen is used to generate a JPA Java model from a relational database.
	Here are the command line arguments that codeGen takes.

	These are the one that can set parameters, i.e., password=foo.
	They take the form <parameter name>=<value>
	
	Parameters
	appConfigDir	Where to find the config.properties file (defaults to ./codegen)
	propertiesFile  The location of properties where parameters can be stored 
					(defaults to ./codegen/config.properties)

	JDBC/Database Parameters
	url				JDBC URL of database
	userName		User name for JDBC connection
	password		Password for connection
	driver			Driver name for JDBC connection
	tableNames		List of tables names to process
	
	Code Generataion Parameters
	packageName		Package name of clases that will be generated
	outputDir		The output directory of the classes
	xmlFileName		XML file that contains the reversed model
	debug			Puts the app in debug mode

	Command line parameters take precedence over what is stored in the config.properties file.
	All command line arguments can be stored in the properties file using the saveProps action.

	Actions denote what codeGen should do. They do not get passed a parameter.

	reverse			Processes the database into table and classes model
	write			Write the database and classes model into an xml file
	read			Read the XML table and classes model back into memory
	generate		Generate Java classes based on the table and classes model
	all 			Does all of the above
	help			Prints this message
	saveProps		Saves the properties passed to command line to the properties file

	Example 
	codegen.sh all url=jdbc:hsqldb:file:/db-codeGen/testdb userName=sa driver=org.hsqldb.jdbcDriver packageName=org.crank outputDir=./target appConfigDir=./codegen tableNames=Department,Employee
	"""		
	}
	
}

```

In 315 short lines of code we process the arguments, read/write configuration properties to the properties file, and run the code based on the actions and properties configured. We also document how to use the command line utility using Groovy here docs. I think the code is easy to read and grok. Let me know if you agree.

## JdbcUtils and DataBaseMetaDataReader ##
I thought about using Groovy's GSQL but did not know how to access the database metadata from GSQL. I figured there was a built-in way to use closures with **ResultSet** but could not find it. If someone can point to a more groovy way to get database metadata, let me know.  Colin Taylor posted an example using **GroovyResultSetProxy** which looked promising but I did not grok it yet. In the mean time I wrote my own support by defining and using my own closures as follows:

#### JdbcUtils.groovy ####
```
package com.arcmind.codegen
import java.sql.*;
/** Utility class for working with low-level JDBC connections and ResultSets. */
class JdbcUtils {
	String url
	String driver
	String userName
	String password
	Connection connection
	boolean debug
	
	def executeScript(String sql) {
		execute {Connection con ->
			Statement statement = con.createStatement();
			try {
				statement.execute(sql);
			} finally {
				statement?.close();
			}
		}
	}
	
	def execute(Closure callme) {
		Class.forName(driver)
		try {
			connection = DriverManager.getConnection (url,userName,password)
			callme(connection)
		} finally {
			connection?.close()
			connection=null
		}
	}
	
	def iterate(ResultSet resultSet, Closure callme) {
		try {
			while (resultSet.next()) {
				callme(resultSet)
			}
		} finally {
			resultSet?.close()
		}
		
	}
}

```

Notice that **JdbcUtils** uses closures in two instances in the **execute** method and the **iterate** method. The **execute** method creates a database connection and then passes the connection to the closure so that the client code does not have to worry about connection allocation or connection clean up. Similarly the **iterate** method takes a **ResultSet** and closure and calls the closure after each iteration of the ResultSet and then cleans up after the ResultSet afterwards. Note that **executeScript**, **execute** and **iterate** all close the underlying resource, namely, the **statement**, the **connection** and the **resultSet**. Notice that it uses the Groovy language feature to safely call a method on an object that may be null, i.e., `statement?.close()`,  `connection?.close()` and `resultSet?.close()`.

The **DataBaseMetaDataReader** class uses the **JdbcUtils**' **iterate** method and its **execute** method as follows:

#### DataBaseMetaDataReader.groovy ####
```
class DataBaseMetaDataReader {
    /** List of tables read from database. **/
    List <Table> tables = []
    /** Catalog for the database connection, default is null. */
    String catalog
    /** Schema for the database connection, default is null. */
    String schema
    /** Tables types that we will reverse, the default is just tables (no views) */
    String[] tableTypes = ["TABLE"]
    /** Utility class for managing database connection. */
    JdbcUtils jdbcUtils
    /* The current connection */
    private Connection connection
    ...

    /**
    * Connect to the database and read the database meta-data for a list of tables. 
    */
    def processTables() {
        if(debug) println "DataBaseMetaDataReader: Processing Tables "
        jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes),
            { ResultSet resultSet ->
                String tableName = resultSet.getString ("TABLE_NAME")
                if (debug) println "DataBaseMetaDataReader: processTables() tableName=${tableName}"
                tables << new Table(name:tableName)
            }
        )
        if(debug) println "DataBaseMetaDataReader: Done Processing Tables " + tables
    }

   ...
   ...
   /** Process the database pulling out tables and columns whilst creating 
     * JavaClasses and Bean properties. */
    def processDB(){
        jdbcUtils.execute {Connection c ->
            if (debug) println "Processing the database"
            connection = c
         	processTables()
         	processPrimaryKeys()
         	processColumns()
         	processKeys()
        }
    }

```

There is also a test called **DatabaseReverseXMLTest** that uses **JdbcUtils** as follows:

```
/**
 * 
 */
package com.arcmind.codegen

import junit.framework.TestCase


/**
 * @author richardhightower
 *
 */
public class DatabaseReverseXMLTest extends TestCase{

    static JdbcUtils jdbcUtils = new JdbcUtils(url:"jdbc:hsqldb:file:/db-codeGen/testdb",
            userName:"sa", password:"", driver:"org.hsqldb.jdbcDriver")
    DataBaseMetaDataReader reader = new DataBaseMetaDataReader()
    JavaModelGenerator modelGen = new JavaModelGenerator()
    CodeGenerator codeGen = new CodeGenerator()
    XMLPersister persister = new XMLPersister()

    static {
    	try {
	    	File dbDir = new File("/db-codeGen")
	    	dbDir.eachFile {File file ->
	    		file.delete()
	    	}
    	} catch(ex) {
    		
    	}
    	jdbcUtils.executeScript(sqlDDL)
    }
	
	static String sqlDDL = '''
DROP TABLE EMPLOYEE IF EXISTS;
DROP TABLE Department IF EXISTS;

CREATE TABLE Department (
  ID INTEGER IDENTITY,
  name VARCHAR(30),
  PRIMARY KEY (id)
);
CREATE TABLE EMPLOYEE (
  EMP_ID INTEGER IDENTITY,
  FK_DEPARTMENT_ID INTEGER NULL,  
  firstName VARCHAR(30),
  LAST_NAME VARCHAR(30),
  PRIMARY KEY (EMP_ID),
  FOREIGN KEY (FK_DEPARTMENT_ID) REFERENCES Department(ID)  
);
''';

	public void setUp () {
	}
	
	public void testTest() {
		use(StringCategory) { 
			reader.jdbcUtils = jdbcUtils
	        reader.processDB()
	        
	        modelGen.tables = reader.tables 
	        modelGen.packageName="org.crank.codegentest"
	        modelGen.convertTablesToJavaClasses()
	        
	        codeGen.classes = modelGen.classes
	        codeGen.writeClassFiles()
	 
	        persister.tables = reader.tables
	        persister.classes = modelGen.classes
	        persister.persist()
	        persister.read()
	        persister.fileName = "codeGenTest.xml"
	        persister.persist()
	        
	        assertEquals(new File(persister.outputDir, "codeGen.xml").text, new File(persister.outputDir, "codeGenTest.xml").text)
		}
	}
}

```

Notice that **DatabaseReverseXMLTest** uses Groovy here-docs to define SQL DDL for the test in the test class.

Here is the complete listing for **DataBaseMetaDataReader**, which is used to read database metadata and create a hierarchy of **Table**, **Column** and **Key** model objects as follows:

#### DataBaseMetaDataReader.groovy complete listing ####
```
package com.arcmind.codegen
import java.sql.*

/**
* Used to read database metadata and create a hierarchy of Table, Column and Key model objects. 
* The database metadata is read from connection.metaData.getTables, connection.metaData.getColumns, 
* connection.metaData.getPrimaryKeys, connection.metaData.getExportedKeys and 
* connection.metaData.getImportedKeys see Java API docs for JDBC Connection for more detail.
*/
class DataBaseMetaDataReader {
    /** List of tables read from database. **/
    List <Table> tables = []
    /** Catalog for the database connection, default is null. */
    String catalog
    /** Schema for the database connection, default is null. */
    String schema
    /** Tables types that we will reverse, the default is just tables (no views) */
    String[] tableTypes = ["TABLE"]
    /** Utility class for managing database connection. */
    JdbcUtils jdbcUtils
    /* The current connection */
    private Connection connection
    boolean debug



    /**
    * Connect to the database and read the database meta-data for a list of tables. 
    */
    def processTables() {
        if(debug) println "DataBaseMetaDataReader: Processing Tables "
        jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes),
            { ResultSet resultSet ->
                String tableName = resultSet.getString ("TABLE_NAME")
                if (debug) println "DataBaseMetaDataReader: processTables() tableName=${tableName}"
                tables << new Table(name:tableName)
            }
        )
        if(debug) println "DataBaseMetaDataReader: Done Processing Tables " + tables
    }

    /**
    *  Process list of columns from the list of tables. Add the columns to the table object.
    */
    def processColumns() {
        tables.each {Table table ->
            jdbcUtils.iterate connection.metaData.getColumns(catalog, schema, table.name, null),
               { ResultSet resultSet ->
                  Column column = new Column()
                  column.name = resultSet.getString ("COLUMN_NAME")
                  column.typeName = resultSet.getString ("TYPE_NAME")
                  column.type = resultSet.getInt ("DATA_TYPE")
                  column.nullable = resultSet.getString ("IS_NULLABLE") == "YES" ? true : false
                  if (table.primaryKeys.contains(column.name)) {
                            	 column.primaryKey = true
                  }
                  table.columns << column
                             column.table = table
                }
        }
    }
    
    /**
     * Find the primary keys for each table.
     */
    def processPrimaryKeys() {
        tables.each {Table table ->
           jdbcUtils.iterate connection.metaData.getPrimaryKeys(catalog, schema, table.name), 
             { ResultSet resultSet -> table.primaryKeys << resultSet.getString ("COLUMN_NAME")}
        }
    }
    
    
    /**
     * Process import keys and export keys
     */
    def processKeys() {
        tables.each {Table table ->
            processKeys(table, connection.metaData.&getExportedKeys, table.exportedKeys, false)
            processKeys(table, connection.metaData.&getImportedKeys, table.importedKeys, true)
        }
    }

	def processKeys(Table table, getKeys, List<Key> keyList, boolean imported) {
        jdbcUtils.iterate getKeys(catalog, schema, table.name),
                { ResultSet resultSet ->
        		  Key key = new Key()
        		  key.imported = imported
        		  key.foriegnKey.name = resultSet.getString ("FKCOLUMN_NAME")
        		  key.foriegnKey.table = tables.find{it.name==resultSet.getString ("FKTABLE_NAME")}
        		  key.primaryKey.name = resultSet.getString ("PKCOLUMN_NAME")
        		  key.primaryKey.table = tables.find{it.name==resultSet.getString ("PKTABLE_NAME")} 
        		  keyList << key
                }
	}



    /** Process the database pulling out tables and columns whilst creating 
     * JavaClasses and Bean properties. */
    def processDB(){
        jdbcUtils.execute {Connection c ->
            if (debug) println "Processing the database"
            connection = c
         	processTables()
         	processPrimaryKeys()
         	processColumns()
         	processKeys()
        }
    }

}
```


As you can see thedatabase metadata is read from connection.metaData.getTables, connection.metaData.getColumns, connection.metaData.getPrimaryKeys, connection.metaData.getExportedKeys and connection.metaData.getImportedKeys. The connection is retrieved by using **JdbcUtils**' **execute** method and the iteration through the metadata is done through **JdbcUtils**' **iterate** method.


## JavaModelGenerator ##
Takes the table hierarchy produced from **DataBaseMetaDataReader** or read from an XML file and generates a hierarchy of **JavaClass**, **JavaProperty** and **Relationship** model objects. It uses the functions from the **StringCategory** class described earlier. It also uses the Groovy switch statement that works with Strings. It uses collection methods added by the GDK and does some String processing with GDK methods as well.

Here is the JavaModelGenerator as follows:

#### JavaModelGenerator.groovy ####

```
import java.sql.*



/** Takes the table hierarchy produced from DataBaseMetaDataReader or read from an XML file 
 *  and generates a hierarchy of JavaClass, JavaProperty and Relationship model objects.
 *  @author richardhightower
 */
public class JavaModelGenerator{
    /** List of Java classes calculated from tables. */
    List <JavaClass> classes = []
    /** Table names to process. */
    Set <String> tableNames = []    
    /** Map of tables to Java classes */
    HashMap <String, JavaClass> tableToJavaClassMap = [:]
    /** Map of Java classes to tables */
    HashMap <String,Table> javaClassToTableMap = [:]
    /** Tables */
    List<Table> tables
    /** The name of the packageName that we will be using. */
    String packageName
    boolean debug

	
    /**
    *   Convert the columns to Java properties.
    */
    def convertColumnsToJavaProperties(JavaClass javaClass, Table table){
          javaClass.properties = table.columns.collect {Column column ->

               String propertyName = generateName(column.name).unCap()
               JavaProperty property = new JavaProperty(name:propertyName, column:column)
               javaClass.columnNameToPropertyMap[column.name]=property
               property.javaClass = convertColumnToJavaClass (column)
               property
          }
          JavaProperty idProperty = javaClass.properties.find{JavaProperty javaProperty -> javaProperty.column.primaryKey==true}
          javaClass.properties.remove(idProperty)
          javaClass.id = idProperty
    }

    /** Convert the column type to the equivalent Java class/type.
    */
    JavaClass convertColumnToJavaClass(Column column) {
            switch (column.type) {
                case [Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY]:
                    return new JavaClass(name:"byte[]", packageName:"java.lang")
                case [Types.VARCHAR, Types.CHAR, Types.LONGNVARCHAR, Types.CLOB, Types.LONGVARCHAR]:
                    return new JavaClass(name:"String", packageName:"java.lang")
                case [Types.FLOAT, Types.DOUBLE]:
                    return column.nullable ?
                        new JavaClass(name:"Double", packageName:"java.lang") :
                        new JavaClass(name:"double", primitive:true)
                case Types.REAL:
                    return column.nullable ?
                        new JavaClass(name:"Float", packageName:"java.lang") :
                        new JavaClass(name:"float", primitive:true)
                case Types.BIGINT:
                    return column.nullable || column.primaryKey ?
                        new JavaClass(name:"Long", packageName:"java.lang") :
                        new JavaClass(name:"long", primitive:true)
                case Types.SMALLINT:
                    return column.nullable ?
                        new JavaClass(name:"Short", packageName:"java.lang") :
                        new JavaClass(name:"short", primitive:true)
                case Types.TINYINT:
                    return column.nullable ?
                        new JavaClass(name:"Byte", packageName:"java.lang") :
                        new JavaClass(name:"byte", primitive:true)
                case Types.BIT:
                    return column.nullable ?
                        new JavaClass(name:"Boolean", packageName:"java.lang") :
                        new JavaClass(name:"boolean", primitive:true)
                case [Types.NUMERIC, Types.DECIMAL]:
                    return new JavaClass(name:"BigDecimal", packageName:"java.math")
                case [Types.DATE, Types.TIME, Types.TIMESTAMP]:
                    return new JavaClass(name:"Date", packageName:"java.util")
                case [Types.INTEGER] :
                    return column.nullable || column.primaryKey ?
                        new JavaClass(name:"Integer", packageName:"java.lang") :
                        new JavaClass(name:"int", primitive:true)
                default:
                    println "Unable to map type for column " + column
                    return new JavaClass(name:"Object", packageName:"java.lang")                    
            }
    }

    String generateName(dbName) {
    	String name
        if (dbName.contains("_")) {
        	name = dbName.split("_").collect{ String namePart -> namePart.capAndLower() }.join()
        } else {
        	if (dbName.isAllUppers()) {
        		name = dbName.capAndLower()
        	} else {
        		name = dbName.cap()
        	}
        }
    	name
    }
    
    /**
     *  Convert the Database tables into Java classes.
     */
     def convertTablesToJavaClasses(){
         tables.each{Table table ->
            String className = generateName(table.name)
            if (!tableNames.empty && !tableNames.contains(table.name)) {
            	return;
            }
            JavaClass javaClass = new JavaClass(name:className, packageName:packageName, table:table)
            classes << javaClass
            javaClassToTableMap[javaClass.name]=table
            tableToJavaClassMap[table.name]=javaClass
            convertColumnsToJavaProperties(javaClass, table)
            
         }
         classes.each{JavaClass javaClass ->
         	convertKeysToRelationships(javaClass)
         }
     }
     
     def convertKeysToRelationships(JavaClass javaClass) {
     	/* Note: Exported keys are fkeys in other tables that are pointing to this table. */
     	javaClass.table.exportedKeys.each { Key key ->
     		JavaClass relatedClass = this.tableToJavaClassMap[key.foriegnKey.table.name]
     		if (relatedClass == null) {
                return
            }
     		String relationshipName = relatedClass.name.unCap()
            relationshipName = relationshipName.endsWith('s') ? relationshipName + "es" : relationshipName + "s"
     		javaClass.relationships << 
     			new Relationship(name:relationshipName, relatedClass:relatedClass, key:key, type:RelationshipType.ONE_TO_MANY)

     	}
     	/* Note: Imported keys are the keys that correlate to columns in this Classes table. 
     	 * Imported keys list the foriegn keys in this table
     	 * */
     	 javaClass.table.importedKeys.each { Key key ->
 			JavaClass relatedClass = this.tableToJavaClassMap[key.primaryKey.table.name]
     		if (relatedClass == null) {
                return
            }
 			String relationshipName = key.foriegnKey.name

            relationshipName = relationshipName - "_ID"
            relationshipName = relationshipName - "FK_"
            relationshipName = generateName(relationshipName).unCap()
            if (relationshipName.endsWith("Id")) {
                relationshipName = relationshipName - "Id"
            }
 			javaClass.relationships << 
 				new Relationship(name:relationshipName, relatedClass:relatedClass, key:key, type:RelationshipType.MANY_TO_ONE)
 			javaClass.properties.remove(javaClass.columnNameToPropertyMap[key.foriegnKey.name])
     	}
     }

	
	
}

```



## CodeGenerator ##
The **CodeGenerator** generates .java files from **JavaClass** models objects. The **CodeGenerator** uses **GString** and **GPath** support to use a here-doc as a code generation template. With **SimpleTemplateEngine** you can do similar scripting that you could do in JSP as demonstrated in the **textTemplate** property below:

Here is the **CodeGenerator** as follows:

#### CodeGenerator.groovy ####

```

import groovy.text.SimpleTemplateEngine

/**
 * Generates .java files from JavaClass models objects. 
 */
class CodeGenerator {
    List<JavaClass> classes
    /** The target output dir. Defaults to ./target */
    File outputDir = new File("./target")
    boolean debug
    String textTemplate = """
<% import com.arcmind.codegen.RelationshipType; %>

package ${bean.packageName};

import java.io.Serializable;
import javax.persistence.Entity;

<% imports.each { imp-> %>import ${imp};
<% } %>

@Entity <% if (!bean.namesMatch) { %> @Table('${bean.table.name}') <% } %>
public class ${bean.name} implements Serializable {
    /** ID */
    @Id @Column ('${bean.id.column.name.toUpperCase()}') @GeneratedValue( strategy = GenerationType.AUTO )
    private ${bean.id.javaClass.name} id;

    /* ------- Relationships ------ */
   <% bean.relationships.each {r -> 
        if (r.type == RelationshipType.ONE_TO_MANY) {
   %>
    @OneToMany(cascade = CascadeType.ALL) @JoinColumn('${r.key.foriegnKey.name}')
    private Set <${r.relatedClass.name}> ${r.name};
   <% } else if (r.type == RelationshipType.MANY_TO_ONE) { %>
    @ManyToOne (cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    private ${r.relatedClass.name} ${r.name};
   <%}} %>

    /** Properties's fields */
   <% bean.properties.each { property-> %>
    <% if (!property.namesMatch) {%>@Column('${property.column.name.toUpperCase()}')<% } %>
    private ${property.javaClass.name} ${property.name.unCap()};
   <% } %>

    public ${bean.name} () {

    }

    public void setId(${bean.id.javaClass.name} id) {
    	this.id = id;
    }
    public ${bean.id.javaClass.name} getId() {
    	return id;
    }

   <% bean.relationships.each { r->
        if (r.type == RelationshipType.ONE_TO_MANY) { %>

    public Set<${r.relatedClass.name}> get${r.name.cap()}() {
        return this.${r.name.unCap()};
    }

    public void set${r.name.cap()}(Set<${r.relatedClass.name}> ${r.name.unCap()}} ) {
        this.${r.name.unCap()} = ${r.name.unCap()};
    }

    <% } else if (r.type == RelationshipType.MANY_TO_ONE) { %>

    public ${r.relatedClass.name} get${r.name.cap()}() {
        return this.${r.name.unCap()};
    }

    public void set${r.name.cap()}(${r.relatedClass.name} ${r.name.unCap()}} ) {
        this.${r.name.unCap()} = ${r.name.unCap()};
    }

    <% }} %>


   <% bean.properties.each { property-> %>
    public ${property.javaClass.name} get${property.name.cap()}() {
        return this.${property.name.unCap()};
    }

    public void set${property.name.cap()}(${property.javaClass.name} ${property.name.unCap()}} ) {
        this.${property.name.unCap()} = ${property.name.unCap()};
    }
   <% } %>
}
"""

    boolean needsColumnImport(JavaClass bean) {
         for (JavaProperty property : bean.properties) {
             //If they don't match, then you need a column
             if (!property.namesMatch || !property.column.nullable) {
                 return true
             }
         }
         return false
    }

    Set<String> calculateImportsFromBean(JavaClass bean) {
    	
    	
    	 
         List<String> imports = bean.properties.collect { JavaProperty property ->
              if (!property.javaClass.primitive && !property.javaClass.packageName.startsWith("java.lang")) {
                return "${property.javaClass.packageName}.${property.javaClass.name}"
              } else {
                  return null;
              }
         }
         
         imports.add("javax.persistence.GeneratedValue")
 		 imports.add("javax.persistence.GenerationType")
 		 imports.add("javax.persistence.Id")
         
         bean.relationships.each {Relationship relationship ->
         	if (relationship.type == RelationshipType.ONE_TO_MANY) {
         		imports.add("javax.persistence.OneToMany") 
         		imports.add("javax.persistence.JoinColumn")
         		imports.add("javax.persistence.CascadeType")
         	} else if (relationship.type == RelationshipType.MANY_TO_ONE) {
         		imports.add("javax.persistence.ManyToOne")
         		imports.add("javax.persistence.CascadeType")
         	}
         }
         
         Set<String> impSet = new HashSet<String> (imports)
         impSet.remove (null)
         if (needsColumnImport(bean)) {
            impSet.add("javax.persistence.Column")
         }
         impSet
    }

    def writeClassFiles() {
        for (JavaClass bean in classes) {
            def binding = ["bean":bean, "imports":calculateImportsFromBean(bean)]
            def engine = new SimpleTemplateEngine()
            def template = engine.createTemplate(textTemplate).make(binding)
            File outputFileDir = new File(outputDir, bean.packageName.replace('.','/'))
            outputFileDir.mkdirs()
            File javaFile = new File (outputFileDir, bean.name + ".java")
            javaFile.newWriter().withWriter{BufferedWriter writer->
            	writer.write(template.toString())
            }
        }
}
```

Notice that the **writeClassFiles** method uses the **SimpleTemplateEngine** to process the template against the JavaClass model objects to generate the Java classes. The writeClasses could have written the files out like this:

```
            FileWriter fw = null
            try {
                fw = new FileWriter(javaFile)
                fw.write(template.toString())
            } finally {
                fw.close()
            }

```

Since we are using Groovy, we can output the file with 1/3 of the lines of code as follows:

```
            javaFile.newWriter().withWriter{BufferedWriter writer->
            	writer.write(template.toString())
            }

```

We could even shorten further as follows:
```
            javaFile.newWriter().withWriter{ it.write(template.toString())}

```

I prefer the stronger typed version so don't use the **it** default argument much. Perhaps I will grow out of this for simple cases. Hmmm....

## XMLPersister ##

The **XMLPersister** reads and writes editable XML files. This allows the end user to override what we guessed in the generation process.

The **XMLPersister** uses the **MarkupBuilder** to write XML to a file. To read the XML file it uses the **XmlSlurper** to read the XML file back into the model objects.


#### XMLPersister.groovy ####
```

import groovy.xml.MarkupBuilder


/** Reads and writes editable XML files. This allows the end user to override what we guessed in 
 *  the generation process.
 *  @author richardhightower
 */
public class XMLPersister{

	List <Table> tables
	List <JavaClass> classes
	File outputDir = new File("./target")
	String fileName = "codegen.xml"
	boolean debug
	
	/* Persist our classes and tables (and their hierarchy) to the XML file. */
	void persist() {
		List <Table> _tables = tables
		List <JavaClass> _classes = classes 
		BufferedWriter bWriter = new File (outputDir, fileName).newWriter()
		bWriter.withWriter {writer ->
		def xmlDocument = new MarkupBuilder(writer)
		xmlDocument.codeGen(){

				classes {
					_classes.each {JavaClass c -> c.with {
						if (debug) println "Writing class ${name}"
						'class'(name: name, packageName: packageName, tableName: table.name) {
							/* Write the id for the Java class. */
							'id'(name: id.name, column: id.column.name, className: id.javaClass.name, 
									packageName: id.javaClass.packageName, primitive: id.javaClass.primitive)
							/* Write out the properties for the Java class. */
							properties { c.properties.each {JavaProperty p -> p.with {
								property (name: p.name, column: column.name, className: javaClass.name, 
										packageName: javaClass.packageName, primitive: javaClass.primitive)
							}}}//properties
							/* Write out the relationships for the Java class. */
							relationships { c.relationships.each {Relationship r -> r.with {
								relationship (name: r.name, type: type.toString()) {
									'relatedClass' (name: relatedClass.name, packageName: relatedClass.packageName)
									key?.with {
										'key'(primaryKeyColumn: primaryKey.name,
										 primaryKeyTable: primaryKey.table.name,
										 foriegnKeyColumn: foriegnKey.name,
										 foriegnKeyTable: foriegnKey.table.name,
										 imported: imported)
									}//key.with
								}//relationship
							}}}//relationships
						}//'class'
					}}//classes.each
				}//classes

                tables {
					_tables.each {Table t ->
					  if (debug) println "Writing table ${t.name}"
					  table(name: t.name) {
						primaryKeys {
							t.primaryKeys.each{String pk ->
								primaryKey(name:pk)
							}//primaryKeys.each
						}//primaryKeys
						columns { t.columns.each { Column c -> c.with {
							column (name: name, type: type, typeName: typeName, nullable: nullable, primaryKey: primaryKey) 
						}/*columns.each*/ }}
						exportedKeys {t.exportedKeys.each { Key k -> k.with {
						key(primaryKeyColumn: primaryKey.name, 
							primaryKeyTable: primaryKey.table.name,
							foriegnKeyColumn: foriegnKey.name,
							foriegnKeyTable: foriegnKey.table.name)
						}}}//exportedKeys
						importedKeys {t.importedKeys.each { Key k -> k.with {
						key(primaryKeyColumn: primaryKey.name, 
							primaryKeyTable: primaryKey.table.name,
							foriegnKeyColumn: foriegnKey.name,
							foriegnKeyTable: foriegnKey.table.name)
						}}}//importedKeys
					  }//table	
					}//tables.each
				}//tables
        }//codeGen
		}//withWriter
	}//persist
	
	/* Read our model from XML. */
	void read() {
		def codeGen = new XmlSlurper().parse(new File (outputDir, fileName))
		Map<String,Table> tableMap = [:]
        readTables(tableMap, codeGen)
        readClasses(tableMap, codeGen)
	}	
    void readTables(Map<String,Table> tableMap, codeGen) {
    	if (debug) println "Reading tables"
		tables = []
       
		codeGen.tables.table.each {tbl ->
			Table table = new Table(name: tbl.@name)
			tables << table
            tableMap[tbl.@name.toString()] = table
		}//tables        
		codeGen.tables.table.each {tbl ->

            Table table =  tableMap[tbl.@name.toString()]
            assert table!=null
            tbl.primaryKeys.primaryKey.each {
				table.primaryKeys << it.@name.toString()
			}//primaryKeys
			tbl.columns.column.each { c ->
				table.columns << new Column(table: table, name: c.@name, 
						type: Integer.valueOf(c.@type.toString()), typeName: c.@typeName,
						nullable: Boolean.valueOf(c.@nullable.toString()), 
						primaryKey: Boolean.valueOf(c.@primaryKey.toString()))
			}//columns
			tbl.exportedKeys.key.each {k ->
				Key key = new Key(imported:false)
				key.primaryKey = new Column(name: k.@primaryKeyColumn, table: tableMap[k.@primaryKeyTable.toString()])
				key.foriegnKey = new Column(name: k.@foriegnKeyColumn, table: tableMap[k.@foriegnKeyTable.toString()])
				table.exportedKeys << key
			}//exportedKeys
			tbl.importedKeys.key.each {k ->
				Key key = new Key(imported:true)
				key.primaryKey = new Column(name: k.@primaryKeyColumn, table: tableMap[k.@primaryKeyTable.toString()])
				key.foriegnKey = new Column(name: k.@foriegnKeyColumn, table: tableMap[k.@foriegnKeyTable.toString()])
				table.importedKeys << key
			}//importedKeys
        }

    }
	

	
	/** Read the classes from the XML document. */
	void readClasses(Map<String,Table> tableMap, codeGen) {
		if (debug) println "Reading classes"
		classes = []
		Map<String,JavaClass> classMap = [:]
		/* Read the classes. */
		codeGen.classes.'class'.each {cls ->
			JavaClass clz = new JavaClass(name: cls.@name, packageName: cls.@packageName)
			classMap[clz.name]=clz
			classes << clz
			clz.table = tableMap[cls.@tableName.toString()]
			/* Read the id. */
			clz.id = new JavaProperty(name: cls.id.@name)
			clz.id.javaClass = new JavaClass(name: cls.id.@className, packageName: cls.id.@packageName, 
					primitive: Boolean.valueOf(cls.id.@primitive.toString()))
			clz.id.column = clz.table.columns.find{it.name==cls.id.@column.toString()}
			/* Read the properties. */
			cls.properties.property.each { prop ->
				JavaProperty jp = new JavaProperty(name: prop.@name)
				JavaClass jc = new JavaClass(name: prop.@className, packageName: prop.@packageName, 
						primitive: Boolean.valueOf(prop.@primitive.toString()))
				jp.javaClass = jc
				jp.column = clz.table.columns.find{it.name==prop.@column.toString()}
				clz.properties << jp
			}
		}
		
		if (debug) println "Reading relationships from classes"
		/** Read the relationships from the XML document. */
		codeGen.classes.'class'.each {cls ->
		cls.relationships.relationship.each { rel ->
			/* Create the relationship. */
			Relationship relationship = new Relationship(name: rel.@name, type: Enum.valueOf(RelationshipType.class, 
											rel.@type.toString()))
			/* Lookup the actual class object based on the class element. */
			JavaClass clz = classMap[cls.@name.toString()]
			clz.relationships << relationship
			
			/* Create the related class based on the relationhip element. */
			relationship.relatedClass = new JavaClass(name: rel.relatedClass.@name, 
					packageName: rel.relatedClass.@packageName)
			
			/* Pull out the information that we need to look up the correct tables and columns. */
			String primaryKeyTable = rel.key.@primaryKeyTable.toString()
			String primaryKeyColumn = rel.key.@primaryKeyColumn.toString()
			boolean imported = Boolean.valueOf(rel.key.@imported.toString())
			String foriegnKeyTable = rel.key.@foriegnKeyTable.toString()
			String foriegnKeyCoumn = rel.key.@foriegnKeyColumn.toString()
			
			/* Lookup the keys based on looking up the table and the right key from the right source. */
			if (!imported) {
				relationship.key = tableMap[primaryKeyTable].exportedKeys.find{it.primaryKey.name==primaryKeyColumn}
			} else if (imported) {
				relationship.key = tableMap[foriegnKeyTable].importedKeys.find{it.foriegnKey.name==foriegnKeyCoumn}
			}
		}//relationships
		}//classes
	}//readClasses
}
```

In just 192 lines of code, we can read and write our entire object model. Here is an example xml file that the above generates and reads into our object model as follows:

```
<codeGen>
  <classes>
    <class name='Department' packageName='org.crank.codegentest' tableName='DEPARTMENT'>
      <id name='id' column='ID' className='Integer' packageName='java.lang' primitive='false' />
      <properties>
        <property name='name' column='NAME' className='String' packageName='java.lang' primitive='false' />
      </properties>
      <relationships>
        <relationship name='employees' type='ONE_TO_MANY'>
          <relatedClass name='Employee' packageName='org.crank.codegentest' />
          <key primaryKeyColumn='ID' primaryKeyTable='DEPARTMENT' foriegnKeyColumn='FK_DEPARTMENT_ID' foriegnKeyTable='EMPLOYEE' imported='false' />
        </relationship>
      </relationships>
    </class>
    <class name='Employee' packageName='org.crank.codegentest' tableName='EMPLOYEE'>
      <id name='empId' column='EMP_ID' className='Integer' packageName='java.lang' primitive='false' />
      <properties>
        <property name='firstname' column='FIRSTNAME' className='String' packageName='java.lang' primitive='false' />
        <property name='lastName' column='LAST_NAME' className='String' packageName='java.lang' primitive='false' />
      </properties>
      <relationships>
        <relationship name='department' type='MANY_TO_ONE'>
          <relatedClass name='Department' packageName='org.crank.codegentest' />
          <key primaryKeyColumn='ID' primaryKeyTable='DEPARTMENT' foriegnKeyColumn='FK_DEPARTMENT_ID' foriegnKeyTable='EMPLOYEE' imported='true' />
        </relationship>
      </relationships>
    </class>
  </classes>
  <tables>
    <table name='DEPARTMENT'>
      <primaryKeys>
        <primaryKey name='ID' />
      </primaryKeys>
      <columns>
        <column name='ID' type='4' typeName='INTEGER' nullable='false' primaryKey='true' />
        <column name='NAME' type='12' typeName='VARCHAR' nullable='true' primaryKey='false' />
      </columns>
      <exportedKeys>
        <key primaryKeyColumn='ID' primaryKeyTable='DEPARTMENT' foriegnKeyColumn='FK_DEPARTMENT_ID' foriegnKeyTable='EMPLOYEE' />
      </exportedKeys>
      <importedKeys />
    </table>
    <table name='EMPLOYEE'>
      <primaryKeys>
        <primaryKey name='EMP_ID' />
      </primaryKeys>
      <columns>
        <column name='EMP_ID' type='4' typeName='INTEGER' nullable='false' primaryKey='true' />
        <column name='FK_DEPARTMENT_ID' type='4' typeName='INTEGER' nullable='true' primaryKey='false' />
        <column name='FIRSTNAME' type='12' typeName='VARCHAR' nullable='true' primaryKey='false' />
        <column name='LAST_NAME' type='12' typeName='VARCHAR' nullable='true' primaryKey='false' />
      </columns>
      <exportedKeys />
      <importedKeys>
        <key primaryKeyColumn='ID' primaryKeyTable='DEPARTMENT' foriegnKeyColumn='FK_DEPARTMENT_ID' foriegnKeyTable='EMPLOYEE' />
      </importedKeys>
    </table>
  </tables>
</codeGen>
```

### Swing front end ###
I initially decide not to use any Swing builders, but then had a change in heart once I saw that all of the examples used the builders.

Here is a first shot at a GUI using the Groovy SwingBuilder:

#### GeneratorSwingApp.groovy ####
```
package com.arcmind.codegen

import javax.swing.*
import groovy.swing.SwingBuilder
import java.awt.BorderLayout


/**
 * @author richardhightower
 *
 */
public class GeneratorSwingApp{

	JFrame mainFrame
	CodeGenMain main = new CodeGenMain()
	SwingBuilder swing = new SwingBuilder()
	JLabel status
	Action viewConsole
	Action hideConsole
	JTextArea console
	List<Action> viewActions
	List<Action> mainActions
	List<Action> fileActions
	
	boolean debug = true
	
	def println(String message) {
		console.append(message + "\n")
	}
	
	Closure printlnClosure = { String message ->
		console.append(message + "\n")
	}

	public void exit() {
		System.exit(0)
	}
	
	public void showConsole() {
		println "Show console"
		hideConsole.enabled=true
		viewConsole.enabled=false
		console.show()
		console.setSize(500, 500)
		
	}
	
	public void closeConsole() {
		println "Close console"
		viewConsole.enabled=true
		hideConsole.enabled=false
		console.hide()
		mainFrame.invalidate()
		mainFrame.repaint()
	}
		
	public GeneratorSwingApp() {
		main.readProperties()
		main.configureCollaborators()

		mainFrame=
			  swing.frame(title:'CodeGen Code Generator', size:[300,300], defaultCloseOperation:JFrame.EXIT_ON_CLOSE,  show:true) {
			  fileActions = actions() {
				  action(name: "Exit", mnemonic: 'X', closure: { exit() })
			  }
			  mainActions = actions() {
				   action(name: "Reverse DB", mnemonic: 'R', closure: {use(StringCategory){main.reverseDB()}})
				   action(name: "Generate Java", mnemonic: 'G', closure: {use(StringCategory){main.generateJavaClasses() }})
			       action(name: "Write XML", mnemonic: 'W', closure: {use(StringCategory) {main.writeXML() }})
			       action(name: "Read XML", mnemonic: 'C', closure: {use(StringCategory) {main.readXML() }})
			       action(name: "Save Properties", mnemonic: 'S', closure: {use(StringCategory){main.writeProperties()}})
			       action(name: "Modify Properties", mnemonic: 'o', closure: {  })				  
			  }
			  viewActions = actions () {
			       viewConsole = action(name: "View Console", mnemonic: 'V', closure: { showConsole() })
			       hideConsole = action(name: "Hide Console", mnemonic: 'H', closure: { closeConsole() })
			  }
			  menuBar() {
				    menu(text: "File", mnemonic: 'F') {
				    	fileActions.each {menuItem(it)}				    	
				    }
				    menu (text: "Main", mnemonic: 'M') {
				    	mainActions.each {menuItem(it)}				    	
				    }
				    menu (text: "Window") {
				    	viewActions.each {menuItem(it)}
				    }
			  }

			  
			  status = label(text:"Welcome to CodeGen",
		                      constraints: BorderLayout.NORTH)
		      console = textArea(constraints: BorderLayout.CENTER, size:[500, 500])
		}
		
		hideConsole.enabled=false
		console.hide()
		console.append("Welcome to CodeGen... Let's get crack-ah-lackin!")
		
	}
	
	public static void main (String [] args) {
		GeneratorSwingApp app = new GeneratorSwingApp()
	}
	
	
}

```
#### Took another swing at the Groovy App ####
I added two tree models, two trees, some scroll panels, a split pane, a tool bar, etc.
It is starting to look like a real app. Oh Joy!

The problem I have with GUIs is they are never done. It always seems like you could tweak them to make them look better.

It looks a lot more done, although there is still a lot of work to do...

Here are the two tree models and helper classes (look more like Java than Groovy)
#### JavaClassTreeModel and friends ####
```
package com.arcmind.codegen

class ListHolder {
	List list
	String name
	public String toString() {
		name
	}
}

/**
 * 
 */
package com.arcmind.codegen
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath


class JavaClassHolder {
	JavaClass javaClass
	ListHolder properties
	ListHolder relationships
	List<ListHolder> lists

	void setJavaClass (JavaClass javaClass) {
		this.javaClass = javaClass
		properties = new ListHolder(name: "Properties", list: javaClass.properties)
		relationships = new ListHolder(name: "Relationships", list: javaClass.relationships)
		lists = [properties, relationships]
	}
	public String toString() {
		javaClass.name
	}
}



/**
 * @author richardhightower
 *
 */
public class JavaClassTreeModel implements TreeModel{
	List<JavaClassHolder> javaClassHolders = []
	
    private Vector<TreeModelListener> treeModelListeners =
    new Vector<TreeModelListener>();

    /**
     * The only event raised by this model is TreeStructureChanged with the
     * root as path, i.e. the whole tree has changed.
     */
    protected void setClasses(List<JavaClass> classes) {
    	javaClassHolders.clear()
    	for (JavaClass javaClass : classes) {
    		JavaClassHolder holder = new JavaClassHolder(javaClass:javaClass)
    		javaClassHolders << holder
    	}
        TreeModelEvent e = new TreeModelEvent(this, [this] as Object[]);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

	
    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     */
    public Object getChild(Object parent, int index) {
    	if (parent instanceof JavaClassTreeModel) {
    		return javaClassHolders[index]
    	} else if (parent instanceof JavaClassHolder) {
    		JavaClassHolder javaClass = (JavaClassHolder)parent;
    		return javaClass.lists[index]
    	} else if (parent instanceof ListHolder) {
    		ListHolder listHolder = (ListHolder) parent
    		return listHolder.list[index]
    	}
    }
    
    /**
     * Returns the number of children of parent.
     */
    public int getChildCount(Object parent) {
    	if (parent instanceof JavaClassTreeModel) {
    		return javaClassHolders==null ? 0 : javaClassHolders.size()
    	} else if (parent instanceof JavaClassHolder) {
    		JavaClassHolder javaClass = (JavaClassHolder) parent;
    		return javaClass.lists.size()
    	} else if (parent instanceof ListHolder) {
    		ListHolder listHolder = (ListHolder) parent
    		return listHolder.list == null ? 0 : listHolder.list.size()
    	} 
    }

    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild(Object parent, Object child) {
    	if (parent instanceof JavaClassTreeModel) {
    		return javaClassHolders.indexOf(child)
    	} else if (parent instanceof JavaClassHolder) {
    		JavaClassHolder javaClass = (JavaClassHolder) parent;
    		return javaClass.lists.indexOf(child)
    	}  else if (parent instanceof ListHolder) {
    		ListHolder listHolder = (ListHolder) parent
    		return listHolder.list.indexOf(child)
    	}
    }
    
    /**
     * Returns the root of the tree.
     */
    public Object getRoot() {
        return this;
    }
    
    /**
     * Returns true if node is a leaf.
     */
    public boolean isLeaf(Object node) {
        node instanceof JavaProperty || node instanceof Relationship
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }
    
    /**
     * Messaged when the user has altered the value for the item
     * identified by path to newValue.  Not used by this model.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("*** valueForPathChanged : "
            + path + " --> " + newValue);
    }


    public String toString() {
    	return "classes";
    }
}

```

#### DBTableTreeModel and friends ####
```
/**
 * 
 */
package com.arcmind.codegen
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath


class TableHolder {
	Table table
	ListHolder primaryKeys
	ListHolder columns
	List<ListHolder> lists

	void setTable (Table table) {
		this.table = table
		primaryKeys = new ListHolder(name: "Primary Keys", list: table.primaryKeys)
		columns = new ListHolder(name: "Columns", list: table.columns)
		lists = [primaryKeys, columns]
	}
	public String toString() {
		table.name
	}
}



/**
 * @author richardhightower
 *
 */
public class DBTableTreeModel implements TreeModel{
	List<TableHolder> tableHolders = []
	
    private Vector<TreeModelListener> treeModelListeners =
    new Vector<TreeModelListener>();

    /**
     * The only event raised by this model is TreeStructureChanged with the
     * root as path, i.e. the whole tree has changed.
     */
    protected void setTables(List<Table> tables) {
    	tableHolders.clear()
    	for (Table table : tables) {
    		TableHolder holder = new TableHolder(table:table)
    		tableHolders << holder
    	}
        int len = treeModelListeners.size();
        TreeModelEvent e = new TreeModelEvent(this, [this] as Object[]);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

	
    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     */
    public Object getChild(Object parent, int index) {
    	if (parent instanceof DBTableTreeModel) {
    		return tableHolders[index]
    	} else if (parent instanceof TableHolder) {
    		TableHolder table = (TableHolder)parent;
    		return table.lists[index]
    	} else if (parent instanceof ListHolder) {
    		ListHolder listHolder = (ListHolder) parent
    		return listHolder.list[index]
    	}
    }
    
    /**
     * Returns the number of children of parent.
     */
    public int getChildCount(Object parent) {
    	if (parent instanceof DBTableTreeModel) {
    		return tableHolders==null ? 0 : tableHolders.size()
    	} else if (parent instanceof TableHolder) {
    		TableHolder table = (TableHolder) parent;
    		return table.lists.size()
    	} else if (parent instanceof ListHolder) {
    		ListHolder listHolder = (ListHolder) parent
    		return listHolder.list == null ? 0 : listHolder.list.size()
    	} 
    }

    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild(Object parent, Object child) {
    	if (parent instanceof DBTableTreeModel) {
    		return tableHolders.indexOf(child)
    	} else if (parent instanceof TableHolder) {
    		TableHolder table = (TableHolder) parent;
    		return table.lists.indexOf(child)
    	}  else if (parent instanceof ListHolder) {
    		ListHolder listHolder = (ListHolder) parent
    		return listHolder.list.indexOf(child)
    	}
    }
    
    /**
     * Returns the root of the tree.
     */
    public Object getRoot() {
        return this;
    }
    
    /**
     * Returns true if node is a leaf.
     */
    public boolean isLeaf(Object node) {
        node instanceof Column || node instanceof String
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }
    
    /**
     * Messaged when the user has altered the value for the item
     * identified by path to newValue.  Not used by this model.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("*** valueForPathChanged : "
            + path + " --> " + newValue);
    }


    public String toString() {
    	return "tables";
    }
}

```

The **TableHolder**, **JavaClassHolder** and **ListHolder** are used to adapt the model hierarchy to more of a true tree hierarchy needed by JTree.

Here is the updated GeneratorSwingApp with Scrollbars, ScrollPanes, ToolBars, Actions and tons of other fun.

#### GeneratorSwingApp.groovy ####

```
/**
 * 
 */
package com.arcmind.codegen

import javax.swing.*
import groovy.swing.SwingBuilder
import java.awt.BorderLayout


/**
 * @author richardhightower
 *
 */
public class GeneratorSwingApp{

	JFrame mainFrame
	CodeGenMain main 
	SwingBuilder swing = new SwingBuilder()
	JLabel status
	Action viewConsole
	Action hideConsole
	JTextArea console
	List<Action> viewActions
	List<Action> mainActions
	List<Action> fileActions
	JTabbedPane treeTabPane
	JTabbedPane mainTabPane
	JScrollPane consolePane
	DBTableTreeModel tableTreeModel = new DBTableTreeModel()
	JavaClassTreeModel classTreeModel = new JavaClassTreeModel()
	
	boolean debug = true
	
	def println(String message) {
		console.append(message + "\n")
	}
	
	Closure printlnClosure = { String message ->
		console.append(message + "\n")
	}

	public void exit() {
		System.exit(0)
	}
	
	public void showConsole() {
		println "Show console"
		hideConsole.enabled=true
		viewConsole.enabled=false
		mainTabPane.addTab("Console", consolePane)
	}
	
	public void closeConsole() {
		println "Close console"
		viewConsole.enabled=true
		hideConsole.enabled=false
		mainTabPane.remove(consolePane)
	}
	
	public void reverseDB() {
		use(StringCategory){
			main.reverseDB()
		}
		tableTreeModel.setTables(main.reader.tables)
		classTreeModel.setClasses(main.modelGen.classes)
	}
		
	public GeneratorSwingApp() {


		mainFrame=
        swing.frame(title:'CodeGen Code Generator', size:[1000,1000], defaultCloseOperation:JFrame.EXIT_ON_CLOSE,  show:true) {
            fileActions = actions() {
                action(name: "Exit", mnemonic: 'X', closure: { exit() })
            }
            mainActions = actions() {
                action(name: "Reverse DB", mnemonic: 'R', closure: { reverseDB() })
                action(name: "Generate Java", mnemonic: 'G', closure: {use(StringCategory){main.generateJavaClasses() }})
                action(name: "Write XML", mnemonic: 'W', closure: {use(StringCategory) {main.writeXML() }})
                action(name: "Read XML", mnemonic: 'C', closure: {use(StringCategory) {main.readXML() }})
                action(name: "Save Properties", mnemonic: 'S', closure: {use(StringCategory){main.writeProperties()}})
                action(name: "Modify Properties", mnemonic: 'o', closure: {  })
            }
            viewActions = actions () {
                viewConsole = action(name: "View Console", mnemonic: 'V', closure: { showConsole() })
                hideConsole = action(name: "Hide Console", mnemonic: 'H', closure: { closeConsole() })
            }
            menuBar() {
                menu(text: "File", mnemonic: 'F') {
                    fileActions.each {menuItem(it)}
                }
                menu (text: "Main", mnemonic: 'M') {
                    mainActions.each {menuItem(it)}
                }
                menu (text: "Window") {
                    viewActions.each {menuItem(it)}
                }
            }
			  
            toolBar (constraints: BorderLayout.PAGE_START) {
                fileActions.each {button(it)}
                mainActions.each {button(it)}
                viewActions.each {button(it)}
            }
			  
            status = label(text:"Welcome to CodeGen",
                constraints: BorderLayout.NORTH)

			  
            splitPane() {
                treeTabPane = tabbedPane(constraints: BorderLayout.WEST, preferredSize:[300,300]) {
                    scrollPane(title:"Tables", tabMnemonic: "T") { tree(model: tableTreeModel) }
                    scrollPane(title:"Classes", tabMnemonic: "C") { tree(model: classTreeModel) }
                }
                mainTabPane = tabbedPane(constraints: BorderLayout.CENTER) {
                    consolePane = scrollPane (title:"Console", tabMnemonic: "C") {
                        console = textArea()
                    }
                }
            }
		      
			  


		}
		
		CodeGenMain.metaClass.println = printlnClosure
		main = new CodeGenMain()
		main.readProperties()
		main.configureCollaborators()
		
		hideConsole.enabled=false
		mainTabPane.remove(consolePane)
		
	}
	
	public static void main (String [] args) {
		GeneratorSwingApp app = new GeneratorSwingApp()
	}
	
	
}

```

What amazes me is how much this does and how small it is. Groovy indeed.

### Yet Another swing at Groovy ###
I took another swing at Groovy. I added support for editing JavaClass, JavaProperty, and Relationship. It may be the only example of using a comboBox, flowLayout, working with trees, etc. (the only one that I could find). There are also some example of working with Swing/Threads using Groovy's SwingBuilder support of Swing/Thread in this code. I am not sure if it is 100% correct, but it seems to work. Hopefullly someone will come along a review the code. Google has excellent tools for doing code reviews. You can review this code here: http://code.google.com/p/krank/source/browse/crank-codegen/src/main/groovy/com/arcmind/codegen/GeneratorSwingApp.groovy

Here is the code as is (not quite done, but a lot closer than before).

#### GeneratorSwingApp.groovy ####
```
/**
 *
 */
package com.arcmind.codegen

import javax.swing.*
import groovy.swing.SwingBuilder
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.FlowLayout
import javax.swing.event.TreeSelectionEvent


/**
 * @author richardhightower
 *
 */
public class GeneratorSwingApp{

	JFrame mainFrame
	CodeGenMain main
	SwingBuilder swing = new SwingBuilder()
	JLabel status
	Action viewConsole
	Action hideConsole
	JTextArea console
	List<Action> viewActions
	List<Action> mainActions
	List<Action> fileActions
	JTabbedPane treeTabPane
	JTabbedPane mainTabPane
	JScrollPane consolePane
	DBTableTreeModel tableTreeModel = new DBTableTreeModel()
	JavaClassTreeModel classTreeModel = new JavaClassTreeModel()
	JavaClass currentClass
	JavaProperty currentProperty
	Relationship currentRelationship
	JPanel classPane
	JPanel propertyPane
	JPanel relationshipPane
	ClassEditSupport classEditSupport
	JavaPropertyEditSupport propertyEditSupport
	RelationshipEditSupport relationshipEditSupport

	boolean debug = true

	def println(String message) {
		console.append(message + "\n")
	}

	Closure printlnClosure = { String message ->
		console.append(message + "\n")
	}

	public void exit() {
		System.exit(0)
	}

	public void showConsole() {
		println "Show console"
		hideConsole.enabled=true
		viewConsole.enabled=false
		mainTabPane.addTab("Console", consolePane)
		mainTabPane.setSelectedComponent(consolePane)
	}

	public void closeConsole() {
		println "Close console"
		viewConsole.enabled=true
		hideConsole.enabled=false
		mainTabPane.remove(consolePane)
	}

	public void clearConsole() {
		console.text = ""
	}

	public void reverseDB() {
		main.reader.tables=[]
		main.modelGen.classes=[]

		use(StringCategory){
			main.reverseDB()
		}

		tableTreeModel.setTables(main.reader.tables)
		classTreeModel.setClasses(main.modelGen.classes)

	}

	def selecteProperty(JavaProperty property) {
		mainTabPane.addTab("Property", propertyPane)
		mainTabPane.setSelectedComponent(propertyPane)
		currentProperty = property
		propertyEditSupport.populateForm(property)

	}
	def selecteClass(JavaClass javaClass) {
		mainTabPane.addTab("Class", classPane)
		mainTabPane.setSelectedComponent(classPane)
		currentClass = javaClass
		classEditSupport.populateForm(currentClass)

	}
	def selecteRelationship(Relationship relationship) {
		mainTabPane.addTab("Relationship", relationshipPane)
		mainTabPane.setSelectedComponent(relationshipPane)
		currentRelationship = relationship
		relationshipEditSupport.populateForm(relationship)
	}

	public void treeClassSelected(TreeSelectionEvent event) {
		setStatus ""

		/* close the tab that was open. */
		if (currentClass!=null) {
			mainTabPane.remove(classPane)
		} else if (currentProperty!=null) {
			mainTabPane.remove(propertyPane)
		} else if (currentRelationship!=null) {
			mainTabPane.remove(relationshipPane)
		}
		/* Reset last selected item. */
		currentClass = null
		currentProperty = null
		currentRelationship = null

		/* Select the new item. */
		Object selectedItem = event.path.lastPathComponent
		if (selectedItem instanceof JavaProperty) {
			selecteProperty(selectedItem)
		} else if (selectedItem instanceof JavaClassHolder) {
			selecteClass(selectedItem.javaClass)
		} else if (selectedItem instanceof Relationship) {
			selecteRelationship(selectedItem)
		} else {
			println "You selected ${selectedItem} of type ${selectedItem.class.name}"
		}
	}

	def setStatus(String msg){
		status.setText("  " + msg)
	}

	public GeneratorSwingApp() {

		/* Initialize edit support for class, property and relationships. */
		classEditSupport = new ClassEditSupport(classTreeModel:classTreeModel)
		propertyEditSupport = new JavaPropertyEditSupport(classTreeModel:classTreeModel)
		relationshipEditSupport = new RelationshipEditSupport(classTreeModel:classTreeModel)

		buildGUI ()

		/* Initialize CodeGenMain. */
		CodeGenMain.metaClass.println = printlnClosure
		main = new CodeGenMain()
		main.readProperties()
		main.configureCollaborators()

		/* Initilize Console and mainTabPane. */
		hideConsole.enabled=false
		mainTabPane.remove(consolePane)
		mainTabPane.remove(relationshipPane)
		mainTabPane.remove(classPane)
		mainTabPane.remove(propertyPane)


	}

	public static void main (String [] args) {
		GeneratorSwingApp app = new GeneratorSwingApp()
	}

	/* This method is very long, but it uses a builder to layout the complete GUI. It is layed out in a hierarchy so it is easy to read.
	 * This method contains no logic. It cust contains layout and event wiring.
	 */
	def buildGUI() {
		mainFrame=
        swing.frame(title:'CodeGen Code Generator', size:[1200,1000], defaultCloseOperation:JFrame.EXIT_ON_CLOSE,  show:true) {
            fileActions = actions() {
                action(name: "Exit", mnemonic: 'X', closure: { exit() })
            }

            Closure handleGenerateJavaAction = {
                doOutside { //Runs in a seperate thread
                    edt {setStatus "Reverse engineering database please standby..."}
                    reverseDB()
                    edt {setStatus "Done reverse engineering database." }
                }
            }

            mainActions = actions() {
                action(name: "Reverse DB", mnemonic: 'R', closure: handleGenerateJavaAction)
                action(name: "Generate Java", mnemonic: 'G', closure: {use(StringCategory){main.generateJavaClasses() }})
                action(name: "Write XML", mnemonic: 'W', closure: {use(StringCategory) {main.writeXML() }})
                action(name: "Read XML", mnemonic: 'e', closure: {use(StringCategory) {main.readXML() }})
                action(name: "Save Properties", mnemonic: 'S', closure: {use(StringCategory){main.writeProperties()}})
                action(name: "Modify Properties", mnemonic: 'o', closure: {  })
            }
            viewActions = actions () {
                viewConsole = action(name: "View Console", mnemonic: 'V', closure: { showConsole() })
                hideConsole = action(name: "Hide Console", mnemonic: 'H', closure: { closeConsole() })
                action(name: "Clear Console", mnemonic: 'l', closure: { clearConsole() })
            }
            menuBar() {
                menu(text: "File", mnemonic: 'F') {
                    fileActions.each {menuItem(it)}
                }
                menu (text: "Main", mnemonic: 'M') {
                    mainActions.each {menuItem(it)}
                }
                menu (text: "Window") {
                    viewActions.each {menuItem(it)}
                }
            }

            toolBar (constraints: BorderLayout.PAGE_START) {
                fileActions.each {button(it)}
                mainActions.each {button(it)}
                viewActions.each {button(it)}
                status = label(text:"  Welcome to CodeGen")
            }



            splitPane(constraints: BorderLayout.CENTER) {
                treeTabPane = tabbedPane(constraints: BorderLayout.WEST, preferredSize:[300,300]) {
                    scrollPane(title:"Tables", tabMnemonic: "T") { tree(model: tableTreeModel) }
                    scrollPane(title:"Classes", tabMnemonic: "C") {
                        tree(model: classTreeModel, valueChanged: {treeClassSelected(it)})
                    }
                }
                mainTabPane = tabbedPane(constraints: BorderLayout.CENTER) {
                    consolePane = scrollPane (title:"Console", tabMnemonic: "C") {
                        console = textArea()
                    }
                    classPane = panel (title:"Class", tabMnemonic: "l") {
                        flowLayout(alignment:FlowLayout.LEFT)
                        panel{
                            boxLayout(axis:BoxLayout.Y_AXIS)
                            label("Edit Class")
                            label("", preferredSize:[20,20])
                            panel {
                                boxLayout(axis:BoxLayout.X_AXIS)
                                label("Package", preferredSize:[100,20])
                                classEditSupport.packageName = textField(preferredSize:[200,20])
                                label(preferredSize:[100,20])
                            }
                            panel {
                                boxLayout(axis:BoxLayout.X_AXIS)
                                label("Class Name", preferredSize:[100,20])
                                classEditSupport.className = textField(preferredSize:[125,20])
                                label(preferredSize:[100,20])
                            }
                            panel {
                                button(text:"Apply", actionPerformed: {classEditSupport.updateObject(this.currentClass)})
                            }
                        }//panel
                    }//classPane
                    propertyPane = panel (title:"Property", tabMnemonic: "P") {
                        flowLayout(alignment:FlowLayout.LEFT)
                        panel{
                            boxLayout(axis:BoxLayout.Y_AXIS)
                            label("Edit Property")
                            label("", preferredSize:[20,20])
                            panel {
                                boxLayout(axis:BoxLayout.X_AXIS)
                                label("Name", preferredSize:[100,20])
                                propertyEditSupport.propertyName = textField(preferredSize:[200,20])
                                label(preferredSize:[100,20])
                            }
                            panel {
                                button(text:"Apply", actionPerformed: {propertyEditSupport.updateObject(this.currentProperty)})
                            }
                        }//panel
                    }
                    relationshipPane = panel (title:"Relationship", tabMnemonic: "R") {
                        flowLayout(alignment:FlowLayout.LEFT)
                        panel{
                            boxLayout(axis:BoxLayout.Y_AXIS)
                            label("Edit Relationship")
                            label("", preferredSize:[20,20])
                            panel {
                                boxLayout(axis:BoxLayout.X_AXIS)
                                label("Name", preferredSize:[100,20])
                                relationshipEditSupport.relationshipName = textField(preferredSize:[200,20])
                                label(preferredSize:[100,20])
                            }
                            panel {
                                boxLayout(axis:BoxLayout.X_AXIS)
                                label("Type", preferredSize:[100,20])
                                DefaultComboBoxModel model = new DefaultComboBoxModel([RelationshipType.ONE_TO_ONE, RelationshipType.ONE_TO_MANY, RelationshipType.MANY_TO_MANY, RelationshipType.MANY_TO_ONE] as Object[])
                                relationshipEditSupport.type = comboBox(model:model)
                                label(preferredSize:[100,20])
                            }
                            panel {
                                button(text:"Apply", actionPerformed: {relationshipEditSupport.updateObject(this.currentRelationship)})
                            }
                        }//panel

                    }

                }
            }




        }

	}

}

class ClassEditSupport {
	JTextField packageName
	JTextField className
	JavaClassTreeModel classTreeModel
	def updateObject (JavaClass cls) {
		println "update object was ${cls}"
		cls.packageName = packageName.text
		cls.name = className.text
		println "update object now ${cls}"
	}
	def populateForm (JavaClass cls) {
		packageName.text = cls.packageName
		className.text = cls.name
	}
}

class JavaPropertyEditSupport {
	JTextField propertyName
	JavaClassTreeModel classTreeModel
	def updateObject (JavaProperty prp) {
		println "update object was ${prp}"
		prp.name = propertyName.text
		println "update object now ${prp}"
	}
	def populateForm (JavaProperty prp) {
		propertyName.text = prp.name
	}
}


class RelationshipEditSupport {
	JTextField relationshipName
	JComboBox type
	JavaClassTreeModel classTreeModel
	def updateObject (Relationship rel) {
		println "update object was ${rel}"
		rel.name = relationshipName.text
		rel.type = (RelationshipType) type.selectedItem
		println "update object now ${rel}"
	}
	def populateForm (Relationship rel) {
		relationshipName.text = rel.name
		type.selectedItem = rel.type
	}
}


```

### Conclusions ###
I've written this same project four different ways: Jython, JPython, Java and now Groovy. The Groovy version was the most pleasant. Groovy hits a sweet spot with Java compatibility and Python like language features. I really enjoy programming in Groovy.

Groovy makes working with XML very easy. It makes working with String, collections and such extremely easy.

Some caveats: the tool support of Groovy is pretty weak. You best bet is to use the IDE support from IntelliJ. I will report back once we add the maven and Swing support.

Feedback welcome.