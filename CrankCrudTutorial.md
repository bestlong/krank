# Introduction #

There comes a time when writing a framework when you must explain to others how to use a framework. This is that time.

Crank CRUD is part of the Crank project. It allows one to quickly create (C)reate-(R)ead-(U)pdate-(D)elete (CRUD) listings.

The Crank CRUD that currently works builds on top of JSF, Facelets, Spring and JPA. Many of the base classes of Crank CRUD do no rely on JSF or Spring or JPA so in theory it could be ported to other technology stacks (Spring MVC, JSP tagfiles, iBatis, EJB3, etc.). For this tutorial we will focus on JSF, Facelets, Spring and JPA.

The JSF/Facelets support makes heavy use of Ajax4JSF so the forms and listings are Ajax enabled with no additional effort. This includes text auto completion, Ajax pagination, master detail managment, etc.

Speaking of master detail managment, Crank provides support to work with object relationships via master detail forms, drop down support, text completion support and more.

Instead of trying to explain these features, let's demonstrate how to use them. Working sample code is always better than a verbose description.

## Crank CRUD Tutorial ##

To start using Crank CRUD run its maven archetype. You can find directions to run the maven archetype here (CrankCrudMavenArchetype) (note this does not exist yet). Or you could use the sample project as a starting point. The sample project is called crank-crud-webapp-sample and it can be found in the svn repository.

## Configuration ##


