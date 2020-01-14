package org.crank.crud.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.crank.annotations.design.AllowsConfigurationInjection;
import org.crank.annotations.design.ExpectsInjection;
import org.crank.annotations.design.OptionalInjection;
import org.crank.core.CrankValidationException;
import org.crank.core.PropertiesUtil;
import org.crank.core.RequestParameterMapFinder;
import org.crank.core.spring.support.SpringBeanWrapperPropertiesUtil;
import org.crank.crud.GenericDao;
import org.crank.message.MessageManagerUtils;
import org.crank.message.MessageUtils;
import org.crank.web.RequestParameterMapFinderImpl;
//import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Rick Hightower, Tom Cellucci
 * 
 * @param <T>
 *            Entity type
 * @param <PK>
 *            primary key type
 * 
 * Please note that some methods are marked transactional but are not unless
 * this gets proxied.
 */
public abstract class CrudControllerBase<T extends Serializable, PK extends Serializable>
		implements CrudOperations<T>, Serializable {
	protected Logger logger = LogManager.getLogger(CrudControllerBase.class);

    /* The DAO we use to persist T objects to the databasae. */
    protected GenericDao<T, PK> dao;
	protected EntityLocator<T> entityLocator;
	protected PropertiesUtil propertyUtil = new SpringBeanWrapperPropertiesUtil();
	protected String idPropertyName = "id";
	protected boolean readPopulated = true;
	protected Class<T> entityClass;
	protected CrudState state;
	protected T entity;
	private Map<String, DetailController<? extends Serializable, ? extends Serializable>> children = new HashMap<String, DetailController<? extends Serializable, ? extends Serializable>>();
	private ToggleSupport toggleSupport = new ToggleSupport();
	private String name;
	protected CrudOperations<?> parent;
	protected RequestParameterMapFinder requestParameterMapFinder = new RequestParameterMapFinderImpl();
	protected Map<String, Object> dynamicProperties = new CrankMap();
	protected FileUploadHandler fileUploadHandler;
	protected String idParam = "id";
	protected String deleteStrategy = CrudOperations.DELETE_BY_ENTITY;
	protected String addStrategy = CrudOperations.ADD_BY_CREATE;
	protected boolean transactional = false;
    protected boolean  suppressStatusMessages = false;

    public boolean isSuppressStatusMessages() {
        return suppressStatusMessages;
    }

    public void setSuppressStatusMessages(boolean suppressStatusMessages) {
        this.suppressStatusMessages = suppressStatusMessages;
    }

    public boolean isTransactional() {
		return transactional;
	}

	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}

	public CrudControllerBase() {
		super();
	}

	public String getName() {
		return name != null ? name : CrudUtils.getClassEntityName(entityClass);
	}

	public String getNameUpperCase() {
		return getName().toUpperCase();
	}

	public String getNamePlural() {
		String name = getName();
		if (name.endsWith("s")) {
			return name + "es";
		} else {
			return name + "s";
		}
	}

	public String getNamePluralAndUpperCase() {
		return getNamePlural().toUpperCase();
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see Toggleable#addToggleListener(ToggleListener)
	 */
	public void addToggleListener(ToggleListener listener) {
		toggleSupport.addToggleListener(listener);
	}

	/**
	 * @see Toggleable#addToggleListener(ToggleListener)
	 */
	public void removeToggleListener(ToggleListener listener) {
		toggleSupport.removeToggleListener(listener);
	}

	/**
	 * Fire an event to the Toggle listeners.
	 * 
	 */
	protected void fireToggle() {
		toggleSupport.fireToggle();
	}

	/**
	 * @see CrudOperations#getEntity()
	 */
	public T getEntity() {
		return entity;
	}

	public void setDao(GenericDao<T, PK> dao) {
		this.dao = dao;
	}

	@ExpectsInjection
	public void setPropertyUtil(PropertiesUtil propertyUtil) {
		this.propertyUtil = propertyUtil;
	}

	@OptionalInjection
	public void setIdPropertyName(String idPropertyName) {
		this.idPropertyName = idPropertyName;
	}

	@AllowsConfigurationInjection
	public void setReadPopulated(boolean readPopulated) {
		this.readPopulated = readPopulated;
	}

	@AllowsConfigurationInjection
	public void setEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public CrudState getState() {
		return state;
	}

	public Map<String, DetailController<? extends Serializable, ? extends Serializable>> getChildren() {
		return children;
	}

	public void setChildren(
			Map<String, DetailController<? extends Serializable, ? extends Serializable>> children) {
		this.children = children;
	}

	public void init() {
		logger.debug("init called setting CrudState to UNKNOWN");
		this.state = CrudState.UNKNOWN;
		initDetailChildren();
		parentChildren();
	}

	private void initDetailChildren() {
		if (children != null) {
			for (CrudControllerBase<? extends Serializable, ? extends Serializable> detailController : children
					.values()) {
                logger.debug(String.format("initializing child %s %s", detailController.getName(), detailController));
                detailController.init();
			}
		}
	}

	/**
	 * Inject this parent into all Children. It's fathers day. Give all the
	 * children a daddy.
	 */
	private void parentChildren() {
		if (children != null) {
			for (CrudControllerBase<? extends Serializable, ? extends Serializable> detailController : children
					.values()) {
				detailController.setParent(this);
			}
		}
	}

	/**
	 * Call cancelSubForms on all children.
	 * 
	 */
	protected void cancelChildren() {
		if (children != null) {
			for (CrudControllerBase<? extends Serializable, ? extends Serializable> detailController : children
					.values()) {
				detailController.cancel();
			}
		}
	}

	public <CE extends Serializable, CEPK extends Serializable> CrudControllerBase<CE, CEPK> addChild(
			String name, DetailController<CE, CEPK> detailController) {
		this.children.put(name, detailController);
		if (detailController.getRelationshipManager()
				.getChildCollectionProperty() == null) {
			detailController.getRelationshipManager()
					.setChildCollectionProperty(name);
		}
		detailController.setParent(this);
		return detailController;
	}

	/**
	 * Create a new entity. An Entity is the object we are managing.
	 * 
	 */
	protected void createEntity() {
        logger.debug("Calling createEntity");
        try {
			this.entity = entityClass.newInstance();
            logger.debug(String.format("Entity created", this.entity));
        } catch (Exception ex) {
            logger.error("Unable to create entity",ex);
            throw new RuntimeException(ex);
		}
	}

	public CrudOperations<?> getParent() {
		return parent;
	}

	public void setParent(CrudOperations<?> parent) {
		this.parent = parent;
	}

	public boolean isShowListing() {
		return state != CrudState.ADD || state != CrudState.EDIT;
	}

	public boolean isShowForm() {
		return state == CrudState.ADD || state == CrudState.EDIT;
	}

	public void setRequestParameterMapFinder(
			RequestParameterMapFinder requestParameterMapFinder) {
		this.requestParameterMapFinder = requestParameterMapFinder;
	}

	public Map<String, Object> getDynamicProperties() {
		return dynamicProperties;
	}

	public void setDynamicProperties(Map<String, Object> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}

	public void setFileUploadHandler(FileUploadHandler fileUploadHandler) {
		this.fileUploadHandler = fileUploadHandler;
	}

	/** Create an object. */
	//@Transactional
	public CrudOutcome create() {

		if (fileUploadHandler != null) {
			fileUploadHandler.upload(this);
		}
		try {
			logger.debug("before create");
			fireBeforeCreate();
			CrudOutcome outcome = doCreate();
            if (!suppressStatusMessages) {
                MessageManagerUtils.getCurrentInstance().addStatusMessage(
					"Created %s", MessageUtils.createLabel(this.getName()));
            }
            logger.debug("create");
			fireAfterCreate();
			logger.debug("create after event handling");
			return outcome;
		} catch (CrankValidationException ex) {
			if (transactional == true) {
				throw ex;
			}
		}
		return null;
	}

	/** Load create an object. */
	public CrudOutcome loadCreate() {
		try {
			logger.debug("loadCreate called");
			fireBeforeLoadCreate();
			CrudOutcome outcome = doLoadCreate();
			fireAfterLoadCreate();
			return outcome;
		} catch (CrankValidationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Update an object. */
	//@Transactional
	public CrudOutcome update() {
		if (fileUploadHandler != null) {
			fileUploadHandler.upload(this);
		}
		try {
			logger.debug("before update");
			fireBeforeUpdate();
			CrudOutcome outcome = doUpdate();
            if (!suppressStatusMessages) {
                MessageManagerUtils.getCurrentInstance().addStatusMessage(
					"Updated %s", MessageUtils.createLabel(this.getName()));
            }
            logger.debug("update");
			fireAfterUpdate();
			logger.debug("after event handling");
			return outcome;
		} catch (CrankValidationException ex) {
			if (transactional == true) {
				throw ex;
			}
		}
		return null;
	}

	/** Update an object. */
	//@Transactional
	public CrudOutcome delete() {
		logger.debug("delete");
		fireBeforeDelete(this.entity);
		CrudOutcome outcome = doDelete();
        if (!suppressStatusMessages) {
            MessageManagerUtils.getCurrentInstance().addStatusMessage("Deleted %s",
				MessageUtils.createLabel(this.getName()));
        }
        fireAfterDelete(this.entity);
		return outcome;
	}

	/** Update an object. */
	//@Transactional
	public CrudOutcome read() {
		fireBeforeRead();
		CrudOutcome outcome = doRead();
		fireAfterRead();
		return outcome;
	}

	/** Load Listing. */
	public CrudOutcome loadListing() {
		fireBeforeLoadListing();
		CrudOutcome outcome = doLoadListing();
		fireAfterLoadListing();
		return outcome;
	}

	/** Update an object. */
	public CrudOutcome cancel() {
		fireBeforeCancel();
		CrudOutcome outcome = doCancel();
		fireAfterCancel();
		return outcome;
	}

	protected abstract CrudOutcome doCancel();

	/** Create an object. */
	protected abstract CrudOutcome doCreate();

	/** Update an object. */
	protected abstract CrudOutcome doUpdate();

	/** Delete an object. */
	protected abstract CrudOutcome doDelete();

	/** Read an object. */
	protected abstract CrudOutcome doRead();

	/** Read an object. */
	protected abstract CrudOutcome doLoadCreate();

	/** Read an object. */
	protected abstract CrudOutcome doLoadListing();

	protected String retrieveId() {
		String[] params = this.requestParameterMapFinder.getMap().get(
				this.idParam);
		if (params != null && params.length > 0) {
			return params[0];
		} else {
			return null;
		}
	}

	private List<CrudControllerListener> listeners = new ArrayList<CrudControllerListener>();

	public void addCrudControllerListener(CrudControllerListener listener) {
		listeners.add(listener);
	}

	public void removeCrudControllerListener(CrudControllerListener listener) {
		listeners.remove(listener);
	}

	protected void fireAfterUpdate() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterUpdate(event);
		}
	}

	protected void fireBeforeUpdate() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeUpdate(event);
		}
	}

	protected void fireBeforeCreate() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeCreate(event);
		}
	}

	protected void fireAfterCreate() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterCreate(event);
		}
	}

	protected void fireBeforeLoadCreate() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeLoadCreate(event);
		}
	}

	protected void fireAfterLoadCreate() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterLoadCreate(event);
		}
	}

	protected void fireBeforeRead() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeRead(event);
		}
	}

	protected void fireAfterRead() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterRead(event);
		}
	}

	protected void fireBeforeDelete(T beforeDeleteEntity) {
		CrudEvent event = new CrudEvent(this, beforeDeleteEntity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeDelete(event);
		}
	}

	protected void fireAfterDelete(T afterDeleteEntity) {
		CrudEvent event = new CrudEvent(this, afterDeleteEntity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterDelete(event);
		}
	}

	protected void fireBeforeCancel() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeCancel(event);
		}
	}

	protected void fireAfterCancel() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterCancel(event);
		}
	}

	private void fireAfterLoadListing() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.afterLoadListing(event);
		}
	}

	private void fireBeforeLoadListing() {
		CrudEvent event = new CrudEvent(this, this.entity);
		for (CrudControllerListener ccl : listeners) {
			ccl.beforeLoadListing(event);
		}
	}

	public String getDeleteStrategy() {
		return deleteStrategy;
	}

	public void setDeleteStrategy(String deleteStrategy) {
		this.deleteStrategy = deleteStrategy;
	}

	public String getAddStrategy() {
		return addStrategy;
	}

	public void setAddStrategy(String addStrategy) {
		this.addStrategy = addStrategy;
	}

	public CrudOutcome deleteSelected() {
		List<T> listToDelete = getSelectedEntities();
		/* You could change this to delete a list of ids. */
		for (T entity : listToDelete) {
			fireBeforeDelete(entity);
			doDelete(entity);
			fireToggle();
			fireAfterDelete(entity);
		}
    	MessageManagerUtils.getCurrentInstance().addStatusMessage("Deleted selections");        
		return null;
	}

	protected List<T> getSelectedEntities() {
		return entityLocator.getSelectedEntities();
	}

	@ExpectsInjection
	public void setEntityLocator(EntityLocator<T> entityLocator) {
		this.entityLocator = entityLocator;
	}

	@SuppressWarnings("unchecked")
	protected void doDelete(T entity) {
		if (deleteStrategy.equals(CrudOperations.DELETE_BY_ENTITY)) {
			entity = dao.read((PK) propertyUtil.getPropertyValue(
					idPropertyName, entity));
			dao.delete((T) entity);
		} else {
			dao.delete((PK) propertyUtil.getPropertyValue(idPropertyName,
					entity));
		}
	}

}