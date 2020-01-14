package org.crank.crud.jsf.support;

import java.io.Serializable;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import org.apache.logging.log4j.LogManager;
import org.crank.crud.controller.*;
import org.crank.crud.criteria.Select;
import org.crank.message.MessageUtils;
import org.crank.core.LogUtils;
import org.apache.logging.log4j.Logger;

/**
 * This class adapts a CrudController to the JSF world.
 * @author Rick Hightower
 *  "I'll never be afraid of some pilsner fresh fat (guy) who eats donut hamburgers and only gets exercise when he plays World of Warcraft on a DDR pad." --Zed
 *
 * @param <T> Type of entity that we are providing CRUD for.
 * @param <PK> Primary key type.
 */
public class JsfCrudAdapter<T extends Serializable, PK extends Serializable> implements EntityLocator<T>, Serializable {
	private static final long serialVersionUID = 1L;
	protected FilterablePageable paginator;
    protected DataModel model = new ListDataModel();
    protected CrudOperations<T> controller;
	protected List<T> page;
    protected String[] availableProperties;
    protected String[] properties;
    protected List<SelectItem> availablePropertyList;
    protected boolean propertyEditorOpen;
    protected String entityName;

    protected Logger log = LogManager.getLogger(JsfCrudAdapter.class);


    public JsfCrudAdapter() {
        readCookie();
    }

    public JsfCrudAdapter(FilterablePageable filterablePageable, CrudOperations<T> crudController) {
        this(org.crank.crud.controller.CrudUtils.getEntityName(filterablePageable.getType()),
                filterablePageable, crudController);

    }

    public JsfCrudAdapter(CrudOperations<T> crudController) {
        this(org.crank.crud.controller.CrudUtils.getEntityName(crudController.getEntityClass()),
                null, crudController);
    }

    public JsfCrudAdapter(CrudOperations<T> crudController, Class<?> type) {
        this(org.crank.crud.controller.CrudUtils.getEntityName(type),
                null, crudController);
    }

    
    @SuppressWarnings("unchecked")
	public JsfCrudAdapter(String eName, FilterablePageable filterablePageable, CrudOperations<T> crudController) {
        this.entityName = eName;
        this.paginator = filterablePageable;
        this.controller = crudController;
        crudController.addCrudControllerListener( new CancelListener() );
        if (crudController instanceof CrudController) {
        	((CrudController<T, PK>)crudController).setEntityLocator( this );
        }
        if (this.controller != null) {
        	setupCrudControllerWiring();
        }
        if (this.paginator!=null) {
        	setupPaginatorEventWiring();
            if (this.paginator.getPropertyNames()!=null) {
                this.setAvailableProperties(this.paginator.getPropertyNames().toArray(new String[this.paginator.getPropertyNames().size()]));
            }
        }
        readCookie();
    }

    private String propertyToMove;

    public void setPropertyToMove(String propertyToMove) {
        this.propertyToMove = propertyToMove;
    }


    /**
     * This method should not fail if the cookie cannot be written (just warn).
     */
    private void sendCookie() {
        log.debug("sendCookie");
        /* Write the cookie */
        try {
            /* If the entityName was present, try to write out the cookie. */
            if (entityName !=null) {
                HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                Cookie cookie = new Cookie("JsfCrudAdapter::" + entityName, Arrays.asList(properties).toString().replace(',','_').replace(' ', '_'));
                LogUtils.debug(log, "Writing cookie %s %s", entityName, Arrays.asList(properties).toString().replace(',','_').replace(' ', '_'));
                cookie.setMaxAge(365*24*60*60);
                cookie.setPath("/");
                response.addCookie(cookie);
            } else {
                log.warn("entity name missing so we could not write the cookie");
            }
        } catch (Exception ex) {
            log.warn("Unable to write cookie", ex);
        }
    }