Crank Crud uses the [Spring Java configuration module](http://www.springframework.org/javaconfig). If you have not used the [Spring Java configuration module](http://www.springframework.org/javaconfig) and you are a Spring user (and who isn't these days), then you must give [Spring Java configuration module](http://www.springframework.org/javaconfig) a try. It allows you have the benefits of Spring without all of the hassle of XML.


First we must register an Configuration object.

#### Setting up `CrankCrudExampleApplicationContext` ####
```
@Configuration (defaultLazy=Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {
```


Notice that the configuration object uses the `@Configuration` annotation (`org.springframework.config.java.annotation.Configuration`). The `@Configuration` annotation is from the Spring Java configuration module.

Also note that the `CrankCrudExampleApplicationContext` subclasses the `CrudJSFConfig`. The `CrudJSFConfig` configures all of the `crudControllers` (backing beans for data entry forms), `paginators` (backing beans for listings), `selectItemGenerators` (backing beans for drop down and ajax text completion), and JSF `converters` that are needed to make you object model editable. To learn more about the `crudControllers`, `paginators`, `selectItemGenerators` and `converters` see CrankCrudDesignDocs.

To manage an object in a CRUD listing (or as a drop down), you must let the base class `CrudJSFConfig` know that it exists. You do this by registering the managed objects as follows:

```
@Configuration (defaultLazy=Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {

    private static List<CrudManagedObject> managedObjects;
    
    @Bean (scope = DefaultScopes.SINGLETON)    
    public List<CrudManagedObject> managedObjects() {
    	if (managedObjects==null) {
	    	managedObjects = new ArrayList<CrudManagedObject>();
	        managedObjects.add( new CrudManagedObject(Employee.class, EmployeeDAO.class) );
			managedObjects.add( new CrudManagedObject(Employee.class, DepartmentDAO.class) );
    	}
        return managedObjects;
	}    
```

The above manages registers the `Employee.class` as a managed object. If you have not defined a DAO yet, then you just pass a `null` for the second argument of the `CrudManagedObject` as follows:


```
@Configuration (defaultLazy=Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {

    private static List<CrudManagedObject> managedObjects;
    
    @Bean (scope = DefaultScopes.SINGLETON)    
    public List<CrudManagedObject> managedObjects() {
        if (managedObjects==null) {
            managedObjects = new ArrayList<CrudManagedObject>();
            managedObjects.add( new CrudManagedObject(Employee.class, null) );
			managedObjects.add( new CrudManagedObject(Department.class, null) );
        }
        return managedObjects;
    }    
```

Note the `new CrudManagedObject(Employee.class, null)` replaces the `new CrudManagedObject(Employee.class, EmployeeDAO.class)`. If you do not pass a DAO then a GenericJpaDAO will be created for you. See UsingDAO for more details. (Note UsingDAO should show up as a link, there is a WIKI entry for UsingDAO).

Once the Employee object is managed then we can create a listing for it as follows (listing.xhtml):

#### /webapp/pages/crud/employee/listing.xhtml ####
```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:ui="http://java.sun.com/jsf/facelets"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:a4j="https://ajax4jsf.dev.java.net/ajax"
	xmlns:c="http://java.sun.com/jstl/core"
	xmlns:crank="http://www.googlecode.com/crank"
	xmlns:t="http://myfaces.apache.org/tomahawk">
	
<ui:composition template="/templates/layout.xhtml">
	<ui:define name="content">
        <c:set var="crud" value="${cruds.employee.controller}"/>

        <crank:crudBreadCrumb crud="${crud}"/>

        <span class="pageTitle">Employee Entry</span>
	
        <a4j:form id="employeeForm">
            <crank:form crud="${crud}"
                propertyNames="firstName,lastName,department,description,age,numberOfPromotions"/>
        </a4j:form>
	</ui:define>
</ui:composition>
</html>
```

If the above makes no sense at all, then you probably are new to JSF or Facelets or both. If this is the case, before you continue, you are strongly advised to read about JSF and Facelets, check out GettingStartedWithCrank for a list of articles that will get you up to speed with JSF and Facelets (written by the same guy who wrote a fair bit of Crank).

The above uses a page template called /templates/layout.xhtml. Most of the above that we care about is defined in the `content` definiton. Here are the interesting bits:

```
        <c:set var="crud" value="${cruds.employee.controller}"/>

        <crank:crudBreadCrumb crud="${crud}"/>

        <span class="pageTitle">Employee Entry</span>
	
        <a4j:form id="employeeForm">
                <crank:form crud="${crud}"
                    propertyNames="firstName,lastName,department,dob,active,description,age,numberOfPromotions"/>
        </a4j:form>
```

Let's step through this step by step. The first line as follows:

```
		<c:set var="crud" value="${cruds.employee.controller}"/>
```

This pulls the employee CRUD backing bean out of the controller. But you may ask where was `cruds` defined. The cruds object is a `Map` of `JsfCrudAdapter`(s). The `JsfCrudAdapter` has a property called controller which is a `CrudControllerBase`.

The next line defines the bread crumb trail at the top of the page as follows:
```
		<crank:crudBreadCrumb crud="${crud}"/>
```

Then we define the title for the page:
```
	    <span class="pageTitle">Employee Entry</span>

```

Next we use an `a4j:form`:

```
		<a4j:form id="employeeForm">
				...
		</a4j:form>

```

The `a4j:form` is from the Ajax4JSF project. It allows you to send ajax requests from the JSF form. This will allow us to have master detail section on the form, etc.

Inside of the `a4j:form` we use our own composition component as follows:

```
        <crank:form crud="${crud}"
            propertyNames="firstName,lastName,department,dob,active,description,age,numberOfPromotions"/>
```

The above states that we will have a `firstName` property, a `lastName` property, a `department` property, a 'dob' property, an `active` property,a `description` property, an `age` property, and a `numberOfPromotions` on the form.

The interesting thing is that `firstName`, `lastName`, `description` are Strings (java.lang.String) and `age` and `numberProperty are Integers, `dob` is a Date, `active` is a boolean, while department is a `@OneToMany` relationship. They all get rendered correctly.

Let's examine the Employee as follows:

```
public class Employee implements Serializable{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    ...
    @Column( nullable = false, length = 32 )
    private String firstName;
    private String lastName;


    private boolean active;

    private int age;

    @Column (nullable=false)
    private Integer numberOfPromotions;

    private Date dob;
    
    @ManyToOne( )
    private Department department;
    ...

```

Now we have form, let's add a listing as follows:

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
	>
<ui:composition template="/templates/layout.xhtml">
	<ui:define name="content">
	
	
	    <span class="pageTitle">Employee Listing</span>


		<a4j:form id="employeeListingForm">
				<crank:listing  jsfCrudAdapter="${cruds.employee}" 
					propertyNames="firstName,lastName,status,active,dob,age,department.name"
					parentForm="employeeListingForm"/>
		</a4j:form>
	</ui:define>
</ui:composition>
</html>

```

The above adds a listing that allows `firstName,lastName,status,active,dob,age,department.name` properties to be sortable and filterable. Notice that not only properties can be sortable but also properties as properties like `employee.department.name`. See the screen shots in the design doc at CrankCrudDesignDocs.

#### Adding a Composite object (Address to Employee) @Embedded ####
One of the goals of Crank CRUD is to be able to edit from GUI perspective anything you can configure in JPA. So let's say that you add an `Address` object to `Employee` as follows:

```
@Embeddable
public class Address {

    private String line_1;
    private String line2;
    private String zipCode;

	public String getZipCode() {
        return zipCode;
    }
    public void setZipCode( String zipCode ) {
        this.zipCode = zipCode;
    }
	public String getLine_1() {
		return line_1;
	}
	public void setLine_1(String line_1) {
		this.line_1 = line_1;
	}
	public String getLine2() {
		return line2;
	}
	public void setLine2(String line2) {
		this.line2 = line2;
	}
    
}
```

`Employee` has a composite relationship with `Address`.

```
public class Employee  implements Serializable {

	...
    @Embedded
    private Address address;

	...

```



Then you add a `crank:compositePanel' inside of the `crank:form`.

```
        ...
				<crank:form crud="${crud}"  parentForm="employeeForm"
					propertyNames="firstName,lastName,department,dob,active,description,age,numberOfPromotions">

					<crank:compositePanel entity="${crud.entity.address}" 
					                      name="address" propertyNames="line_1,line2,zipCode"/>
 	
				</crank:form>
```

You can change the listing to sort on the `employee.address.zip` as follows:
```
				<crank:listing  jsfCrudAdapter="${cruds.employee}" 
					propertyNames="firstName,lastName,status,active,dob,age,department.name,address.zip"
					parentForm="employeeListingForm"/>
```

Now `address.zip` in the `Employee` listing is sortable and filterable. Thus `@ManyToOne` is sortable as well as the `@Embedded` property.


#### Master/Detail form, Adding a List of Task object to Employee (@OneToMany) ####

Crank CRUD can manage master details using a composition component called `crank:detailListing`.

Let's say we add a `List` of `Task`s to `Employee` as follows:

```
public class Task implements Serializable {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )   
    private Long id;
    private Date startDate;
    private Date endDate;
    private String name;
    private String description;
    private boolean complete;

...
public class Employee implements Serializable {
	...
    @OneToMany( cascade = CascadeType.ALL )
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})    
    private Set<Task> tasks = new HashSet<Task>();

	...tasks getter/setter omitted
	
    public void addTask( Task task ) {
        this.tasks.add( task );
    }

    public void removeTask( Task task ) {
        this.tasks.remove( task );
    }
	
}
```


The `Employee.readPopulated` named query needs to be updates as follows:

```
@NamedQueries( {
        @NamedQuery( name = "Employee.readPopulated", 
                query = "select distinct employee from Employee employee " +
                        "left outer join fetch employee.department " + 
                        "left outer join fetch employee.tasks " +
                        "where employee.id=?" )

} )
public class Employee ...

