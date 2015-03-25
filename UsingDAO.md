# Using the JPA DAO #

## Introduction ##

This document describes how to use Crank's JPA DAO support.

## Features of JPA DAO covered in this document ##

  * JPA support for CRUD operations Create, Read, Update, Delete
  * Support for defining no-code finder methods
  * Support for Criteria API/DSL
  * Support for easy to use finder methods

## Getting Started with DAO ##

The DAO support is based on the [Spring JPA support](http://www.springframework.org/docs/reference/orm.html). There is some talk on the Crank team for supporting similar features for non-Spring developers, but for now both of you will have to use Spring to use this DAO support.

Everything that applies to `org.springframework.orm.jpa.support.JpaDaoSupport` also applies to Cranks DAO support (`org.crank.crud.GenericDaoJpa`) as it subclasses `JpaDaoSupport` from Spring. Note that all of our DAO objects implement `org.crank.crud.GenericDao`.

Steps to using Crank JPA DAO support
  1. Setup Spring environment
  1. Configure DAO object
  1. Start using the DAO

## Step 1 Setup Spring environment ##


Here is how the example projects sets up the Spring environment:

Main context
```
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">


    <import resource="classpath:spring/dao-beans.xml" />
    <import resource="classpath:spring/resource-defs.xml" />

</beans>
```

Resource context
```
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalEntityManagerFactoryBean">
        <property name="persistenceUnitName" value="crank-crud-test" />
    </bean>

    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="entityManagerFactory" />
    </bean>

</beans>
```

See [Spring JPA support](http://www.springframework.org/docs/reference/orm.html) for more details.

Of course you will have to setup a persistence.xml file as well refer to JPA documents for how to setup a persistence.xml file. For this project, the persistence.xml file is as follows:

/META-INF/persistence.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence 
			http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
    version="1.0">

    <persistence-unit name="crank-crud-test" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.ejb.HibernatePersistence</provider>

        <class>org.crank.crud.test.model.Employee</class>

        <properties>
            <!-- Hibernate settings -->
            <property name="hibernate.connection.driver_class"
                      value="com.mysql.jdbc.Driver" />
            <property name="hibernate.connection.url"
                      value="jdbc:mysql://localhost:3306/crank-crud?autoReconnect=true" />
            <property name="hibernate.connection.username"
                      value="crank" />
            <property name="hibernate.connection.password"
                      value="sleeplessintucson" />
            <property name="hibernate.dialect"
                      value="org.hibernate.dialect.MySQLDialect" />
	    
	        <!-- you may want to change this provider in production -->
            <property name="hibernate.cache.provider_class"
                      value="org.hibernate.cache.HashtableCacheProvider" />
	    
            <!-- Print SQL to stdout -->
            <property name="hibernate.show_sql" value="true" />
            <!--  create non-existing tables automatically  -->
            <property name="hibernate.hbm2ddl.auto" value="update" />
        </properties>
    </persistence-unit>

</persistence>
```

## Step 2 Configure DAO object ##

In order to use the DAO object, you must configure it. The DAO object needs to know which entity it is managing; thus, you need to pass it an Entity class as follows:

DAO Beans context file
```
	<bean id="genericDao" class="org.crank.crud.GenericDaoJpa">
		<property name="type" value="org.crank.crud.test.model.Employee"/>
		<property name="entityManagerFactory" ref="entityManagerFactory" />
	</bean>
```

Next inject the genericDao through a setter method as follows (using normal Spring injections):

```
    private GenericDao<Employee, Long> genericDao;

    public void setGenericDao( final GenericDao<Employee, Long> baseJpaDao ) {
        this.genericDao = baseJpaDao;
    }

```

Notice that we define the genericDao as a DAO that works with Employees who have a PK that is a Long.

## Step 3 Start using the DAO ##

Once you have the DAO defined and injected, you can start using it.

You can read objects by id:
```
        Employee employee = (Employee) genericDao.read( 1L );

```

You can create new objects:
```
      genericDao.create(new Employee("Rick", "Hightower"));
```

You can update objects (not needed if in same transaction):
```
        Employee employee = (Employee) genericDao.read( 1L );
        ... //Present to user
        employee.setFirstName("Rick");
        genericDao.update( employee );
```

JPA does dirty checking so the update is not needed if run in the same transaction.

You can also delete objects as follows:

You can delete an object as well:
```
        Employee employee = (Employee) genericDao.read( 1L );
        genericDao.delete( employee.getId() );
```

There are helper methods for finding objects as follows:

```
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("firstName", "Rick");
        List <Employee> employees = genericDao.find( params, new String[] { "firstName" });

```

The above finds and employee with the first name of Rick. If there are more than one employees named Rick, the list will be ordered by firstName.

You can also search by related objects. Below we search for employees who are in the department named Engineering (where Employee has an related entity called Department).
```
List<Employee> employees = genericDao.find("department.name", "Engineering");
```

The above is shorthand for the following:

```
import static org.crank.crud.criteria.Example.*;
import static org.crank.crud.criteria.Comparison.*;
import static org.crank.crud.criteria.Group.*;
...
...
    	List<Employee> employees = genericDao.find(eq("department.name", "Engineering"));

```

Notice the use of the `eq` method. We provided a Criteria DSL where you can any number of criterion.

Here are some examples of using the Criteria DSL
```
    	employees = genericDao.find(
                                  eq("department.name", "Engineering"),
                                  or( startsLike("firstName", "Rick"),like("firstName", "Ri"))
    				);
    	
    	employees = genericDao.find(
                                or (
                                      eq("department.name", "Engineering"), 
                                      like("firstName", "Ri")
                                )
                     );

    	employees = genericDao.find( in("age", 1, 2, 3, 4, 5, 6, 40) );

```

The Criteria DSL has all of the operators that you expect, e.g., and, or, eq, ne, gt, ge, etc.


The Criteria DSL even has support for Query by Example (QBE):

```
       Employee employee = new Employee();
       employee.setActive(true);
       employee.setAge(40);
       employee.setFirstName("Rick");
       employee.setLastName("Rick");
		
       List<Employee> employees = genericDao.find(
                       like(employee).excludeProperty("lastName"));

    	
```

You can even refer to nested QBE objects:

```
    employee = new Employee();
    employee.setFirstName("Ric");
    employee.setAge(0);
    employee.setActive(true);
    Department department = new Department();
    department.setName("Eng");
    employee.setDepartment(department);

    employees = genericDao.find(like(employee));

```

See that the above uses an Employee that has a Department. Both the Employee data and the Department data will be used in the geneated query.

## Using DAO methods to Named Queries mapping ##


In addition to using the API provided you can extend the API with your own custom Named Queries.

To this you need to:
  1. Define an interface with finder methods that start with "find"
  1. Define named queries that match the name of your finder methods
  1. Use the DaoFactory to configure a Generic DAO
  1. Use the new finder methods

### Define an interface with finder methods that start with "find" ###
```
package org.crank.crud.test.dao;

import java.util.List;

import org.crank.crud.GenericDao;
import org.crank.crud.test.model.Employee;

public interface EmployeeDAO extends GenericDao<Employee, Long>{
	List<Employee> findEmployeesByDepartment(String deptName);
}
```

Notice that this interface extends GenericDao.

### Define named queries that match the name of your finder methods ###
```
package org.crank.crud.test.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries( {
	@NamedQuery(name="Employee.findEmployeesByDepartment", query="from Employee employee where employee.department.name=?")
	
})
public class Employee {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )	
	private Long id;
    ...
```

Notice the name of the named query is findEmployeesByDepartment and this matches the name in the EmployeeDAO interface.

### Use the DaoFactory to configure a Generic DAO ###
```
	<bean id="daoFactory"
		class="org.crank.crud.GenericDaoFactory"
		abstract="true">
		<constructor-arg ref="transactionInterceptor"></constructor-arg>
		<property name="entityManagerFactory"
			ref="entityManagerFactory" />
	</bean>

	<bean id="genericDao" parent="daoFactory">
		<property name="interface">
			<value>org.crank.crud.test.dao.EmployeeDAO</value>
		</property>
		<property name="bo" >
			<value>org.crank.crud.test.model.Employee</value>
		</property>
	</bean>	

    <bean id="transactionInterceptor" class="org.springframework.transaction.interceptor.TransactionInterceptor">
        <constructor-arg index="0" ref="transactionManager" />
        <constructor-arg index="1">
            <bean class="org.springframework.transaction.annotation.AnnotationTransactionAttributeSource"></bean>
        </constructor-arg>
    </bean>
        
```

Notice that the daoFactory only has to be defined once per project while the genericDao is defined per DAO object (per entity) (perhaps a better name for this example would be employeeDao instead of genericDao).



### Use the new finder methods ###
```
    	EmployeeDAO employeeDAO = (EmployeeDAO) this.genericDao;
    	List<Employee> employees = employeeDAO.findEmployeesByDepartment("Engineering");

```

The first one may be somewhat difficult to setup, but your next finder method is a mere matter of adding it to the DAO interface and then creating the named query.