#### Introduction ####

Crank, and Presto before it, has always had the ability to create paginateable, filterable, sortable listings for several years now. Lately we ran into two issues as follows:

  1. Unable to sort, filter on fields that don't have a relationship to entity
  1. Unable to sort, filter on fields in a sublcass


We did a major update to the `FilteringPaginator` and the Criteria DSL to support selecting and filtering multiple entities in a single listing.

This document describes how to use this new feature.

#### Related Entries ####
| Wiki Entry | Description |
|:-----------|:------------|
| UpdatingListingControllerToAllowEntityJoins | Problem defininion and notes on impl, shows error messages that JPA throws when filtering on fields in a sublcass  |
| [CriteriaDSLUpdate](CriteriaDSLUpdate.md) | Documentation on updates to the DSL to support new features related to Entity joins |

#### Unable to sort, filter on fields that don't have a relationship to entity ####

This is another problem that this new feature should fix (example to follow). Essentially we have a tiered architecture where we have three libraries as follows:

  * `CoreBiz` module
  * `ServiceA` module
  * `ServiceB` module

Think of the modules as Java jar files or seperate maven projects pom.xml (which in our case they actually are both).

The `CoreBiz` module does not depend on the `ServiceA` module or the `ServiceB` module.
The `ServiceA` module does not depend on the `ServiceB` but does depend on `CoreBiz`.
The `ServiceB` module depends on the `ServiceA` and `CoreBiz`.

Imagine that each tier has an hierarchy of Objects that are mapped to a set of Database tables via JPA. Thus there are relationships in the database that do not exist in the Object model, but are instead managed by `ServiceManagers`. Otherwise we would have one giant module with one huge Object graph mapped to the database. However, we do need to join objects at times for listings for Admin tools. Whew! Clear? Probably not.

Let's say I have a library that manages Tags and you can associate and Employee with a tag, but you only store the tag id not the Tag. This new feature can support this as well.


#### Unable to sort, filter on fields in a sublcass ####

You can't access properties of a subclass when sorting and filtering. This needs explanation.

#### Sample Classes ####
Let's say we have classes as follows:

```
//inquiries
class Inquiry
class PetStoreInquiry extends Inquiry has property a
class PetClinicInquiry extends Inquiry has property b

//leads
class Lead has an Inquiry (one to one)
class PetStoreLead extends Lead
class PetClinicLead extends Lead
```

In this example: A `Lead` is a sales lead. An `Inquiry` generates a `Lead`. The `Lead` has a reference to the `Inquiry` that generated the lead. There are two subclasses of `Inquiry`, namely, `PetStoreInquiry` and `PetClinicInquiry` see figure 1.

_**Figure 1: UML Diagram showing how base classes define the relationship of Lead to Inquiry**_


