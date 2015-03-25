# Introduction #

Tooltips are those little boxes of text that appear on a web page when you hover the cursor over an element without clicking on that element.  They are often used to provide a hint as to what the field should contain or what will happen when you click the control.  Tooltips can be added to Crank Crud applications by annotating the fields in the entities that are maintained by the application.  Tooltips can be added to the input portion of the field or its label or both.

In the following screen shot, the cursor is hovering over the age field causing the tooltip “Age must be entered even though birth date is supplied” to be displayed.

![http://krank.googlecode.com/svn/wiki/img/ToolTip1.jpg](http://krank.googlecode.com/svn/wiki/img/ToolTip1.jpg)

# Details #

There are two annotations that can be used to add tooltips to your crud application, @ToolTip and @ToolTipFromNameSpace.  The annotations can be added to the field property itself or to its mutator method.  If @ToolTip and one of the “NameSpace” annotations are both applied to the same field, the @ToolTip annotation will be the one displayed.

The @ToolTip annotation has two elements, “value” and “labelValue” that provide tooltip text for the input portion of the field and its label respectively.  To add tooltips to a field using this annotation, simply add the text to the field property of the entity as follows:

```
public class Employee extends Person {

...

    @ToolTip(value = "Do not use an AOL or Lotus Notes internal format.",
   	     labelValue = "Employee's company e-mail address.")
    private String email;

...
}

```

Or to the property's mutator method:

```
public class Employee extends Person {

...
    @ToolTip(value = "Age must be entered even though birthdate is supplied.",
    	     labelValue = "Employee's age at time of hire.")
    public void setAge( int age ) {
        this.age = age;
    }


...
}

```


If your application is JSF based you may have the tooltips derive from a resource bundle using the @ToolTipFromNameSpace annotation.

1. Add your tooltip text to the appropriate properties file.  The property for the tooltip that is displayed over the input portion of the field is should be named “fieldName.toolTip”.  The fields label can also have a tooltip defined by the property name “fieldName.labelToolTip”. You can also use the more specific version of the field name “entity.fieldName” (see CrankMessaging for details on defining the name space.)

messages.properties
```
...

firstName.toolTip=The first or given name.
firstName.labelToolTip=The given, first or Christian name.

...
```


2.Add the @ToolTipFromNameSpace annotation to the field or mutator of the entity.  Note that @ToolTipFromNameSpace is a marker annotation and has no elements.

```
public class Person implements Serializable {

    @ToolTipFromNameSpace
    @Column( nullable = false, length = 32 )
    private String firstName;
...
}

```