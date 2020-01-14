package org.crank.crud.controller;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.crank.crud.relationships.RelationshipManager;

/**
 * 
 * @author Rick Hightower, Tom Cellucci
 *
 * @param <T> Entity type
 * @param <PK> primary key type
 */
public class DetailController<T extends Serializable, PK extends Serializable> extends CrudControllerBase<T, PK> {    
	private static final long serialVersionUID = 1L;
	
	/** manage the relationship with the parent entity object. */
	protected RelationshipManager relationshipManager = new RelationshipManager();
	
    private boolean showDetails = false;
    
    /** Always synchronize to the database, can't be rolled back by hitting cancel. */
    private boolean forceUpdate = false;
    
    /** Because we needed to keep track of the items that were modified; they need to become part of the persistent context
     * when the parent entity is saved or if the parent collection isn't the owner
     * there was a case where you'd remove an parent-child relationship
     * but it would never stick because the child was the owner
     * updating the parent wouldn't save your change
     * @author Tom Cellucci
     */
    private Collection<T> changedEntities = new HashSet<T>();

    protected Logger logger = LogManager.getLogger(DetailController.class);
    
    private boolean forcePersist;
    
    public boolean isForcePersist() {
		return forcePersist;
	}

	public void setForcePersist(boolean forcePersist) {
		this.forcePersist = forcePersist;
	}

	public boolean isShowDetails() {
		return showDetails;
	}

	public void setShowDetails(boolean showDetails) {
		this.showDetails = showDetails;
	}

    public void setIdParam( String idParam ) {
        this.idParam = idParam;
    }


    public void setRelationshipManager( RelationshipManager relationshipManager ) {
        this.relationshipManager = relationshipManager;
    }

    public RelationshipManager getRelationshipManager() {
        return this.relationshipManager;
    }

    public DetailController () {
    }
    
    public DetailController (final Class<T> entityClass) {
        this.entityClass = entityClass;
        relationshipManager.setEntityClass( entityClass );
    }

    public DetailController (final CrudOperations<?> parent, final Class<T> entityClass) {
        this.entityClass = entityClass;
        setParent(parent);
        relationshipManager.setEntityClass( entityClass );
    }
     
    /*
     * Searches for the top-level CrudController in the CrudOperation hierarchy
     * returns null if none found
     */
	private CrudController<?, ?> findCrudController() {
    	CrudController<?, ?> rv = null;
    	CrudOperations<?> ancestor = parent;
    	while ((ancestor != null)) {
    		if (ancestor instanceof CrudController) {
    			rv = (CrudController<?, ?>)ancestor;
    			break;
    		}
    		else if (ancestor instanceof CrudControllerBase) {
    			ancestor = ((CrudControllerBase<?, ?>)ancestor).parent;
    		}
    		else {
    			// unknown class type for ancestor
    			break;
    		}
    	}
    	return rv;
    }
    
    public CrudOutcome doCreate() {
        Object parent = (Object) this.parent.getEntity();

        /* Add this object to the parent. */
        relationshipManager.addToParent(parent, this.getEntity());
        this.state = CrudState.UNKNOWN;
        
        if (forceUpdate) {
        	if (dao!=null) {
        		dao.merge(this.getEntity());
        	} else {
        		CrudController<?, ?> crudController = findCrudController();
        		crudController.update();
        		crudController.state = CrudState.EDIT;
        	}
        } else {
        	if (forcePersist) {
        		changedEntities.add((T)this.getEntity());
        	}
        }
        parent = (Object) this.parent.getEntity();
        return null;
    }


	public CrudOutcome doDelete() {
        doDelete(entity);
        return null;
    }

    /** 
     * We're overriding the delete method in this case to ensure that a
     * non-null entity is passed in the fireBeforeDelete/fireAfterDelete methods.
     * The base class implementation passes its internal 'entity' member which
     * appears to be null at least some of the time. To fix, we explicitly set 
     * super.entity prior to invoking the super implementation. 
     * 
     */    
	@SuppressWarnings("unchecked")
	@Override
	public CrudOutcome delete() {
        /* Read the entity from the parent object. */
        this.entity = (T)relationshipManager.readEntityFromParent( parent.getEntity(), retrieveId());
		return super.delete();
	}
    
	@Override
	protected void doDelete(T entity) {
        logger.debug(String.format("About to remove entity (%s) from parent (%s)", entity, parent.getEntity()));
        relationshipManager.removeFromParent(parent.getEntity(), entity);
        if (forceUpdate) {
        	if (dao!=null) {
                logger.debug("Calling DAO merge(entity) in doDelete");
                dao.merge(entity);
        	} else {
                logger.debug("Calling parent crudController update() in doDelete");
                findCrudController().update();
        	}
        } else {
        	if (forcePersist) {
                logger.debug("Adding delete object to list that needs to be updated on update in doDelete");
                changedEntities.add(entity);
        	}
        }
	}

	public String getObjectId (Object row) {
        return this.relationshipManager.getObjectId( parent.getEntity(), row);
    }


    public CrudOutcome doLoadCreate() {
        init();
        createEntity();
        this.state = CrudState.ADD;
        return null;
    }

    @SuppressWarnings("unchecked")
	public CrudOutcome doRead() {
        /* Initialize this form and subForms to their initial state. */
        init();

        /* Turn on addMode, turn off editMode. */
        this.state = CrudState.EDIT;

        String index = retrieveId();


        /* Read the entity from the parent object. */
        this.entity = (T)relationshipManager.readEntityFromParent( parent.getEntity(), index );
        
        return null;
    }

    protected CrudOutcome doUpdate() {
        this.state = CrudState.UNKNOWN;
        if (forceUpdate) {
        	if (dao!=null) {
        		dao.merge(entity);
        	} else {
        		findCrudController().update();
        	}
        }
        return null;
    }

    
    public CrudOutcome doCancel() {
        state = CrudState.UNKNOWN;
        cancelChildren();
        return null;
    }
        
    public void toggleShowDetails() {
    	showDetails = !showDetails;
    }

	@Override
	protected CrudOutcome doLoadListing() {
		return CrudOutcome.LISTING;
	}

	@Override
	public void setParent(CrudOperations<?> parent) {	
		super.setParent(parent);
        final CrudController<?, ?> controller = findCrudController();
        if (controller != null && forcePersist) {
        	/*
        	 * this crud listener makes sure that entities changed
        	 * by this detail crudController are part of the persistent
        	 * context when the top-level operation is persisted.
        	 */
        	controller.addCrudControllerListener(new CrudControllerListenerAdapter() {
				@Override
				public void afterUpdate(CrudEvent event) {
					changedEntities.clear();
				}
				@Override
				public void afterCancel(CrudEvent event) {
					changedEntities.clear();
				}
				@Override
				public void beforeUpdate(CrudEvent event) {
					changedEntities = controller.manageRelated(changedEntities);
				}
			});
        }
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}
    
}
