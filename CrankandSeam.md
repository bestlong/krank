# Introduction #

This is short tutorial on how to install and get crank working with Seam. I believe that this is the "minimum" you have to do to get Seam and Crank working together. The environment I use is JBoss 4.2,Seam 2.0, Facelets and Richfaces.


# Details #

These are the exact steps that I took to get Seam and Crank talking.

## **1:** ##
Use Seam Gen that comes with Seam 2.0 to create a New project (You can also use Jboss Tools in Eclipse, which is what I use, but the ant file used by Seam gen can be used by everyone.

## **2:** ##
Import the project into Eclipse

## **3:** ##
Tested that the Seam project worked by deploying to Jboss (using the 'explode/restart' ant target.

## **4:** ##
Create and Employee entity to test crank with
```
@Entity
public class Employee implements Serializable {
	
	@Id @GeneratedValue
	private Long id;
	private String firstname;
	private String lastname;
	private Boolean alive;
	
	public Long getId() {
	     return id;
	}

	public void setId(Long id) {
	     this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Boolean getAlive() {
		return alive;
	}

	public void setAlive(Boolean alive) {
		this.alive = alive;
	}
}
```


## **6:** ##
Install Crank,Spring and Dependencies Jars

You will need to add the following jars to your project
**crank-core.jar**crank-crud.jar
**crank-jsf-support.jar**crank-jsf-validation.jar
**crank-validation.jar**aspectjrt.jar
**aspectjweaver.jar**spring-javaconfig-1.0-m2.jar

The Seam gen project already has the Spring Jar

I then altered the build.xml ear target and add
```
<include name="lib/spring*.jar" />
				<include name="lib/crank*.jar" />
				<include name="lib/aspect*.jar" />
```
to the included files


## **7:** ##
Add applicationContext.xml to the project

```
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
           http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd">

    <bean class="com.test.crank.seam.CrankApplicationContext" />

    <bean
        class="org.springframework.config.java.process.ConfigurationPostProcessor" />
    <import resource="validationContext.xml" />

</beans>
```

## **8:** ##
Add validationContext.xml (You can find this in the example project)

## **9:** ##
> Modify web.xml to startup Spring and Crank... add the following
```
<listener>
		<listener-class>
			org.crank.web.servlet.CrankListener
		</listener-class>
	</listener>
    
	<listener>
		<listener-class>
			org.springframework.web.context.ContextLoaderListener
		</listener-class>
	</listener>

	<listener>
		<listener-class>
			org.springframework.web.context.request.RequestContextListener
		</listener-class>
	</listener>
```

## **10: ##
> ###**_Add variable resolver_**###
... this is where the magic happens... that gets Seam and Spring working together... add the folloing line to your faces-config.xml (in the**

&lt;application&gt;

 tag)

```
<variable-resolver>org.springframework.web.jsf.DelegatingVariableResolver</variable-resolver>
```


## **11:** ##
Copy the default images (to /images), javascript helper and stylesheets from the example project to your project

Ok, now Crank and Seam is installed so you should be able to follow other Wiki examples from here.

## **12:** ##
I want to use Seam managed persistance in crank, there is a easy way to do this.. just create the following class
```
public class SeamDAO<T, PK extends Serializable> extends
		GenericDaoJpaWithoutJpaTemplate<T, PK> {

	public SeamDAO() {
		super();
	}

	public SeamDAO(Class<T> type) {
		super(type);
	}

	@Override
	public EntityManager getEntityManager() {
		return (EntityManager) Component.getInstance("entityManager");
	}
}
```


And here is a Example CrankApplicationContext and facelets file that should work.

```
@Configuration(defaultLazy = Lazy.TRUE)
public abstract class CrankApplicationContext extends CrudJSFConfig {

	private List<CrudManagedObject> managedObjects;

	private Map<String, GenericDao> repos = new HashMap<String, GenericDao>();

	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
			createManagedObject(Employee.class);
		}
		return managedObjects;
	}

	@SuppressWarnings("unchecked")
	private void createManagedObject(Class<?> DaoClassName) {
		CrudManagedObject crudManagedObject = new CrudManagedObject(
				DaoClassName, SeamDAO.class);
		SeamDAO dao = new SeamDAO(DaoClassName);
		managedObjects.add(crudManagedObject);
		repos.put(crudManagedObject.getName(), dao);
	}
	
	@Bean(scope = DefaultScopes.SINGLETON)
	public Map<String, GenericDao> repos() {
		return repos;
	}
}
```

and we must then add the crank tags to our facelet file

```
<c:set var="controller" value="#{cruds['employee'].controller}" />
		<a4j:form id="employeeForm" enctype="multipart/form-data">
			<h:panelGroup rendered="${controller.showForm}">
				<crank:form crud="#{controller}"
					propertyNames="firstname,lastname,alive" parentForm="employeeForm">
				</crank:form>
			</h:panelGroup>
		</a4j:form>

		<a4j:form id="employeeListingForm">
			<crank:listing jsfCrudAdapter=""#{cruds['employee']}"
				propertyNames="firstname,lastname,alive"
				parentForm="employeeListingForm" reRender="employeeListingForm"/>
		</a4j:form>
```

and don't forget to add the helper.js and stylesheet to the template

```
<link rel="stylesheet" href="${request.contextPath}/stylesheet/crud.css"
	type="text/css" media="all" />
<script type="text/javascript"
	src="${request.contextPath}/js/helpers.js" />
```


Any questions/troubles, just ask in the forum. I will see if I can help.