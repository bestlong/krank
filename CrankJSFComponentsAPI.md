# Introduction #

"Onions have layers. Ogres have layers. Onions have layers. You get it? We both have layers" -- Shrek. Crank like Ogres has layers like an onion. The top layer can be any number of rendering technology, such as JSF, Struts, Spring MVC, or Flex. The focus here is to discuss the crank-jsf-support package, and the various components within it.


## Listings ##
Listing component is a top level component in Crank JSF. There are other subcomponents that make up the listing component, such as paginator. If there are components within the listing component you can control their attributes as well, as is the case with the paginator.

**Listing componet**

```
 <a4j:region renderRegionOnly="false">
   <a4j:form id="${daoName}ListForm">
      <crank:listing
                        id="${daoName}Listing"
                        paginator="#{paginators[daoName]}"
                        jsfCrudAdapter="#{cruds[daoName]}"
                      propertyNames="id,createdBy,updatedBy,name,description,firstName,lastName"
                        pageTitle="${daoName}"
                        autoLink="false"
                        parentForm="${daoName}ListForm"
                        reRender="${daoName}ListForm"
                        crud="#{employeeCrud.controller}"
                        enableDelete="${true}"
                        isSelectableColumn="${false}"/>
  </a4j:form>
</a4j:region>
```
![http://krank.googlecode.com/svn/wiki/img/listingProperty.jpg](http://krank.googlecode.com/svn/wiki/img/listingProperty.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|id|JSF component id which should be unique to this page|String|yes|${daoName}Listing|
|paginator|paginator object for the data object|object|yes|#{paginators[daoName](daoName.md)}|
|jsfCrudAdapter|references the crud interfaces|object|yes|#{cruds[daoName](daoName.md)}|
|propertyNames|comma separated list of the value object's properties(make sure there are no spaces)|String|only properties to display|none|
|pageTitle|Used to display the page title on the paginator|String|NO|Listing|
|autoLink|Crank can determine if a string might be a URL and will attempt to format it as a clickable link.Set to false to disable this feature.|boolean|NO|TRUE|
|parentForm|Reference to the parent form object in this case an a4j form|String|yes|${daoName}ListForm|
|reRender|Re-rendering component reference|String|yes|${daoName}ListForm|
|crud|Reference to the crank JsfCrudAdapter controller|Object|Yes|  |
|enableDelete|Flag to show and allow deletion of records from the list in the Action coumn|boolean|no|true|
|isSelectableColumn|Enables the display of the select row column|boolean|no|true|
|enableEdit|Flag to show and allow editing of a row item in the Action column|boolean|no|true|
|debug|Flag to show crank debug information|boolean|no|false|


---

## Paginator ##
This a component within the listing component that handles filtering and pagination of the listings. The paginator being a child within the JSF component tree can be managed from the parent crank:listing. In otherwords, the property/parameters set here can and should be set on the crank:listing component.

**Paginator componet**
![http://krank.googlecode.com/svn/wiki/img/paginator_controlls.jpg](http://krank.googlecode.com/svn/wiki/img/paginator_controlls.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|disableExport|Flag to disable export button|boolean|no|false|
|readOnly|Flag to disable not allow any changes to fields this removes the green + and the edit and delete options in the Actions column|boolean|no|false|
|enableAdd|Flag to enable the + add button|boolean|no|true|
|debug|Flag to show crank debug information|boolean|no|false|



---

## Form ##
The crank form allows for adding or editing a selected row. This is a top level form component which can have other detail controllers within it, crank fields, selectManyListing,selectManyByIdListing,simpleField and others.

**Form componet**
```
 <crank:form
                        id="${daoName}DetailForm"
                        crud="#{employeeCrud.controller}"
                        propertyNames="id,createdBy,updatedBy,name,description,firstName,lastName"
                        readOnlyProperties="id,createdBy"
                        daoName="${daoName}"
                        parentForm="${daoName}DetailFormB"
                        reRender="${reRender}"
                        cancelAlwaysOn="true"
                        ajax="${true}">
   </crank:form>
```
![http://krank.googlecode.com/svn/wiki/img/master_detail_pic_controlle_mapping.jpg](http://krank.googlecode.com/svn/wiki/img/master_detail_pic_controlle_mapping.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|id|JSF component id which should be unique to this page|String|yes|${daoName}DetailForm|
|crud|Reference to the crank JsfCrudAdapter controller|Object|Yes|  |
|propertyNames|The property names, comma separated, from the data object that are to be displayable and edited|String|yes|none|
|readOnlyProperties|The property names, comma separated, which should only be displayed and not editable. You must include the read only property in the propertyNames parameter as well to make the field display, but go to read-only mode.|String|no|none|
|daoName|Data access object name|String|yes|none|
|parentForm|Reference to the parent form object in this case an a4j form can also be jsf form|String|yes|${daoName}DetailFormB|
|reRender|Re-rendering component reference|String|yes|${reRender}|
|crud|Controller for the component|Object|yes|none|
|cancelAlwaysOn|Flag to show cancel button|boolean|no|true|
|hideCancel|Flag to enable or hid cancel button|boolean|no|false|
|showFormButtons|Flag to enable or disable showing the form CREATE RESET and CANCEL buttons |boolean|no|true|
|reportingMode|  |boolean|no|false|
|debug|Flag to show crank debug information|boolean|no|false|
|ajax|Enable or disable ajax rendering|boolean|no|false|
|formFieldLabels|comma separated field that allows overriding the label display for the property named labels.|String|no|will use property names if not set|
|overrideButtonLabels|If set to true then the button labels must be specified|boolean|no|false|
|createButtonText|Text to display on create button|String|no only if overrideButtonLabels set to true|"CREATE "|
|updateButtonText|Text to display on update button|String|no only if overrideButtonLabels set to true|"UPDATE "|
|resetButtonText|Text to display on reset button|String|no only if overrideButtonLabels set to true|"Reset"|
|cancelButtonText|Text to display on cancel button|String|no only if overrideButtonLabels set to true|"Cancel"|


---

## SelectOneListing ##
This component is typically found wrapped inside a 

&lt;crank:form&gt;

 component. This is used to represents a many to one relationship in a crank form, and allows for searchable listings.

**selectOneListing component**
```
 <crank:form
                        id="${daoName}DetailForm"
                        crud="#{httpPostCrud.controller}"
                        propertyNames="id,createdBy,updatedBy,name,description,..."
                        readOnlyProperties="id,createdBy"
                        daoName="${daoName}"
                        parentForm="${daoName}DetailFormB"
                        reRender="${reRender}"
                        cancelAlwaysOn="true"
                                                reportingMode="true"
                        ajax="${true}">

                    <crank:selectOneListing jsfSelectOneController="${httpNotificationController}"
                                            propertyNames="emailAddress,assertionMessage,notifierType"
                                            parentForm="${daoName}DetailFormB"
                            />


                </crank:form>
```
![http://krank.googlecode.com/svn/wiki/img/selectOneListing.jpg](http://krank.googlecode.com/svn/wiki/img/selectOneListing.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|jsfSelectOneController|Used to reference the crank controller object|Object|yes|none|
|propertyNames|The property names, comma separated, from the data object that are to be displayable and selectable|String|yes|none|
|parentForm|Reference to the parent form object in this case an a4j form can also be jsf form|String|yes|${daoName}DetailFormB|
|autoLink|Crank can determine if a string might be a URL and will attempt to format it as a clickable link.Set to false to disable this feature.|boolean|NO|TRUE|
|useLabel| to display the select one field name|boolean|no|true|
|dataTableWidth|sizes the width of the listing to the form|percentage|no|75%|
|debug|Flag to show crank debug information|boolean|no|false|
|readOnly|NOT USED|boolean|no|false|
|expandText|Text link to display next to field to show listing|String|no|"edit"|
|collapseText|Text link to display next to field to collapse listing|String|no|"(cancel)"|
|showField|Show the select on listing field|boolean|no|true|



---

## selectOneByIdListing ##
{placeholder todo}



---

## selectMany ##
This component represents from a UI standpoint an object or form that has a many relationship. In this example, an employee has many roles.
**selectMany**
```
<crank:selectMany jsfSelectManyController="${employeeToRoleController}" 
					propertyNames="name" parentForm="employeeForm" />
```

_Selected relations shown in comma separated view_

![http://krank.googlecode.com/svn/wiki/img/selectManyStep1.jpg](http://krank.googlecode.com/svn/wiki/img/selectManyStep1.jpg)

_After selecting ... view of listings with selection option_

![http://krank.googlecode.com/svn/wiki/img/selectManyStep2.jpg](http://krank.googlecode.com/svn/wiki/img/selectManyStep2.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|jsfSelectManyController|Used to reference the many controller object|Object|yes|none|
|propertyNames|The property names, comma separated, from the data object that are to be displayable and selectable|String|yes|none|
|readOnlyProperties|The property names, comma separated, which should only be displayed and not editable, You must include the read only property in the propertyNames parameter as well to make the field display, but go to read-only mode|String|no|none|
|autoLink|Crank can determine if a string might be a URL and will attempt to format it as a clickable link.Set to false to disable this feature.|boolean|NO|TRUE|
|dataTableWidth|sizes the width of the listing to the form|percentage|no|75%|
|readOnly|To set the fields to no editable state|boolean|no|false|
|useLabel|Show and render the field label|boolean|no|true|
|expandText|Text link to display next to field to show listing|String|no|"..."|



---

## selectManyByIdListing ##
{placeholder todo}



---

## compositePanel ##
Composite panel is used to represent an object that is annotated as @Embedded inside another object. For example, address object is referenced inside a Person object. From the person object you want to edit the address object from the current form. The composite panel is referenced within a 

&lt;crank:form&gt;

 See http://code.google.com/p/krank/wiki/CrankCrudTutorial.

**compositePanel**
```
<crank:compositePanel entity="${crud.entity.address}" name="address"
					propertyNames="line_1,line2,zipCode" />
```
![http://krank.googlecode.com/svn/wiki/img/compositePanel.jpg](http://krank.googlecode.com/svn/wiki/img/compositePanel.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|entity|Used to reference the entity object within the controller|Object|yes|none|
|propertyNames|The property names, comma separated, from the data object that are to be displayable and selectable|String|yes|none|
|readOnlyProperties|The property names, comma separated, which should only be displayed and not editable|String|no|none|
|readOnly|To set the fields to no editable state|boolean|no|false|



---

## multipleField componet ##
This component is built around the concept of the composite panel. It is used to represent a list of fields that represent one line item. For example, a phone number in the database might be represented with a area code, prefix, number and extension. Rather then display this in a composite panel which shows each item on a separate line with it's associated label next to the feild you may want to display something like Home phone and each field next separated by dashes. This can be applied to SSN type fields, credit card and so on.

**multipleField**
```
 <crank:multipleField entity="${crud.entity.phoneInfo.home}" name="phoneInfoHome"
                                       propertyNames="areaCode,exchange,number,extension" token="-" label="Home" renderSize="4"/>
```
![http://krank.googlecode.com/svn/wiki/img/multipleField.jpg](http://krank.googlecode.com/svn/wiki/img/multipleField.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|entity|Used to reference the entity object within the controller|Object|yes|none|
|propertyNames|The property names, comma separated, from the data object that are to be displayable and selectable|String|yes|none|
|readOnlyProperties|The property names, comma separated, which should only be displayed and not editable|String|no|none|
|readOnly|To set the fields to no editable state|boolean|no|false|
|token| The separation value between each field|String|no| - (dash) |
|renderSize|The size of the input field|Number|no|40|
|label| Label to be applied to the set of fields.|String|no| blank|



---

## commandLink ##
{placeholder todo}



---

## crudBreadCrumb ##
This component renders a bread crumb trail for a user experience. The breadcrumb trail creates navigation links to forms, listings, and the home page of the crank JSF enabled application. This component does not sit inside a 

&lt;crank:form&gt;

. It can create a trail based on the crank crud controller object.

**crudBreadCrumb component**
```
<crank:crudBreadCrumb crud="${employeeCrud.controller}" />
```
![http://krank.googlecode.com/svn/wiki/img/breadCrumb.jpg](http://krank.googlecode.com/svn/wiki/img/breadCrumb.jpg)

| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|crud|Used to reference the entity object's the controller|Object|yes|none|


---

## field ##
This is the lowest level component of the jsf support api. The field component is meant to render jpa annotated fields and provide some levels of validation based on additional annotations. There are different data types and the field component will render them according to their type. This can be boolean, string, long strings, enum, and date. Additional not associated to a data type is many to one as a selection, and file.

String fields when they are larger than 80 characters will be rendered as an input text area. Boolean fields are rendered as a check-box. Date fields are rendered as a input text field for manual entry and with a button that pops-up a calendar widget; so users can select a date which fills the input text field. Enum type objects are rendered as a select one drop-down.

The additional fields such as a file object allows upload feature. The many to one relationship is rendered as a select one drop down.

Fields are rendered with a label of the annotated field name and an associated input field. Most cases when dealing with crank the developer won't have to use a field component as it is rendered automatically when specified in the crank:form component. The rare cases that do require a field component modification are because the developer needs some overriding feature. When this is the case and the field is encapsulated in the crank:form component. **Do not include the field in the form propertyNames list in the crank:form**.

**field**
```
  <crank:form
                        crud="#{eduInquiryCrud.controller}"
                        propertyNames=""
                        createButtonText="Create Inquiry"
                        updateButtonText="Update Inquiry Information"
                        cancelButtonText="Cancel"
                        resetButtonText="Reset"
                        ajax="${false}">
                    <h:panelGrid id="${crud.name}_${name}FieldsFormPanelGridId"
                                 columns="3" rowClasses="formRowClass1, formRowClass2">
                        <crank:field entity="#{crud.entity}" type="#{crud.entity.class}" fieldName="firstName" forceSmallText="true"/>
                        <crank:field entity="#{crud.entity}" type="#{crud.entity.class}" fieldName="lastName" forceSmallText="true"/>
                        <crank:field entity="#{crud.entity}" type="#{crud.entity.class}" fieldName="email" forceSmallText="true"/>
                    </h:panelGrid>
```


| **Parameters** | **Description** | **Type** | **Required** | **Default** |
|:---------------|:----------------|:---------|:-------------|:------------|
|entity|Used to reference the entity object's the controller|Object|yes|none|
|type|Used to reference the entity object's class type|Object|yes|none|
|fieldName|Used to display the field label|String|yes|none|
|forceSmallText|To force string fields with lengths larger than 80 characters to render a normal input text field but still retain length validation|boolean|no|false|
|renderSize|The default input text field length is normally used but some cases may require to render a smaller input text field|integer|no|40|



---

## autoComplete ##
{placeholder todo}