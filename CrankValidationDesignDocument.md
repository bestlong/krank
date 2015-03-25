## Introduction ##

This document discusses the current design of the Crank Validation framework. Specifically it covers the base framework not the specific JSF and Spring MVC bindings.

The document starts off talking about the Spring MVC and JSF neutral parts of the framework. Later it does discusses the Spring MVC and JSF bindings just enough to understand how the base framework is used.

This is not a getting started guide. If you are looking for a programming guide, the closest we have is [here](ExtendingCrankValidationWritingNewRules.md). There is also a [JSF example validation app](http://krank.googlecode.com/svn/trunk/crank-jsf-validation-sample/) and a [Spring MVC example validation app](http://krank.googlecode.com/svn/trunk/crank-springmvc-validation-sample/).

## Overview of Crank Validations ##

One of the main design principles behind Crank Validation is to allow for a rich set of validation metadata that can be read from annotations, property files, and a database.

The data to configure a validation rule is separate from the implementation that consumes it. This allows us to swap implementations of design rules to fit a specific set of business requirements. The swapping of one implementation for another is done primarily with Spring's IoC container, but could easily be done with Guice or some custom IoC/DI container.

This is a feature that we have used extensively to customize validation rules based on client's specific requirements. It is felt by the team that this is a good way to go. We rely heavily on the flexibility that this separation of concern provides.

There is not a one-to-one mapping between Validation Metadata and its corresponding validation rule. This allows us to use composition to write validation rules.


## Reading Validation Metadata ##

`ValidatorMetaDataReader` is an extension point for reading validation metadata.  There are currently three implementations framework, (and a customer specific implementation that reads the data out of a database for a custom workflow engine) as follows:

  * `PropertiesFileValidatorMetaDataReader` reads validation metadata from a Java properties file.
  * `AnnotationValidatorMetaDataReader` reads the validation metadata from Java 5 Annotation.
  * `ChainValidatorMetaDataReader` allows you to configure multiple readers


#### UML Class Diagram Showing Validation Framework Readers ####
> ![http://krank.googlecode.com/svn/wiki/img/validation-metadata.png](http://krank.googlecode.com/svn/wiki/img/validation-metadata.png)

We used `ChainValidatorMetaDataReader` on a project recently to allow validation rules to be written using Annotation, but overridden on a workflow basis, based on validation metadata stored in a relational database.

## Field Validation ##

When designing field validation, we first took a long hard look at other frameworks (WebWork, Tapestry, Apache Commons Validaitions, Spring Modules Validation etc.). None quite fit what we are trying to do and none fit our exact usage. That said, the `FieldValidation` framework relies heavily on the architecture and design of WebWork validation and Tapestry validations (mostly WebWork). It was inspired the most by those two frameworks. It adds the concept of composition validations. Out of the box it comes with many, many validation rules. One reason it has so many is because it leans on the DI container (Spring but could be Guice) and the composition pattern so you can configure many validation rules without writing code for each.

The heart of the Field validation framework is the `FieldValidation` interface and the `AbstractValidator` classes. The `CompositeValidator` implements the composition design pattern and let's you configure many validation rules out of other validation rules. There are several rules that simplify working with regular expressions as well. Notice also the `CommonBridgeValidator` is a bridge to the Apache commons validation framework. You could easily write other bridges, thus allowing you favorite validation framework to use our flexible validation-metadata readers and support for Ajax and JavaScript. Please read the comments in the UML diagram as follows:

#### UML Class Diagram Showing Field Validation ####
> ![http://krank.googlecode.com/svn/wiki/img/field-validation.png](http://krank.googlecode.com/svn/wiki/img/field-validation.png)

Notice that the validation annotations are not mapped one-to-one with the number of validation classes.

#### UML Class Diagram Showing Validation Annotations ####
> ![http://krank.googlecode.com/svn/wiki/img/annotation-validation.png](http://krank.googlecode.com/svn/wiki/img/annotation-validation.png)

## Resolving Error Messages ##

The `ValidatorMessage` holds the detail and summary message for validation errors. A `FieldValidator` can produce many `ValidatorMessage`s and returns them via the `ValidatorMessageHolder` (ValidatorMessageHolder validate(Object fieldValue, String fieldLabel)). A `ValidatorMessageHolder` can be either a `ValidatorMessages` which is a group of `ValidationMessage`s or a `ValidationMessage` which is a single `ValidationMessage` illustrated in UML as follows:

#### UML Class Diagram Showing Validation Annotations ####
> ![http://krank.googlecode.com/svn/wiki/img/validation-message.png](http://krank.googlecode.com/svn/wiki/img/validation-message.png)


A `FieldValidator` produces `ValidationMessage`. Generally classes that implement `FieldValidator` produces `ValidationMessage` by using a `MessageSpecification`. To simplify configuration, an `AbstractValidator` is a `MessageSpecification` (it started off having a `MessageSpecification` but seemed to suffer from having too many fine grained objects which made it difficult to configure in the Spring application context so for simplification we combined the concepts). Most `FieldValidator` are subclasses of `AbstractValidator`.

The `MessageSpecification` contains information about how to generate a message.  The `MessageSpecification` knows how to create a message. The `MessageSpecification` will look up the message in the resource bundle if it starts with a "{" (called the I18n Marker).

The `MessageSpecification` uses `ResourceBundleLocator` to locate a `ResourceBundle`. How a `ResourceBundle` gets located varies for JSF, Spring and our internal custom work flow engine. This allows us to change how a `ResourceBundle` gets located. On a recent project, we had to back our `ResourceBundle` with a database and this extention point helped us.

The `MessageSpecification` specifies both a `detailMessage` and a `summaryMessage` specification.

If the `message` starts with an I18n Marker (`{`), `MessageSpecification` generates the `key` for the `ResourceBundle`. The `key` is the `message` with the curly brackets pulled off (the '{' and '}' removed). `MessageSpecification` then looks up `message` in the `ResourceBundle` using `subject` and the `key`, i.e., `subject.key`. If the `subject.key` is not found in the `ResourceBundle` then the `MessageSpecification` looks up the `message` just using the `key`. If the message key is still not found, `MessageSpecification` just returns the `key` as the `message`.

`MessageSpecification` does the above for both `detailMessage` and `summaryMessage`.

In the case of `FieldValidator`s the subject is the name of the field.

This is more clear with an example as follows: If the field is called `firstName` and the validator is called `required`. First the `message key` `firstName.validator.required` is looked up and then if the `message` is not found then the `message key` `validator.required` is looked up.

If the `message` does not start with the I18n markers then the `MessageSpecification` checks to see if the `message` has a '.' in it. If the `message` has a dot, `MessageSpecification` try to look it up using the same technique as before.

If neither the I18n marker at the start of the message is found or a dot is found in the message, then the key becomes the message.

The arguments for a `MessageSpecification` can be passed at runtime or configured in the application context (or the equivalent in Guice). If arguments are not passed at runtime, `MessageSpecification` checks to if there were arguments configured. If neither arguments are found, it generates an empty argument list. The dynamic args are used to pass for example the actual configured `length` for `LengthValidator`.

The argument list that is created is not the final argument list. If the subject is not null, a new argument list is created and the `subject`'s label is the first argument (for FieldValidators remember the subject is the field name), followed by the rest of the arguments in the list. The `subject` label is looked up in the `ResourceBundle` and if it not found it is converted from a field name to a human readable name, e.g., if `firstName` is not found in the `ResourceBundle` it is automatically converted to `First Name`. (It gets converted with MessageUtils.getLabel(getSubject(), resourceBundleLocator.getBundle()));)) Lastly, the arguments are applied to the message to produce the I18n enabled message (MessageFormat.format(message, argumentList.toArray())).

The following is illustrated with an activity diagram:

#### UML Activity Diagram Showing Resolving Error Messages ####
> ![http://krank.googlecode.com/svn/wiki/img/validation-error-message-resolution.png](http://krank.googlecode.com/svn/wiki/img/validation-error-message-resolution.png)



## Validation Metadata: Locating validation rules, Copying rule data from validation metadata to `FieldValidation` rules ##

The heart of the JSF integration is the `JSFBridgeMetaDataDrivenValidator`. The heart of the Spring MVC integration is the `SpringMVCBridgeMetaDataDrivenValidator`.

There are some key differences `JSFBridgeMetaDataDrivenValidator` works primarily with parsing EL expressions to find the object (field) and its parent (typically a domain object). While the `SpringMVCBridgeMetaDataDrivenValidator` focuses on walking the object/property tree (given spring MVC property bindings) and figuring out the object (field) and parent (typically the commandObject in Spring speak). Once they have the information they need, they then process the information the same way. Below is a general high level flow (with some of the details of JSF and Spring MVC left out for brevity). Also some caching was enabled to reduce creation of objects (this was left out of this discussion for clarity).

  * Step 1) Read the validation meta data: `ValidatorMetaDataReader.readMetaData(Class clazz, String propertyName) return (List<ValidatorMetaData>)`
  * Step 2) Create a `CompositeValidator` for each `validationMetaData` object associated with the property
  * Step 3) Based on the `names` of the `validation rules` (`FieldValidators`) look up the `validation rules` in the `ObjectRegistry` (the `ObjectRegistry` is an extension point that can have a Spring or Guice implementation, currently, only the Spring implementation exists).
  * Step 4) Once the validation rule (`FieldValidator`) is looked up, then apply the validation properties to it, literally copy the properties (stored in map) from the `validationData` to the validation rule that was looked up. Except for detailMessage and summaryMessage properties, if they are empty strings they do not replace the configured properties.
  * Step 5) After the validation rule (`FieldValidator`) is looked up and its properties are initialized then add that rule to the `CompositeValidator`.
  * Step 6) Use the newly created `CompositeValidator` to validate the property.
  * Step 7) Convert `ValidatorMessage`s to Spring and JSF equiv. messages


#### UML Activity Diagram Showing Validation Rule lookup ####
> ![http://krank.googlecode.com/svn/wiki/img/validation-rule-resolution.png](http://krank.googlecode.com/svn/wiki/img/validation-rule-resolution.png)

Additional notes on the above diagram:

The `CompositeValidator`s can be cached so it is not looked up each time. The objects looked up in the `ObjectRegistry` (backed by Spring or Guice) are prototype scope not singletons (thus the need for caching, and the reason there is no issues with copying the data). In step one, we use the `ValidatorMetaDataReader` which was described earlier so the validation metadata can be based on annotations, property files, or database entries (or combinations of the above or some other mechanism), read the **Reading Validation Metadata** for more background on this.