# Introduction #

It is unlikely that you want to display large stack traces to end users. It is funny how much end users do not like large stack traces when something goes wrong. Exceptions do happen. You should handle them and wrap them. However, we do provide some default wrapping of error message such that the message of the exception show up as JSF messages and the actual stack trace gets logged.

We have two ways of accomplishing this:

  1. `org.crank.crud.jsf.support.JsfMessageActionListener`
  1. `org.crank.crud.jsf.support.JsfMessageInterceptor`

You can decorate idividual objects with `JsfMessageInterceptor`. Or, you can decorate all action calls (thus, more or less all objects) with `JsfMessageActionListener`. `JsfMessageActionListener` is the prefered approach.

Both ways classes have a common base class that puts the current exceptions information (message, stack) as a string in the request scope under `crankErrorException`.

To configure JsfMessageActionListener do the following in the faces-config.xml:

```
  <application>
    <action-listener>org.crank.crud.jsf.support.JsfMessageActionListener</action-listener>
    ...    
  </application>

```

`JsfMessageInterceptor` is a Spring `MethodInterceptor` and can be configured as such with for example a `BeanNameAutoProxy`.

Both techniques expose a `MessageManager` via a utility class called `MessageManagerUtils`, which classes can use to add, status, warning, error, etc. messages that can be displayed with `h:messages`.

`MessageManager` and `MessageManagerUtils` are not tied to JSF per se.

Here is an example usage of adding a status message.

```
    /** Create an object. */
    public CrudOutcome create() {
        if (fileUploadHandler!=null) {
            fileUploadHandler.upload( this );
        }
        fireBeforeCreate();
        CrudOutcome outcome = doCreate();
    	MessageManagerUtils.getCurrentInstance().addStatusMessage("Created");        
        fireAfterCreate();
        return outcome;
    }

```

Here is the MessageManager interface:

```
package org.crank.message;

import java.util.List;

public interface MessageManager {
	
	public void addStatusMessage(String message, Object... args);
	public void addErrorMessage(String message, Object... args);
	public void addFatalMessage(String message, Object... args);
	public void addWarningMessage(String message, Object... args);
	
	public List<String> getStatusMessages();
	public List<String> getErrorMessages();
	public List<String> getFatalMessages();
	public List<String> getWarningMessages();
}

```