package org.crank.crud.controller;

import org.apache.logging.log4j.LogManager;
import org.crank.crud.GenericDao;
import org.crank.core.CrankException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.io.Serializable;
import static org.crank.core.LogUtils.debug;
import org.crank.message.MessageManagerUtils;
import org.apache.logging.log4j.Logger;


public class BulkUpdaterController <T> {
    //private Map<String, Object> properties = new HashMap<String, Object>();
    private Map<String, Boolean> useProperties = new HashMap<String, Boolean>();
    
    private GenericDao<T, Serializable> repo;
    private Class<T> type;
    private EntityLocator<?> entityLocator;
    private boolean show;
    protected Logger log = LogManager.getLogger(BulkUpdaterController.class);
    private T prototype;
    @SuppressWarnings("unchecked")
	private Map map;
    private Set<String> excludeProperties = new HashSet<String> (Arrays.asList(new String[]{"name", "id"}));
    private Map<String, Boolean> excludeProps = new Map<String, Boolean>() {

		public void clear() {
		}

		public boolean containsKey(Object key) {
			return false;
		}

		public boolean containsValue(Object value) {
			return false;
		}

		public Set<java.util.Map.Entry<String, Boolean>> entrySet() {
			return null;
		}

		public Boolean get(Object key) {

            if (excludeProperties==null) {
				return false;
			} else {
			    if (excludeProperties.contains(key)) {
                    return true;
                }
			}
            for (String excludeProp : excludeProperties) {
                String sKey = (String)key;
                 if (sKey.startsWith(excludeProp)) {
                     return true;
                 }
            }
            return false;
        }

		public boolean isEmpty() {
			return false;
		}

		public Set<String> keySet() {
			return null;
		}

		public Boolean put(String key, Boolean value) {
			return null;
		}

		public void putAll(Map<? extends String, ? extends Boolean> t) {
		}

		public Boolean remove(Object key) {
			return null;
		}

		public int size() {
			return excludeProperties==null?0:excludeProperties.size();
		}

		public Collection<Boolean> values() {
			return null;
		}        
    };


    public Map<String, Boolean> getExcludeProps() {
        return excludeProps;
    }

    public BulkUpdaterController() {

    }

    public T getPrototype() {
    	if(prototype==null) {
    		try {
				prototype = type.newInstance();
			} catch (Exception e) {
				throw new CrankException(e, "Unable to instantiate class=%s", type);
			}
    	}
		return prototype;
	}

    public void setExcludeProperties(String... excludePrps) {
          excludeProperties = new HashSet<String>(Arrays.asList(excludePrps));
    }



    @SuppressWarnings("unchecked")
	public Map getMap() {
        if (map == null) {
            map = new MagicMap(getPrototype());
        }
        return map;
    }


    public void setPrototype(T prototype) {
		this.prototype = prototype;
	}

	public void show () {
    	this.show = true;
    }
    
    public void hide () {
    	this.show = false;
    }
    
    public boolean isShow() {
		return show;
	}
	public void setShow(boolean show) {
		this.show = show;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public void process() {
        List list = entityLocator.getSelectedEntities();
        boolean error=false;

        if (list.size()==0) {
            MessageManagerUtils.getCurrentInstance().addErrorMessage("At least one row must be selected.");
            MessageManagerUtils.getCurrentInstance().addErrorMessage("Try filtering the results to the ones you want to change.");
            MessageManagerUtils.getCurrentInstance().addErrorMessage("Then hit the select all button in the left corner of the table.");
            error = true;
        }


        int size = 0;
        for (Map.Entry<String,Boolean> entry : useProperties.entrySet()) {
                if (entry.getValue()) {
            		size++;
                }
        }
        if (size==0) {
            MessageManagerUtils.getCurrentInstance().addErrorMessage("At least one field must be selected to edit.");
            MessageManagerUtils.getCurrentInstance().addErrorMessage("The column headers have check boxes that signify which fields you want to edit");
            MessageManagerUtils.getCurrentInstance().addErrorMessage("Select the check box and then change the field value that you would like to bulk edit.");
            error = true;
        }

        if (error) {
            return;
        }
        debug(log, "Process list=%s", list);
        for (Object object : list) {
            BeanWrapper bw = new BeanWrapperImpl(object);
            
            Map map = getMap();
            Set<Map.Entry<String,Boolean>> entries = useProperties.entrySet();
            for (Map.Entry<String,Boolean> entry : entries) {
                String propertyName = entry.getKey();
                if (useProperties.get(propertyName)==null || !useProperties.get(propertyName)) {
            		continue;
            	}
                Object value = null;
                if (CrudUtils.isParentManyToOne(this.type, propertyName)) {
                	debug(log, "isParentManyToOne true");
                	propertyName = CrudUtils.getParentProperty(propertyName);
                }
                value = map.get(propertyName);
                debug(log, "Process property key=%s value=%s", propertyName, value);
                bw.setPropertyValue(propertyName, value);
            }
            repo.merge((T)object);
        }
        this.show=false;
        this.useProperties.clear();
        this.map = null;
		try {
			prototype = type.newInstance();
		} catch (Exception e) {
			throw new CrankException(e, "Unable to instantiate class=%s", type);
		}
        MessageManagerUtils.getCurrentInstance().addStatusMessage("Bulk update complete.");
    }
    @SuppressWarnings("unchecked")
	public void setRepo(GenericDao repo) {
        this.repo = repo;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public void setEntityLocator(EntityLocator entityLocator) {
        this.entityLocator = entityLocator;
    }

	public Map<String, Boolean> getUseProperties() {
		return useProperties;
	}

	public void setUseProperties(Map<String, Boolean> useProperties) {
		this.useProperties = useProperties;
	}
}
