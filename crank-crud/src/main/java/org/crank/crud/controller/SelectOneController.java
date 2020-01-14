package org.crank.crud.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.crank.core.LogUtils.debug;
import org.crank.core.CrankValidationException;
import org.crank.message.MessageManagerUtils;
import org.crank.message.MessageUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.Serializable;

/**
 * This class can be used three ways.
 * 1) Has a parentEntity
 * 2) Works with a Crank CrudController
 * 3) No parent object
 *
 *
 */
public abstract class SelectOneController<T extends Serializable, PK extends Serializable> implements Selectable {

    /**
     * Used to paginate over the listing and allow the user to select.
     */
    private FilterablePageable paginator;
    /**
     * Should the selection listing show.
     */
    private boolean show;
    /**
     * Target property.
     */
    protected String targetPropertyName;

    /** Entity class of the class instance that are being selected. */
    protected Class<?> entityClass;

    /** Name of the id property. */
    protected String idProperty = "id";

    /** Name of the label property in the entity class. */
    protected String labelProperty = "name";

    /** Name of the property we are looking up from the selected entity. */
    protected String sourcePropertyName = null;

    /** Selection support class. Fires events and such. */ 
    protected SelectSupport selectSupport = new SelectSupport(this);

    /** CrudControllerBase class. Holds the parent entity instance. */
    protected CrudControllerBase<T, PK> crudController;


    /** Used for logging. */
    protected Logger logger = LogManager.getLogger(SelectOneController.class);

    /* Optional parentEntity. */
    protected Object parentEntity = null;


    @SuppressWarnings("unchecked")
    public SelectOneController(Class entityClass, String propertyName, FilterablePageable pageable, CrudOperations crudController) {
        debug(logger, "JsfSelectOneListingController(entityClass=%s, propertyName=%s, pageable=%s, crudController=%s)", entityClass, propertyName, pageable, crudController);
        this.paginator = pageable;
    	if (!this.paginator.isInitialized()){
    		this.paginator.moveToStartPage();
    	}
        
        this.crudController = (CrudControllerBase<T, PK>) crudController;
        this.targetPropertyName = propertyName;
        this.entityClass = entityClass;
        init();
    }

    @SuppressWarnings("unchecked")
	public SelectOneController(Class<?> entityClass, String propertyName, FilterablePageable pageable, CrudOperations crudController, String sourceProperty) {
        this(entityClass, propertyName, pageable, crudController);
        this.sourcePropertyName = sourceProperty;
        debug(logger, "sourcePropertyName=%s", sourceProperty);
    }

    public SelectOneController(Class<?> entityClass, FilterablePageable pageable) {
        this(entityClass, null, null, pageable);
    }


    public SelectOneController(Class<?> entityClass, Object parentEntity, String controllerProperty, FilterablePageable pageable) {
        debug(logger, "JsfSelectOneListingController(entityClass=%s, parentEntity=%s, controllerProperty=%s, pageable=%s)", entityClass, parentEntity, controllerProperty, pageable);
        this.entityClass = entityClass;
        this.paginator = pageable;
        this.parentEntity = parentEntity;
        this.targetPropertyName = controllerProperty;
        init();
    }


    protected void init() {
        debug(logger, "init() called");
        if (crudController!=null) {
            debug(logger, "Register event handler.");
            crudController.addCrudControllerListener(new CrudControllerAdapter(){
                @Override
                public void beforeCreate(CrudEvent event) {
                    prepareUpdate();
                }

                @Override
                public void beforeUpdate(CrudEvent event) {
                    prepareUpdate();
                }
            });
        } else {

        }
    }


    public FilterablePageable getPaginator() {
        return paginator;
    }

    public void setPaginator(FilterablePageable paginator) {
        this.paginator = paginator;
    }

    public abstract Row getSelectedRow();

    /** This method get called when the users selects a row from the table. */
    public void select() {
        Row selectedRow = getSelectedRow();
        Object valueBean = selectedRow.getObject();
        debug(logger, "select(): selectedRow=%s, valueBean=%s", selectedRow, valueBean);
        Object valueProperty = getValueProperty(valueBean);


        logger.debug("Setting the property value in the 'parent' object.");

        if (parentObject()!=null) {
            extractWrappedParent().setPropertyValue(this.targetPropertyName, valueProperty);
        }

        selectSupport.fireSelect(valueProperty);
        this.show = false;
        prepareUI();
    }

    private Object getValueProperty(Object valueBean) {
        /* If no source property is found than the value property is set to the selected object. */
        Object valueProperty = valueBean;

        /* If the sourceProperty is found,
           * then get the current value of the source property from the selected row. */
        if ((sourcePropertyName != null) && !"".equals(sourcePropertyName)) {
            debug(logger, "select() found source property: sourcePropertyName=%s", sourcePropertyName);
            BeanWrapper valueWrapper = new BeanWrapperImpl(valueBean);
            valueProperty = valueWrapper.getPropertyValue(this.sourcePropertyName);
            debug(logger, "select(): this value was selected from sourcePropertyName : valueProperty = %s", valueProperty);
        }
        return valueProperty;
    }

    /** Get the parent object which is either the entity contained by the crudController or the parentEntity or none.
     * This method could return null.
     * @return the parent object.
     * */
    private Object parentObject() {
        Object object = null;
        if (this.parentEntity != null) {
            object = this.parentEntity;
        } else if (crudController != null) {
            object = crudController.getEntity();
        } else {
            logger.debug("Neither the parentEntity or crudController are set.");
        }
        return object;
    }
    private BeanWrapper extractWrappedParent() {
        return new BeanWrapperImpl(parentObject());
    }

    public void unselect() {
        if (parentObject()!=null) {
            extractWrappedParent().setPropertyValue(this.targetPropertyName, null);
        }
        this.show = false;
        selectSupport.fireUnselect(null);
        prepareUI();

    }

    public void prepareUpdate() {
        debug(logger, "prepareUpdate()");
        Class<?> parentClass = parentObject().getClass();
        if (CrudUtils.isRequired(parentClass, targetPropertyName)) {
            debug(logger, "prepareUpdate() the field was required --- parentObject class = %s, entityClass=%s, targetPropertyName=%s", parentClass, entityClass, targetPropertyName);
            if (extractWrappedParent().getPropertyValue(this.targetPropertyName) == null) {
                debug(logger, "The field was required and it is NULL!");
                MessageManagerUtils.getCurrentInstance().addErrorMessage("You must set %s",
                        MessageUtils.createLabelWithNameSpace(
                                CrudUtils.getClassEntityName(parentClass), targetPropertyName));
                throw new CrankValidationException("");
            }
        }
    }

    public abstract void prepareUI();

    public void cancel() {
        this.show = false;
    }

    public void showSelection() {
        this.show = true;
    }


    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public CrudControllerBase<T, PK> getController() {
        return crudController;
    }

    public void setController(CrudControllerBase<T, PK> controller) {
        this.crudController = controller;
    }


    public Class<?> getEntityClass() {
        return entityClass;
    }


    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public String getPropertyName() {
        return targetPropertyName;
    }


    public void setPropertyName(String propertyName) {
        this.targetPropertyName = propertyName;
    }


    public String getIdProperty() {
        return idProperty;
    }


    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }


    public String getLabelProperty() {
        return labelProperty;
    }


    public void setLabelProperty(String labelProperty) {
        this.labelProperty = labelProperty;
    }

    public void addSelectListener(SelectListener listener) {
        selectSupport.addSelectListener(listener);
    }


    public void removeSelectListener(SelectListener listener) {
        selectSupport.removeSelectListener(listener);
    }



}