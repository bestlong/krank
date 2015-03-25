This document will take you on a wild ride through crank validation.

To use a custom validator, you can use the sample validationContext.xml as a guide which you can find in the jsf sample and the SpringMVC sample. Remember that the validation framework works with JSF, and SpringMVC. It allows for three forms of validation: Java, generated JavaScript that runs on the browsers and Ajax.

(I am looking for volunteers who will port this to Struts 2/WebWork. Any takers?)

You can replace any of the built in validations with your own validation. You can plug-in different meta-data sources for your validation rules (essentially you can configure you validation rules with Spring, Annotations, Database, Properties files or any combination thereof). It currently has a commons validator bridge (looking to write a Hibernate validation bridge and others).

To validate a property just mark it as so:
```
@Required ()
    public void setAddress1( String address1 ) {
        this.address1 = address1;
    }

    @Required @Zip @Zip2Address
    public void setZipCode( ZipCodeBO zipCode ) {
        this.zipCode = zipCode;
    }
```
Notice that the property zip-Code takes three different validation rules. @Required and @Zip are built-in rules. @Zip2Address is a custom validation rule.

That is it. You add the annotations. Whether you are using Spring or JSF, it picks up the annotations and validates. Now let's cover something more advanced.

### Writing custom validations ###

In this example we want to override the built in Required support to do some special Required support.

Here is how the built in Required support is defined:
```
package org.crank.validation.validators;


import org.crank.validation.ValidatorMessage;
import org.crank.validation.ValidatorMessageHolder;


/**
 *
 * <p>
 * <small>
 * Required validator.
 * </small>
 * </p>
 * @author Rick Hightower
 */
public class RequiredValidator extends AbstractValidator {

    public void init () {
        this.setDetailMessage( "{validator.required.detail}" );
        this.setSummaryMessage( "{validator.required.summary}" );
    }

    public ValidatorMessageHolder validate(Object object, String fieldLabel) {
        ValidatorMessage message = new ValidatorMessage();

        if (object instanceof String) {
            String string = (String) object;
            boolean valid =  string != null && !string.trim().equals("");
            if (!valid) {
                populateMessage(message, fieldLabel);
            }

        } else {
            if (object == null) {
                populateMessage(message, fieldLabel);
            }
        }

        return message;
    }


}
```
Let's say we want to change the required support so that it checks the toString value of the object if the object is not null. To replace the above you would do this:
```
package com.somecompany.biz.fieldvalidator;

import org.crank.validation.ValidatorMessage;
import org.crank.validation.ValidatorMessageHolder;
import org.crank.validation.validators.RequiredValidator;

/**
 *
 * @author Rick Hightower
 * @version $Revision: 3574 $
 *
 */
public class ForceToStringRequiredValidator extends RequiredValidator {

    public ValidatorMessageHolder validate(Object object, String fieldLabel) {
        ValidatorMessage message = new ValidatorMessage();

        if (object instanceof String) {
            String string = (String) object;
            boolean valid =  string != null && !string.trim().equals("");
            if (!valid) {
                populateMessage(message, fieldLabel);
            }

        } else {
            if (object == null) {
                populateMessage(message, fieldLabel);
            } else {
                String string =  object.toString();
                boolean valid =  string != null && !string.trim().equals("");
                if (!valid) {
                    populateMessage(message, fieldLabel);
                }
            }
        }

        return message;
    }

}
```
Then you register the validator in the validationContext.xml file as follows:
```
<bean name="crank/validator/required"
	      parent="crank/validator"
	      class="com.somecompany.biz.fieldvalidator.ForceToStringRequiredValidator"
	      scope="prototype"
	      init-method="init"
	      />
```
Crank reads the annotations (meta-data from any source really) at run-time. Figures out the name of the annotation is Required. Then it lower cases the first letter. Then it appends crank/validator to the front (which is configurable and can be changed to work with properties files or a database) and then looks up the validator in the application context (which is also configurable and could be changed to work with Guice).

If you replace the implementation, you override the way Required works everywhere.

Now let's say you want to write a custom validation rule.

h4. Another custom validation rule

Let's say that you want to validate that zip-code matches the city and state of its parent address object.