    /**
     * This method should not fail if the cookie cannot be read (just warn).
     */
    private void readCookie() {
        log.debug("1 readCookie");
        try {

            /* Read the cookies. */
            Cookie cookie = (Cookie) FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestCookieMap().get("JsfCrudAdapter::" + entityName);

            LogUtils.debug(log, "2 Just read cookie %s for %s", cookie, entityName);

            if (log.isDebugEnabled()) {
                Set<Map.Entry<String, Object>> entries = FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestCookieMap().entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    LogUtils.debug(log, "key %s value %s", entry.getKey(), ((Cookie)entry.getValue()).getValue());
                }
            }

            if (cookie == null) {
                LogUtils.debug(log, "3 Cookie was null for %s", entityName);
                return;
            }
            
            String cookieValue = cookie.getValue();
            if (cookieValue == null || cookieValue.length()==0) {
                LogUtils.debug(log, "4 Cookie was not set for %s", entityName);
                return;
            }

            LogUtils.debug(log, "5 Cookie value %s for %s", cookieValue, entityName);

            /* Parse the cookie. */
            String[] comps = cookieValue.split("[_\\[\\]]");
            List<String> props = new ArrayList<String>();

            for (String prop : comps) {
               if (prop.trim().length()==0) {
                 continue;
               } else {
                  LogUtils.debug(log, "6 Read header from cookie %s", prop);
                  props.add(prop.trim());
               }
            }

            /* If there were headers in the cookie, set them into properties. */
            if (props.size()>0) {
                this.setProperties(props.toArray(new String[props.size()]));
            }
        } catch (Exception ex) {
            log.warn("UNABLE TO READ COOKIE", ex);
        }        
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }


    public void movePropertyRight() {

        int index = Arrays.asList(properties).indexOf(propertyToMove);

        if (index == -1) {
            return;
        }
        if (index + 1 >= properties.length) {
            return;
        }
        String replaceString = properties[index+1];
        properties[index+1]=propertyToMove;
        properties[index] = replaceString;
        availablePropertyList = null;
        sendCookie();
    }

    public void movePropertyLeft() {
        int index = Arrays.asList(properties).indexOf(propertyToMove);
        if (index == -1) {
            return;
        }
        if (index - 1 < 0) {
            return;
        }
        String replaceString = properties[index-1];
        properties[index-1]=propertyToMove;
        properties[index] = replaceString;
        availablePropertyList = null;
        sendCookie();
    }

    public boolean isPropertyEditorOpen() {
        return propertyEditorOpen;
    }

    public void setPropertyEditorOpen(boolean propertyEditorOpen) {
        this.propertyEditorOpen = propertyEditorOpen;
    }

    public boolean isPropertiesEditable() {
        return availableProperties!=null && availableProperties.length>0;
    }

    @SuppressWarnings("unchecked")
	public List<SelectItem> getAvailablePropertyItems() {
        if (availablePropertyList == null) {
            availablePropertyList = new ArrayList<SelectItem>();
            Set<String> existingProps = null;
            if (properties !=null) {
                existingProps = new HashSet(Arrays.asList(this.properties));
                for (String propertyName : properties) {
                    availablePropertyList.add(new SelectItem(propertyName, MessageUtils.createLabel(propertyName)));
                }
            }
            String[] props = this.getAvailableProperties();

            for (String propertyName : props) {
                if (existingProps!=null && existingProps.contains(propertyName)) {
                    continue;
                }
                availablePropertyList.add(new SelectItem(propertyName, MessageUtils.createLabel(propertyName)));
            }
        }
        return availablePropertyList;
    }

    public void openPropertiesEditor() {
            propertyEditorOpen = true;
    }

    public void closePropertiesEditor() {
            availablePropertyList = null;
            propertyEditorOpen = false;
            this.paginator.clearAll();
            sendCookie();
    }


    public String[] getAvailableProperties() {
        return availableProperties;
    }

    public void setAvailableProperties(String[] availableProperties) {
        this.availableProperties = availableProperties;
    }

    public boolean isPropertiesPresent() {
        return properties != null && properties.length > 0;
    }
    
    public String[] getProperties() {
        if (properties == null || properties.length==0) {
            return availableProperties;
        } else {
            return properties;
        }
    }

    public void setProperties(String[] properties) {
        this.properties = properties;
    }
    
    private void setupCrudControllerWiring() {
		((Toggleable)this.controller).addToggleListener( new ToggleListener(){
            public void toggle( ToggleEvent event ) {
                JsfCrudAdapter.this.crudChanged();
            }} );

        this.controller.addCrudControllerListener(new CrudControllerListenerAdapter(){
			public void afterCreate(CrudEvent event) {
				getPage();
			}

			public void afterDelete(CrudEvent event) {
				getPage();
			}

			public void afterUpdate(CrudEvent event) {
				getPage();
			}

			public void beforeCancel(CrudEvent event) {
				FacesContext.getCurrentInstance().renderResponse();
			}

        	});
	}

	private void setupPaginatorEventWiring() {
		/* Registers for events. */
        this.paginator.addFilteringListener(new FilteringListener(){

			public void afterFilter(FilteringEvent fe) {
				getPage();
			}

			public void beforeFilter(FilteringEvent fe) {
			}});
        
        this.paginator.addPaginationListener(new PaginationListener(){
			public void pagination(PaginationEvent pe) {
				getPage();
			}});
	}
    
    @SuppressWarnings("unchecked")
	protected void getPage() {
    	page = paginator.getPage();
    }
    
    /**
     * @see EntityLocator#getEntity()
     */
    @SuppressWarnings("unchecked")
	public T getEntity() {
        /** If the selected entity is not equal return it and set it to null. */
       if (selectedEntity!=null) {
    	   Object tmp = selectedEntity;
    	   selectedEntity = null;
    	   return (T) tmp;
       }
       else if (model.isRowAvailable()) {
           return (T) ((Row)model.getRowData()).getObject();
       }
       else {
           return null;
       }
    }
    
    private Object selectedEntity;
    public void setSelectedEnity(Object selectedEntity) {
    	this.selectedEntity = selectedEntity;
    }

    private void crudChanged() {
        paginator.reset();
    }

    @SuppressWarnings("unchecked")
	public DataModel getModel() {
    	if (page == null) {
    		page = paginator.getPage();
    	}
        /* Note if you wire in events from paginators, you will only have to change this
         * when there is a next page event.
         */
        List<Row> wrappedList = new ArrayList<Row>(page.size());
        for (Object rowData : page) {
        	Row row = null;
        	
        	/* If the rowData is an Object array it means we have select statements. */
        	if (rowData instanceof Object[]) {
        		Object [] columns = (Object[]) rowData;
        		/* First column is always the entity in question. */
        		row = new Row(columns[0]);
        		
        		/* Extract the rest of the columns. Skip the first one, which is the entity.*/
        		List<Select> selects = paginator.getSelects();
        		for (int index = 1; index < columns.length; index++) {
        			row.putInMap(selects.get(index-1).getName(), columns[index]);
        		}
        	} else {
        		row = new Row(rowData);
        	}
        	wrappedList.add(row);
        }
        model.setWrappedData( wrappedList );
        return model;
    }
    
    public void clear () {
    	page = null;
    }

    public void setModel( DataModel model ) {
        this.model = model;
    }

    public CrudOperations<T> getController() {
        return controller;
    }

    public FilterablePageable getPaginator() {
        return paginator;
    }

    @SuppressWarnings("unchecked")
    public List<T> getSelectedEntities() {
        List<Row> list = (List<Row>) model.getWrappedData();
        List<T> selectedList = new ArrayList<T>(Math.max(list.size(), 10));
        for (Row row : list){
            if (row.isSelected()) {
                selectedList.add( (T)row.getObject() );
            }
        }
        return selectedList;
    }

    class CancelListener extends CrudControllerListenerAdapter {
       @Override
        public void afterCancel( CrudEvent event ) {
            clear(); // ensure that entity edit that was canceled is resync'd with DB
        }
        
    }
}
