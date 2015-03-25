#### author Walter Bogaardt ####
# Introduction #
We can all look at the Crank web application example and create a web application using crank. Another is take the blank project and start building using that as our template to start our new crank project. Still to the novice developer jumping into a web application and seeing something we need to know what we are doing.

The goal here is to take a blank project and make something of it. Walk the developer through simple steps in creating the first web application and do it within an a few hours. Had to find the happy medium between "learn crank in 24 hours" or "crank for dummies".

## Requirements ##
  * Jdk 1.5 or higher
  * Mysql database
  * Maven 2.07 or higher
  * Crank 1.0 or higher


## Building with Maven ##
Some may agree, some may not, but Maven 2 proves pretty useful for managing dependencies and the cursed jarmagedon. The basis of the application will be Maven 2 so the first step is download it http://maven.apache.org/Maven2.

Once installed and know it is executable from a command line, you should be off and running. The first thing is look at the project pom.xml. Maven has a typical file structure it likes to follow so following maven's recipe of where java files, templates, resources and the works are important. Break the recipe and instead of cake you have disaster.

The  maven project folder should mirror or copy from the crank examples->blank-project to any location on the computer. Make sure to copy everything in webapp, resources, org/crank to the respective folders

![http://krank.googlecode.com/svn/wiki/img/maven-folder-structure.jpg](http://krank.googlecode.com/svn/wiki/img/maven-folder-structure.jpg)

Now, modify some dependencies in the pom.xml and understand other things about the pom.xml to move forward. The first section is the basic project. The goal is making a movie database application so the pom.xml project should reflect that. At the top of the pom.xml, change the blank-project stuff over to the movie application project. First step. Remove reference to 

&lt;parent&gt;

 pom by removing this section. The reason is to be independent of building entire crank libraries. The crank libraries for all purposes should have been built already on the local system if using a snapshot release. (See building crank http://code.google.com/p/krank/wiki/BuildingCrankWithMaven)

Set the group id tag to 'org.crank'. Artifact tag group to 'crank-movie'. Name of the project 'Crank movie example'. The other tags can be ignored or deleted.

The result is the following:

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
    
    <groupId>org.crank</groupId>
    <artifactId>crank-movie</artifactId>
    <packaging>war</packaging>
    <version>0.1.0.0-SNAPSHOT</version>
    <name>Crank movie example</name>
   ...
```

Couple of things to learn about dependencies. Let's start with dependencies in the pom.xml of the blank project. First remove everything from the 'dependencies' section tag as these steps will cover adding new dependency libraries.

The first set of dependencies is for spring wiring. Spring if it is not understood is an inversion of control container. More information about it is at this link  http://www.springframework.org/ to the Spring's website. The reason it is used in crank is it helps deal with configurations, allows loose coupling of the application. This provides the facility to use different layers of crank and wire it into some other framework, say like the requirement to use Ibatis over Hibernate, or the need to wire into Struts instead of JSF.

```
  <dependencies> <!--start of dependencies -->
  <!-- spring library dependencies -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-support</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-javaconfig</artifactId>
            <version>1.0-m2</version>
            <exclusions>
                <exclusion>
                    <groupId>asm</groupId>
                    <artifactId>asm-commons</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-support</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.aopalliance</groupId>
                    <artifactId>aopalliance</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-web</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-webmvc</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-aop</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- spring aspect libraries -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>aopalliance</groupId>
            <artifactId>aopalliance</artifactId>
            <version>1.0</version>
        </dependency>
        <dependency>
            <groupId>aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.5.3</version>
        </dependency>

```

The application also needs the ability to log messages to the console or file. To accommodate logging and offer the libraries like spring and hibernate dependencies, at the same time, exclude log4j when the application is deployed to an application server container. The following dependency is added:
```
<!-- common to multiple layers of application (spring, hibernate)-->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.13</version>
            <scope>provided</scope>
            <exclusions>
                <exclusion>
                    <groupId>avalon-framework</groupId>
                    <artifactId>avalon-framework</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>logkit</groupId>
                    <artifactId>logkit</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

The next set of dependencies allows crank to use a object-relational mapping framework, or and integrate to the Java Persistence framework (JPA). In this case, it will be hibernate. The other dependency is to the database driver itself to MySQL, and some logging frameworks used by both spring and the apache commons.

Set up your driver dependency for the database, for this exercise this is mysql.
```
  <!-- database driver for mysql-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.0.3</version>
        </dependency>
```

Set up some pooling resource dependencies. The application will make use of the apache dbcp library to do this, and some configurations to be covered later. It's possible to use JNDI look up from an application container, but that will be saved for a more advanced discussion.
```
 <!-- database pooling within webapp -->
        <dependency>
            <groupId>commons-dbcp</groupId>
            <artifactId>commons-dbcp</artifactId>
            <version>1.2.2</version>
            <exclusions>
                <exclusion>
                    <groupId>commons-collections</groupId>
                    <artifactId>commons-collections</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>commons-collections</groupId>
            <artifactId>commons-collections</artifactId>
            <version>3.2</version>
        </dependency>
```

The final library is hibernate libraries and spring JPA, which allows crank to plugin hibernate and spring together with the JPA framework.
```
  <!-- hibernate integration with JPA -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-tools</artifactId>
            <version>3.2.0.beta9a</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>3.2.1.ga</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-annotations</artifactId>
            <version>3.2.1.ga</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate</artifactId>
            <version>3.2.2.ga</version>
            <scope>compile</scope>
        </dependency>
          <dependency>
            <groupId>asm</groupId>
            <artifactId>asm</artifactId>
            <version>1.5.3</version>
        </dependency>
        <dependency>
            <groupId>org.apache.openjpa</groupId>
            <artifactId>openjpa-persistence-jdbc</artifactId>
            <version>1.0.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jpa</artifactId>
            <version>2.0.6</version>
        </dependency>
        <!-- used for hibernate cacheing-->
        <dependency>
            <groupId>net.sf.ehcache</groupId>
            <artifactId>ehcache</artifactId>
            <version>1.3.0</version>
        </dependency>
```

After the third party dependencies are added, add the crank libraries that provides interfaces to these libraries and provides additional support. These are the crank-core, crank-crud, and crank-validation libraries. Crank-core provides support for crank based annotations, utilities used by crank higher level frameworks such as crank-jsf-support, and crank based listeners.

Crank-crud is the bread and butter to crank's create, update, retrieve, delete operations. It provides filter facilities, cacheing, and criteria support. All of these facilities help to make issues with transaction management, sorting and querying information easier for end development.

The last crank-validation provides annotation support for column, table validation. Some validation features are phone number, email, length, number, and regular expression. These  annotations for validations can be applied to the JPA object, which crank display frameworks can use to validate the data and error to the user messages.
```
 <!-- Crank core libraries -->
        <dependency>
            <groupId>org.crank</groupId>
            <artifactId>crank-core</artifactId>
            <version>1.0.5-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.crank</groupId>
            <artifactId>crank-crud</artifactId>
            <version>1.0.5-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.crank</groupId>
            <artifactId>crank-validation</artifactId>
            <version>1.0.5-SNAPSHOT</version>
        </dependency>
```

Unit testing is important and typically with crank, it's been using TestNG. Little more beefier than JUnit. Notice, this particular library the scope is set to test as it is used only for unit testing and builds; not the final deployment into the webapplication. The other is spring-mock which allows use of spring mock object when used in unit testing interfaces.
```
 <!-- unit testing library dependencies -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>5.1</version>
            <classifier>jdk15</classifier>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-mock</artifactId>
            <version>2.0.6</version>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-beans</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-context</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-core</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-jdbc</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-jpa</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-webmvc</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

```

This application is going to make use of JSF and crank's support for this. Therefore the following dependencies incorporate servelet support, jsf, and java EL(expression language) support, and rich faces for ajax capabilities.

```
 <dependency>
            <groupId>org.richfaces.ui</groupId>
            <artifactId>richfaces-ui</artifactId>
            <version>3.3.4-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>xerces</groupId>
                    <artifactId>xercesImpl</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>xml-apis</groupId>
                    <artifactId>xml-apis</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>xerces</groupId>
                    <artifactId>xmlParserAPIs</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.richfaces.framework</groupId>
            <artifactId>richfaces-api</artifactId>
            <version>3.3.4-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>xerces</groupId>
                    <artifactId>xercesImpl</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>xml-apis</groupId>
                    <artifactId>xml-apis</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>xerces</groupId>
                    <artifactId>xmlParserAPIs</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.richfaces.framework</groupId>
            <artifactId>richfaces-impl</artifactId>
            <version>3.3.4-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>xerces</groupId>
                    <artifactId>xercesImpl</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>xml-apis</groupId>
                    <artifactId>xml-apis</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>xerces</groupId>
                    <artifactId>xmlParserAPIs</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <!-- JSF stuff -->
        <!-- excel exporter bean -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi</artifactId>
            <version>3.7</version>
        </dependency>
        <!-- JSF ControllerBean library depenency-->
        <dependency>
            <groupId>org.htmlparser</groupId>
            <artifactId>htmlparser</artifactId>
            <version>1.6</version>
        </dependency>
        <dependency>
            <groupId>joda-time</groupId>
            <artifactId>joda-time</artifactId>
            <version>1.2</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>javax.faces</groupId>
            <artifactId>jsf-api</artifactId>
            <version>1.2_04-p01</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.faces</groupId>
            <artifactId>jsf-impl</artifactId>
            <version>1.2_04-p01</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jsp-api</artifactId>
            <version>2.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.4</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.sun.facelets</groupId>
            <artifactId>jsf-facelets</artifactId>
            <version>1.1.14</version>
        </dependency>
        <dependency>
            <groupId>el-impl</groupId>
            <artifactId>el-impl</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.1.2</version>
        </dependency>
        <dependency>
            <groupId>commons-validator</groupId>
            <artifactId>commons-validator</artifactId>
            <version>1.3.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>oro</groupId>
            <artifactId>oro</artifactId>
            <version>2.0.8</version>
            <scope>test</scope>
        </dependency>
```

Now, add the final crank libraries that interface int JSF. This is the crank-jsf-support, which provides the facilities of controllers, validators, and transformers. This library also contains the crank components templates, which are templates that are used to render out listings, fields, and forms. This is the visual things you see when you think of crank.

The other library is crank-jsf-validation; this provides a number of utilities for JSF validation.

```
 <!-- crank jsf presentation layer libraries -->
        <dependency>
            <groupId>org.crank</groupId>
            <artifactId>crank-jsf-support</artifactId>
            <version>1.0RC2-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>javax.el</groupId>
                    <artifactId>el-api</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>el-impl</groupId>
                    <artifactId>el-impl</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>javax.faces</groupId>
                    <artifactId>jsf-api</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>javax.faces</groupId>
                    <artifactId>jsf-impl</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.crank</groupId>
            <artifactId>crank-jsf-validation</artifactId>
            <version>1.0RC2-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>javax.el</groupId>
                    <artifactId>el-api</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>el-impl</groupId>
                    <artifactId>el-impl</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>javax.faces</groupId>
                    <artifactId>jsf-api</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>javax.faces</groupId>
                    <artifactId>jsf-impl</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
      </dependencies> <!--no more dependencies-->
```

To make builds go by fast and see the application, the project can be run within jetty. A plugin feature in maven allows maven to start up a jetty container and runs the application. It reduces the time to compile, package the application, copy it to a application container's deploy directory, start up the application container, and then see the results. This is much faster when doing debuging than building a whole web application war and deploying it into and application container such as tomcat or jboss.

Running the application within jetty from maven the command is:

_mvn jetty:run_

It's possible to use the batch files in the blank project as other examples for running in debug mode, and connecting to the database. Modify the build section of the pom.xml to have a final name of 'movie-demo'. The build section of the pom should look like this:

```
  <build>
        <finalName>movie-demo</finalName>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
        <testResources>
            <testResource>
                <directory>src/test/resources</directory>
                <filtering>true</filtering>
            </testResource>
        </testResources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.3.2</version>
                <configuration>
                    <source>1.5</source>
                    <target>1.5</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.mortbay.jetty</groupId>
                <artifactId>maven-jetty-plugin</artifactId>
                <version>6.1.5</version>
                <dependencies>
                    <dependency>
                        <groupId>log4j</groupId>
                        <artifactId>log4j</artifactId>
                        <version>1.2.13</version>
                    </dependency>
                    <dependency>
                        <groupId>javax.faces</groupId>
                        <artifactId>jsf-api</artifactId>
                        <version>1.2_04</version>
                    </dependency>
                    <dependency>
                        <groupId>javax.faces</groupId>
                        <artifactId>jsf-impl</artifactId>
                        <version>1.2_04</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
```

Now, that you've gone through most of the pom.xml for maven all that takes to compile the project is the command:

_mvn clean install_

Obviously,  there won't be anything yet because there is still the issue to write some code.

## Create a Simple table and JPA object ##
If you haven't read http://code.google.com/p/krank/wiki/CrankCrudTutorial that may be ok. The fundamentals are there but there are some evolution changes that have happened up to crank 1.0 and we will start from the ground up in building the web application.

The goal of the application is to create a table in a database (mysql), and display its information and allow the user to add, edit, delete, or update data in this. Sounds simple and it should. The data model that the application is going to simulate is a movie database.

First create the database called "crank". (_These are Mysql commands_)
```
create database crank;
```

Next, create a table called "movie". Movie table will have a primary key, created by and date, updated by and date, name, description, ,checked out, year, rating, genre.

The reason we have all this created data and updated date name and description is not that it's necessary, but allows you to create some sort of history of change to a row in a database table.

It is possible to allow hibernate to auto generate the table in the database from the annotations on the persistent type object. This is good for development, bad for production release. Developers run the risk of forgetting that they are auto generating tables and then go to production and see tables are dropped, data is lost, and have a big headache later.

```
create table `MOVIE` (`MOVIE_ID` int(11) NOT NULL auto_increment,
  `CREATED_BY` varchar(45) default NULL,
  `CREATED_DATE` datetime default NULL,
  `UPDATED_BY` varchar(45) default NULL,
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `NAME` varchar(80) default NULL,
  `DESCRIPTION` varchar(255) default NULL,
  `CHECKED_OUT` bit(1) default NULL,
  `YEAR` int(4) default NULL,
  `RATING` varchar(4),
  `GENRE` varchar(30) NOT NULL,
  PRIMARY KEY  (`MOVIE_ID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

The next thing is to write some code that represents an object of this database. Pretty much if you follow this tutorial http://code.google.com/p/krank/wiki/CrankCrudTutorial on the employee table and apply it to this project you got it.

Create a MovieBO object and put this in the project src/org/crank/movie/ location.

Here is the solution on how the annotations should look. Import the JPA libraries and add annotations of Entity, the table and have the object in this case the MovieBO implement serializable.

The next are the attributes that are annotate to the columns they represent in the database.
```
package org.crank.movie;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Child http assertion
 */
@Entity
@Table( name = "MOVIE" )
public class MovieBO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "ID" )
    private Long id;
    @Column( name = "CREATED_BY", length = 45 )
    private String createdBy;
    @Temporal( value = TemporalType.TIMESTAMP )
    @Column( name = "CREATED_DATE" )
    private Date createdDate;
    @Column( name = "UPDATED_BY", length = 45 )
    private String updatedBy;
    @Temporal( value = TemporalType.TIMESTAMP )
    @Column( name = "UPDATED_DATE" )
    private Date updatedDate;
     @Column(name="NAME", length=80)
    private String name;
    @Column(name="DESCRIPTION")
    private String description;
     @Column(name="CHECKED_OUT", length=65535)
    private boolean checkedOut;
     @Column(name="YEAR")
    private int year;
    @Column(name="RATING", length=4)
    private int rating;
    @Column(name="GENRE", length=30)
    private String genre;
    ...
```

@Entity defines this object as a persistable entity. @Table annotation describes what table this entity object maps to. @Column annotations describe which column in the database each attribute maps. There is a good tutorial on some of this http://www.onjava.com/pub/a/onjava/2007/02/08/an-introduction-to-hibernate-3-annotations.html
and at http://www.hibernate.org

The rest is going to be the access modifier methods which can be auto generated through an IDE (auto generate getter/setter). One difference is the access/modifier for the boolean attribute, this looks like this:
```
  . . .
    public boolean isCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
    }
  . . . 
```

## Data access object (DAO) ##
To run 'queries' and select data sets, you need a data access object. In crank, this can be accomplished by extending facilities used in the crank's GenericDao interface. The GenericDao allows the basic functionality of reads, update, delete, creates of objects. The read facility allows filtering on columns for set values encapsulated in overloaded "find" methods. There is dated documentation on how the finder method's work in the GenericDao. http://code.google.com/p/krank/wiki/UsingDAO

This data access object is going to be a simple, in that it will extend the GenericDao interface. The interface then allows the DAO created to make use of crank's GenericDaoJpa facilities. This code will go in the same package that the MovieBO was placed. The GenericDao is set to the class followed by a comma and the primary key class type identifier. The model object is using a Long as its primary id type. The primary key could also be a String or any other type.

```
package org.crank.movie;

import org.crank.crud.GenericDao;

public interface IMovieDao extends GenericDao<MovieBO, Long> {
}
```


## Map to persistence ##
If everything was copied from the blank crank project there should be in the resources/META-INF/ directory a persistence.xml. This is going to be modified from what is there to suite this project needs.

Change the persistence unit name to "crank-movie". The next is to add the object created above to the persistence unit group so it is accessible. This is required for Hibernate JPA persistence management. This allows hibernate to know which classes are persistable.



&lt;class&gt;

org.crank.movie.MovieBO

&lt;/class&gt;



Solution:
```
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence 
			http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
    version="1.0">

    <persistence-unit name="crank-movie" transaction-type="RESOURCE_LOCAL">
        <class>org.crank.movie.MovieBO</class>
    </persistence-unit>

</persistence>
```

## Modify JPA settings ##
After modifying the persistence.xml file, change the jpa.properties file in resources directory. The jpa.properties file is an externalization of the persistence.xml properties. It is possible to have these properties in the persistence.xml and not in a separate file as in this project. As discussed above, it was pointed out that it might not be good to have hibernate auto generate the tables in the database. Change the 'hibernate.hbm2ddl.auto=update' to 'false'.

```
openjpa.jdbc.SynchronizeMappings=buildSchema(SchemaAction='add')
openjpa.Log=DefaultLevel=TRACE,SQL=TRACE

hibernate.hbm2ddl.auto=false
hibernate.show_sql=false
hibernate.format_sql=true
hibernate.use_sql_comments=true
hibernate.query.substitutions=true 1, false 0
```

## Spring wire data sources ##
The application needs access to the database, and to do this it needs to know a couple of things about the user and password to access the database. In the resources directory, there is an applicationContext.xml file. Right now all we will change is the reference to 'employeeDatasources' to the movie application source name, which will be 'movieDataSource'. A find and replace should work here.

Solution:
```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:tx="http://www.springframework.org/schema/tx"    
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
           http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx-2.0.xsd">

    <bean class="org.crank.controller.MovieExampleApplicationContext" />
   
    <bean id="movieDataSource"
          class="org.apache.commons.dbcp.BasicDataSource"
          destroy-method="close" lazy-init="true">
        <property name="driverClassName" value="${krank.jdbc.driver}" />
        <property name="url" value="${krank.jdbc.url}" />
        <property name="username" value="${krank.jdbc.user}" />
        <property name="password" value="${krank.jdbc.password}" />
        <property name="minIdle" value="5" />
        <property name="maxIdle" value="15" />
        <property name="initialSize" value="200" />
        <property name="maxOpenPreparedStatements" value="100" />
        <property name="maxWait" value="3000" />
        <property name="timeBetweenEvictionRunsMillis" value="3600000" />
    </bean>

    <bean id="jpaConfigProperties"
          class="org.springframework.beans.factory.config.PropertiesFactoryBean">
        <property name="location" value="classpath:jpa.properties" />
    </bean>

    <bean name="jpaVendorAdapter"
          class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter">
        <property name="showSql" value="false" />
        <property name="databasePlatform" value="${krank.hibernate.dialect}" />
    </bean>

    <bean class="org.crank.config.spring.support.CrudDAOConfig">
        <property name="dataSource" ref="movieDataSource" />
        <property name="jpaProperties" ref="jpaConfigProperties" />
    </bean>

    <bean class="org.springframework.config.java.process.ConfigurationPostProcessor" />
    <import resource="classpath:/validationContext.xml" />
</beans>
```

Notice, the jpaProperties are referenced by a spring PropertiesFactoryBean and then injected into the CrudDAOConfig.

There are also a number of 'krank' properties, and these are property values that are found and replaced, when the maven build is run. These properties are fed in by the maven pom.xml file so add the project specific properties to the the pom.xml.  The result is this for an example:

pom.xml add modification:
```
. . .
    <properties>
        <krank.jdbc.url>
            jdbc:mysql://localhost:3306/crank?autoReconnect=true
        </krank.jdbc.url>
        <krank.jdbc.driver>com.mysql.jdbc.Driver</krank.jdbc.driver>
        <krank.jdbc.user>crankuser</krank.jdbc.user>
        <krank.jdbc.password>crank</krank.jdbc.password>
       <krank.hibernate.dialect>org.hibernate.dialect.MySQLInnoDBDialect</krank.hibernate.dialect>
    </properties>
. . .
```

The last step is now to add our controller to the application context. This is a bean that is specified in applicationContext.xml. This example it will be org.crank.controller.MovieExampleApplicationContext. Further discussion on creating this controller is as follows :



&lt;bean class="org.crank.controller.MovieExampleApplicationContext" /&gt;



This is the controller that is going to be created.
## Create A Controller ##
The controller is the heart of the application. It is used to connect crank to the datasources and entity objects. Learn more about this architecture here:
http://code.google.com/p/krank/wiki/CrankCrudDesignDocs

First create a .java source file in your src/main/org/crank/controller/. Called MovieExampleApplicationContext. This will be an abstract class and extend the abstract class of CrudJSFConfig. The class will be annotated to be a Spring configuration.

```
@Configuration(defaultLazy = Lazy.TRUE)
public abstract class MovieExampleApplicationContext extends CrudJSFConfig {
```

The jsf pages will use crank's CrudManagedObjects, which will be wrapped in a JsfCrudAdapter controller. Set up the managed objects as a list, that will be a static singleton list like this:
```
        private static List<CrudManagedObject> managedObjects;

	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
            managedObjects.add(new CrudManagedObject(MovieBO.class, "MovieBO", IMovieDao.class));
        }
		return managedObjects;

	}
```

The next step is to wire in the datasource and persistence unit group that was changed in the applicationContext.xml and the persistence.xml. This is done as follows, specify a string return method called persistenceUnitName the returns the name of the persistence.xml unit name.

Remember, the CrudDAOConfig created above will use the dataSource property to get the access to the data base throughout the crank web application. Here it is specified to the same data source declared earlier in the persistence.xml.
```
  . . . 
      @Bean
     public String persistenceUnitName() {
		return "crank-movie";
     }

   
   . . .
```

Create a simple controller that will allow one page to display a listing and another page to display a form for editing and updating a selected listing. This case the JSF controller will use the MovieBO managed crud object above. The wrapping controller is a JsfCrudAdapter. The method is going to be called 'movieCrud'. The method and controller will be scoped on requests. It can be set to SESSION as well, which works for long running transactional events such as a form wizard. The JsfCrudAdapter plugs into the MovieDao created, and so the return on the method should follow the same typing, which is the class followed by a comma and the primary key type of the model object.

Solution:
```
. . . 
    @SuppressWarnings("unchecked")
    @Bean(scope = DefaultScopes.REQUEST)
    public JsfCrudAdapter<MovieBO, Long> movieCrud() throws Exception {
        JsfCrudAdapter adapter = cruds().get("MovieBO");
        return adapter;
    }
. . . 
```

That about completes to this point. This will be revisited and enhanced later for some other functionality. The entire solution listing looks like this:
```
package org.crank.controller;

import org.crank.config.spring.support.CrudJSFConfig;
import org.crank.crud.controller.CrudManagedObject;
import org.crank.crud.jsf.support.JsfCrudAdapter;
import org.crank.crud.jsf.support.SelectItemGenerator;
import org.crank.movie.IMovieDao;
import org.crank.movie.MovieBO;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.util.DefaultScopes;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration(defaultLazy = Lazy.TRUE)
public abstract class MovieExampleApplicationContext extends CrudJSFConfig {

	private static List<CrudManagedObject> managedObjects;

	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
            managedObjects.add(new CrudManagedObject(MovieBO.class, "MovieBO", IMovieDao.class));
              }
		return managedObjects;

	}

	@Bean
	public String persistenceUnitName() {
		return "crank-movie";
	}

        @ExternalBean
	abstract DataSource movieDataSource();

        @SuppressWarnings("unchecked")
        @Bean(scope = DefaultScopes.REQUEST)
        public JsfCrudAdapter<MovieBO, Long> movieCrud() throws Exception {
             JsfCrudAdapter adapter = cruds().get("MovieBO");
             return adapter;
        }
}
```

## Navigation Rules for JSF ##
JSF needs to understand which pages to send a user to based on various outcomes. Crank has a number of outcomes that occur with the JsfCrudAdapter's controller. The most common in crank are LISTING, FORM, and EXPORT. These outcome flows will be created a little later. For now, open the faces-config.xml in webapp/WEB-INF directory.

Remove all navigation-rules that do not have from-outcomes of 'HOME' or 'EMPLOYEES'. Then the 'EMPLOYEES' will get changed to 'MOVIE'

Recall adding to the home the 'MOVIE' action, this is going to navigate the user to /pages/crud/listing.xhtml. In JSF the faces-config.xml contains the flows when controllers return set states or outcomes occur. The 'MOVIE' outcome flows the user to the Listing.xhtml page for movies.

The 'HOME' outcome is expected outcome when clicking on the Home link from a crank:breadcrumb component. This will be discussed later, for now, set up an outcome for 'HOME' to go to the home.xhtml page. The 'MOVIE' outcome flows the user to the Listing.xhtml page for movies.

The total faces-config.xml listing should look like this:

```
 <?xml version="1.0"?>
<faces-config xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_1_2.xsd"
    version="1.2">

  <application>
    <action-listener>org.crank.crud.jsf.support.JsfMessageActionListener</action-listener>
    <variable-resolver>org.springframework.web.jsf.DelegatingVariableResolver</variable-resolver>
    <message-bundle>messages</message-bundle>
  </application>

  <component>
    <component-type>org.crank.javax.faces.SelectOne</component-type>
    <component-class>org.crank.javax.faces.component.UISelectOne</component-class>
  </component>
  
  <render-kit>
      <renderer>
      <component-family>javax.faces.SelectOne</component-family>
      <renderer-type>org.crank.javax.faces.Listbox</renderer-type>
      <renderer-class>
			org.crank.javax.faces.component.ListboxRenderer
  	  </renderer-class>
    </renderer>
  
  </render-kit>
    
  <navigation-rule>
  	<navigation-case>
  		<from-outcome>HOME</from-outcome>
  		<to-view-id>/pages/home.xhtml</to-view-id>
  	</navigation-case>
  </navigation-rule>
  
  <navigation-rule>
  	<navigation-case>
  		<from-outcome>MOVIE</from-outcome>
  		<to-view-id>/pages/crud/movie/Listing.xhtml</to-view-id>
  	</navigation-case>
  </navigation-rule>
</faces-config> 
```


## Create A Page ##
The first two pages to be created will be a navigation page to the listings. The navigation page is going to be a modification of the home.xhtml page in src/main/webapp/pages/.

First remove everthing between 'ui:define name="content"' tag and add the following:

```
        <h:form>
            <h:panelGrid columns="1">
                <br/>
                <h:commandLink action="MOVIE" value="Movie List" styleClass="titleCommandLink"/>               
         </h:panelGrid>
        </h:form>
```

Earlier, the navigation rule for 'MOVIE' outcome was set in faces-config.xml. What will occur here is when the user clicks on the JSF _h:commandLink_ component and with the action set to "MOVIE". JSF will redirect the user to the listings.xhtml page that will be created next.

Entire home.xhtml solution:
```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:ui="http://java.sun.com/jsf/facelets"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:f="http://java.sun.com/jsf/core"
	>
<ui:composition template="/templates/layout.xhtml">
	<ui:define name="content">
		<h:form>
            <h:panelGrid columns="1">
                <br/>
                <h:commandLink action="MOVIE" value="Movie List" styleClass="titleCommandLink"/>               
         </h:panelGrid>
        </h:form>
	</ui:define>
</ui:composition>
</html>
```

## Create a Listing Page ##
Create in 'pages' directory create a subdirectory called crud/movie/.  This subdirectory is where we will put our editing form, listing form and export form. The first is create a listing form. Create a file called listing.xhtml.

The same steps in modifying the home.xhtml file can be applied here. It's possible just to copy the home.xhtml file rename the copied file to listing.xhtml and remove everything between the 'ui:define name="content"' tag.

There are a number of components going to be used by this page and more then what the home.xhtml declared in the html tag so to allow use of these additional components(a4j,crank,core JSTL) on the page and java standard template library (JSTL); it has to be modified as such:
```
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://java.sun.com/jsf/facelets"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:a4j="https://ajax4jsf.dev.java.net/ajax"
      xmlns:rich="http://richfaces.ajax4jsf.org/rich"
      xmlns:c="http://java.sun.com/jstl/core"
      xmlns:crank="http://www.googlecode.com/crank"
        >
```
The first thing use JSTL to define a variable which references the 'MovieBO' name.

```
  <c:if test="${empty daoName}">
            <c:set var="daoName" value="MovieBO"/>
  </c:if>
```

This leads to adding a number of crank components to our page. Read more about the different components here: http://code.google.com/p/krank/wiki/CrankJSFComponentsAPI

The first component is a bread crumb component. The bread crumb allows a trail for navigation but it needs to know about the controller we created earlier, in the 'MovieExampleApplicationContext'. This is how the bread crumb is added to the page:

```
 <crank:crudBreadCrumb crud="${movieCrud.controller}" />
```

The 'movieCrud', is the name of the method that returns the JsfAdapterCrud. The dot'controller' is the controller object within the JsfAdapterCrud.

Add to the page a page title, and call it 'Movie' or any other name like 'Movie Listing'. Use the h:output component of Jsf. The styleClass in this component can use the Crank style sheet class 'pageTitle'.

```
  <h:outputText value="Movie" styleClass="pageTitle"/>
```

Here is where things can get hairy, but to allow some dynamic rendering for sorting and filter the page will use a4j, which are ajax components. The a4j region and form will wrap around our crank listing component. Read more on crank listing attributes under the CrankJSFComponentAPI wiki.

The crank listing specify for the propertyNames in comma separated (no spaces) the method/attributes you annotated in the MovieBO object.

Here is the result:
```
<a4j:region renderRegionOnly="false">
            <a4j:form id="${daoName}ListForm">
                <crank:listing
                        id="${daoName}Listing"
                        paginator="#{paginators[daoName]}"
                        jsfCrudAdapter="#{cruds[daoName]}"
                        propertyNames="id,createdDate,createdBy,updatedDate,updatedBy,name,checkedOut,rating,year,genre"
                        pageTitle="${daoName}"
                        autoLink="false"
                        parentForm="${daoName}ListForm"
                        reRender="${daoName}ListForm"
                        crud="#{movieCrud.controller}"
                        isSelectableColumn="${false}"/>
            </a4j:form>
        </a4j:region>
```

Notice, that the 'daoName' variable is used to create variable names. This is why it was specified earlier in the page. This could have been done without the variable declaration but would have been a lot more repeat typing. There are many other options and features to crank listing, but this is the most common set-up.

Following the above steps, the entire page for listings should look like this:
```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://java.sun.com/jsf/facelets"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:a4j="https://ajax4jsf.dev.java.net/ajax"
      xmlns:rich="http://richfaces.ajax4jsf.org/rich"
      xmlns:c="http://java.sun.com/jstl/core"
      xmlns:crank="http://www.googlecode.com/crank"
      xmlns:vmc="http://www.vantagemedia.com/vmc"
        >
<ui:composition template="/templates/layout.xhtml">
    <ui:define name="content">

        <c:if test="${empty daoName}">
            <c:set var="daoName" value="MovieBO"/>
        </c:if>

        <crank:crudBreadCrumb crud="${movieCrud.controller}" />

        <h:outputText value="Movie" styleClass="pageTitle"/>


        <a4j:region renderRegionOnly="false">
            <a4j:form id="${daoName}ListForm">
                <crank:listing
                        id="${daoName}Listing"
                        paginator="#{paginators[daoName]}"
                        jsfCrudAdapter="#{cruds[daoName]}"
                        propertyNames="id,createdDate,createdBy,updatedDate,updatedBy,name,checkedOut"
                        pageTitle="${daoName}"
                        autoLink="false"
                        parentForm="${daoName}ListForm"
                        reRender="${daoName}ListForm"
                        crud="#{movieCrud.controller}"
                        isSelectableColumn="${false}"/>
            </a4j:form>
        </a4j:region>

    </ui:define>
</ui:composition>
</html>
```

## See the Results ##
You should be able to run at this point:

_mvn jetty:run_

Go to http://localhost:8080  and see a link to the application.

There should be a home page with a link to Movie and this takes the user to the movie listing. Of course, if there is no data in the database it won't show anything, and the application hasn't been configured to have a form for adding and editing either.

# Add a form #
There lacks still the ability to do full CRUD operations currently, all that happens is a select of all information in the database. The next step is to add a crank form that will allow users to add or edit a row in the database table.

## Form navigation ##
The form navigation rules are the same for all forms. This is why if there is a table that is going to have crank crud operations the folders should be separated in pages/crud/tablename or similar (for clarity only developers are free to do what they want).

First is add to the faces-config.xml the following changes:
```
<navigation-rule>
  	<from-view-id>/pages/crud/movie/*</from-view-id>
  	<navigation-case>
  		<from-outcome>LISTING</from-outcome>
  		<to-view-id>/pages/crud/movie/Listing.xhtml</to-view-id>
  	</navigation-case>
  	<navigation-case>
  		<from-outcome>FORM</from-outcome>
  		<to-view-id>/pages/crud/movie/Form.xhtml</to-view-id>
  	</navigation-case>  	
  	<navigation-case>
  		<from-outcome>EXPORT</from-outcome>
  		<to-view-id>/pages/crud/movie/ExportListing.xhtml</to-view-id>
  	</navigation-case>  	
  </navigation-rule>
```

The rules for the outcome are always the same when using the JsfCrudAdapter's controller. The 'to-view-id' pages url are based on where the specified page for listing, form or export listing are located.

## Crank Form ##
Recall the inital steps for the Create a Listing page. Same thing here, only this time we call the page Form.xhtml.

Add a bread crumb component first:
```
 <crank:crudBreadCrumb crud="${movieCrud.controller}" />
```

Add a display title switching so the form displays one title for edit mode and another title for add mode:
```
        <c:choose>
            <c:when test='${movieCrud.controller.state == "ADD"}'>
                <h:outputText value="Create Movie Entry" styleClass="pageTitle"/>
            </c:when>
            <c:otherwise>
                <h:outputText value="Edit Movie" styleClass="pageTitle"/>
            </c:otherwise>
        </c:choose>
```

The form can display error messages, because a user may enter text in a numeric only field, or not enter information when there is required information. Validation errors willl show up next to the entry field, but form erros will need to be displayed on top or bottom of the form. Here we will put it at the top of the form.

```
<h:messages errorClass="pageErrorMessage"/>
```

Finally, add the crank:form with a comma separated (no spaces) list of the attributes to be editable and added. Note if some fields were annotated in the MovieBO to be required and it's not included on the form for add. The user will experience a required field entry error and never be able to add the information until the property is added to the crank form. The form will also provide a number of basic validations and error messages based on field level annotations in the MovieBO. There is more on the architectual detail of the messages here: http://code.google.com/p/krank/wiki/CrankValidationDesignDocument

```
   <a4j:region renderRegionOnly="false">
            <a4j:form
                    id="movieForm">

                <crank:form
                        crud="#{movieCrud.controller}"
                        propertyNames="createdBy,createdDate,updatedBy,updatedDate,name,description,checkedOut,year,rating,genre"
                        createButtonText="Create Inquiry"
                        updateButtonText="Update Inquiry Information"
                        cancelButtonText="Cancel"
                        resetButtonText="Reset"
                        ajax="${false}">
                </crank:form>

            </a4j:form>
        </a4j:region>
```

Go ahead and run the application again:
_mvn jetty:run_

The application now should display a listing, and click on the green plus button on the listing page and the user is presented with a blank form to enter information. If all the data entered is correct the form returns back to the listing page, and the new entry will show up on this page. The user can also click on the notepad with pencil icon on a row in the listing, and the form will display with the information of this row pre-filled in the form, and the user can make modifications and submit.

## Export Listing (Advanced) ##
Export listings allow the user to export the data base information to an excel spreadsheet. This is a feature you may want or not so it's left for last. The first thing is notice the "ExcelExporterBean" in the example project. This bean should be proxyed to the user session. Do the following in in the 'MovieExampleExampleContext.java' controller. Eventually someone will include this bean as part of the crank framework till then do the following:

```
        @Bean(scope = DefaultScopes.SESSION)
	@ScopedProxy
	public ExcelExportControllerBean controllerBean() throws Exception {
		return new ExcelExportControllerBean();
	}
```

Noticed above that change to the 'faces-config.xml' contained reference to 'EXPORT' outcome. If you tried to click on the listing page's icon for an excel spread sheet you would have gotten an error. This is because no listing page existed.

## Create Listing page ##
Same process of creating the form, and listing page applies to the export listing. Create a file called 'ExportListing.xhtml'.

Add a title for the page within the ui:define tags:
```
<span class="pageTitle">Export Employees Preview</span>
```

Add a jsf form and add an crankExportListing component.

```
     <h:form>
		<crank:exportListing  jsfCrudAdapter="#{cruds['MovieBO']}"
				propertyNames="id,createdDate,createdBy,updatedDate,updatedBy,name,description,checkedOut,year,rating,genre"/>
     </h:form>
```

That's all there is to it to have a listing that is exportable to excel.