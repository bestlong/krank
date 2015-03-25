# Overview #

If you are new to Crank, this document may not be for you, please see CrankCrudTutorial, GettingStartedWithCrank and CrankCrudDesignDocs first, then come back here.

By popular demand, Crank now supports read-only form field properties.  Why read-only form fields?  Well, for one thing, it addresses a use case we've been tasked with recently.  Additionally, this feature can be implemented to support workflow and role-restricted fields while still leveraging the flexible Crank CRUD operations.

## Details ##

To use the feature, simply override the default `propertyNames` attribute when using the `form` tag.  For instance, the following example will render `firstName` and `lastName` as read-only fields in the form:

```

	<crank:form crud="${crud}" parentForm="employeeForm"
				propertyNames="firstName,lastName,department,description,file,age,numberOfPromotions,specialty:Specialty"
				readOnlyProperties="lastName,lastName"
				enableGroups="active:status:dob:phone:email">

```

This feature can also be applied to form fields rendered via `detailListing` tags:

```

	<crank:detailListing 
				detailController="${employeeDetailController}" 
				propertyNames="firstName,lastName,active,dob,age,numberOfPromotions,specialty.name"
				readOnlyProperties="firstName,lastName"
				>

```

And crank 'compositePanel' tag:

```
 <crank:compositePanel entity="${crud.entity.address}" name="address"
      readOnlyProperties="address_1,address_2,city,postalCode" 
      propertyNames="address_1,address_2,city,postalCode"/>

```

However, you must be sure to override the `readOnlyProperties` attribute when nesting `detailListing` tags in a `form` tag which contain common property names.  Otherwise, the default behavior of Facelets will propogate the `readOnlyProperties` to all child tags and would subsequently propogate the read-only behavior for those fields.  While this is desirable in some cases, here is how you can override the `readOnlyProperties` attributes for `detailListing` forms nested inside a `form` tag:

```

	<crank:form crud="${crud}" 
		propertyNames="name,description,startDate,endDate"
		readOnlyProperties="startDate,endDate"
		>

		<!-- The readOnlyProperties here are "reset" to make sure the detail listing form allows editing of all fields. -->
		<crank:detailListing 
			detailController="${detailController}" 
			propertyNames="item,cost,startDate,endDate"
			readOnlyProperties=""
			/>
	</crank:form>

```