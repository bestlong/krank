# Introduction #

This is a quick overview of how to setup I18n messages with Crank.
This assumes you have gone through the getting started tutorial on Crank.

## Changing labels and messages ##

Crank uses a utility class called `JSFMessageUtils`. `JSFMessageUtils` is a static method whose methods are exposed to the Universal EL as functions.

You can inject a `ResourceBundleLocator` into `JSFMessageUtils` so that it can find the message resources. By default, `JSFMessageUtils` uses the `JSFResourceBundleLocator`, which uses the resource bundle setup in faces-config.xml as follows (for example):

```
  <application>
    <message-bundle>messages</message-bundle>
  </application>
```

(There is another Locator that works with Spring application context. We also have an internal one that reads bundles from the db.)

You can modify field and entity labels in Crank. By default all labels are caclulated by turning camel case words into regular words, e.g., `EmployeeStatus` becomes "Employee Status", `firstName` becomes "First Name". Thus, if your domain object represent the world and you only support one language (i.e., English), then you do nothing with resource bundles (reasonbable defaults).

If you want to support more than one language than you use the standard `ResourceBundle` business (see Java-Docs on `ResourceBundle`s for more details).

### Example ###
Let's say that we want to change firstName from its default of "FirstName" to "Given Name", then you would configure it in messages.properties (which is on the classpath) as follows:

```
firstName=Given Name
```

Everywhere that there was a firstName property being used, its label would become "Given Name" (in listings, forms, and other facelets comp components).

### Getting more specific ###
Well let's say that you have two domain object that use firstName, namely `User` and `Employee`. For User you want to use "First Name" (the default), but for Employee, you want to use "Given Name". If you use the last approach, both would be "Given Name", which is not what we want.

To only change firstName label of the Employee object to "Given Name" put the following key in the resource bundle:

```
Employee.firstName=Given Name
```

### Actual code ###

Here is the actual code that does a lot of the work:

```
	public static String createLabelWithNameSpace(final String namespace, final String fieldName,
			final ResourceBundle bundle) {

		String label;

		/** Look for fieldName, e.g., firstName. */
		try {
			try {
				label = bundle.getString(namespace + '.' + fieldName);
			} catch (MissingResourceException mre) {
				label = bundle.getString(fieldName);
			}
		} catch (MissingResourceException mre) {
			label = generateLabelValue(fieldName);
		}

		return label;
	}

```

First it looks for the namespace (which for Crud in the name of the Entity) and the fieldName in the resource bundle. Then it looks up just the fieldName in the resource bundle. If it can't find a value for either key, it generates it.