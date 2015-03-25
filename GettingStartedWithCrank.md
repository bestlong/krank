# Introduction #

This is a short guide to get you moving in the Crank direction and get the most out of Crank.

# Details #

First, thanks for looking into Crank. I think you will be pleasantly surprised.


The idea is to come up to speed with Crank.

Think of a simple object model and then map it to Crank. Make it up. This is a prototype. We plan on throwing it away.


Before you get started (proposed steps) you will want a little background on the Crank project. If you are using the JSF Crud development model, you need to understand JSF, Facelets, Crank and Ajax4JSF.

To get an understanding of JSF read these four articles:

Step 1: Clearing up the JSF FUD
  * [Intro to JSF](http://www-128.ibm.com/developerworks/library/j-jsf1/)
  * [JSF Lifecycle](http://www-128.ibm.com/developerworks/library/j-jsf2/)
  * [Conversion and Validation](http://www-128.ibm.com/developerworks/library/j-jsf3/)
  * [Intro to creating components](http://www-128.ibm.com/developerworks/library/j-jsf4/)
  * [If your team can afford it, the best JSF training course would not hurt](http://www.arc-mind.com/courses/jsfCourse.html)

Then to come up to speed on Facelets read this:

Step 2: Facelets:
  * [Intro to Facelets](http://www-128.ibm.com/developerworks/java/library/j-facelets/)
  * [Advanced Facelets](http://www-128.ibm.com/developerworks/web/library/j-facelets2.html)
  * [Facelets is covered in ArcMinds JSF course as well](http://www.arc-mind.com/courses/jsfCourse.html)

The last two article are very important because they discuss the design principles that back Crank. (Crank improves on them quite a bit, thanks in large part to Paul Tabor, the king of composition components).

Once you have the above under your belt please read about Crank itself

Step 3 Crank:
  * [Home page](http://code.google.com/p/krank/)
  * [Introduction to Crank (should make a lot of sense based on Facelets articles)](http://code.google.com/p/krank/wiki/CrankCrudIntro)
  * [Design of Crank Crud](http://code.google.com/p/krank/wiki/CrankCrudDesignDocs)
  * [Building Crank](http://code.google.com/p/krank/wiki/BuildingCrankWithMaven)
  * [Crank Crud Tutorial](http://code.google.com/p/krank/wiki/CrankCrudTutorial)
  * [Customize what shows up in a listing](http://code.google.com/p/krank/wiki/CrankCrudTutorialUsingCustomListings)
  * [More Advanced Tutorial](http://code.google.com/p/krank/wiki/CrankCrudTutorialSelfReferentialEntitiesAndCustomListings)
  * [Crank Select Many Tutorial](http://code.google.com/p/krank/wiki/CrankCrudSelectMany)


Step 4: Manage some objects
You can download the sandbox code from svn at https://krank.googlecode.com/svn/trunk

In order to manage objects you need to know a thing or two about JPA.
[Check out JPA Resources](http://www.thearcmind.com/confluence/display/SpribernateSF/JPA+Resources) and the best [JPA training course](http://www.thearcmind.com/confluence/display/ArcMindOutlines/JPA+QuickStart+Training+Course+Outline)

You need maven 2 to build any of our apps.

Base you example on the sandbox
  * http://krank.googlecode.com/svn/trunk/crank-crud-webapp-sample

Don't worry about the real object model. Simplify it as much as you need to and get familar with the technique.



--Rick Hightower