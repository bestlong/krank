# Overview #

If you are new to Crank, this document may not be for you, please see CrankCrudTutorial, GettingStartedWithCrank and CrankCrudDesignDocs first, then come back here.

This document outlines a nifty little feature in Crank - the autocomplete field.  This feature is easy to implement and provides automatic field completion based on a data source for the RichFaces suggestion list, from which the user may choose a value.  In its current incarnation, this feature is used for handling Many-to-one form field input.  Here is an example of the feature in action:

![http://krank.googlecode.com/svn/wiki/img/autocomplete-sample.png](http://krank.googlecode.com/svn/wiki/img/autocomplete-sample.png)

Here is our task list for setting up and using the autocomplete field:
  1. Update `CrankCrudExampleApplicationContext` configuration class, add a wired `autocomplete` controller
  1. Modify the `Employee` form to include Specialty as a form field using the auto-completion attribute syntax
  1. Test the field's input behavior

## Update `CrankCrudExampleApplicationContext` configuration class, add a wired `autocomplete` controller ##
First we make the changes to `CrankCrudExampleApplicationContext` as follows:
```
@Configuration (defaultLazy=Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {

	...
	    
    @SuppressWarnings("unchecked")
    @Bean (scope = DefaultScopes.SESSION) 
    public Map<String, AutoCompleteController> autocomplete () throws Exception {
        Map<String, AutoCompleteController> autocomplete = new HashMap<String, AutoCompleteController>();
        
        // Create a data source to be used by the auto-complete controller
        // and add DAO for the entity containing the auto-complete data.
        DaoFilteringDataSource dataSource = new DaoFilteringDataSource();
        dataSource.setDao( repos().get( "Specialty" ));
      
        // Resolve the CRUD controller for the entity to be affected by the 
        // auto-complete selected value.
        CrudOperations controller = cruds().get("Employee").getController();

        // Create the auto-complete controller.
        // Arguments are:
        //     sourceClass -         
        //	      the class of the entity containing the source value property.
        //     sourceProperty - 
        //	      the property on sourceClass containing the source values.
        //     dataSource - 
        //	      the data source for sourceClass        	
        //     targetCrudController -   
        //	      the CRUD controller for the entity to be affected.
        //     targetProperty -
        //        the property of the target entity to be completed by the 
        //        auto-complete value.	
        AutoCompleteController autoController = new JsfAutoCompleteController( 
        		Specialty.class, "name", dataSource, controller, "specialty");
        
        // Add to the auto-complete map...
        //     This will be accessed via the field.xhtml as the AutoComplete 
        //     controller for the associated target property.
        autocomplete.put("Specialty", autoController);
        return autocomplete;
    }

        
```

The above code snippet creates a new JsfAutoCompleteController to wire a `dataSource`, which contains the "source" entity for the list of suggestions, to a specific database entity's property.

The first three parameters of the JsfAutoCompleteController describe the datasource.  First is the class for the entity containing the properties for the list.  The property itself is described by the `sourceProperty`, the second parameter.  The third parameter, the 'datasource' is the filering datasource that will be used to obtain the list of values.

The final two parameters relate to the target of the selection.  The `targetCrudController` is, surprisingly enough, the crud controller for the target entity, and the `targetProperty` represents the property name of the child entity which is value bound by the selection.

Next, the autocomplete controller must be registered with the entity associated with the suggestion `dataSource`.  This is needed for the auto-validation functionality which we'll discuss in a bit.

Finally, the autocmplete controller is put into scope via the `autocomplete()` map.  The key for this controller represents the token used in the autocomplete form field attribute syntax (explained below).

This completes the changes we needed to make to the `CrankCrudExampleApplicationContext` class.

## Modify the `Employee` form to include `Specialty` as a form field using the auto-completion attribute syntax ##

Next, we have to modify the `Employee` form to include `Specialty` as a form field using the auto-completion attribute syntax which tells the Facelets component to use the RichFaces suggestion box with associated autocomplete controller.  Here is the definition of the expected attribute syntax:

```
    <crank:form ...
        propertyNames="[field1],[field2],...,[autocompleteField1:autocompleteController1],..."
        >
	...
```

Here is a working example (note the `specialty:Specialty` autocomplete field).  In this example, the autocomplete field is the last field in the `propertyNames` list.  However, you can have as many autocomplete fields at any place in the list and comingled with "normal" fields:

```
	<crank:form crud="${crud}" parentForm="employeeForm"
		propertyNames="firstName,lastName,department,description,file,age,numberOfPromotions,specialty:Specialty"
		>
	...
```

## Test the field's input and validation behavior ##

As mentioned earlier, the nice thing about the autocomplete field is that the underlying controller will automatically validate the user's selection against the underlying datasource.  Meaning, that although this is rendered as a "free-form" text input field, the result must exist in the provided "parent" data source in order for the value to be updated in the associated "child" entity.  This is achieved by the default behavior of the `AutocompleteController` class which is registered as a contoller listener with the associated child entity in our `CrankCrudExampleApplicationContext` class.  This way, the autocomplete controller can validate the selection during update and create events.

Here is an example of a successfully validated selection:

![http://krank.googlecode.com/svn/wiki/img/autocomplete-flow.png](http://krank.googlecode.com/svn/wiki/img/autocomplete-flow.png)

Here is an example of an invalid selection and the associated error:

![http://krank.googlecode.com/svn/wiki/img/autocomplete-error.png](http://krank.googlecode.com/svn/wiki/img/autocomplete-error.png)