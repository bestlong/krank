package org.crank.crud.jsf.support;

import java.io.Serializable;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.crank.core.CrankContext;
import org.crank.core.ObjectRegistry;
import org.crank.core.LogUtils;
import org.crank.crud.GenericDao;
import org.crank.crud.controller.CrudManagedObject;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.apache.logging.log4j.Logger;

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
    protected Logger logger = LogManager.getLogger(EntityConverter.class);

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
	public Object getAsObject(final FacesContext facesContext,
			final UIComponent component, final String value) {
        logger.debug(String.format("getAsObject() called value=%s, component=%s, value class=%s", value, component.getClientId(facesContext), value.getClass().getName()));
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
				throw new ConverterException(new FacesMessage("Can't find object with id " + value,
						"Can't find object with id " + value));
            	}
			}
            LogUtils.debug(logger, "Returning converted object %s", object);
            return object;
		} catch (ConverterException ex){
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
	 * @param facesContext current faces context
	 * @param component current component
	 * @param value current value of the entity
	 *
	 * @return value converted to string.
	 *
	 */
	public String getAsString(final FacesContext facesContext,
			final UIComponent component, final Object value) {

        logger.debug(String.format("getAsString called value=%s, component=%s, value class=%s", value, component.getClientId(facesContext), value.getClass().getName()));
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
	 * @param aDaoCrud DAOCrud used to look up Entity.
	 */
	public void setDao(final GenericDao aDaoCrud) {
		this.dao = aDaoCrud;
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
	 * @param aIdPropertyName idPropertyName
	 */
	public void setIdPropertyName(final String aIdPropertyName) {
		this.idPropertyName = aIdPropertyName;
	}

	/**
	 * Class type of id.
	 * @param aClassTypeOfId Class type of id.
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
