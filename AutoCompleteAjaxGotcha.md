At work, we’re starting to do some pretty cool things with RichFaces. Among them is a controller that uses three Crank Auto-complete Controller components to allow the user to select a specific item from a hierarchy.   The Auto-complete Controller is based on the RichFaces suggestionBox, and automatically includes the field to be completed and the suggestionBox in a single component.

The problem we are solving with this component was similar to this - Imagine selecting a car by specifying manufacturer, make, and model from a set of three Auto-complete fields, where you could start by selecting from any of the three. As selections are made the higher order fields are automatically completed and the list of lower order values is automatically constrained.  For example if you start with manufacturer and choose ‘GM’, only GM makes would appear in the makes suggestions list.  If you then choose ‘Chevrolet’ from the makes list, only Chevys would appear in the models list.  From the short list of models, you might then select ‘Impala’.

You might arrive at the same state by starting with the model directly.  Without the manufacture and make selections, however, the model list would contain every model known to the application (hence the use of auto-complete rather than a drop-down box).  You could then select ‘Impala’ directly and have the ‘GM’ manufacturer and ‘Chevrolet’ make automatically selected for you.  Likewise you could have started by selecting the make and have the manufacturer set and the model list constrained.

We had hoped to use Ajax to have the related selections appear in the high order fields automatically when a low order field was completed via the Auto-complete Controller. In developing this component, we became aware of a problem with suggestionBox.  It seems that suggestionBox fails when included in an a4j outputPanel or when included in a panelGroup that has its id included in the suggestionBox's reRender set.

To illustrate the failure, here’s a little sample code.  This is not the code from the actual component, but it demonstrates the problem equally well.  To prove that the problem is in RichFaces, the Crank components have been eliminated and their results have been coded directly.

The following page works fine as coded –

```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
  xmlns:ui="http://java.sun.com/jsf/facelets"
  xmlns:h="http://java.sun.com/jsf/html"
  xmlns:f="http://java.sun.com/jsf/core"
  xmlns:a4j="https://ajax4jsf.dev.java.net/ajax"
  xmlns:rich="http://richfaces.ajax4jsf.org/rich"
  xmlns:c="http://java.sun.com/jstl/core"
  xmlns:crank="http://www.googlecode.com/crank"
  xmlns:t="http://myfaces.apache.org/tomahawk">

  <ui:composition template="/templates/layout.xhtml">
    <ui:define name="content">

      <span class="pageTitle">SuggestionBox</span>

      <a4j:form id="sbForm">

        <h:panelGrid columns="3" rowClasses="formRowClass1, formRowClass2"
          columnClasses="topLeft">
          <a4j:outputPanel ajaxRendered="true" keepTransient="true">
            <h:outputLabel id="properyLabel" rendered="${true}"
              value="Property" for="suggest"
              styleClass="${formLabelStyleClasses}" />
          </a4j:outputPanel>

          <h:panelGroup>
            <h:inputText
              id="suggest"
              value="#{sbController.property}"
              styleClass="${styleClass}">
            </h:inputText>

            <rich:suggestionbox
              id="properySuggestBox"
              for="suggest"
              ajaxSingle="true"
              suggestionAction="#{sbController.autocomplete}"
              var="result">
              <h:column>
                <h:outputText value="${result.text}" />
              </h:column>
            </rich:suggestionbox>
          </h:panelGroup>
        </h:panelGrid>
      </a4j:form>
    </ui:define>
  </ui:composition>
</html>

```

And renders as follows -

![http://krank.googlecode.com/svn/wiki/img/AutoCompleteAjaxGotchaPage1.jpg](http://krank.googlecode.com/svn/wiki/img/AutoCompleteAjaxGotchaPage1.jpg)

However, changing the enveloping panelGroup to an a4j ouputPanel as follows –

```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
  xmlns:ui="http://java.sun.com/jsf/facelets"
  xmlns:h="http://java.sun.com/jsf/html"
  xmlns:f="http://java.sun.com/jsf/core"
  xmlns:a4j="https://ajax4jsf.dev.java.net/ajax"
  xmlns:rich="http://richfaces.ajax4jsf.org/rich"
  xmlns:c="http://java.sun.com/jstl/core"
  xmlns:crank="http://www.googlecode.com/crank"
  xmlns:t="http://myfaces.apache.org/tomahawk">

  <ui:composition template="/templates/layout.xhtml">
    <ui:define name="content">

      <span class="pageTitle">SuggestionBox</span>

      <a4j:form id="sbForm">

        <h:panelGrid columns="3" rowClasses="formRowClass1, formRowClass2"
          columnClasses="topLeft">
          <a4j:outputPanel ajaxRendered="true" keepTransient="true">
            <h:outputLabel id="properyLabel" rendered="${true}"
              value="Property" for="suggest"
              styleClass="${formLabelStyleClasses}" />
          </a4j:outputPanel>
          <!-- This is what changed, it was a panelGroup. -->
          <a4j:outputPanel ajaxRendered="true">
            <h:inputText
              id="suggest"
              value="#{sbController.property}"
              styleClass="${styleClass}">
            </h:inputText>

            <rich:suggestionbox
              id="properySuggestBox"
              for="suggest"
              ajaxSingle="true"
              suggestionAction="#{sbController.autocomplete}"
              var="result">
              <h:column>
                <h:outputText value="${result.text}" />
              </h:column>
            </rich:suggestionbox>
          </a4j:outputPanel>
        </h:panelGrid>
      </a4j:form>
    </ui:define>
  </ui:composition>
</html>

```



Yields this result –

![http://krank.googlecode.com/svn/wiki/img/AutoCompleteAjaxGotchaPage2.jpg](http://krank.googlecode.com/svn/wiki/img/AutoCompleteAjaxGotchaPage2.jpg)

The same result occurs if you add the id of the panel group to the reRender tag of the suggestionBox.

I’ve reported this as a bug to the RichFaces project http://jira.jboss.com/jira/browse/RF-1235 but until there’s a final resolution, be careful not wrap the auto-complete components in Ajax active containers.