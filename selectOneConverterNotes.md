# Introduction #

Just some notes on how we worked around issues with JSF 1.2.


# Details #

Both Presto and Crank use a class called SpringApplication, which is a Spring enabled, JSF application class implementation.

This class is used to load converters and such that allow DI via Spring. The problem with this class is that in JSF 1.2, it uses the EL mechanism to load the converters and it by passes converters loaded with the Application object and somehow gets them right from faces-config, which of course is a bug that has not been fixed as of release 1.2.04. I created an incredible hack for Crank to get around this problem, that I hope I don't have to recreate for Presto 2.

([To learn more about this class SpringApplication go here](http://opensource.atlassian.com/confluence/spring/display/JSF/Configuring+JSF+Validators,+Converters,+and+UIComponents+in+Spring).) The problem with this approach is it does not work in JSF 1.2 at least for h:selectOneMenu.

The SpringApplication converter architecture works fine in JSF 1.1 but breaks in JSF 1.2. I traced this down for Crank about 18 months ago and came up with a workaround.

The problems is actually in the h:selectOneMenu renderer implementation, it uses the EL library to look up the converter which then bypasses the custom Application object that I setup (SpringApplication) and uses the default Application object somewhere in the bowls of the EL Impl jar. I even wrote my own renderer that worked just like the previous one but it kept breaking at odd times. Then I came up with a hack, which I will use again with Presto 2 since it always works. Essentially the SpringApplication.createConverter method does not get called with h:selectOneMenu. Now for the workaround (which I will need to reverse engineer from crank to recreate for Presto2). I first have to remember the hack, which I will now reverse engineer from Crank.

Crank uses defines a tag called field.xhtml. The field.xhtml tag displays the correct field based on the type of the property. It figures out if a field is a manyToOne and draws a selectOneMenu tag by default as follows:

#### field.xhtml ####
```
			<c:if test="${simpleType}">
		    <c:if test="${crud:isManyToOne(type,fieldName) and not forceInputText}">
		        <c:set var="propEntityName" value="${crud:getPropertyEntityName(type,fieldName)}" />
		        <a4j:outputPanel ajaxRendered="true" rendered="${rendered}" > 
		        <h:selectOneMenu id="${fieldId}" disabled="${disabled}" value="#{entity[fieldName]}" 
		        				 required="${not crud:isManyToOneOptional(type,fieldName)}"
		        				 title="${title}" >
		        		
					<f:selectItems value="${selectItemGenerators[propEntityName].listOptional}" />        				 
					<f:attribute name="beanType" value="${propEntityName}"/>
					<c:if test="${not crud:isManyToOneOptional(type,fieldName)}">
						<f:attribute name="required_bean" value="${propEntityName}"/>
					</c:if>
					<ui:insert />
				</h:selectOneMenu>
				</a4j:outputPanel>		
		    </c:if>
			</c:if>
```

The **propEntityName** gets passed as a value to the component with

```
 <f:attribute name="beanType" value="${propEntityName}"/>
```

The **propEntityName** gets defined earlier as follows:

#### field.xhtml ####
```
<c:set var="propEntityName" value="${crud:getPropertyEntityName(type,fieldName)}" />
```

The getPropertyEntityName is defined as follows:

#### CrudUtils.java ####
```
    public static String getPropertyEntityName(Class clazz, String propertyName) {
        PropertyDescriptor descriptor = getPropertyDescriptor( clazz, propertyName);

        AnnotationData data = (AnnotationData)MapUtils.convertListToMap( "name",
                AnnotationUtils.getAnnotationDataForClass( descriptor.getPropertyType(), allowedPackages )).get( "entity" );
        if (data != null) {
            String entityName = (String) data.getValues().get( "name");
            if (entityName != null && entityName.trim().length() > 0){
                return (String) data.getValues().get( "name");
            }
        }
        return descriptor.getPropertyType().getSimpleName();
    }
```

This gets the annotation and if it can't get the annotation, it just returns the simpleName of the class which is the default entity name.

The converter which we can't manager with Spring any longer thanks to the bug with JSF 1.2 is defined as follows:

#### EntityConverter.java ####
```
package org.crank.crud.jsf.support;

import java.io.Serializable;
import java.util.Map;

import org.crank.core.CrankContext;
import org.crank.core.ObjectRegistry;
import org.crank.core.LogUtils;
import org.crank.crud.GenericDao;
import org.crank.crud.controller.CrudManagedObject;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.apache.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Converts an integer string into some object.
 * 
 * @author Rick Hightower
 */
@SuppressWarnings("unchecked")
public class EntityConverter implements Converter, Serializable {

	/**
	 * Logger.
	 */
	protected Logger logger = Logger.getLogger(EntityConverter.class);

	/**
	 * DAO crud to look up Entity based on id.
	 */
	private GenericDao dao;

	/**
	 * Name of id property. Defaults to "id".
	 */
	private String idPropertyName = "id";

	/**
	 * Class of id. Defaults to java.lang.Long.
	 */
	private Class idType = Long.class;

	/**
	 *
	 */
	private CrudManagedObject managedObject;

	/**
	 * Is the id primitive. Defaults to false.
	 */
	private boolean primitive = false;

	/**
	 * Converts the value into an entity. The type of the entity depends on the
	 * type that the daoCrud returns.
	 * 
	 * @param facesContext
	 *            current faces context
	 * @param component
	 *            current component
	 * @param value
	 *            current value submitted from user.
	 * 
	 * @return An Entity
	 * 
	 */
	public Object getAsObject(final FacesContext facesContext,
			final UIComponent component, final String value) {
		logger.debug(String.format(
				"getAsObject() called value=%s, component=%s, value class=%s",
				value, component.getClientId(facesContext), value.getClass()
						.getName()));
		UIInput input = (UIInput) component;
		if (value.equals("-1")
				&& (input.isRequired() || component.getAttributes().get(
						"required_bean") != null)) {
			logger.debug("Required field and the value was -1");
			throw new ConverterException(new FacesMessage("Required",
					"Required"));
		}

		try {
			Serializable entityId = CrudUtils.getIdObject(value, this.idType);
			logger.debug(String.format("entityId %s", entityId));
			if (dao == null) {
				ObjectRegistry objectRegistry = CrankContext
						.getObjectRegistry();
				Map<String, GenericDao> repos = (Map<String, GenericDao>) objectRegistry
						.getObject("repos");

				if (managedObject != null) {
					logger.debug("Looking up DAO by managedObject");
					dao = repos.get(managedObject.getName());
				} else {
					Object key = component.getAttributes().get("beanType");
					logger.debug("Looking up DAO by beanType");
					dao = repos.get((String) key);
				}

			}
			Object object = dao.read(entityId);
			logger.debug(String.format("Read object %s", object));
			if (object == null) {
				if ("-1".equals(value)) {
					logger.debug("No object found and the value was -1");
					throw new ConverterException(new FacesMessage("Required",
							"Required"));
				} else {
					throw new ConverterException(new FacesMessage(
							"Can't find object with id " + value,
							"Can't find object with id " + value));
				}
			}
			LogUtils.debug(logger, "Returning converted object %s", object);
			return object;
		} catch (ConverterException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error("Unable to convert object", ex);
			String message = String.format(
					"Unable to convert, fatal issue, %s ", ex.getMessage());
			throw new ConverterException(new FacesMessage(message, message));

		}
	}

	/**
	 * Converts the entity to a String.
	 * 
	 * @param facesContext
	 *            current faces context
	 * @param component
	 *            current component
	 * @param value
	 *            current value of the entity
	 * 
	 * @return value converted to string.
	 * 
	 */
	public String getAsString(final FacesContext facesContext,
			final UIComponent component, final Object value) {

		logger.debug(String.format(
				"getAsString called value=%s, component=%s, value class=%s",
				value, component.getClientId(facesContext), value.getClass()
						.getName()));
		if (value == null) {
			logger.debug("Value was null, can't convert");
			return "";
		}

		if (value instanceof String) {
			logger.debug("Value was a string");
			return value.toString();
		}

		BeanWrapper bwValue = new BeanWrapperImpl(value);

		try {
			String sValue = bwValue.getPropertyValue(idPropertyName).toString();
			logger.debug(String.format("string value %s", sValue));
			return sValue;
		} catch (Exception ex) {
			logger.debug("Unable to find value returning -1");
			return "-1";
		}
	}

	/**
	 * Exposed for Spring to do injection.
	 * 
	 * @param aDaoCrud
	 *            DAOCrud used to look up Entity.
	 */
	public void setDao(final GenericDao aDaoCrud) {
		this.dao = aDaoCrud;
	}

	/**
	 * Is the id primitive or an object wrapper?
	 * 
	 * @return if the id is primitive.
	 */
	public boolean isPrimitive() {
		return primitive;
	}

	/**
	 * Is the id primitive or an object wrapper?
	 * 
	 * @param aPrimitive
	 *            set to this.
	 */
	public void setPrimitive(final boolean aPrimitive) {
		this.primitive = aPrimitive;
	}

	/**
	 * @param aIdPropertyName
	 *            idPropertyName
	 */
	public void setIdPropertyName(final String aIdPropertyName) {
		this.idPropertyName = aIdPropertyName;
	}

	/**
	 * Class type of id.
	 * 
	 * @param aClassTypeOfId
	 *            Class type of id.
	 */
	public void setIdType(final Class aClassTypeOfId) {
		idType = aClassTypeOfId;
	}

	public CrudManagedObject getManagedObject() {
		return managedObject;
	}

	public void setManagedObject(CrudManagedObject managedObject) {
		this.managedObject = managedObject;
	}

}

```

The converter checks to see if dao is null (which it will be for any JSF 1.2 app).
If it is, it uses the ObjectRegistry (which is just a helper class that wraps the app context with the idea that one day Crank might use Guice or webbeans or something).

```
			if (dao == null) {
				ObjectRegistry objectRegistry = CrankContext
						.getObjectRegistry();
				Map<String, GenericDao> repos = (Map<String, GenericDao>) objectRegistry
						.getObject("repos");

				if (managedObject != null) {
					logger.debug("Looking up DAO by managedObject");
					dao = repos.get(managedObject.getName());
				} else {
					Object key = component.getAttributes().get("beanType");
					logger.debug("Looking up DAO by beanType");
					dao = repos.get((String) key);
				}

			}

```

Notice it uses the beanType we passed earlier to look up the dao.

```
          Object key = component.getAttributes().get("beanType");
          logger.debug("Looking up DAO by beanType");
          dao = repos.get((String) key);
```


The EntityConverter from Presto is very similar as follows:

```
package qcom.cas.commons.faces;

import java.io.Serializable;
import qcom.cas.commons.dao.DAOCrud;
import qcom.cas.commons.model.Entity;
import qcom.cas.commons.model.ModelUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;


/**
 * Converts an integer string into an Entity.
 *
 * @author Rick Hightower
 */
public class EntityConverter implements Converter {
    /**
     * DAO crud to look up Entity based on id.
     */
    private DAOCrud daoCrud;

    /**
     * Converts the value into an entity.
     * The type of the entity depends on the type that the daoCrud returns.
     *
     * @param facesContext current faces context
     * @param component current component
     * @param value current value submitted from user.
     *
     * @return An Entity
     *
     */
    public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
        Serializable entityId = ModelUtils.getIdObject(value, daoCrud.getClassTypeOfId());
        return daoCrud.read(entityId);
    }

    /**
     * Converts the entity to a String.
     *
     * @param facesContext current faces context
     * @param component current component
     * @param value current value of the entity
     *
     * @return DOCUMENT ME!
     *
     */
    public String getAsString(final FacesContext facesContext,
        final UIComponent component, final Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof Entity) {
            return ((Entity) value).getObjectId().toString();
        }

        return value.toString();
    }

    /**
     * Exposed for subclasses.
     *
     * @return current DaoCrud exposed.
     */
    protected DAOCrud getDaoCrud() {
        return daoCrud;
    }

    /**
     * Exposed for Spring to do injection.
     *
     * @param aDaoCrud DAOCrud used to look up Entity.
     */
    public void setDaoCrud(final DAOCrud aDaoCrud) {
        this.daoCrud = aDaoCrud;
    }
}

```

The difference is we don't use lookups as they are not needed which is the crank would have been had JSF 1.2 worked like JSF 1.1 and we could use the SpringApplication object.

So if we are going to upgrade Presto to use JSF 1.2, we need to change it not to use injection in the converter but to look up the dao like Crank does at least until JSF gets fixed (which seems doubtful).

Presto has many options for editing fields. One option is defined in dropdown.jspx as follows:

#### dropdown.jspx ####
```
                    <h:selectOneMenu converterId="#{converterId}" id="#{id}" value="#{entity[fieldName]}" styleClass="dropdown" onchange="#{onchange}">
                        <f:selectItem itemLabel="#{instructionItemLabel}" itemValue="-1"/>
                        <f:selectItems value="#{selectItems}"/>
                    </h:selectOneMenu>
```

The sample app in Presto configures the converters in spring, we will have to change it to configure the converts in JSF (faces-config.xml).


The sample app in Presto configures the dao's in spring as follows:

#### dao-config.xml ####
```
    <bean id="wordDAOCrud" parent="simpleDAOCrud">
        <constructor-arg value="qcom.qa.model.Word"/>
    </bean>
    <bean id="filterDAOCrud" parent="simpleDAOCrud">
        <constructor-arg value="qcom.qa.model.Filter"/>
    </bean>
    <bean id="alphaDAOCrud" parent="simpleDAOCrud">
        <constructor-arg value="qcom.qa.model.Alpha"/>
    </bean>
...
...
```

We should probably create alias's to the full class name since we are looking things up.
```

    <bean id="wordDAOCrud" parent="simpleDAOCrud" name="qcom.qa.model.Word-DAO">
        <constructor-arg value="qcom.qa.model.Word"/>
    </bean>

```

Then for backwards compatibility we will look things up using the full alias qcom.qa.model.Word-DAO, then if not found we will look things up as wordDAOCrud.

```
WebApplicationContextUtils.getWebApplicationContext(ServletContextUtils.context());
```

This gets the app context so we can look up the bean.

In the dropdown.jspx we will create some new variables as follows:

#### dropdown.jspx ####
```
	<c:set var="type" value="#{managedBean.entityClass}"/>
	<c:set var="classType" value="#{type.name}" />
	<c:set var="classShortName" value="#{type.shortName}" />
```

Then we will use these variables to pass values to each h:selectOne in dropdown.xhtml as follows:
#### dropdown.jspx ####
```
<f:attribute name="classType" value="${classType}"/>
<f:attribute name="classShortName" value="${classShortName}"/>
```
Then it is just a matter of changing the dao to read these values.

On further investigation the generic entity converter is called GenericEntityConverter in Presto defined as follows:

#### GenericEntityConverter.java ####
```
package qcom.cas.commons.faces;

import java.io.Serializable;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import qcom.cas.commons.dao.DAOCrud;
import qcom.cas.commons.model.ModelUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;


/**
 * Converts an integer string into some object.
 * This version does not depend on the Entity interface.
 *
 * @author Rick Hightower
 */
public class GenericEntityConverter implements Converter {
    /**
     * DAO crud to look up Entity based on id.
     */
    private DAOCrud daoCrud;

    /**
     * Name of id property. Defaults to "id".
     */
    private String idPropertyName = null;

    /**
     * Class of id. Defaults to java.lang.Long.
     */
    private Class classTypeOfId = null;

    /**
     * Is the id primitive. Defaults to false.
     */
    private boolean primitive = false;


    /**
     * Converts the value into an entity.
     * The type of the entity depends on the type that the daoCrud returns.
     *
     * @param facesContext current faces context
     * @param component current component
     * @param value current value submitted from user.
     *
     * @return An Entity
     *
     */
    public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
        Serializable entityId = ModelUtils.getIdObject(value, getClassTypeOfId());
        return daoCrud.read(entityId);
    }

    /**
     * Converts the entity to a String.
     *
     * @param facesContext current faces context
     * @param component current component
     * @param value current value of the entity
     *
     * @return value converted to string.
     *
     */
    public String getAsString(final FacesContext facesContext, final UIComponent component, final Object value) {
        if (value != null) {
            BeanWrapper bwValue = new BeanWrapperImpl(value);

            if (value instanceof String) {
                return value.toString();
            }

            Object object = bwValue.getPropertyValue(getIdPropertyName());
            if (object != null) {
                return object.toString();
            }
        }
        return "";
    }

    /**
     * Exposed for subclasses.
     *
     * @return current DaoCrud exposed.
     */
    protected DAOCrud getDaoCrud() {
        return daoCrud;
    }

    /**
     * Exposed for Spring to do injection.
     *
     * @param aDaoCrud DAOCrud used to look up Entity.
     */
    public void setDaoCrud(final DAOCrud aDaoCrud) {
        this.daoCrud = aDaoCrud;
    }

    /**
     * Is the id primitive or an object wrapper?
     * @return if the id is primitive.
     */
    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * Is the id primitive or an object wrapper?
     * @param aPrimitive set to this.
     */
    public void setPrimitive(final boolean aPrimitive) {
        this.primitive = aPrimitive;
    }
    /**
    *
    * The name of the id property, defaults to "id".
    * @return the id of the value.
    */
   public String getIdPropertyName() {
       return idPropertyName == null ? daoCrud.getIdPropertyName() : idPropertyName;
   }

   /**
    * @param aIdPropertyName idPropertyName
    */
   public void setIdPropertyName(final String aIdPropertyName) {
        this.idPropertyName = aIdPropertyName;
   }

    /**
     * What it the type of the id class, defaults to java.lang.Long.
     * @return type of the id class.
     */
    public Class getClassTypeOfId() {
        return classTypeOfId == null ? daoCrud.getClassTypeOfId() : classTypeOfId;
    }

    /**
     * Class type of id.
     * @param aClassTypeOfId Class type of id.
     */
    public void setClassTypeOfId(final Class aClassTypeOfId) {
        classTypeOfId = aClassTypeOfId;
    }
}

```

This is the one we need to change. For now we will get it to work with/or without injection (although we know.. it is pretty much always without until they fix JSF).

Here is the first shot of the lookup mehtod:

#### GenericEntityConverter ####
```
    private DAOCrud daoCrud(FacesContext facesContext, UIComponent component) {
    	if (daoCrud == null) {
        	String classType = (String) component.getAttributes().get("classType");
        	if (classType == null) {
        		throw new RuntimeException("daoCrud was not set and classType is null");
        	}
        	ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        	ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
        	daoCrud = (DAOCrud) applicationContext.getBean(classType + "-DAO");
        	if (daoCrud == null) {
        		String classShortName = (String) component.getAttributes().get("classShortName");
            	if (classShortName == null) {
            		throw new RuntimeException("daoCrud was not set and classShortName is null");
            	}
        		classShortName = classShortName.substring(0, 1).toLowerCase() + classShortName.substring(1, classShortName.length());
        		daoCrud = (DAOCrud) applicationContext.getBean(classShortName + "DAOCrud");
        	}
    	}
    	if (daoCrud == null) {
       		throw new RuntimeException("Could not find daoCrud");   		
    	}
    	return daoCrud;
	}

```

Then we just call it at the start of the converter methods as follows:

```
    /**
     * Converts the value into an entity.
     * The type of the entity depends on the type that the daoCrud returns.
     *
     * @param facesContext current faces context
     * @param component current component
     * @param value current value submitted from user.
     *
     * @return An Entity
     *
     */
    public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
        daoCrud(facesContext, component);
        Serializable entityId = ModelUtils.getIdObject(value, getClassTypeOfId());        
        return daoCrud.read(entityId);
    }


	/**
     * Converts the entity to a String.
     *
     * @param facesContext current faces context
     * @param component current component
     * @param value current value of the entity
     *
     * @return value converted to string.
     *
     */
    public String getAsString(final FacesContext facesContext, final UIComponent component, final Object value) {
        daoCrud(facesContext, component);
    	if (value != null) {
            BeanWrapper bwValue = new BeanWrapperImpl(value);

            if (value instanceof String) {
                return value.toString();
            }

            Object object = bwValue.getPropertyValue(getIdPropertyName());
            if (object != null) {
                return object.toString();
            }
        }
        return "";
    }
```

Opps... Now we have to register it in faces-config instead of the spring application context.

#### faces-config.xml ####
```
    <converter>
    	<converter-for-class>qcom.qa.model.State</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>
```

Ok... well now I got another converter error so now I have to register them all...

```
    <converter>
    	<converter-for-class>qcom.qa.model.State</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>


    <converter>
    	<converter-for-class>qcom.qa.model.Word</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Filter</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Country</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Address</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Person</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Alpha</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Beta</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Delta</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.Gamma</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.FormTestAttribute</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

    <converter>
    	<converter-for-class>qcom.qa.model.FormTest</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>

```

Okay... I was missing two more things:
I got the name wrong on simpleName bit ( i thought it was short name

#### dropdown.xhtml ####
```

	<c:set var="type" value="#{managedBean.entityClass}"/>
	<c:set var="classType" value="#{type.name}" />
	<c:set var="classShortName" value="#{type.simpleName}" />
```

The next mistake was not first checking to see if the App context has a bean before I ask for it as follows:

```
    private DAOCrud daoCrud(FacesContext facesContext, UIComponent component) {
    	if (daoCrud == null) {
        	String classType = (String) component.getAttributes().get("classType");
        	if (classType == null) {
        		throw new RuntimeException("daoCrud was not set and classType is null");
        	}
        	ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        	ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
        	if (applicationContext.containsBean(classType + "-DAO")) {
        		daoCrud = (DAOCrud) applicationContext.getBean(classType + "-DAO");
        	}
        	if (daoCrud == null) {
        		String classShortName = (String) component.getAttributes().get("classShortName");
            	if (classShortName == null) {
            		throw new RuntimeException("daoCrud was not set and classShortName is null");
            	}
        		classShortName = classShortName.substring(0, 1).toLowerCase() + classShortName.substring(1, classShortName.length());
        		if (applicationContext.containsBean(classShortName + "DAOCrud")) {
        			daoCrud = (DAOCrud) applicationContext.getBean(classShortName + "DAOCrud");
        		}
        	}
    	}
    	if (daoCrud == null) {
       		throw new RuntimeException("Could not find daoCrud");   		
    	}
    	return daoCrud;
	}
```

It was calling my converter. Then I was still getting a converter error. I noticed that Crank does have a custom renderer for select ones and such. So I included it in the pom.xml


#### pom.xml ####
```
 		<dependency>
				<groupId>org.crank</groupId>
				<artifactId>crank-jsf-support</artifactId>
				<version>1.0.3-SNAPSHOT</version>
		</dependency>
```

#### faces-config.xml from crank-jsf-support ####
```
<?xml version="1.0"?>
<faces-config xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_1_2.xsd"
    version="1.2">
  <component>
    <component-type>org.crank.javax.faces.SelectOne</component-type>
    <component-class>org.crank.javax.faces.component.UISelectOne</component-class>
  </component>
  <render-kit>
      <renderer>
      <component-family>javax.faces.SelectOne</component-family>
      <renderer-type>org.crank.javax.faces.Listbox</renderer-type>
      <renderer-class>
			org.crank.javax.faces.component.ListboxRenderer
  	  </renderer-class>
    </renderer>
  </render-kit>
</faces-config>
```

This makes me wonder.... Maybe a fresh pair of eyes on this tomorrow.

I changed Crank to latest version of jsf (build 10) and it broke my renderer so I changed back to the version build 2. I still get the converter error (changed the presto app as well). The next step involves a debugger and some break points.

After debugging... I finally was able to reproduce the error. First if Crank gets upgrade to release 10 of JSF it breaks as well. :(

Second after downgrading Presto to JSF 1.2\_2, it was still failing. (This is really an upgrade since it was using JSF 1.1).

The missing mojo was in the dropdown.jspx

```
	<c:set var="type" value="#{z:getPropertyClass(fieldName,managedBean.entityClass)}"/>
	<c:set var="classType" value="#{type.name}" />
	<c:set var="classShortName" value="#{type.simpleName}" />

```

I forgot to get the class of the property. I was just using the class of the parent class. This was causing the converter of the parent not the child to get used, which of course did not work.

I need to do a crazy Ivan and try it one more time with JSF 1.2 w/o the crank patch to see if it works. I do this b/c of this error it is impossible to tell if 1.2\_10 fixed things or not. I hope it does, if not I will downgrade for now and add a note to get Crank/Presto working with the lastest release of JSF in the future.

The problem still exists. We will need the Crank patch to the renderer and for now we will need to downgrade to JSF 1.2\_4 until I port the patch to JSF 1.2\_10.

Upon further investigation... Presto allows changing the data fine for states. The conversion happens as it should, but when we try to redisplay the form, it always says alabama is the state. We can even print out the state and get the right name just not the right name to show up in the drop down. I hacked the form as such to debug things and isolate them more:

```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://java.sun.com/jsf/facelets"
      xmlns:c="http://java.sun.com/jstl/core"
      xmlns:q="http://www.foo.com/cas"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:z="http://www.foo.com/jsf/core"
      xmlns:f="http://java.sun.com/jsf/core"
      >

<ui:composition template="../listing2Template.jsp">
    <ui:param name="subtitle" value="Listing 2 Form"/>
    <ui:param name="subcrumb" value="Listing 2 Form"/>
    <ui:define name="description">This is a form.</ui:define>
    <ui:define name="content">
        <q:form managedBean="#{Listing2CRUD}">

            <q:fields
                    fieldNames="description,someInt,someInteger,someDecimal,someDate,someBool"
                    fieldRequireds="true,false,false,false,false,false"
                    />
            <!-- <q:dropdown fieldName="state" sortBy="name" entity="#{entity}" defaultLabel="Arizona"/> -->
            ENTITY #{entity.state}
            <h:selectOneMenu  id="state" value="#{entity.state}" styleClass="dropdown">
                        <f:selectItems value="#{z:getSelectItems('stateDAOCrud', 'name', 'name')}"/>
                        <f:attribute name="classType" value="qcom.qa.model.State"/>
						<f:attribute name="classShortName" value="State"/>
            </h:selectOneMenu>
        </q:form>
    </ui:define>
</ui:composition>
</html>
```

Notice I use the h:selectOne directly instead of relying on composition component. This gives me a better idea which branch I am working with.

I went through the select item generators and converters for Crank (where this works) and Presto 2 where this does not work. I have not found a difference (yet). I even changed the Presto converter to do a lot more debugging.

So I debugged this by putting break points in the State class's equals method and the converters convert methods. I noticed that the value that gets passed in is null. So I tried to put a break point on the MenuRenderer (the hacked version). This is when my world was shattered a bit. Turns out, it is not using the hacked MenuRederer as I thought it did. My world slowy starts to shatter and previous beliefs and suppositions start to crumble.... Pause... take a walk... Resume

Ok. So just to make sure I am not going crazy I commented out the hack and reran the crank sample app, and it works without the hack. How is this possible? I was sure it was using the hack still. First I was sure that it was, and then I was sure that it was not.

Earlier, it seemed like the conversion was not working unless I included Crank and its hack, but now it seems like this is impossible. Ok. So I will rerun, Presto sample app with and without including crank-jsf-support and note the difference. But first I will run it with the hack taken out.

What kind of strange version hell am I in. I need to reread my copious notes.

The presto2 sample app works with the crank hack for renderer taken out. Will it work with crank jsf support taken out (seemed like earlier it would not)...

It works with our without crank-jsf-support jar file (earlier it seemed like it would only work with it). So now it works on submission, but not on redisplay. I still have the redisplay issue which does not exist with Crank sample app (although it did before I fixed it).

The bottom line is that the menu renderer is passing a null to the equals method of the State object; therefore, nothing is equal, thus the selected state is no rendered as such.

It seems locating the source code and debugging it is in order.

I need to locate the source for JSF and debug the menu renderer and see why it is passing nulls:

```
mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true
```

The above netted nothing.

When I run the app it says I am using 1.2\_04-b10-p01.
The source code for the builds seems to be here:
https://maven-repository.dev.java.net/repository/javax.faces/java-sources/

Got the source code, did some debugging... traced the issue into the MenuRenderer in the renderOption method as follows:

```
    protected void renderOption(FacesContext context, UIComponent component,
                                SelectItem curItem) throws IOException {
           ...
           type = valuesArray.getClass().getComponentType();
```

The type is qcom.qa.model.State_$$_javassist\_2 instead of qcom.qa.model.State so JSF can't find the converter.

It later looks up the converter using this call:

```
        try {
            newValue = context.getApplication().getExpressionFactory().
                 coerceToType(itemValue, type);
        } catch (Exception e) {
            // this should catch an ELException, but there is a bug
            // in ExpressionFactory.coerceToType() in GF
            newValue = null;
        }

```

Since the type is unknown it fails and then winds up passing a null to the equals method of the State object. (Which btw really blows... thanks guys.... what the hell)...

We could change the mapping in Hibernate so this is not proxied or we could add another entry for the converter as follows:

#### faces-config.xml ####
```
    <converter>
    	<converter-for-class>qcom.qa.model.State_$$_javassist_2</converter-for-class>
    	<converter-class>qcom.cas.commons.faces.GenericEntityConverter</converter-class>
    </converter>
```

Not only is the above a horrible hack. It does not work.

The fix was to get Hibernate to stop proxying the State object as follows:
```
    <class name="Filter" table="filter">
        <id name="id" type="long" column="id">
            <generator class="native" />
        </id>
        <many-to-one name="state" class="State" column="state_id" lazy="false"/>
```

The problem with this fix is that it relies on changing the mapping files to work. Another workaround would be to override the coerce method of the ExpressionFactory then overriding our getExpressionFactory() in our SpringApplication object. Then we could strip the cglib and others proxies like we did for JSF1.1.

I also tried this:


```
    <class name="Filter" table="filter">
        <id name="id" type="long" column="id">
            <generator class="native" />
        </id>
        <many-to-one name="state" class="State" column="state_id" lazy="noproxy"/>
```

And it did not work.