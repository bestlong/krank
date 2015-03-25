#How do you configure external database resources with the crank framework.

# Introduction #
One of the questions is how to configure a data source for a crank application. The reason you may want to configure a datasource is that the application may run on multiple environments. Each environment may be configured differently. This will explain a couple of ways to do this.

# Persistence xml file #
Typically with the hibernate or jpa configuration the application uses a persistence.xml file in the META-INF directory. The crud-webapp-sample has one, and it looks something like this.

```
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence 
			http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
    version="1.0">

    <persistence-unit name="crank-crud-app" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.ejb.HibernatePersistence</provider>

        <class>org.crank.crud.model.Employee</class>
        ....

        <properties>
            <!-- Hibernate settings -->
            <property name="hibernate.connection.driver_class"
                      value="com.mysql.jdbc.Driver" />
            <property name="hibernate.connection.url"
                      value="jdbc:mysql://localhost:3306/crank_crud?autoReconnect=true" />
            <property name="hibernate.connection.username"
                      value="crank" />
            <property name="hibernate.connection.password"
                      value="crank" />
            <property name="hibernate.dialect"
                      value="org.hibernate.dialect.MySQLDialect" />
	    
	        <!-- you may want to change this provider in production -->
            <property name="hibernate.cache.provider_class"
                      value="org.hibernate.cache.HashtableCacheProvider" />

           ....
        </properties>
    </persistence-unit>

</persistence>
```

Typically you could have replacer values put into your properties for database connections in this file. There are a number of problems. The first is this requires the application to be built each time for each new environment to get the new replacement values. Good and not so good depending if each environment shares the same set-up. The other problem is anyone getting a hold of your compiled web application can search and open this file and read the user name and password to the database. The last problem is maybe you have operations support or you want to use some other mechanism to manage your datasource say through a JNDI tree. This is where CrudJSFConfig comes in handy.

## Controller with flair ##
The CrankCrudExampleApplicationContext has or is the controller that starts up a lot of the crank/spring configurations in the sample application. The first thing in any start-up application context controller with crank extend it from CrudJSFConfig. Then make a method called persistenceUnitName() and annotate it as a @Bean. The method should return a string which is the persistence unit name that is specified in the persistence.xml file.

```
package org.crank.sample;

import org.crank.config.spring.support.CrudJSFConfig;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.util.DefaultScopes;


@Configuration (defaultLazy=Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {

    @Bean
    public String persistenceUnitName() {
    	return "crank-crud-app"; 
    }

}
```

# Configuring application context #
The next step is configuring a data source outside of persistence.xml. We want to be able to remove this section of mark up from persistence.xml and have it configurable.

```
            <!-- Hibernate settings -->
            <property name="hibernate.connection.driver_class"
                      value="com.mysql.jdbc.Driver" />
            <property name="hibernate.connection.url"
                      value="jdbc:mysql://localhost:3306/crank_crud?autoReconnect=true" />
            <property name="hibernate.connection.username"
                      value="crank" />
            <property name="hibernate.connection.password"
                      value="crank" />
            <property name="hibernate.dialect"
                      value="org.hibernate.dialect.MySQLDialect" />
```

We can take the applicationContext.xml in WEB-INF and either add a import of resource or set of beans that can be configured during the build process. Here we will just take the applicationContext.xml and import another spring configuration which then will use either a BasicDataSource or JNDI lookup to a data source, that is configured in our appserver.

## applicationContext.xml ##
Modify the application context to import the new datasource-defs.xml
```
<beans xmlns="http://www.springframework.org/schema/beans"
	     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	     xmlns:aop="http://www.springframework.org/schema/aop"
	     xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
           http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd">

	 <bean class="org.crank.sample.CrankCrudExampleApplicationContext"/>
	 <bean class="org.crank.config.spring.support.CrudDAOConfig"/>
	 
	 <bean class="org.springframework.config.java.process.ConfigurationPostProcessor"/>
	 <import resource="/validationContext.xml"/>
         <import resource="/datasource-defs.xml" />

</beans>

```

## New datasource-defs.xml ##
Put this in the WEB-INF directory.
```
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

    <bean id="crankJndiDataSource" class="org.springframework.jndi.JndiObjectFactoryBean" lazy-init="true">
        <property name="jndiName" value="java:crankDS" />
    </bean>

    <bean id="crankDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close" lazy-init="true">
        <property name="driverClassName" value="${jdbc.driverClassName}"/>
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
        <property name="minIdle" value="5"/>
        <property name="maxIdle" value="15"/>
        <property name="initialSize" value="200"/>
        <property name="maxOpenPreparedStatements" value="100"/>
        <property name="maxWait" value="3000"/>
        <property name="timeBetweenEvictionRunsMillis" value="3600000"/>
    </bean>
    
    <bean class="org.crank.config.spring.support.CrudDAOConfig">
        <property name="dataSource" ref="${crank.build.config.bean.name}" />
    </bean>
</beans>
```

Using Maven for the build then in a profile setting all that needs to be added for each
profile build the property value to ${crank.build.config.bean.name} to switch between using a basic datasource in the build or a jndi lookup datasource.

# What Happened #
Basically, the CrudDAOConfig is setup with a method called setDataSource. If a dataSource is passed into this method CrudJSFConfig will then use that passed in datasource. If it does not exist then when CrudJSFConfig is instantiated it will use a basic data source, which would normally be configured in persistence.xml.

This snippet highlights how the CrudDAOConfig is set up in the spring context.
```
 <bean class="org.crank.config.spring.support.CrudDAOConfig">
        <property name="dataSource" ref="myCrankyDatasource" />
    </bean>
```