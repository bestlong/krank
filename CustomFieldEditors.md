# Introduction #

If you're using Crank to generate forms in your application, you may have noticed that the type of the HTML input element varies based on the Java type of the field it's bound to.  For instance, Crank will generate a checkbox HTML element for a boolean field type, date picking HTML for a java.util.Date field type, etc.  This is generally good behavior, but sometimes it's necessary to vary the type of input in a more dynamic way.

Suppose you had an entity with a String field 'foo' and an Enumerated field 'fooType':
```
@Entity
class E {
   enum FooType { String, Boolean, Date };
   ...
   private String foo;
   private FooType fooType;
   ...
}
```
It would be nice to get Crank to display different 'foo' input elements to the user, based on the runtime value of fooType.


# Details #

By default Crank bases its field editors on the static Java type of the field itself, so boolean and Boolean fields result in checkboxes, variants of Date become a date picker, and all other fields are rendered as input text.  You can specify a non-default editor for any field (or fields) by adding a few attributes into your current Crank CRUD tags.  Consider the following Crank tag that uses the default editor for a 'name' field:

```
<crank:form crud="${crud}" 
   propertyNames="name" 
/>
```

Not a particularly interesting form, but would generate a web form with a single input text field bound to the name property of an [unspecified](unspecified.md) entity.  Now here's the same form updated to use a custom editor for the name field:

```
<crank:form crud="${crud}" 
   propertyNames="name" 
   property.name.editor="/editors/departmentNameEditor.xhtml" 
/>

```

The updated form now specifies that Crank should include the custom facelet '/editors/departmentNameEditor.xhtml' as the editor for the name field of the currently selected entity.  This custom facelet could be anything, from a tree control to a set of radio buttons, provided it sets the appropriate value into the name field.  For a simple example, the facelet below is included:

```
<ui:composition>
   <h:inputText value="#{entity[fieldName]}"/>	
</ui:composition>
```

Crank defines the 'entity' and 'fieldName' vars as EL variables representing the persistent entity and field name currently under edit, respectively.

It's also possible to customize the label and message output within Crank.  Normally Crank generates a label field and message area for each displayed property, but with a custom editor it's possible that you'd like to omit them or generate them within the editor.  To manipulate these properties use the showLabel/showMessages attributes:

```
<crank:form crud="${crud}" 
   propertyNames="name" 
   property.name.editor="/editors/departmentNameEditor.xhtml" 
   property.name.showLabel="true"
   property.name.showMessages="true"
/>
```