```

We added a fetch clause to `employee.tasks` as follows:
```
left outer join fetch employee.tasks
```

This is needed so we can manage the collection of tasks in the employee form.

Next we need to add `employeeCrud` to `CrankCrudExampleApplicationContext` as follows:

```
    @SuppressWarnings("unchecked")
    @Bean (scope = DefaultScopes.SESSION) 
    public JsfCrudAdapter employeeCrud() throws Exception {
        JsfCrudAdapter adapter = cruds().get( "Employee");
        adapter.getController().addChild( "tasks", new JsfDetailController(Task.class));
        return adapter;
    }

```

The base `CrudJSFConfig` configures a map of `JsfCrudAdapter`s (adapter). The adapter has a `controller` property which is a `CrudController`. A `CrudController` has a map of `DetailController`s which can have a map of `DetailController`s ad infinitum (see CrankCrudDesignDocs for more details). The above adds a `JsfDetailController` (a subclass of `DetailController`) that manages the `Task` List. A `DetailController` can map Lists, Arrays, Sets or Maps. The `DetailController` created above (and added to the employeeCrud.controller) is the backing bean for the `crank:detailListing`.

Now add a `crank:detailListing` as follows:

```

		<c:set var="crud" value="${employeeCrud.controller}" />
		<c:set var="taskDetailController" value="${crud.children.tasks}" />
		...
		
			<crank:form crud="${crud}" parentForm="employeeForm"
				propertyNames="firstName,lastName,department,description,age,numberOfPromotions,status,dob">

				<crank:compositePanel entity="${crud.entity.address}" name="address"
					propertyNames="line_1,line2,zipCode" />

				<crank:detailListing detailController="${taskDetailController}"
					propertyNames="name,startDate,endDate,complete" />

			</crank:form>

