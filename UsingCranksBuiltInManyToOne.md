Crank supports automatically rendering many to one support by providing a dropdown menu selection. This had some slight issues.

The technique we used worked with JSF 1.1, but with the introduction of the Universal EL, it does not. The problem is that the SelectOne component in JSF does not consult any other JSF Application object except the default so you can't replace the functionality to look up a converter.

The first solution was to get the JSF components themselves and hack them. This had some issues as well. Now we use the built in JSF components, but register converters in faces-config.xml as follows:

```
  <converter>
  	<converter-for-class>org.crank.crud.model.Department</converter-for-class>
  	<converter-class>org.crank.crud.jsf.support.EntityConverter</converter-class>
  </converter>

  <converter>
         <converter-for-class>com.vmc.stf.validation.ValidatorTypeBO</converter-for-class>
         <converter-class>org.crank.crud.jsf.support.EntityConverter</converter-class>
  </converter>
	
  <converter>
       <converter-for-class>com.vmc.stf.view.converter.ConverterType</converter-for-class>
       <converter-class>org.crank.crud.jsf.support.EntityConverter</converter-class>
   </converter>


```

Again the problems seems to be that the Universal EL does not consult the converter that you register with the JSF component via <f:converter, or the converter that you return using a custom JSF Application object. If either of these worked then we could provide this feature automatically like we did in JSF 1.1.