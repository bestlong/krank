# Overview #

Standard Disclaimer: If you are new to Crank, this document may not be for you, please see CrankCrudTutorial, GettingStartedWithCrank and CrankCrudDesignDocs first, then come back here.

This document described the `selectMany` tag. The `selectMany` tag is used to edit a many to many relationship. For example let's say that our `Employee` object had a many to many relationship to our `Role` object. How do you render this? Well you could use a list of check-boxes (which is what we did in Presto). But, what happens if there are not a few `Role`s but thousands of `Role`s. It would be nice if we could have an Ajax enabled, filterable, paginateable listing. This would allow you to handle basic cases and complex cases with the same component.

Enter stage left, the `selectMany` tag (and its underlying controllers) provide an Ajax enabled, filterable, paginateable listing that allows us to manage a many to many relationship.

Here is what the `selectMany` tag looks like when it is closed:

![http://krank.googlecode.com/svn/wiki/img/selectManyClosed.png](http://krank.googlecode.com/svn/wiki/img/selectManyClosed.png)

Here is what the `selectMany` tag looks like when it is open:

![http://krank.googlecode.com/svn/wiki/img/selectManyOpen.png](http://krank.googlecode.com/svn/wiki/img/selectManyOpen.png)

There are no screen refreshes as we are using Ajax for all operations. It has a very rich client feel to it.


Here is our task list:
  1. Add the many to many relationship to `Employee` to `Role`
  1. Configure the controllers needed for the `selectMany` tag
  1. Use the `selectMany` tag on the `Employee` form.


## Add the many to many relationship to `Employee` to `Role` ##
First we make the changes to `Employee` as follows:
```
public class Employee extends Person {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

	...
    @ManyToMany()
    @JoinTable(name="EMPLOYEE_ROLE", 
            joinColumns={@JoinColumn(name="FK_EMPLOYEE_ID")},
            inverseJoinColumns={@JoinColumn(name="FK_ROLE_ID")})	    
    private Set<Role> roles = new HashSet<Role>();
	
```

The `Employee.readPopulated` named query needs to be updates as follows:

```
@Entity
@NamedQueries( {
        @NamedQuery( name = "Employee.readPopulated", 
                query = "select distinct employee from Employee employee " +
                        "left outer join fetch employee.department " + 
                        "left outer join fetch employee.tasks " +
                        "left outer join fetch employee.contacts " +
                        "left outer join fetch employee.directReports " +
                        "left outer join fetch employee.roles " +
                        "where employee.id=?" ), ...

} )
public class Employee extends Person {
...
```



We added a fetch clause to `employee.roles` as follows:
```
left outer join fetch employee.roles 
```

Note that you can alternatively make the `Role` relationship eager, but the JPA QL query allows us more flexibility in loading strategies. (A third option that we are exploring is a Seam style conversation scope either using Seam or implementing our own using Spring scope support. This would simplify the framework a bit, but is not a top priority at this point.)

This completes the changes we needed to make to the `Employee` class.

## Configure the controllers needed for the `selectMany` tag ##

Now we have to configure the controller (backing bean) for the `selectMany` tag:

```
@Configuration (defaultLazy=Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {

	@Bean (scope = DefaultScopes.SINGLETON)    
    public List<CrudManagedObject> managedObjects() {
    	if (managedObjects==null) {
	    	managedObjects = new ArrayList<CrudManagedObject>();
			...
	        managedObjects.add( new CrudManagedObject(Role.class, RoleDAO.class) ); //added this
    	}
    	return managedObjects;
		
	}    

	...
    @SuppressWarnings("unchecked")
    @Bean(scope = DefaultScopes.SESSION, aliases = "empCrud") //added new alias
	public JsfCrudAdapter employeeCrud() throws Exception {
		...
	}
	
    @ExternalBean
    abstract JsfCrudAdapter<Employee, Long> empCrud();

    @Bean(scope = DefaultScopes.SESSION)
	public JsfSelectManyController<Role, Long> employeeToRoleController() throws Exception {
		JsfSelectManyController<Role, Long> controller = 
		 new JsfSelectManyController<Role, Long>(Role.class, "roles", 
		                                         paginators().get("Role"), 
												 empCrud().getController()); 
    	return controller;
    }
	
	
```

Hopefully by now most of this is a reoccurring theme. If you are using a CRUD tag, you need to register a controller (JSF backing bean). The steps could be summarized as follows:

  1. Register the domain object (Role.class) as a managed object, this will create the bulk of the controllers needed by default.
  1. Tweak or create controllers specific to a tag.


So based on the above list, we have a new domain object called `Role` that we need to register as follows:

```
    public List<CrudManagedObject> managedObjects() {
            ...
	        managedObjects.add( new CrudManagedObject(Role.class, RoleDAO.class) ); //added this
            ...
            return managedObjects;

    }    

```

Next we expose the configured `employeeCrud`, which was configured with detail controllers and such in the last few examples. By exposing it as an alias then catching the alias with a @ExternalBean as follows:

```

	//Notice we expose this as the alias empCrud aliases = "empCrud"
    @SuppressWarnings("unchecked")
    @Bean(scope = DefaultScopes.SESSION, aliases = "empCrud") //<-------- added new aliases
	public JsfCrudAdapter employeeCrud() throws Exception {
		...
	}

	//Now we catch this with the @ExternalBean
    @ExternalBean
    abstract JsfCrudAdapter<Employee, Long> empCrud();
	

```

Now you may wonder, why would we expose a bean name only to catch it in the same class. The answer is if we used `employeeCrud` method directly then we would not get the proxied version. We want the same session to use the same `employeeCrud` so it has the same state and can listen to events. Thus we use the empCrud to get the proxied version of `employeeCrud` to configure our new controller as follows:

```
    @Bean(scope = DefaultScopes.SESSION)
	public JsfSelectManyController<Role, Long> employeeToRoleController() throws Exception {
		JsfSelectManyController<Role, Long> controller = 
		 new JsfSelectManyController<Role, Long>(Role.class, "roles", 
		                                         paginators().get("Role"), 
		                                         empCrud().getController()); 
    	return controller;
    }

```

The `JsfSelectManyController` is the brains behind the `selectMany` tag. It is a adapter/facade over the crank crud classes that know how to edit a many to many relationship (`Set`, `Map` or `List`) in a reliable manner.

Notice that the `JsfSelectManyController` is a mooching aggregator what some of us would call a collaborator. It colloborates with the paginator backing bean and the crud controller.

It needs the paginator to show a listing of `Role`s to select.

## Add the `crank:selectMany` tag to the `Employee` `form.xhtml` ##
Lastly we need to add the `crank:selectMany` to the form.xthml as follows:

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
		<c:set var="crud" value="${employeeCrud.controller}" />
		<c:set var="taskDetailController" value="${crud.children.tasks}" />
		<c:set var="contactDetailController" value="${crud.children.contacts}" />
		<c:set var="directReportDetailController"
			value="${crud.children.directReports}" />


		<crank:crudBreadCrumb crud="${crud}" />

		<span class="pageTitle">Employee Entry</span>

		<a4j:form id="employeeForm" enctype="multipart/form-data">

			<crank:form crud="${crud}" parentForm="employeeForm"
				propertyNames="firstName,lastName,department,description,file,age,numberOfPromotions,active,status,dob">

				<crank:compositePanel entity="${crud.entity.address}" name="address"
					propertyNames="line_1,line2,zipCode" />

				<crank:detailListing
					detailController="${directReportDetailController}" 
					propertyNames="firstName,lastName,description,age,numberOfPromotions" />

				<crank:detailListing detailController="${taskDetailController}"
					propertyNames="name,startDate,endDate,complete" />

				<crank:detailListing detailController="${contactDetailController}"
					propertyNames="name,phone" />

				<crank:selectMany jsfSelectManyController="${employeeToRoleController}" 
					propertyNames="name"
					parentForm="employeeForm"/>
					
			</crank:form>
		</a4j:form>
	</ui:define>
</ui:composition>
</html>

```

Here is the `selectMany` isolated as follows:

```
				<crank:selectMany jsfSelectManyController="${employeeToRoleController}" 
					propertyNames="name"
					parentForm="employeeForm"/>

```

Notice that the we can pass a list of property names to show up in the listing. Let's say the `Role` had an enum called  type (`RoleType`) and a description (`String`), and you wanted the end user to be able filter on these. Also it might be nice when picking a `Role` if the end user could see a description for that `Role`. You would change the tag as follows:

```
				<crank:selectMany jsfSelectManyController="${employeeToRoleController}" 
					propertyNames="name,type,description"
					parentForm="employeeForm"/>

```

One final point, in order for this to work the `Role` object needs to have `equals` and `hashCode` implemented. Here is a quick and dirty implementation as follows:

```
public class Role {
	...
	public boolean equals(Object other) {
		if (!(other instanceof Role)) {
			return false;
		}
		Role otherRole = (Role)other;
		if (otherRole.id!=null && this.id  == null) {
			return false;
		}
		if (this.id !=null && otherRole.id == null) {
			return false;
		}
		
		if (this.id != null) {
			return this.id.equals(otherRole.id);
		} else {
			return this.name.equals(otherRole.name);
		}
		
	}
	
	public int hashCode() {
		return ("" + id + ":" + name).hashCode();
	}

```

If you are using Eclipse there are several plugins that will genearate `equals` and `hashCode` for you. If you are using IntelliJ, equals and hashCode generation are built it. The above works for our purposes but is not the best implementation.