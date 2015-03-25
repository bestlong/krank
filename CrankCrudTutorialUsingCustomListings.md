# Introduction #

If you are new to Crank, this document may not be for you, please see CrankCrudTutorial, GettingStartedWithCrank and CrankCrudDesignDocs first, then come back here.

The listings are very flexible. At times you may need to add extra links or you need to limit the results that show up.

This is a bit tricky if you want to programmatically add filters that the end users can't not modify. We added another set of filters to the `FilteringPaginator` to facilitate this. These filters are applied programmatically to limit what shows up in the listing. You use our Criteria DSL (see UsingDAO).

JD (Jason) and I found the need for this when we were working together. Walter and I refined the implementation so you can pass addition links to the listing. Walter and I also refined the `readOnly` support that Paul T started. Now when you use `readOnly` on the listing, it disables the add button (as before), the edit button and the delete button.

For example, the department listing has a link to the employee listing, when the end user selects a department we want to show the employees for that department only in that listing. However, when they click the main link on the home page for employees, we want to show all employees.

To do this we will do the following:

### TODO List ###
  1. Create a new controller called `SelectEmployeeListingController`
  1. Inject a `FilteringPaginator` into `SelectEmployeeListingController` to programmatically adjust what shows up in the employee listing.
  1. Define an action method called `SelectEmployeeListingController.showListingForDepartment()` that shows the department's employees that was selected
  1. Define an action method called `SelectEmployeeListingController.showListing()` that shows all employees
  1. Register the SelectEmployeeListingController with the `CrankCrudExampleApplicationContext` in `REQUEST` scope.
  1. Define the commandLink to our new controller action on the `.../Department/Listing.xhtml`
  1. Define the commandLink to our new controller action on the `/Home.xhtml`


### Create a new controller called `SelectEmployeeListingController` ###
```
/**
 * Created by IntelliJ IDEA.
 * User: Rick
 * Date: Sep 27, 2007
 * Time: 11:58:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectEmployeeListingController {
}

```

### Inject a `FilteringPaginator` into `SelectEmployeeListingController` to programmatically adjust what shows up in the employee listing ###

```
public class SelectEmployeeListingController {
    private FilterablePageable employeePaginator;

    public SelectEmployeeListingController(FilterablePageable employeePaginator) {
        this.employeePaginator = employeePaginator;
    }

}

```

The `employeePaginator` is needed to set up the new filters programmatically. (We use our own Criteria API/DSL for creating filters).

### Define an action method called `SelectEmployeeListingController.showListingForDepartment()` that shows the department's employees that was selected ###

```
public class SelectEmployeeListingController {
    private FilterablePageable employeePaginator;

    public SelectEmployeeListingController(FilterablePageable employeePaginator) {
        this.employeePaginator = employeePaginator;
    }

    public String showListingForDepartment () {
        String sId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        employeePaginator.addCriterion(Comparison.eq("department.id",Long.valueOf(sId)));
        employeePaginator.filter();
        return "EMPLOYEES";
    }

}
```

The key take away is the following code:

```
		//1
        String sId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		//2
        employeePaginator.addCriterion(Comparison.eq("department.id",Long.valueOf(sId)));
		//3
        employeePaginator.filter();

```

The above does the following:
  1. Gets the `id` from the HttpRequest
  1. Add a `Criterion` to the `paginator` to only show the employees for the department that was selected
  1. Apply the filter

If you wanted to clear past filters you could do this:

```
        employeePaginator.disableFilters();
        String sId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        employeePaginator.addCriterion(Comparison.eq("department.id",Long.valueOf(sId)));
        employeePaginator.filter();

```

This might be good, especially if the end user can sort by department.

To disable all sorts, you can do this:
```
        employeePaginator.disableSorts();
        String sId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        employeePaginator.addCriterion(Comparison.eq("department.id",Long.valueOf(sId)));
        employeePaginator.filter();

```

To clear everything that the user has set, you can do this:

```
        employeePaginator.clearAll();
        String sId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        employeePaginator.addCriterion(Comparison.eq("department.id",Long.valueOf(sId)));
        employeePaginator.filter();

```

There are tradeoffs in useablity if you always clear sorts and such. End users may want things sorted a certain way.

(A future version of crank crud plans on having user profiles that remember how users like sorts and filters setup by default. This project may be another project that extends crank crud.)


### Define an action method called `SelectEmployeeListingController.showListing()` that shows all employees ###

```
public class SelectEmployeeListingController {
    private FilterablePageable employeePaginator;

    public SelectEmployeeListingController(FilterablePageable employeePaginator) {
        this.employeePaginator = employeePaginator;
    }

    public String showListingForDepartment () {
        String sId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        employeePaginator.addCriterion(Comparison.eq("department.id",Long.valueOf(sId)));
        employeePaginator.filter();
        return "EMPLOYEES";
    }

    public String showListing() {
        employeePaginator.getCriteria().clear();
        employeePaginator.filter();
        return "EMPLOYEES";
    }
}

```

The key to this code is the following:

```
    public String showListing() {
        employeePaginator.getCriteria().clear();
        employeePaginator.filter();
        return "EMPLOYEES";
    }

```

This clears the programatically set filters but not the user defined filters and sorts.

### Register the SelectEmployeeListingController with the `CrankCrudExampleApplicationContext` in `REQUEST` scope ###

Now that we have our paginator all setup, let's register it so we can start using it.

```
    @SuppressWarnings("unchecked")
    @Bean (scope = DefaultScopes.REQUEST)
    public SelectEmployeeListingController selectEmployeeListingController() throws Exception {
           SelectEmployeeListingController controller = new SelectEmployeeListingController(paginators().get("Employee"));
           return controller;
    }

```


Noitce that this controller maintains no state so it is registered in the `REQUEST` scope.

### Define the commandLink to our new controller action on the `.../Department/Listing.xhtml` ###

Now that we have our controller registerd we can use it from our pages.

```
            <crank:listing  jsfCrudAdapter="${cruds.department}" 
					parentForm="departmentsListingForm"
					propertyNames="name">
                    
                    <h:commandLink action="${selectEmployeeListingController.showListingForDepartment}" 
                                   value="employees...">
                         <f:param name="id" value="${row.object.id}"/>
                    </h:commandLink>

            </crank:listing>

```

The key to the above is the following:

```
                    <h:commandLink 
                            action="${selectEmployeeListingController.showListingForDepartment}" 
                            value="employees...">
                        <f:param name="id" value="${row.object.id}"/>
                    </h:commandLink>

```

Notice that we pass the `Department` id as follows:

```
                    <f:param name="id" value="${row.object.id}"/>

```

Please notice that the h:commandLink is passed in the body of the crank:listing. Any components that show up in the body of the crank:listing display in the Actions column of the page.


### Define the commandLink to our new controller action on the `/Home.xhtml` ###
We also want to change the home page link so that it calls an action instead of linking directly to a navigation outcome.

```
			<h:panelGrid columns="1">
				<h:commandLink action="${selectEmployeeListingController.showListing}" 
				               value="Employees" styleClass="titleCommandLink"/>
				<h:commandLink action="DEPARTMENTS" value="Departments" styleClass="titleCommandLink"/>
				<h:commandLink action="SPECIALTY" value="Specialties" styleClass="titleCommandLink"/>
				<h:commandLink action="ROLES" value="Roles" styleClass="titleCommandLink"/>
            </h:panelGrid>

```

That is it folks. Feedback from crank users welcome.