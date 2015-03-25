# Release notes for the upcoming release 23 #

  * Fixed pagination issue whereby on a listing if you selected show all rows and were not on the first page, the rows would be blank

  * Required fields that have not been annotated in their entity class may be identified using the new "requiredProperties" attribute of the 

&lt;crank:form /&gt;

 tag.  This attribute takes a comma separated list of properties.