You write your validation rule as follows:
```
package com.somecompany.biz.fieldvalidator;

import org.apache.log4j.Logger;
import org.crank.validation.ValidatorMessage;
import org.crank.validation.ValidatorMessageHolder;
import org.crank.validation.validators.AbstractValidator;
import org.crank.web.validation.spring.support.SpringValidatorContext;

import com.somecompany.biz.domain.AddressBO;
import com.somecompany.biz.domain.ZipCodeBO;

/**
 *
 * @author Rick Hightower
 * @version $Revision: 3641 $
 *
 */
public class ZipCodeToAddressValidator extends AbstractValidator {

    private Logger log = Logger.getLogger( this.getClass() );

    public ValidatorMessageHolder validate(Object object, String fieldLabel) {
        ValidatorMessage message = new ValidatorMessage();
        ZipCodeBO zipCode = (ZipCodeBO) object;
        Object parent = SpringValidatorContext.get().getParentObject();

        if (parent instanceof AddressBO) {
            AddressBO address = (AddressBO) parent;
            if (zipCode.getCity() == null || zipCode.getState() == null || zipCode.getId() == null) {
                populateMessage(message, fieldLabel);
                return message;
            }
            if (zipCode.getCity().equals( address.getCity() )
                    && zipCode.getState().getAbbreviation().equals( address.getState().getAbbreviation() )) {
                log.debug( "Zip is valid "  + object);
            } else {
                populateMessage(message, fieldLabel);
                return message;
            }
        } else {
            log.debug( "Not an address. You are not using this correctly." );
        }

        return message;
    }

}
```
The above validates that we have a proper zip code based on its state and city. The next step is to configure this in validationContext.xml as follows:
```
<bean name="crank/validator/zip2Address"
	      parent="crank/validator"
	      class="com.somecompany.biz.fieldvalidator.ZipCodeToAddressValidator"
	      scope="prototype"
	      >
	      <property name="detailMessage" value="{crank.validate.zip2address.detail}"/>
	      <property name="summaryMessage" value="{crank.validate.zip2address.summary}"/>

	 </bean>
```
Note if the message has a { then it is considered to be a message key that is to be looked up in the message bundle. If the { is missing then it is considered to be a hardcoded string.

Now the issue is that we don't have an annotation for this. Thus the next step is to add one as follows:
```
package com.somecompany.biz.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 *
 * @author Rick Hightower
 * @version $Revision$
 *
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.TYPE } )
public @ interface Zip2Address {

    String detailMessage() default "";

    String summaryMessage() default "";

}
```
Now we can start using this annotation as follows:
```
@Required @Zip @Zip2Address
    public void setZipCode( ZipCodeBO zipCode ) {
        this.zipCode = zipCode;
    }
```
The only problem is that this wont work. The framework ignores annotations from packages it does not know. In order for the validation framework to know that this is a valid annotation is to tell the AnnotationMetaData reader about it as follows:
```
<bean id="validatorMetaDataReader"
	      class="org.crank.validation.readers.AnnotationValidatorMetaDataReader" >
	 		 <property name="validationAnnotationPackages">
	 		 	<list>
	 		 		<value>org.crank.annotations.validation</value>
	 		 		<value>com.somecompany.biz.base.annotation</value>
	 		 	</list>
	 		 </property>
	 </bean>
```
By default we allow annotations in the org.crank.annotations.validation package. If you define custom annotation, you have to configure their packages like the above. Note that values defined in the annotation if not null will override configuration parameters defined in Spring.

h4. Creating different meta-data sources. By default Crank validation comes with three meta-data sources as follows:

  * AnnotationValidatorMetaDataReader Reads annotations for validation meta-data
  * PropertiesFileValidatorMetaDataReader Reads properties files for validation meta-data
  * ChainValidatorMetaDataReader Allows you to nest validation meta-data

It is easy to write your own meta-data reader, you just have to define it to implement this interface:
```
/**
 * ValidatorMetaDataReader is an extention point for classes that need
 * to read validation meta-data.
 *
 *
 * One implementation reads the meta-data from a properties file.
 * The other implementation reads the data from Java 5 Annotation.
 * @author Rick Hightower
 *
 */
@ExtentionPoint
public interface ValidatorMetaDataReader {

	public List<ValidatorMetaData> readMetaData(Class clazz, String propertyName);

}
```
On a recent project, we defined a ValidatorMetaDataReader that reads validation meta-data out of a database (which can vary based on client).

Here is an example configuration:
```
<bean name="/form.htm /start.htm" class="com.somecompany.biz.web.controller.CustomFlowController">
		<property name="commandName" value="inquiry"></property>
		<property name="commandClass" value="com.somecompany.biz.domain.SomeFormObject"/>
		<property name="validator">
	        <bean class="org.crank.web.validation.spring.support.SpringMVCBridgeMetaDataDrivenValidator">
	        	<property name="validatorMetaDataReader" ref="chainMetaDataReader"/>
	        </bean>
		</property>
...

	<bean id="chainMetaDataReader" class="org.crank.validation.readers.ChainValidatorMetaDataReader">
		<property name="chain">
			<list>
				<ref bean="validatorMetaDataReader"/>
				<ref local="customClientDataBaseMetaDataReader"/>
			</list>
		</property>
	</bean>
```
Again the ChainValidatorMetaDataReader allows you to chain validator readers. This way you can read validation data from more than one source. For example, you could read validation meta data from properties files, and annotations. The last one configured in the chain wins if you have the same type. You can have a required validation rule in a properties file and a regex in an annotation for the same property and both applied. Thus, it also merges as long as they are different types. Thus the above allowed us to override validation rules by defining them for certain clients in the database.

Notice that the main integration point for Spring is the org.crank.web.validation.spring.support.SpringMVCBridgeMetaDataDrivenValidator class.

Thanks. More documentation to come.