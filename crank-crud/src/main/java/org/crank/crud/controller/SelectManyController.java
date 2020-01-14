package org.crank.crud.controller;

import java.io.Serializable;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.crank.crud.relationships.SelectManyRelationshipManager;
import static org.crank.core.LogUtils.debug;
import org.crank.core.CrankValidationException;
import org.crank.message.MessageManagerUtils;
import org.crank.message.MessageUtils;
import org.apache.logging.log4j.Logger;

public abstract class SelectManyController<T extends Serializable, PK extends Serializable> {


	protected Logger logger = LogManager.getLogger(SelectManyController.class);
    
    private SelectManyRelationshipManager manager;
    private FilterablePageable paginator;
    private CrudControllerBase<T, PK> controller;
    private boolean show;
	
    @SuppressWarnings("unchecked")
	public SelectManyController (Class<?> clazz, String propertyName, FilterablePageable pageable, CrudOperations<T> crudController) {
    	this.paginator = pageable;
    	
    	if (!this.paginator.isInitialized()){
    		this.paginator.moveToStartPage();
    	}
    	this.controller = (CrudControllerBase<T, PK>) crudController;
    	
		manager = new SelectManyRelationshipManager();
		manager.setEntityClass(clazz);
		manager.setChildCollectionProperty(propertyName);
		
		controller.addCrudControllerListener(new CrudControllerAdapter() {


			public void afterLoadCreate(CrudEvent event) {
                initEntity();
			}

			public void afterLoadListing(CrudEvent event) {
				initEntity();
			}

			public void afterRead(CrudEvent event) {
				initEntity();
			}


			public void beforeCreate(CrudEvent event) {
				initEntity();
                prepareUpdate();

            }

			public void beforeUpdate(CrudEvent event) {
				initEntity();
                prepareUpdate();
            }}
		);

        paginator.addPaginationListener(new PaginationListener(){
            public void pagination(PaginationEvent pe) {
                processPaginationEvent();
            }
        });

        paginator.addFilteringListener(new FilteringListener(){
            public void beforeFilter(FilteringEvent fe) {
                processPaginationEvent();
            }

            public void afterFilter(FilteringEvent fe) {
            }
        });
        
        initEntity();
    	
    }

    public void prepareUpdate() {
        debug(logger, "prepareUpdate()");

        Class<?> parentClass = controller.getEntityClass();

        debug(logger, "prepareUpdate() is the field required --- parentObject class = %s, childCollectionProperty=%s",
                    parentClass, manager.getChildCollectionProperty());

        if (CrudUtils.isRequired(parentClass, manager.getChildCollectionProperty())) {
            debug(logger, "prepareUpdate() the field was required --- parentObject class = %s, childCollectionProperty=%s",
                    parentClass, manager.getChildCollectionProperty());

            if (manager.getChildCollectionAsCollection()==null || manager.getChildCollectionAsCollection().size() == 0) {
                debug(logger, "The field was required and it is NULL!");
                MessageManagerUtils.getCurrentInstance().addErrorMessage("You must set %s",
                        MessageUtils.createLabelWithNameSpace(
                                CrudUtils.getClassEntityName(parentClass), manager.getChildCollectionProperty()));
                throw new CrankValidationException("");
            }
        }
    }
    public void processPaginationEvent() {
        this.manager.setParentObject(controller.getEntity());
        this.manager.process(getSelectedEntities(), getEntitiesInView());
    }

    public void initEntity() {
        if (logger.isDebugEnabled()){
            logger.debug(String.format("Manager manager=%s crudController=%s entity=%s", manager, controller, controller.getEntity()));
        }
        manager.setParentObject(controller.getEntity());
    }
	public SelectManyRelationshipManager getManager() {
		return manager;
	}

	public void setManager(SelectManyRelationshipManager manager) {
		this.manager = manager;
	}

	public FilterablePageable getPaginator() {
		return paginator;
	}

	public void setPaginator(FilterablePageable paginator) {
		this.paginator = paginator;
	}

	public void process () {
        logger.debug("Process called");
        this.manager.setParentObject(controller.getEntity());
		this.manager.process(getSelectedEntities(), getEntitiesInView());
		this.show = false;
	}
	
	public void cancel () {
		this.show = false;
	}
	
	public void showSelection() {
		this.show = true;
	}

	public abstract Set<Object> getSelectedEntities();

	public abstract Set<Object> getEntitiesInView();

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public CrudControllerBase<T, PK> getController() {
		return controller;
	}

	public void setController(CrudControllerBase<T, PK> controller) {
		this.controller = controller;
	}

    

}
