## **This document is unfinished _Last Modified 11/23/2008 9:07 PM PST_** ##

# Introduction #

The Crank Spring Security Webapp is a mini-example application that utilizes Spring Security 2.x.x to enforce user logins to secured resources. Out of the box, Spring Security 2.x.x does not integrate at all with JSF, so a little code had to be written so that it would. By default if you use Spring Security's JDBC Authentication Provider, Spring assumes that you have certain table names and columns defined to do out of the box authentication with a database. You can define your own tables and authentication queries, but out of the box, Spring has nailed down a pretty good ERD for the way they have implemented the basic concepts of Security which are Users, Roles, Groups, etc... Spring Security has no concept of a User Management UI, so that will be something to be added in the near future. (Acegi lacked one as well)

For a more detailed explanation of the internals of Spring Security 2 you can visit the following links :
  1. [Spring Security Documentation](http://static.springframework.org/spring-security/site/)
  1. [Spring Security Database Schema Explained](http://static.springsource.org/spring-security/site/reference/html/appendix-schema.html)

I highly recommend reading the links provided, it will save you a lot of headache.

# Brief #
The crank-spring-security-webapp is a direct copy of the crank-crud-webapp example with the exception of a few changes:

  * Custom Crank Spring Security tag library code (_Spring Security tag libraries do not work with Java Server Faces without a custom tag library and supporting classes_)
  * modified .xhtml pages to include Crank Spring Security tag namespaces
  * Spring Security application context configuration file.
  * web.xml Spring Security Listeners
  * faces-config.xml navigation rule additions
  * pom.xml Spring Security library additions
  * Additional employee-jpa-model classes added to support the assumed default Spring Security Database Schema

So with that being said, let's examine and explain the changes.

# Integrating Spring Security 2.0.4 and Crank 1.0.4 #
### Default Database Schema ###
The following SQL can be used to create the default database schema that Spring Security assumes when using the JDBC Authentication.
```
CREATE TABLE `users` (
`username` varchar(255) NOT NULL,
`password` varchar(255) NOT NULL,
`enabled` tinyint(1) NOT NULL,
PRIMARY KEY  (`username`)
);

CREATE TABLE `groups` (
`id` int(11) NOT NULL default ‘0′,
`group_name` varchar(255) default NULL,
PRIMARY KEY  (`id`)
);

CREATE TABLE `group_members` (
`group_id` int(11) NOT NULL default ‘0′,
`username` varchar(255) default NULL,
PRIMARY KEY  (`group_id`, `username`)
);

CREATE TABLE `group_authorities` (
`group_id` int(11) NOT NULL default ‘0′,
`authority` varchar(255) NOT NULL default ”,
PRIMARY KEY  (`group_id`,`authority`)
);
```

In crank Hibernate / JPA is being used so all I had to do was create the Entity classes with the correct Annotations. I added these to the 'employee-jpa-model' project under the 'examples' folder which is also used in crank-crud-webapp.

### POM Additions ###
  1. Spring Security 2.0.4 Core Libraries
```
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-core</artifactId>
    <version>2.0.4</version>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-core-tiger</artifactId>
    <version>2.0.4</version>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-acl</artifactId>
    <version>2.0.4</version>
</dependency>
```
  1. Spring Security 2.0.4 currently only works with [Spring JavaConfig Milestone 2](http://www.springsource.org/javaconfig)
```
<!-- start javaconfig -->
        <dependency>
            <groupId>org.springframework.javaconfig</groupId>
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
        <!-- end javaconfig-->
```
  1. Custom Crank Spring Security Tag Dependency
```
   <dependency>
     <groupId>javax.el</groupId>
     <artifactId>el-api</artifactId>
     <version>1.0</version>
    <scope>provided</scope>            
   </dependency>
```


### web.xml additions ###
A configuration file named "_applicationContext-security.xml_" was added to web.xml under the WEB-INF/ folder. It could have been added as a separate namespace configuration within the Spring Context configuration file (_applicationContext.xml_).
```
<context-param>
 <param-name>contextConfigLocation</param-name>
  <param-value>
    classpath:/applicationContext.xml
    /WEB-INF/applicationContext-security.xml <!-- ADDED FILE -->
  </param-value>
</context-param>

<!-- used to track session events (single user session) -->
<listener>
<listener-class>org.springframework.security.ui.session.HttpSessionEventPublisher</listener-class>
</listener>

<filter>
    <filter-name>_filterChainProxy</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
 
<filter>
   <filter-name>filterToBeanProxy</filter-name>
   <filter-class>org.springframework.security.util.FilterToBeanProxy</filter-class>
   <init-param>
   <param-name>targetClass</param-name>
   <param-value>org.springframework.security.util.FilterChainProxy</param-value>
 </init-param>
</filter>
 
<filter-mapping>
 <filter-name>filterToBeanProxy</filter-name>
 <url-pattern>/*</url-pattern>
 <dispatcher>FORWARD</dispatcher>
 <dispatcher>REQUEST</dispatcher>
</filter-mapping>  
```

### applicationContext-security.xml added to WEB-INF/ ###
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:s="http://www.springframework.org/schema/security"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
                        http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security-2.0.1.xsd">



    <s:http auto-config="false" once-per-request="false"  access-denied-page="/accessDenied.jsf">

        <s:intercept-url pattern="/pages/crud/Users/**" access="ROLE_USER,ROLE_SUPERVISOR"/>
        <s:intercept-url pattern="/login.jsf*" filters="none"/>
		<s:form-login login-page="/login.jsf"
                      authentication-failure-url="/accessDenied.jsf"
                      default-target-url="/index.jsp"/>
        
		<s:logout logout-url="/logout" logout-success-url="/"></s:logout>
        <s:concurrent-session-control max-sessions="1" exception-if-maximum-exceeded="true"/>
    </s:http>

    <!-- myUserDetailsService is wired inside of applicationContext.xml -->
   <s:authentication-provider user-service-ref='myUserDetailsService'/>

</beans>
```

### applicationContext.xml Modifications ###
A data source is needed by the Spring Security [AuthenticationProvider](http://static.springsource.org/spring-security/site/reference/html/ns-config.html#ns-auth-providers), however, one is already configured within the applicationContext.xml which is the 'employeeDataSource' below that already exists in most of the crank-crud examples:
```
 <bean id="employeeDataSource"
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

```


Now the only modification needed to applicationContext.xml is to wire the [JdbcDaoImpl](http://static.springsource.org/spring-security/site/reference/html/authentication-common-auth-services.html#jdbc-service) bean which the Spring Security [AuthenticationProvider](http://static.springsource.org/spring-security/site/reference/html/ns-config.html#ns-auth-providers) will use to authenticate users against the databased linked to the existing configured data source.

```
 <bean id="myUserDetailsService" class="org.springframework.security.userdetails.jdbc.JdbcDaoImpl">
    <property name="dataSource" ref="employeeDataSource"/>
 </bean>
```

### faces-config.xml changes ###
Redirect tag added to all faces-config navigation rules
```
  <navigation-rule>
  	<navigation-case>
  		<from-outcome>USERS</from-outcome>
  		<to-view-id>/pages/crud/Users/Listing.xhtml</to-view-id>
        <redirect/>
      </navigation-case>
  </navigation-rule>
```

### New Tag Library added to support Custom Spring Security tags and EL functions ###
file: `\src\main\webapp\WEB-INF\spring-security.taglib.xml`
```
<?xml version="1.0"?>
<!DOCTYPE facelet-taglib PUBLIC  "-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN"
  "facelet-taglib_1_0.dtd">

<facelet-taglib>
	<library-class>
           org.crank.tags.JsfCoreLibrary
      </library-class>    
</facelet-taglib>
```

file: `JsfCoreLibrary.java`

Purpose: Registers the tagname `<springSecurityAuthorize/>` under the name space
`http://www.crank.org/springsecurity/tags` with the java class  `SpringSecurityAuthorize.java`
```
package org.crank.tags;

import com.sun.facelets.tag.AbstractTagLibrary;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class JsfCoreLibrary extends AbstractTagLibrary {
    /** Namespace used to import this library in Facelets pages  */
    public static final String NAMESPACE = "http://www.crank.org/springsecurity/tags";

    /**  Current instance of library. */
    public static final JsfCoreLibrary INSTANCE = new JsfCoreLibrary();

    /**
     * Creates a new JsfCoreLibrary object.
     * iterates through all the methods on the current class using reflection,
     * It then adds all JsfCoreLibrary static methods using the inherited method addFunction().
     */
    public JsfCoreLibrary() {
        super(NAMESPACE);
        
        //Register tag <yournamespace:springSecurityAuthorize/> in the J;sfCoreLibrary
        this.addTagHandler("springSecurityAuthorize", SpringSecurityAuthorize.class);


        try {
            Method[] methods = SpringSecurityTagUtils.class.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (Modifier.isStatic(methods[i].getModifiers())) {
                    this.addFunction(methods[i].getName(), methods[i]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Usage of newly registered tag `<springSecurityAuthorize/>` ###
A new secureListing.xhtml had to be made which is a copy of listing.xhtml with the exception of some addtional logic checks for authenticated users.
```
                       <!-- Only ROLE_SUPERVISOR can select rows -->
                        <s:springSecurityAuthorize ifAnyGranted="ROLE_SUPERVISOR">

                        <h:column>
                            <crank:commandLink id="selectAll${id}"
                                               onclick="toggleAllCheckBoxesGrouped('${parentForm}','${selectedRowsGroupId}',true);"
                                               toolTipText="Click to select all rows"
                                               immediate="${immediate}"
                                    >

                                <h:graphicImage id="selectAll${id}Img" value="${selectAllImg}" border="0"
                                                styleClass="${listingImageButtonClass}"/>
                            </crank:commandLink>
                            <crank:commandLink id="selectNone${id}"
                                               onclick="toggleAllCheckBoxesGrouped('${parentForm}','${selectedRowsGroupId}',false);"
                                               toolTipText="Click to un-select all rows"
                                               immediate="${immediate}"
                                    >
                                <h:graphicImage id="selectNone${id}Img" value="${selectNoneImg}" border="0"
                                                styleClass="${listingImageButtonClass}"/>
                            </crank:commandLink>
                            <crank:commandLink id="disableSelected${id}"
                                               rendered="${enableDelete}"
                                               entity="${crud}"
                                               action="deleteSelected"
                                               onclick="if (!confirm('Are you sure you want to delete all selected items?')) return false"
                                               toolTipText="Click to delete selected rows"
                                               reRender="${reRender},dataTable${id}"
                                               ajax="${false}"
                                               immediate="${immediate}"
                                    >
                                <h:graphicImage id="disableSelected${id}Img" value="${delAllImg}" border="0"
                                                styleClass="${listingImageButtonClass}"/>
                            </crank:commandLink>
                        </h:column>

</s:springSecurityAuthorize>
```