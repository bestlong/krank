package org.crank.crud.relationships;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.logging.log4j.LogManager;
import org.crank.core.CrankException;
import org.crank.core.StringUtils;
import org.crank.crud.controller.CrudUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.apache.logging.log4j.Logger;


public class RelationshipManager implements Serializable {

    protected Logger logger = LogManager.getLogger(RelationshipManager.class);

    /**
     * @CONFIG
     * The class of the entity. Used to create new instances of the entity.
     */
    private Class<?> entityClass;

    /**
     * @CONFIG @JSF_EXPRESSION
     * The name of the entity. This gets used to calculate the button names. And
     * the add method name on the parent object, i.e., add${entityName} is the default
     * add method that will get called on the parent.
     */
    private String entityName;
    
    /**
     * The name of the id property. For example, pet.getId().
     * If the property is indexed, this will be the name of the index property.
     */
    private String idPropertyName = "id";
    
    /**
     * @CONFIG or @RUNTIME
     * This allows developers to override the addMethod of the parent.
     * This will default to add${entityName} if not configured by the developer.
     */
    private String addToParentMethodName = null;

    /**
     * @CONFIG or @RUNTIME
     * This allows developers to override the removeMethod of the parent.
     * This will default to remove${entityName} if not configured by the developer.
     */
    private String removeFromParentMethodName = null;

    /**
     * @CONFIG or @RUNTIME
     * This allows developers to override the getListMethod of the parent.
     * This will default to get${entityName}s if not configured by the developer.
     */
    private String childCollectionProperty = null;

    /**
     * @RUNTIME
     * The cached version of the add method so we don't keep looking it up.
     */
    private Method addToParentMethod = null;

    /**
     * @RUNTIME
     * The cached version of the add method so we don't keep looking it up.
     */
    private Method removeFromParentMethod = null;
    
    /**
     * @CONFIG
     * Is the relationship some sort of bag, i.e., it can't be indexed, set or bag.
     * List that is a bag
     * or a Set. Set is automatically bag like.
     */
    private boolean relationshipIsBag = false;
    
    /**
     * Used to index HashMaps out of the list.
     */
    private Class<?> keyType = String.class;

    
    /**
     * Read entity from parent.
     * @param indexValue The value of the index into the parent's children collection.
     */
    public Serializable readEntityFromParent(Object parentObject, String indexValue) {
        try {

            /* Get the list from the parent. */
            Object listTypeThing = retrieveChildCollectionFromParentObject(parentObject);

            if ((listTypeThing instanceof Set) ||
                    (listTypeThing instanceof List && relationshipIsBag)) {
                return readEntityFromParentBagOrSet(indexValue, listTypeThing);
            } else if (listTypeThing instanceof List && !relationshipIsBag) {
                return readEntityFromParentList(indexValue, listTypeThing);
            } else if (listTypeThing instanceof Array) {
                return readEntityFromParentArray(indexValue, listTypeThing);
            } else if (listTypeThing instanceof Map) {
                return readEntityFromParentMap(indexValue, listTypeThing);
            }
            
        } catch (Exception exp) {
            throw new RuntimeException("Unable to get the method that " +
                    "retrieves the list/map/set of objects from parent object.", exp);
        }
        return null;
    }    
    
    /**
     * Read the value from a map.
     * If not String use the keyType (Class) and look up a JSF converter
     * for it.
     **/
    @SuppressWarnings("unchecked")
	private Serializable readEntityFromParentMap(String indexValue, Object listTypeThing) {
        Map map = (Map) listTypeThing;
        if (keyType==String.class) {
            return (Serializable) map.get(indexValue);
        } else {
            throw new RuntimeException();
        }
    }
    
    private Serializable readEntityFromParentArray(String indexValue, Object listTypeThing) {
        Object [] objects = (Object[])listTypeThing;
        return (Serializable) objects[Integer.parseInt(indexValue)];
    }
    
    @SuppressWarnings("unchecked")
	private Serializable readEntityFromParentList(String indexValue, Object listTypeThing) {
        if (indexValue.startsWith("ix--")){
            int index = Integer.parseInt(indexValue.substring(4));

            /* Read a value from a list. */
            List list = (List) listTypeThing;
            return (Serializable) list.get(index);
        } else {
            return getChildFromChildrenCollectionUsingId(indexValue, listTypeThing);
        }
    }
    
