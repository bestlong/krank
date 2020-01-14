package org.crank.crud.relationships;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class SelectManyRelationshipManager extends RelationshipManager {

    protected Logger logger = LogManager.getLogger(SelectManyRelationshipManager.class);
    
    private Object parentObject;
	private String idProperty="id";
	private String labelProperty="name";


    @Override
    public String toString() {
       return String.format("SelectManyRelationshipManager(parentObject=%s, idProperty=%s,  labelProperty=%s)", parentObject, idProperty, labelProperty);
    }

    @SuppressWarnings("unchecked")
	public Collection getChildCollectionAsCollection() {
          return (Collection) getChildCollection(this.parentObject);
    }
    
    @SuppressWarnings("unchecked")
	public void process(Set<Object> selectedRelatedEntities, Set<Object> entitiesInView) {
		/* Grab the child collection from the parent object, i.e., roles from Employee */
		Object childCollection = getChildCollection(parentObject);
		if (childCollection == null) {
            logger.debug("childCollection was null");
            try {
                childCollection = initChildCollection( parentObject );
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
		
		/* Create an iterator based on the childCollection. */
		Iterator<Object> childCollectioniterator = iterator(childCollection);
		
		/* Create a set based on the childCollection. */
		Set currentValuesInChildCollection = toSet(childCollection);
		
		if (childCollectioniterator == null || currentValuesInChildCollection  ==  null) {
			return;
		}
		
		
		/*
		 * Iterate through the children objects that are already in the parent object, e.g.,
		 * iterate through the roles already in the Employee. 
		 */
		while (childCollectioniterator.hasNext()) {
			Object currentChildObject = childCollectioniterator.next();

            if (logger.isDebugEnabled()){
                logger.debug(String.format("process found this child %s",currentChildObject));
            }

            /* If the current object is in the view, operate on it. Don't mess with objects
			 * that are not on the current page. */
			if (entitiesInView.contains(currentChildObject)) {
				/* If the current object was in the view but not selected then remove it. */
				if (! selectedRelatedEntities.contains(currentChildObject)) {
                    if (logger.isDebugEnabled()){
                        logger.debug(String.format("process removing child=%s from parent=%s", currentChildObject, parentObject));
                    }
					removeFromParent(parentObject, currentChildObject);
				}
			}			
		}
		
		/* Iterate through the selected entities. */
		for (Object selected : selectedRelatedEntities) {
			/* If the entity is not already added, add it. */
			if (!currentValuesInChildCollection.contains(selected)) {
				addToParent(parentObject, selected);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public String getCollectionLabelString() {
		Object collection = getChildCollection(parentObject);
		if (collection == null) {
			return "";
		}
		Iterator iterator = null;
		StringBuilder builder = new StringBuilder(255);

		iterator = iterator(collection);
		
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			BeanWrapper wrapper = new BeanWrapperImpl(object);
			Object propertyValue = wrapper.getPropertyValue(labelProperty);
			String value = (propertyValue == null ? "" : propertyValue.toString());
			builder.append(value);
			builder.append(", ");
		}
		String string = builder.toString();
		if (string.trim().length() >= 2) { 
			return string.substring(0, string.length() - 2);
		} else {
			return string;
		}

	}

	@SuppressWarnings("unchecked")
	private Iterator iterator(Object collection) {
		if (collection == null) {
			return null;
		}
        logger.debug("iterator");
        Iterator iterator = null;
		if (collection instanceof Map) {
            logger.debug("Collection was a map");
            iterator = new ArrayList(((Map)collection).values()).iterator();
		} else {
            logger.debug("Collection was a List or Set");
            iterator = new ArrayList(((Collection)collection)).iterator();
		}
		return iterator;
	}

	@SuppressWarnings("unchecked")
	private Set toSet(Object collection) {
		if (collection==null) {
			return null;
		}
		if (collection instanceof Map) {
			return new LinkedHashSet(((Map)collection).values());
		} else if (collection instanceof Set) {
			return new LinkedHashSet((Set)collection);
		} else {
			return new LinkedHashSet((Collection)collection);
		}
	}

	public Object getParentObject() {
		return parentObject;
	}

	public void setParentObject(Object parentObject) {
		this.parentObject = parentObject;
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

	@SuppressWarnings("unchecked")
	public boolean isSelected(Object object) {
		Object collection = getChildCollection(parentObject);
		if (collection == null) {
			return false;
		}
		Iterator iterator = iterator(collection);

		BeanWrapper wrapper = new BeanWrapperImpl();
		
		while(iterator.hasNext()) {
			Object next = iterator.next();
			wrapper.setWrappedInstance(next);
			Object id1 = wrapper.getPropertyValue(this.idProperty);
			wrapper.setWrappedInstance(object);
			Object id2 = wrapper.getPropertyValue(this.idProperty);
			if (id1.equals(id2)) {
				return true;
			}
			
		}
		
		return false;
	}
}