![http://krank.googlecode.com/svn/trunk/docs/images/inquiryExample.png](http://krank.googlecode.com/svn/trunk/docs/images/inquiryExample.png)


The question is while searching for `PetStoreLead`s how do you access properties of the `Inquiry` subclass?

This query JPA(Hibernate) does not work (expected):

```
SELECT lead FROM PetStoreLead lead WHERE lead.inquiry.a='a'
```

The error you get is that `lead.inquiry` does not have a property called `a`.
The problem is that `Inquiry` does no have a property `a`, its subclass `PetStoreInquiry` does. (We are using joined subclasses different mappings may work.)

Here are the messaages you get when you execute this type of query from JPA/Hibernate (take from test case that reproduces it see [UpdatingListingControllerToAllowEntityJoins](UpdatingListingControllerToAllowEntityJoins.md) for code for test case):

```
			/* Unable to run query : SELECT  o 
			 * FROM PetClinicLead o  WHERE  o.inquiry.bb = :inquiry_bb */
			/* java.sql.SQLException: 
			 * Column not found: INQUIRY1_2_.BB in statement
			 * SQL 
			 * select petclinicl0_.id as id4_, petclinicl0_1_.name as name4_, 
			 * petclinicl0_1_.INQUIRY_ID as INQUIRY3_4_ 
			 * from PetClinicLead petclinicl0_ 
			 * inner join BASE_LEAD petclinicl0_1_ 
			 * on petclinicl0_.id=petclinicl0_1_.id, 
			 * BASE_INQUIRY inquiry1_ 
			 * where petclinicl0_1_.INQUIRY_ID=inquiry1_.id 
			 * and inquiry1_2_.bb=?] */			

```

One possible solution to the above is to rewrite the query as:

```

        SELECT lead 
        FROM PetStoreLead lead, PetStoreInquiry inquiry 
        WHERE inquiry.a='a' AND lead.inquiry=inquiry
```


We updated our Criteria API to support entity joins see: [CriteriaDSLUpdate](CriteriaDSLUpdate.md)

We also changed the `FilteringPaginator` which backs the `<crank:listing` composition component to perform this type of join.

This document has a step by step of example using the `PetClinic` example.



#### Step by Step example ####
This assumes you are familiar with Crank and have followed the getting started guide (see [GettingStartedWithCrank](GettingStartedWithCrank.md) and [GettingStartedWithCrankPresentation](GettingStartedWithCrankPresentation.md) if you have not used Crank yet).

Step 1 add the entities to the Spring configuration so the base class config can process them:

```
...
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
...

@Configuration(defaultLazy = Lazy.TRUE)
public abstract class CrankCrudExampleApplicationContext extends CrudJSFConfig {

	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
                        ...
			managedObjects.add(new CrudManagedObject(PetClinicInquiry.class,
					null));
			managedObjects.add(new CrudManagedObject(Inquiry.class,
					null));
			managedObjects.add(new CrudManagedObject(PetClinicLead.class,
					null));			
                        ...
			
		 return managedObjects;
```

Then we pull the `JsfCrudAdapter` for lead and configure its paginator to do an entity join, i.e., we join to o.inquiry (o is the alias for the main entity).

```
	@SuppressWarnings("unchecked")
	@Bean(scope = DefaultScopes.SESSION)
	public JsfCrudAdapter petClinicLeadCrud() throws Exception {
		/* Pull out the existing CrudAdapter. */
		JsfCrudAdapter adapter = cruds().get("PetClinicLead");
		/* Grab its filtering paginator and configure it. */
		FilterablePageable paginator = adapter.getPaginator();
		/* Call addFilterableEntityJoin adding the class we are joining to,
		 * the name of the entity, the name of the alias, 
		 * an array of property names, and an optional join that will be added to the where clause.
		 */
		paginator.addFilterableEntityJoin(PetClinicInquiry.class, //Class we are joining
					"PetClinicInquiry", //Entity name
					"inquiry", //
					new String []{"anotherProp"}, //Array of property names we want to join to. 
					"o.inquiry"); //How to join to the PetClinicLead 
		return adapter;
	}

```

By adding the above we change the query to:

```
SELECT o FROM PetStoreLead o WHERE <fitlers go here> ORDER BY <sorts go here>
```

To this JPA query:

```

SELECT o, inquiry FROM PetStoreLead o, PetStoreInquiry inquiry WHERE o.inquiry=inquiry AND (<filters go here>) ORDER BY <sorts go here>
```



From a visual standpoint, this adds an extra column to our listing that the end user can filter and sort. The `addFilterableEntityJoin` is a convience method since we have an app that has to do this quite a bit, you could do similar stuff by configuring a hierarchy of `Select`, `FilteringProperty`, `EntityJoin`, `Comparison`, etc., but that would be very error prone for most folks not familiar with the Criteria DSL that Crank relies on (see [UsingDAO](UsingDAO.md), [CriteriaDSLUpdate](CriteriaDSLUpdate.md) and [entry on Criteria | http://www.jroller.com/RickHigh/entry/i\_implemented\_a\_detached\_criteria](Blog.md)) for more details on the Crank Criteria DSL.

To configure the `<crank:listing` tag you would do the following (in a Facelet):
```
<crank:listing jsfCrudAdapter="${petClinicLeadCrud}"
            propertyNames="name,inquiry.anotherProp"
            parentForm="petClinicLeadListForm" />

```

Notice that the `propertyNames` specifies the `inquiry.anotherProperty` as we can now include this in the listing as if it were any other property. See figure 2, it shows both sorting and filtering working with this `entityJoin`.

_**Figure 2 Example showing filtering and sorting working**_
_![http://krank.googlecode.com/svn/trunk/docs/images/donutBurger.png](http://krank.googlecode.com/svn/trunk/docs/images/donutBurger.png)_

The crank-crud-sample webapp has this example in it. Please refer to it.