    private Serializable readEntityFromParentBagOrSet(String indexValue, Object listTypeThing) {
        /* Get object from Set or Bag. */

        if (indexValue.startsWith("hc--")){
            return getChildFromChildrenCollectionUsingHashCode(indexValue, listTypeThing);

        } else {
            return getChildFromChildrenCollectionUsingId(indexValue, listTypeThing);
        }
    }
    
    /** This method finds a child object in a children collection for Sets and Bags using the hashcode of the child.
     *  This is for children who are not in the persistence system yet, i.e., not in the database yet.
     * */
    @SuppressWarnings("unchecked")
	private Serializable getChildFromChildrenCollectionUsingHashCode(String indexValue, Object listTypeThing) {
        /* Parse the hash code out of the indexValue. */
        long hash = Long.parseLong(indexValue.substring(4));


        /** Iterate through the list and grab the id of the current child, if the
         *  id of the current child is equal to the id, then this is the object
         *  we are looking for.
         */
        Collection collection = (Collection) listTypeThing;
        Iterator iterator = collection.iterator();
        Serializable child = null;
        while(iterator.hasNext()){
            Object object = iterator.next();
            if (object.hashCode()==hash) {
                child = (Serializable) object;
                break;
            }
        }

        /**
         * This should not happen. If it does happen let the world no, send out an amber alert!
         */
        if (child==null) {
            throw new RuntimeException("Unable to find child object in parent's child collection using hashcode=" + hash);
        }
         return child;
    }

    
    
    /**
     * Get the child list from the parent, e.g., call owner.getPets() or
     * department.getEmployees(), etc.
     * 
     * @param parent
     *            the parent object (Owner has pets... Owner is the parent).
     * @return The collection of children (The Owner's pets)
     * @throws Exception
     *             Some problem.
     */
    public Object retrieveChildCollectionFromParentObject( Object parent ) {
            //need to guess if null
            Object listTypeThing = getChildCollection( parent );
            return listTypeThing;
    }
    
    public Object retrieveChildCollectionFromParentObject( Object parent, boolean createIfMissing ) {
        Object childCollection = retrieveChildCollectionFromParentObject(parent);
        if (childCollection==null && createIfMissing) {
            try {
                childCollection=initChildCollection( parent );
            } catch (Exception ex) {
                //Do nothing on purpose.
            }
        }
        return childCollection;
        
    }



