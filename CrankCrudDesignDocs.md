# Introduction #

This document covers the basic structure of Crank Crud. At least the CRUD controller parts, namely pagination and CRUD operations.

## CRUD Controller Design Details ##

The `CrudController` class is the controller for forms that edit entities (domain objects like Department, Employee, etc.). `DetailCrudController` class is the controller for editing details. Think of `DetailCrudController` as the "detail" in Master detail. `CrudController` has a map of `DetailController`s. For example a `CrudContoller` might be tied to a `Department` entity class. The `Department` form may allow editing of `employees` on the `Department` form. Thus the `Department` `CrudController` would have an employees (map key = `employees`) `DetailController` associated with it. `CrudControllerBase` is the code that is used by both `CrudController` and `DetailController`.

To visualize what we are talking about, look at the following diagram:

![http://krank.googlecode.com/svn/wiki/img/master_detail_pic.jpg](http://krank.googlecode.com/svn/wiki/img/master_detail_pic.jpg)

Notice that the `Department` object (managed in JPA) is a parent of the `Employee`s list.
An `Employee` is being edited, and it has a list of tasks.
Thus `Department` has employees, and an `Employee` has tasks.

The `CrudController` class implements the `CrudOperation`s interface. The `CrudOperation`s interface defines a composition pattern structure. A master detail page is backed by a `CrudController` that can have `DetailControllers` which in turn can have `DetailControllers` which in turn can have `DetailControllers` ad infinitum.

None of the above is tied to Spring MVC or JSF. You can use the above with any GUI front-end especially with those that use expression binding languages like JSF EL and OGNL.

#### UML diagram for CRUD controller classes: ####

![http://krank.googlecode.com/svn/wiki/img/master_detail.png](http://krank.googlecode.com/svn/wiki/img/master_detail.png)

It may be best to visualize the relationship of controllers with this picture:

#### Controller to Master Detail form elements: ####

![http://krank.googlecode.com/svn/wiki/img/master_detail_pic_controlle_mapping.jpg](http://krank.googlecode.com/svn/wiki/img/master_detail_pic_controlle_mapping.jpg)

## CRUD Pagination Design Details ##

Crank CRUD also supports building paginatable, filterable listings. You can sort by any field/property (`Employee.firstName`, `Employee.dob`) or any relationship field/property (`Employee.department.name`, `Employee.address.zip`). Much of this is accomplished through the use of the Crank Criteria API. The Crank CRUD uses the Crank Criteria API extensively. Below is a picture that shows what a typcial listing might look like:

#### Typical Crank Listing: ####
![http://krank.googlecode.com/svn/wiki/img/paginated_filterable_listing.jpg](http://krank.googlecode.com/svn/wiki/img/paginated_filterable_listing.jpg)

The listing equates to the following features:

#### Feature map: ####
![http://krank.googlecode.com/svn/wiki/img/paginated_filterable_listing_feature_map.jpg](http://krank.googlecode.com/svn/wiki/img/paginated_filterable_listing_feature_map.jpg)

Let's go over the classes and interfaces that make up the filterable, paginatable Crank listings. `Pageable` is the interface for controllers that provide pagination. The `Paginator` is a concrete class that provides pagination support. Again, `Pageable` is not tied to JSF, Spring MVC or Struts. `Pageable` as all classes discussed so far can in theory be used by JSF, Spring MVC, Struts, Swing, etc.

`FilterablePageable` is an interface that controllers implement if they are going to provide pagination and filtering.

`FilteringPaginator` is a controller that extends the `Paginator` class and adds filtering. A `FilteringPagnator` has a list of `FilterableProperty` instances.

Each `FilterableProperty` instance has an `OrderBy` and `Comparison` associated with it (from the [Criteria DSL support](http://code.google.com/p/krank/wiki/UsingDAO)). When the value of `OrderBy` or `Comparison` changes they fire events that the `FilteringPaginator` captures. Once the `FilteringPaginator`, receives events from the `Comparison` or `OrderBy` objects it reprocesses the listing.


There is a `FilterableProperty` for each property of the Entity. The `FilterableProperty` consist of a `Comparison` object and an `OrderBy` object. The `Comparison` object and an `OrderBy` object are from our Crank Criteria DSL. (In actuality `FilterableProperty` works with `OrderByWithEvents`, `BetweenComparisonWithEvents` and `ComparisonWithEvents` which subclass `OrderBy`, `BetweenComparison` and `Comparison` from the [Criteria DSL support](http://code.google.com/p/krank/wiki/UsingDAO) and add change notification event handling.)

#### UML diagram for Pagination and Filtering Controller classes: ####

![http://krank.googlecode.com/svn/wiki/img/pagination.png](http://krank.googlecode.com/svn/wiki/img/pagination.png)

It may help to tie some of these controllers back to the listing picture as follows:

#### Controller map for pagination: ####
![http://krank.googlecode.com/svn/wiki/img/paginated_filterable_listing_mappings.jpg](http://krank.googlecode.com/svn/wiki/img/paginated_filterable_listing_mappings.jpg)

## DataSource ##
The `DataSource` module is really the generic models for our controllers. It allows us to divorce the "how to retrieve the model objects" from the controller. It is more akin to a `DataSource` in a PowerBuilder, VB, Delphi sense than a Java JDBC sense. A `DataSource` is an object that produces a list of model objects. Sometimes these model objects can come from a database, sometimes an xml file, sometime a plain java.util.List, sometimes the list if prefiltered, sometimes it is sorted, sometimes from an Enum class, somtimes a list of constants, etc.

Thus a `DataSource` defines a contract that provides a list of objects. It can be used to back a list box controller for example.

A `PagingDataSource` defines a contract to return a list of items with a index number and the number of items that you want to return.

A `FilteringDataSource` defines a contract to return a list of model object using the current Criteria DSL (`Group` and `OrderBy`) to restrict and sort the results.

A `FilteringPagingDataSource` defines a contract to provide pagination and filtering support.

#### UML Diagrams for DataSource interfaces: ####
![http://krank.googlecode.com/svn/wiki/img/datasource-interfaces.png](http://krank.googlecode.com/svn/wiki/img/datasource-interfaces.png)

`EnumDataSource` turns a Java Enum into a datasource.

`DaoDataSource` reads object out of the database using an instance of the GenericDAO interface.

`DaoFilteringDataSource` is a datasource that uses a `GenericDAO` to read a filtered list.

`DaoPagingDataSource` is a datasource that uses a `GenericDAO` object (perhaps the `GenericJpaDao` object that uses the Crank Criteria DSL) to read model objects a page at a time.

`DaoFilteringPagingDataSource` sorts, filters and pages model objects. It is service to the controller. The controller discussed earlier provides the state of paging (what page are we on, how many page does the user want to see). This object just provides getting the data from the database.

`SimplePagingDataSource` allows any list to become a paginated `DataSource`.

#### UML Diagrams for DataSource Concrete Classes: ####
![http://krank.googlecode.com/svn/wiki/img/datasource.png](http://krank.googlecode.com/svn/wiki/img/datasource.png)
