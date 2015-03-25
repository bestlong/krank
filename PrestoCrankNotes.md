# Introduction #

Just some notes. Probably not worth reading for anyone not also working with Presto.


# Details #

In trying to get Presto to work with Crank libraries. I am attempting to update the Presto sample application which is extremely well done I might add and is a testament to Scott Fauerbach's attention to detail.

I update the web app to use latest version of, Spring, Hibernate, JSF and Facelets. This caused all manner of class loading issues, which I got around with a bit of trouble shooting technique and goat's blood not to mention a few sharp jabs to my eye ball with a hot, sharp fork.

# SpringApplication #
Both Presto and Crank use a class called SpringApplication which is a Spring enabled, JSF application class implementation.

This class is used to load converters and such that allow DI via Spring. The problem with this class is that in JSF 1.2, it uses the EL mechanism to load the converters and it by passes converters loaded with the Application object and somehow gets them right from faces-config, which of course is a bug that has not been fixed as of release 1.2.04. I created an incredible hack for Crank to get around this problem, that I hope I don't have to recreate for Presto 2.

(To learn more about SpringApplication class read this write up at  http://opensource.atlassian.com/confluence/spring/display/JSF/Configuring+JSF+Validators,+Converters,+and+UIComponents+in+Spring to learn more about this class).

The latest versions of faces jar files can be found on Sun's maven repo which can be found here for those playing the home version of family feud:

#### maven repo where latest jsf jar files can be found ####
```
		<repository>
			<id>java.net</id>
			<url>https://maven-repository.dev.java.net/repository</url>
			<layout>legacy</layout>
		</repository>
```

BTW, Java.net's maven 2 repo only has JSF 2 not JSF 1.2 so don't go there.

I figured before I go about hacking Presto with the same hacks I added to Crank to make its converter architecture work in the broken JSF 1.2 world, I bests see if it is still broken. It seems the latest version of jsf is now 1.2\_10. Here goes...

#### from pom.xml ####
```
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

```

#### to pom.xml ####
```
       <dependency>
             <groupId>javax.faces</groupId>
             <artifactId>jsf-api</artifactId>
             <version>1.2_10</version>
             <scope>provided</scope>
       </dependency>
       <dependency>
             <groupId>javax.faces</groupId>
             <artifactId>jsf-impl</artifactId>
             <version>1.2_10</version>
             <scope>provided</scope>
       </dependency>
```

I also have to add it to my jetty plugin as follows:
```
		<plugin>
			<groupId>org.mortbay.jetty</groupId>
			<artifactId>maven-jetty-plugin</artifactId>
			<version>6.1.5</version>
			<dependencies>
				<dependency>
					<groupId>log4j</groupId>
					<artifactId>log4j</artifactId>
					<version>1.2.13</version>
					<type>jar</type>
				</dependency>
				<dependency>
					<groupId>javax.faces</groupId>
					<artifactId>jsf-api</artifactId>
					<version>1.2_10</version>
				</dependency>
				<dependency>
					<groupId>javax.faces</groupId>
					<artifactId>jsf-impl</artifactId>
					<version>1.2_10</version>
				</dependency>
				<dependency>
					<groupId>mysql</groupId>
					<artifactId>mysql-connector-java</artifactId>
					<version>3.1.11</version>
				</dependency>
			</dependencies>
		</plugin>

```

I find jetty makes it much easier to debug and test web apps with. Perhaps more on this later.

Of course, I get an immediate failure:
```
  Alternatively, if you host your own repository you can deploy the file there: 
      mvn deploy:deploy-file -DgroupId=javax.faces -DartifactId=jsf-impl -Dversion=1.2_10 -Dpackaging=jar -Dfile=/path/to/file -Durl=[url] -DrepositoryId=[id]

  Path to dependency: 
  	1) qcom.cas.presto:presto2-qa:war:08.01.00-SNAPSHOT
  	2) javax.faces:jsf-impl:jar:1.2_10

```

I need to add the java.net repo to my list of repos in this pom.xml as follows:
#### adding repos to pom.xml ####
```
	<repositories>
		<repository>
			<id>repo1.maven.org</id>
			<name>Maven Repository</name>
			<url>http://repo1.maven.org/maven2</url>
		</repository>
		<!--
			repo where we get latest jsf and facelets 
                        jar files comment out after
			populating internal repo
		-->
		<repository>
			<id>java.net</id>
			<url>https://maven-repository.dev.java.net/repository</url>
			<layout>legacy</layout>
		</repository>
		<!--  internal repo -->
		<repository>
			<id>cas</id>
			<name>cas</name>
			<url>http://vm-casdev/repo</url>
		</repository>
	</repositories>
```

Ok... After upgrading to the latest and greatest. The Presto converter architecture works fine in JSF 1.1 and still breaks in JSF 1.2. I traced this down for Crank about 18 months ago and came up with a workaround.

The problems is actually in the h:selectOneMenu renderer implementation, it uses the EL library to look up the converter which then bypasses the custom Application object that I setup (SpringApplication) and uses the default Application object somewhere in the bowls of the EL Impl jar. I even wrote my own renderer that worked just like the previous one but it kept breaking at odd times. Then I came up with a hack, which I will use again with Presto 2 since it always works. Essentially the SpringApplication.createConverter method does not get called with h:selectOneMenu. Now for the workaround (which I will need to reverse engineer from crank to recreate for Presto2). I first have to remember the hack, which I will now reverse engineer from Crank.