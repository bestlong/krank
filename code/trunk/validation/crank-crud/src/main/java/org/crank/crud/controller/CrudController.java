package org.crank.crud.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.crank.annotations.design.AllowsConfigurationInjection;
import org.crank.annotations.design.ExpectsInjection;
import org.crank.annotations.design.OptionalInjection;
import org.crank.core.PropertiesUtil;
import org.crank.crud.GenericDao;

/**
 * Controls CRUD operations from an application.
 * @author Rick
 *
 * @param <T> Entity type
 * @param <PK> Entity primary key
 * @see CrudOperations
 * @see Toggleable
 */
public class CrudController<T, PK extends Serializable> implements CrudOperations, Toggleable {
    private GenericDao<T, PK> dao;
    private List<ToggleListener> list = new ArrayList<ToggleListener>();
    private EntityLocator entityLocator;
    private PropertiesUtil propertyUtil;
    private String idPropertyName = "id";
    private boolean readPopulated;
    private Class<T> entityClass;
    private CrudState state;
    
    private T entity;

    public CrudController () {
        
    }

    /**
     * Creates a new instance of the entity class and sets the entity to this new instance.
     * @see CrudOperations#loadCreate()
     */
    public CrudOutcome loadCreate() {
        try {
            entity = entityClass.newInstance();
            this.state = CrudState.ADD;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return CrudOutcome.FORM;
    }

    /**
     * Create a new instance in the database.
     * Notify listeners that the entity was created.
     * @see CrudOperations#create()
     * @retrun outcome
     */
    public CrudOutcome create() {
        dao.create(entity);
        this.state = CrudState.UNKNOWN;
        fireToggle();
        return CrudOutcome.LISTING;
    }

    /**
     * Delete the entity from the data store.
     * Notify listeners that the model changed.
     * @see CrudOperations#delete()
     * @retrun outcome
     */
    public CrudOutcome delete() {
        dao.delete(entity);
        fireToggle();
        return CrudOutcome.LISTING;
    }

    /**
     * Read the entity from the data store. If the readPopulated flag is set, read 
     * the entity fully populated. You need to read the entity fully populated for 
     * master detail forms and such.
     * @see CrudOperations#read()
     * @retrun outcome
     */
    @SuppressWarnings("unchecked")
    public CrudOutcome read() {
        entity = (T)getCurrentEntity();
        state = CrudState.EDIT; 
        if (readPopulated) {
            entity = dao.readPopulated( (PK) propertyUtil.getPropertyValue( idPropertyName, entity ));
        }
        return CrudOutcome.FORM;
    }

    private Object getCurrentEntity() {
        return entityLocator.getEntity();
    }

    /**
     * Update the entity in the data store. 
     * Notify listeners that the entity was updated.
     * @see CrudOperations#update()
     * @return outcome
     */
    public CrudOutcome update() {
        dao.update(entity);
        state = CrudState.UNKNOWN; 
        fireToggle();
        return CrudOutcome.LISTING;
    }

    /** 
     * @see CrudOperations#getEntity()
     */
    public Serializable getEntity() {
        return (Serializable) entity;
    }

    /**
     * @see Toggleable#addToggleListener(ToggleListener)
     */
    public void addToggleListener(ToggleListener listener) {
        list.add( listener );
    }
    /**
     * @see Toggleable#addToggleListener(ToggleListener)
     */    
    public void removeToggleListener(ToggleListener listener) {
        list.remove( listener );
    }

    /**
     * Fire and event to the listeners.
     *
     */
    private void fireToggle() {
        ToggleEvent te = new ToggleEvent(this);
        for (ToggleListener tl : list) {
            tl.toggle( te );
        }
    }

    public void setDao( GenericDao<T, PK> dao ) {
        this.dao = dao;
    }

    @ExpectsInjection
    public void setEntityLocator( EntityLocator entityLocator ) {
        this.entityLocator = entityLocator;
    }

    @ExpectsInjection    
    public void setPropertyUtil( PropertiesUtil propertyUtil ) {
        this.propertyUtil = propertyUtil;
    }

    @OptionalInjection
    public void setIdPropertyName( String idPropertyName ) {
        this.idPropertyName = idPropertyName;
    }

    @AllowsConfigurationInjection
    public void setReadPopulated( boolean readPopulated ) {
        this.readPopulated = readPopulated;
    }

    @AllowsConfigurationInjection
    public void setEntityClass( Class<T> entityClass ) {
        this.entityClass = entityClass;
    }
    
    public Class<T> getEntityClass() {
        return entityClass;
    }

    public CrudState getState() {
        return state;
    }


}
