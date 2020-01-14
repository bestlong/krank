package org.crank.crud.controller;

import java.io.Serializable;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.crank.message.MessageManagerUtils;
import org.apache.logging.log4j.Logger;

/**
 * Controls CRUD operations from an application.
 * @author Rick
 *
 * @param <T> Entity type
 * @param <PK> Entity primary key
 * @see CrudOperations
 * @see Toggleable
 */
public class CrudController<T extends Serializable, PK extends Serializable> extends CrudControllerBase<T, PK>  {
    protected Logger logger = LogManager.getLogger(CrudController.class);
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CrudController () {
        
    }
    
    /**
     * Update the entity in the data store. 
     * Notify listeners that the entity was updated.
     * @see CrudOperations#update()
     * @return outcome
     */
    public CrudOutcome doUpdate() {
        logger.debug(String.format("doUpdate() called. About to call dao.store(entity) %s ", entity));
        entity = dao.store(entity);
        logger.debug(String.format("dao.store(entity) was called from doUpdate() %s", entity));
        state = CrudState.UNKNOWN; 
        fireToggle();
        return CrudOutcome.LISTING;
    }
    
    /**
     * Creates a new instance of the entity class and sets the entity to this new instance.
     * @see CrudOperations#loadCreate()
     */
    public CrudOutcome doLoadCreate() {
        logger.debug(String.format("doLoadCreate() called from class %s", this));

        init();
        createEntity();
        this.state = CrudState.ADD;
        logger.debug("Set state to CrudState.ADD");
        return CrudOutcome.FORM;
    }

    /**
     * Create a new instance in the database.
     * Notify listeners that the entity was created.
     * @see CrudOperations#create()
     * @return outcome
     */
    public CrudOutcome doCreate() {
    	if (CrudOperations.ADD_BY_MERGE.equals(addStrategy)) {
    		this.entity = dao.merge(entity);
    	} else {
    		dao.store(entity);
    	}
        this.state = CrudState.UNKNOWN;
        fireToggle();
        return createOutcome;
    }

    private CrudOutcome createOutcome = CrudOutcome.LISTING;
    public CrudOutcome getCreateOutcome() {
		return createOutcome;
	}

	public void setCreateOutcome(CrudOutcome createOutcome) {
		this.createOutcome = createOutcome;
	}

	/**
     * Delete the entity from the data store.
     * Notify listeners that the model changed.
     * @see CrudOperations#delete()
     * @return outcome
     */ 
    public CrudOutcome doDelete() {
        doDelete(getCurrentEntity());        
        fireToggle();
        return CrudOutcome.LISTING;
    }

    /** 
     * We're overriding the delete method in this case to ensure that the
     * correct entity is passed in the fireBeforeDelete/fireAfterDelete methods.
     * The base class implementation passes its 'entity' member which
     * is only sometimes correct because it differs from getCurrentEntity().
     * 
     */
    @Override
    public CrudOutcome delete() {    	
    	T entity = getCurrentEntity();
        fireBeforeDelete(entity);
        CrudOutcome outcome = doDelete();
    	MessageManagerUtils.getCurrentInstance().addStatusMessage("Deleted");        
        fireAfterDelete(entity);
        return outcome;
    }

   /**
     * Read the entity from the data store. If the readPopulated flag is set, read 
     * the entity fully populated. You need to read the entity fully populated for 
     * master detail forms and such.
     * @see CrudOperations#read()
     * @return outcome
     */
    @SuppressWarnings("unchecked")
    public CrudOutcome doRead() {
        init();
        PK id = null;
        
        String sId = this.retrieveId();
        if (sId==null) {
        	entity = getCurrentEntity();
        	id = (PK) propertyUtil.getPropertyValue( idPropertyName, entity );
        } else {
        	id = (PK) Long.valueOf(sId);
        }
        state = CrudState.EDIT; 
        if (readPopulated) {
            entity = dao.readPopulated( id );
        }
        return CrudOutcome.FORM;
    }

    public void stayOnForm() {
    	state = CrudState.EDIT;
    }
    
    private boolean useEntityAsCurrent = false;
    protected T getCurrentEntity() {
    	if (useEntityAsCurrent) {
    		return entityLocator.getEntity()==null?this.getEntity():entityLocator.getEntity();
    	} else {
    		return entityLocator.getEntity();
    	}
    }


    public CrudOutcome doCancel() {
        state = CrudState.UNKNOWN;
        cancelChildren();
        return CrudOutcome.LISTING;
    }

	@Override
	protected CrudOutcome doLoadListing() {
		return CrudOutcome.LISTING;
	}

	@Override
	public CrudOutcome deleteSelected() {
		super.deleteSelected();
		return CrudOutcome.LISTING;
	}
    
    public <RE> Collection<RE> manageRelated(Collection<RE> relatedEntities) {
    	return dao.mergeRelated(relatedEntities);
    }

	public boolean isUseEntityAsCurrent() {
		return useEntityAsCurrent;
	}

	public void setUseEntityAsCurrent(boolean useEntityAsCurrent) {
		this.useEntityAsCurrent = useEntityAsCurrent;
	}
}