    protected Object getChildCollection( Object parent ) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("getChildCollection(parent=%s)",parent));
        }
        
        if (parent != null) {
	        /*
	         * Invoke the method and return the list type thing (it can be an
	         * Array, List, Set or Map)
	         */
	        BeanWrapper wrapper = new BeanWrapperImpl (parent);
	
	        Object listTypeThing = wrapper.getPropertyValue( childCollectionProperty() );

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("returning %s from getChildCollection(parent=%s)",listTypeThing, parent));
            }
            return listTypeThing;
    	} else {
            logger.info("Parent was null");
            return null;
    	}
    }

    
    private String childCollectionProperty() {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("childCollectionProperty()  this.entityName=%s", this.entityName));
        }

        if (childCollectionProperty == null) {
            String unCapitalizeEntityname = StringUtils.unCapitalize( this.entityName);
            if (unCapitalizeEntityname.endsWith( "s" )) {
                unCapitalizeEntityname = unCapitalizeEntityname + "es";
            } else {
                unCapitalizeEntityname = unCapitalizeEntityname + "s";
            }
            childCollectionProperty = unCapitalizeEntityname;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("returning %s from childCollectionProperty()  this.entityName=%s", this.childCollectionProperty, this.entityName));
        }
        return childCollectionProperty;
    }

    /**
     * Set the child list into the parent, e.g., call owner.getPets() or
     * department.getEmployees(), etc.
     * 
     * @param parent
     *            the parent object (Owner has pets... Owner is the parent).
     * @return The collection of children (The Owner's pets)
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws Exception
     *             Some problem.
     */
    private void setChildCollectionIntoParentObject( Object parent, Object newCollection ) {
            BeanWrapper wrapper = new BeanWrapperImpl(parent);
            wrapper.setPropertyValue( this.childCollectionProperty, newCollection);
    }

    /**
     * This method finds a child object in a children collection for Sets and Bags using the id of the child.
     * This is for children who are in the persistence system already, i.e., in the database already.
     * */
    @SuppressWarnings("unchecked")
	private Serializable getChildFromChildrenCollectionUsingId(String indexValue, Object listTypeThing) {
        
        /** Iterate through the list and grab the id of the current child, if the
         *  id of the current child is equal to the id, then this is the object
         *  we are looking for.
         */
        Collection collection = (Collection) listTypeThing;
        Serializable child = null; //current child.
        try
        {
            /** Get the current id from the index parameter that was passed. */
            Long id = Long.valueOf(indexValue);
            child = this.getChildByLongId(id, collection);
        } catch (NumberFormatException nfe) {
            /** The primary key was probably a string such as a GUID so try this**/
            child = this.getChildByStringId(indexValue, collection);
        }

        /**
         * This should not happen. If it does happen let the world know, send out an amber alert!
         */
        if (child==null) {
            throw new RuntimeException("Unable to find child object in parent's child collection using id=" + indexValue);
        }
        return child;
    }
    /**
     * Most cases the child element's primary key in the collection is a Long/integer
     * as most database models use integer for primary keys.
     * @param id As a long
     * @param collection the selection of children objects.
     * @return Child model object found based on the long id.
     */
    private Serializable getChildByLongId(Long id, Collection collection) {
        Iterator iterator = collection.iterator(); //children.
        Serializable child = null; //current child.

        /**
         * Iterate through children looking for child with id of id passed.
         */
        while(iterator.hasNext()){
            Object object = iterator.next();
            BeanWrapper wrapper = new BeanWrapperImpl(object);
            Long idPropertyValue = (Long) wrapper.getPropertyValue(this.idPropertyName);
            if (id.equals(idPropertyValue)) {
                child = (Serializable) object;
                break;
            }
        }
        return child;
    }
    
    /**
     * This covers the case where the primary key is not an integer and may
     * be instead a String GUID type of key. This iterates through the collection
     * finding the child based on a GUID key instead.
     * @param id String ID as primary key like a GUID
     * @param collection Children objects to iterate through
     * @return the child model object found based on the primary String id.
     */
    private Serializable getChildByStringId(String id, Collection collection) {
        Iterator iterator = collection.iterator(); //children.
        Serializable child = null; //current child.

        /**
         * Iterate through children looking for child with id of id passed.
         */
        while(iterator.hasNext()){
            Object object = iterator.next();
            BeanWrapper wrapper = new BeanWrapperImpl(object);
            String idPropertyValue = (String) wrapper.getPropertyValue(this.idPropertyName);
            if (id.equalsIgnoreCase(idPropertyValue)) {
                child = (Serializable) object;
                break;
            }
        }
        return child;
    }
    
    /**
     * This adds a child to a parent using reflection.
     * It will use the addMethodName and reflection to invoke the addMethodName on the parent,
     * passing the child.
     *
     *  By default the addMethodName defaults to add${entityName}, however, you can inject
     *  a different addMethodName via Spring DI.
     *
     * @param parent parent
     * @param child child
     */
    @SuppressWarnings("unchecked")
    public void addToParent(Object parent, Object child) {
        try {

            Object childCollection = retrieveChildCollectionFromParentObject(parent);
            if (childCollection==null) {
                childCollection=initChildCollection( parent );
            }

        
            /* The following does the equivalent of owner.addPet(pet); */
            Class parentClass = parent.getClass();

            
            /* Initialize the addMethod name if needed. */
            if (addToParentMethodName==null) {
                addToParentMethodName = "add" + entityName;
            }
            
            /*
             * Initialize the addMethod if needed.
             * Look up the addMethod using reflection.
             */
            if (addToParentMethod==null) {
                try {
                    addToParentMethod = parentClass.getMethod(addToParentMethodName , new Class[]{this.entityClass});
                } catch (Exception methodNotFoundException ) {
                    if (this.entityClass != null) {
                        for (Method m : parentClass.getMethods()) {
                            if (m.getName().equals(addToParentMethodName)) {
                                Class[] paramTypes = m.getParameterTypes();
                                if (paramTypes != null && paramTypes.length == 1 && paramTypes[0].isAssignableFrom( this.entityClass )) {
                                    addToParentMethod = m;
                                    break;
                                }
                            }
                        }
                        }
                    if (addToParentMethod == null) { 
                        if (childCollection instanceof Collection) {
                            Collection col = (Collection) childCollection;
                            col.add( child );
                        } else {
                            throw new CrankException(methodNotFoundException, "Unable to add child %s to parent %s because %s",
                                    child, parent, methodNotFoundException.getMessage());
                        }
                    }
                    return;
                }
            }

            /*
             * Invoke the addMethod on parent passing the child as an argument.
             */                
            addToParentMethod.invoke(parent, new Object[]{child});                    
            
        } catch (Exception ex) {
            /* If there are any problems throw a nested exception. */
            throw new CrankException(ex, "Unable to add child to parent");
        }
    }

    @SuppressWarnings("unchecked")
	protected Object initChildCollection( Object parent ) throws Exception {
    	if (parent == null) {
    		return null;
    	}
        BeanWrapper wrapper = new BeanWrapperImpl (parent);
        Object childCollection=null;
        Class propertyType = wrapper.getPropertyType( this.childCollectionProperty );
        if (List.class.isAssignableFrom( propertyType )) {
            childCollection = new ArrayList();
        } else if (Set.class.isAssignableFrom( propertyType )) {
            childCollection = new HashSet();
        } else if (Map.class.isAssignableFrom( propertyType )) {
            childCollection = new HashMap();
        }
        setChildCollectionIntoParentObject( parent, childCollection );
        return childCollection;
    }
    
    @SuppressWarnings("unchecked")
    public void removeFromParent(Object parent, Object child) {
        logger.debug(String.format("RelationshipManager.removeFromParent(%s, %s)", parent, child));
        try {
            /* The following does the equivalent of owner.addPet(pet); */
            Class parentClass = parent.getClass();

            /* Initialize the removeMethod name if needed. */
            if (removeFromParentMethodName==null) {
                removeFromParentMethodName = "remove" + entityName;
            }
            /*
             * Initialize the addMethod if needed.
             * Look up the addMethod using reflection.
             */
            if (removeFromParentMethod==null) {
                try {
                    logger.debug(String.format("About to look up remove method %s",removeFromParentMethodName));
                    removeFromParentMethod = parentClass.getMethod(removeFromParentMethodName, new Class[]{this.entityClass});
                    logger.debug(String.format("Found remove method %s",removeFromParentMethod));
                } catch (Exception methodNotFoundException ) {
                    if (this.entityClass != null) {
                        for (Method m : parentClass.getMethods()) {
                            if (m.getName().equals(removeFromParentMethodName)) {
                                Class[] paramTypes = m.getParameterTypes();
                                if (paramTypes != null && paramTypes.length == 1 && paramTypes[0].isAssignableFrom( this.entityClass )) {
                                    removeFromParentMethod = m;
                                    logger.debug(String.format("Found remove method %s",removeFromParentMethod));
                                    break;
                                }
                            }
                        }
                    }
                    if (removeFromParentMethod == null) { 
                        logger.debug("Since we were unable to locate the remove method, we will try to remove the collection another way");
                        Object childCollection = retrieveChildCollectionFromParentObject( parent );
                        logger.debug(String.format("Found this child collection = %s",childCollection));
                        if (childCollection instanceof Collection) {
                            Collection col = (Collection) childCollection;
                            col.remove( child );
                            logger.debug(String.format("After child removed from collection = %s",col));
    
                        } else {
                            logger.debug("The object retrieved was not a Collection so we will throw the original exception's stack");
                            throw new CrankException(methodNotFoundException, "Unable to remove child %s from parent %s because %s",
                                    child, parent, methodNotFoundException.getMessage());
                        }
                        return;
                    }
                }
            }
            /*
             * Invoke the removeMethod on parent passing the child as an argument.
             */
            removeFromParentMethod.invoke(parent, new Object[]{child});            
        } catch (Exception ex) {
            /* If there are any problems throw a nested exception. */
            throw new CrankException(ex, "Unable to remove child");
        }
    }

    public void setAddToParentMethodName( String addToParentMethodName ) {
        this.addToParentMethodName = addToParentMethodName;
    }

    public void setEntityClass( Class<?> entityClass ) {
        this.entityClass = entityClass;
        if (entityName == null) {
            entityName = CrudUtils.getClassEntityName(entityClass);
        }
    }

    public void setEntityName( String entityName ) {
        this.entityName = entityName;
    }

    public void setIdPropertyName( String idPropertyName ) {
        this.idPropertyName = idPropertyName;
    }

    public boolean isIndexed() {
        return !relationshipIsBag;
    }

    /** Get the object id. */
    @SuppressWarnings("unchecked")
	public String getObjectId(Object parent, Object object) {
        try {
            Object listing = retrieveChildCollectionFromParentObject(parent);
            if (listing instanceof Map) {
                return getObjectParameterIdFromMap(object,(Map)listing);
            } else if (listing instanceof List && this.isIndexed()) { //List
                return getObjectParameterIdFromList(object, (List)listing);
            } else if ((listing instanceof List && !isIndexed()) || listing instanceof Set) { //Bag or Set
                return getObjectParameterIdFromBagOrSet(object, (Collection)listing);
            } else if (listing instanceof Array) {
                throw new IllegalStateException ("Array support not implemented yet");
            } else {
                throw new IllegalStateException ("Can't retrieve value unless from map, list or set");
            }
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException) {
                throw ((RuntimeException)ex);
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
    
    /** Get the object id. */
    @SuppressWarnings("unchecked")
	private String getObjectParameterIdFromBagOrSet(Object object, Collection collection) {
        BeanWrapper wrapper = new BeanWrapperImpl(object);
        Object idValue =  wrapper.getPropertyValue(this.idPropertyName);

        if (idValue==null) {
            return "hc--" + object.hashCode();
        } else if (idValue instanceof String) {
            String strIdValue = (String) idValue;
            if (strIdValue.trim().equals("")) {
                return "hc--" + object.hashCode();
            } else {
                return strIdValue;
            }
        } else {
            return idValue.toString();
        }
    }
    
    /** Get the object id. */
    @SuppressWarnings("unchecked")
	private String getObjectParameterIdFromList(Object object, List list) {


        BeanWrapper wrapper = new BeanWrapperImpl(object);
        Object idValue =  wrapper.getPropertyValue(this.idPropertyName);


        if (!(idValue instanceof Number || idValue == null) ) {
            throw new IllegalStateException("The value of index property is not a number");
        }

        Number number = (Number)idValue;
        

        if (number == null || number.longValue() <= 0) {
            int index = list.indexOf(object) == -1 ? list.size() : list.indexOf( object );
            return "ix--" + index;
        } else {
            return number.toString();
        }
    }

    /** Get the object id. */
    @SuppressWarnings("unchecked")
	private String getObjectParameterIdFromMap(Object object, Map map) {

        Object idValue = null;

        if (!this.idPropertyName.equals("id")) {
            BeanWrapper wrapper = new BeanWrapperImpl(object);
            idValue =  wrapper.getPropertyValue(this.idPropertyName);

            if (keyType==String.class) {
                return idValue.toString();
            } else {
                return idValue.toString();
            }
        } else {
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getValue().equals(object)) {
                    idValue = entry.getKey();
                    break;
                }
            }
        }

        if (idValue == null) {
            throw new IllegalStateException("No index value found");
        }

        if (keyType==String.class) {
            return idValue.toString();
        } else {
            return idValue.toString();
        }
    }
    

    public void setKeyType( Class<?> keyType ) {
        this.keyType = keyType;
    }

    public void setRelationshipIsBag( boolean relationshipIsBag ) {
        this.relationshipIsBag = relationshipIsBag;
    }

    public void setRemoveFromParentMethodName( String removeFromParentMethodName ) {
        this.removeFromParentMethodName = removeFromParentMethodName;
    }

    public String getChildCollectionProperty() {
        return childCollectionProperty;
    }

    public void setChildCollectionProperty( String childCollectionProperty ) {
        this.childCollectionProperty = childCollectionProperty;
    }

	public Class<?> getEntityClass() {
		return entityClass;
	}
    
    
}
