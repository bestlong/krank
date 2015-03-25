Crank allows idiomatic, efficacious GUI development based on your Java object model.

"Even Chuck Norris feared JSF...until he saw Crank" --CM

[Crank 1.0.1 Release Notes and Download](http://code.google.com/p/krank/wiki/Crank101Released)

[Crank Google Group for Questions and Such](http://groups.google.com/group/crank-developer-support)

[Slides from Los Angeles JUG, 1/5/2008](http://krank.googlecode.com/svn/trunk/docs/slides/)


Crank is a master/detail, CRUD, and annotation driven validation framework built with JPA, JSF, Facelets and Ajax. It allows developers to quickly come up with JSF/Ajax based CRUD listings and Master/Detail forms from their JPA annotated Java objects.

Crank uses a lot of the new JSF features from Facelets, Ajax4JSF, etc. that will be used in JSF 2.0. Crank is a use case analysis of what is possible with the new JSF 2.0 stack.

The validation piece does server-side validation, Ajax validation or just emitted JavaScript validation based on Java annotations, property files, XML files, or database tables. Currently works with JSF, Spring MVC and Spring Webflow.

The framework is named Crank as in: "crank out, to make or produce in a mass-production, effortless, or mechanical way: She's able to crank out one (CRUD listing) after another"  and "crank up: to get started or ready", "to stimulate, activate, or produce", and most importantly "to increase one's efforts, output, etc.: Industry began to crank up after the new (CRUD framework became our corporate standard)." http://www.dictionary.com

![http://krank.googlecode.com/svn/trunk/docs/images/logo.jpg](http://krank.googlecode.com/svn/trunk/docs/images/logo.jpg)

The CRUD framework has support for [JPA](http://www.thearcmind.com/confluence/display/SpribernateSF/JPA+Resources) enabled DAO objects. The CRUD framework implements a Detached Criteria API/DSL similar to Hibernates (R) Criteria API except it works with [JPA](http://www.thearcmind.com/confluence/display/SpribernateSF/JPA+Resources). The Detached Criteria API/DSL (DCAD) could be ported to other frameworks for example Hibernate, iBatis, etc.

The CRUD framework has a controller that is framework neutral as well. Currently there is an example the uses JSF to quickly create CRUD listings and master detail forms as follows:

![http://krank.googlecode.com/svn/trunk/docs/images/master_detail_pic.jpg](http://krank.googlecode.com/svn/trunk/docs/images/master_detail_pic.jpg)



We built filterable listings in JSF/[JPA](http://www.thearcmind.com/confluence/display/SpribernateSF/JPA+Resources). We plan on adding support for Struts 2 and Spring MVC that work with the CRUD listing (Create, Read, Update, Delete, Filter, and Sort). We did this before for an internal project called Presto (and before that with an internal framework based on Struts). This is like Presto revisted using Java annotations and generics (and a lot more eyeballs who provided a ton of feedback).

Here is an example of the filterable listing:

![http://krank.googlecode.com/svn/trunk/docs/images/paginated_filterable_listing_feature_map.jpg](http://krank.googlecode.com/svn/trunk/docs/images/paginated_filterable_listing_feature_map.jpg)

You can download the code from svn at https://krank.googlecode.com/svn/code/trunk. There is also a snapshot binary and src release in the download section.

To download the code do the following:

```
svn checkout https://krank.googlecode.com/svn/trunk/ crank
```

To learn more about Crank please read [Crank Crud Intro](CrankCrudIntro.md). If you want to learn more about the design and philosphy of Crank please read [Crank Crud Design documents](CrankCrudDesignDocs.md). There is a getting started guide [Getting Started With Crank](GettingStartedWithCrank.md) for those who are new to JSF, and Facelets. Lasty, try reading our Wiki.

Please see the HomePage for more detail.

### News ###
  * Jan, 2009: Cagatay Civici, world renown JSF expert, has joined the team as project owner and will be helping us port to JSF 2.0 as it becomes available.
  * Jan, 2009: Codegen Alpha has been released. Soon it will be faster than ever to create a new project with Crank!
  * Nov, 2008: Major pom cleanups, poms reviewed with maven expert and revamped, 1.0.1 released, fixed broken examples, upgraded to Spring 2.5.5 from 2.0.6.
  * Oct, 2008: 1.0 released
  * May, 2008: Overhauled Pagination. Optimized to only hit Database after next, previous, etc. is hit (once and only once). Much more efficient.
  * April, 2008: Went through several iterations of patches as we prepare to launch 10+ applications built on top of Crank
  * March, 2008: Improved event management
  * Feb 1, 2008: Sean Burns integrated Crank with Seam see [Crank and Seam](http://code.google.com/p/krank/wiki/CrankandSeam), [Injecting from Spring into Seam](http://code.google.com/p/krank/wiki/SeamAndCrankInjectingFromSpring)
  * Jan 09, 2008: Paul Hixson and I got rid of the dreaded "need to hit update button twice issue". PISFA.
  * Jan 03, 2008: Tom Cellucci has been making a lot of updates so we did a new release Snapshot 013.
  * Nov 11th, 2007: Tom Cellucci added support to GenericDao and implementations for operations on multiple entities
  * Apparently there are some people in the world that can't download a ppt viewer for them Bill created this: http://docs.google.com/Doc?id=dd5hj3c9_1fdzpd8
  * Nov 7th, 2007: Presentation on getting started with Crank. It should take about an hour or less to go through this presentation and build your first crank project. http://krank.googlecode.com/svn/wiki/ppts/Crank-Slidess.ppt
  * Oct 30th, 2007: Added missing support for I18n
  * Oct 30th, 2007: Added support for filtering by enum. Renders enum drop down.
  * Added number next to sorted field so end users could visualize sort order 10/25/2007
  * Added support for numbers for sorting (bug fix) now supports all primitives and wrappers for listing sorting 10/25/2007
  * Added required support for ManyToOne relationship drop down box 10/23/07
  * Optimized pagination so it only hits the db when changed not twice per page load 10/07
  * Build supports MySQL DB and HSQLDB 10/07
  * [Using custom listings](CrankCrudTutorialUsingCustomListings.md) 9/28/07
  * Rick Hightower wrote a selectMany with a how to use tutorial [selectMany Turorial](CrankCrudSelectMany.md) 9/25/07
  * Rick added several tutorial trails 9/?/07
  * Paul Tabor wrote an autocompletion component that works with the field component (8/?/07). He has not yet written a how to use tutorial.
  * 0.7-SNAPSHOT released, includes everything but the wars (Bill Dudney)
  * [Crank Validation Design Document](CrankValidationDesignDocument.md) added on 8/28/2007
  * Rick Hightower added GettingStartedWithCrank on 8/28/2007
  * Chris Mathias added config isolation support to SpringTestNGBase. (8/23/2007)
  * Chris Mathias merged Paul Hixson's OpenEntityManagerInTest work into SpringTestNGBase. (8/23/2007)
  * Chris Mathias added second level cache preload and integration support `PreloadableCacheableGenericDao` (8/23/2007) -- [see design docs](http://code.google.com/p/krank/wiki/CachePreloadingConfigurableDaoSupport).
  * August 21st, 2007: Rick created design documents for the controller architecture see [Crank Crud Design documents](CrankCrudDesignDocs.md)
  * August 19th, 2007: Chris Mathias authored a version of the base dao support which divorces us from Spring's JpaDaoSupport, making EJB integration possible. We have a guy with Seam/EJB3 experience who has volunteered to port one of the example apps to Seam/EJB3 stack.
  * August 19th, 2007: Scott Fauerbach of Presto fame joins the group. Scott worked on Presto which is a similar framework (much more mature than Crank)
  * August 16th, 2007: Paul Tabor Added Enum support. Now Enums show up in forms. Sorting and Filtering Enums is in progress.
  * August 16th, 2007: Rick Hightower added support for editing and sorting on composite relationships, i.e., employee.address.line1 can be edited in the form, and it is sortable/filterable from the listing.
  * August 15th, 2007: Bill Dudney integrated maven sql plugin to destroy and create db when testing.
  * August 12th, 2007: Rick Hightower added support file uploads.
  * August 12th, 2007: Rick Hightower added support sorting/filtering on relationships (employee.department.name) in the listing or even deeply nested relationhships(employee.department.company.name).
  * Please read CrankCrudIntro for a good overview of what Crank provides. (August 9th, 2007, Rick Hightower)
  * Integrated validation framework into JSF crud framework August/3/2007 (Rick Hightower)
  * Crud master detail support improved July 2007 (Rick Hightower, Paul Tabor)
  * Crud listing and add/edit/delete works July 2007 (Rick Hightower, Paul Tabor)
  * Geoff Chandler, Danilo Banillo, John Fryar, Serge Madenian, Paul Tabor added fixes and such, but were too shy to brag. May - July 2007
  * Rick Hightower added asc/desc order by support May/9/2007
  * Rick Hightower Added readPopulated method that is similar to read but tries to located a named query called readPopulated that implements a version of a load that eagerly loads relationships. May/08/2007
  * Rick Hightower Added pagination support. May/08/2007
  * Rick Hightower Added early support for join fetching. May/07/2007
  * John Fryar added support for proper handling nulls for criteria comparisons. May/02/2007
  * Rick Hightower added checked criterias for verifying properties for extra debugging (VerifiedCriteria). April/28/2007
  * Bill Dudney fixed the build to pull things from the Java.net repo so now we don't have to manually populate our Maven repo April/15/2007
  * Chris Mathias reorganized the svn repo so it makes sense now April/05/2007
  * Chris released the first version of Crank April/02/2007