```

The `taskDetailController` is defined as `crud.children.tasks`. The `crud.children.tasks` was setup in the `CrankCrudExampleApplicationContext`. The `taskDetailController` is the backing bean for the `crank:detailListing` composition component.

You may recall that the `Employee` class has a `addTask` method and a `removeTask` method as follows:

```
public class Employee implements Serializable {
	...
    @OneToMany( cascade = CascadeType.ALL )
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})    
    private Set<Task> tasks = new HashSet<Task>();

	...tasks getter/setter omitted
	
    public void addTask( Task task ) {
        this.tasks.add( task );
    }

    public void removeTask( Task task ) {
        this.tasks.remove( task );
    }
	
}
```

The `taskDetailController` finds these methods and calls them when you hit the add link for the detailContoller and the remove icon (respectively, logically). This is done in case the `addTask` maintains the relationship (child.setParent). If you don't have these methods, by default the `DetailController`s `relationshipManager` will try to add the tasks directly to the Map, List or Set property, e.g., Employee.tasks.

Thus for example we had a Set of Contacts as follows:

```
@Entity
public class ContactInfo implements Serializable {
    
    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Long id;
    
    private String name;
    ...

public class Employee implements Serializable {
	...
    @OneToMany( cascade = CascadeType.ALL )
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})    
    private Set<ContactInfo> contacts = new HashSet<ContactInfo>();

	//no addContact or removeContact

```

If there is no `addContact` method or `removeContact` method then the `relationshipManager` will add/remove directly to the `contacts` `Set`.

The `Employee.readPopulated` named query needs to be updates as follows:

```
@NamedQueries( {
        @NamedQuery( name = "Employee.readPopulated", 
                query = "select distinct employee from Employee employee " +
                        "left outer join fetch employee.department " + 
                        "left outer join fetch employee.tasks " +
                        "left outer join fetch employee.contacts " +
                        "where employee.id=?" )

} )
public class Employee ...

```

We added a fetch clause to `employee.contacts` as follows:
```
left outer join fetch employee.contacts
```

This is needed so we can manage the collection of `contacts` in the employee form.


This is it for now. We are giong to add other entries for specific features. You have a basic introduction to Crank CRUD. You should be able to write some CRUD listings.