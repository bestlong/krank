# Overview #

If you are new to Crank, this document may not be for you, please see CrankCrudTutorial, GettingStartedWithCrank and CrankCrudDesignDocs first, then come back here.

This document described a more advanced case, we create a self referential entity called `Employee`. `Employee` now has a property called `directReports` (of type `Employee`) and a property called `manager`. We want to change the listing so that only `Employee` who do not have `manager`s show up on the listing.

Here is our task list:
  1. Update `Employee` object, add `directReports` and manager property
  1. Changed the `Employee` listing to only show `Employee`'s who do not have managers, i.e., managers are null
  1. Add the `crank:detailListing` tag to the Employee form.xhtml.

## Update `Employee` object, add `directReports` and manager property ##
First we make the changes to `Employee` as follows:
```
public class Employee extends Person {

	...
	    
    @ManyToOne( )
    private Employee manager;
    

    @OneToMany( cascade = CascadeType.ALL )
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})    
    private Set<Employee> directReports = new HashSet<Employee>();
	
	...//getter/setters omitted
	
	public void addDirectReport(Employee employee) {
    	employee.setManager(this);
    	directReports.add(employee);
    }

    public void removeDirectReport(Employee employee) {
    	directReports.remove(employee);
    }

        
```

The `Employee.readPopulated` named query needs to be updates as follows:

```
@NamedQueries( {
        @NamedQuery( name = "Employee.readPopulated", 
                query = "select distinct employee from Employee employee " +
                        "left outer join fetch employee.department " + 
                        "left outer join fetch employee.tasks " +
                        "left outer join fetch employee.contacts " +
                        "left outer join fetch employee.directReports " +
                        "where employee.id=?" )

} )
public class Employee extends Person {

```

We added a fetch clause to `employee.directReports` as follows:
```
left outer join fetch employee.directReports
```


This completes the changes we needed to make to the `Employee` class.

## Changed the `Employee` listing to only show `Employee`'s who do not have managers, i.e., managers are null ##

Now we have to configure the listing so it will only show `Employee`s who do not have managers. In order to do this we need to configure the `paginator` of the `JSFCrudAdapter`. The `paginator` property is a `FilteringPaginator` so it has a list of `Criteria` that we can configure (you may want to refer to what the CrankCrudDesignDocs says about `FilteringPaginator` if you have not already).

Let's configure the Criteria as follows:

```

        JsfCrudAdapter adapter = cruds().get( "Employee");
        
        adapter.getPaginator().addCriterion(Comparison.eq("manager",null));
        adapter.getPaginator().filter();

```

The above conifgures that backing bean for the listing to exclude `Employee`'s who `manager`'s are not `null`.

Now we configure a `DetailController` for `directReports` as follows:

```
        JsfDetailController directReports = new JsfDetailController(Employee.class);
        RelationshipManager relationshipManager = directReports.getRelationshipManager();
        relationshipManager.setChildCollectionProperty("directReports");
        relationshipManager.setAddToParentMethodName("addDirectReport");
        relationshipManager.setRemoveFromParentMethodName("removeDirectReport");
        
        
        adapter.getController().addChild( "directReports", directReports);

```

Putting it all together yields (`CrankCrudExampleApplicationContext`):

```
    @Bean(scope = DefaultScopes.SESSION)
	public JsfCrudAdapter employeeCrud() throws Exception {
		JsfCrudAdapter adapter = cruds().get("Employee");

		/*
		 * Filter out employees who do not have a manager. This will create a
		 * "where employee.manager is null" to the query.
		 */
		adapter.getPaginator().addCriterion(Comparison.eq("manager", null));
		adapter.getPaginator().filter();

		/* Setup tasks and contacts DetailControllers. */
		adapter.getController().addChild("tasks",
				new JsfDetailController(Task.class));
		adapter.getController().addChild("contacts",
				new JsfDetailController(ContactInfo.class));

		/*
		 * Setup directReports detail controller. Make sure framework calls
		 * add/remove methods.
		 */
		JsfDetailController directReports = new JsfDetailController(
				Employee.class);
		RelationshipManager relationshipManager = directReports
				.getRelationshipManager();
		relationshipManager.setChildCollectionProperty("directReports");
		relationshipManager.setAddToParentMethodName("addDirectReport");
		relationshipManager.setRemoveFromParentMethodName("removeDirectReport");

		adapter.getController().addChild("directReports", directReports);
		return adapter;
	}

```

## Add the `crank:detailListing` tag to the Employee form.xhtml ##
Lastly we need to add the `crank:detailListing` to the form.xthml.

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

			</crank:form>
		</a4j:form>
	</ui:define>
</ui:composition>
</html>

```