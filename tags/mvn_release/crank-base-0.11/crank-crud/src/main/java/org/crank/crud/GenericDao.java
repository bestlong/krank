package org.crank.crud;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.crank.crud.criteria.Criterion;
import org.crank.crud.criteria.OrderBy;
import org.crank.crud.join.Fetch;

/**
 * This is our GenericDAO interface that supports its own Criteria API and finder AOP mixins.
 * 
 * @param <T>
 *            DAO class
 * @param <PK>
 *            id class
 * @version $Revision:$
 * @author Rick Hightower
 */
public interface GenericDao<T, PK extends Serializable> {

	/**
	 * This method will get the entity into the db if its a new entity or an
	 * existing detached entity.
	 * 
	 * @param entity
	 */
	public T store(T entity);

	/**
	 * Persist the entity. Details of this method are in section 3.2.1 of the <a
	 * href="http://tinyurl.com/2pc93u">JPA spec</a>. Basics - persist will
	 * take the entity and put it into the db.
	 * 
	 * @param entity
	 */
	public void persist(T entity);

	/**
	 * Merge the entity, returning (a potentially different object) the
	 * persisted entity. Details of this method are in section 3.2.4.1 of the <a
	 * href="http://tinyurl.com/2pc93u">JPA spec</a>. Basics - merge will take
	 * an exiting 'detatched' entity and merge its properties onto an existing
	 * entity. The entity with the merged state is returned.
	 * 
	 * @param entity
	 */
	public T merge(T entity);

	/**
	 * Retrieve an object that was previously persisted to the database using
	 * the indicated id as primary key
	 * 
	 * @param id
	 *            The Primary Key of the object to get.
	 * @return Type
	 */
	T read(PK id);

	/**
	 * Retrieves an entity that was previously persisted to the database using
	 * the parameter as the primary key, and maintain an exclusive lock on that
	 * entity's database row(s) until the transaction is committed. Note that
	 * this method must be executed with the bounds of a transaction.
	 * 
	 * @param id
	 *            The Primary Key of the entity to retrieve.
	 * @return Type
	 */
	T readExclusive(PK id);

	/**
	 * Refresh a persistent object that may have changed in another
	 * thread/transaction.
	 * 
	 * @param transientObject
	 *            The Object to refresh.
	 */
	void refresh(T transientObject);

	/**
	 * Write anything to db that is pending operation and clear it.
	 */
	void flushAndClear();

	/**
	 * Remove an object from persistent storage in the database. Since this uses
	 * the PK to do the delete there is a chance that the entity manager could
	 * be left in an inconsistent state, if you delete the object with id 1 but
	 * that object is still in the entity managers cache.
	 * 
	 * You can do two things, put all your PK deletes together and then call
	 * flushAndClear when done, or you can just call the delete method with the
	 * entity wich will not suffer from this problem.
	 * 
	 * @see delete(T entity)
	 * @param id
	 *            The Primary Key of the object to delete.
	 */
	void delete(PK id);

	/**
	 * Remove an object from persistent storage in the database.
	 * 
	 * @param entity
	 *            The Primary Key of the object to delete.
	 */
	void delete(T entity);

	/**
	 * Allows getting an object using a map of the field and values
	 * 
	 * @param propertyValues
	 *            properties of VO to use as filters, values matching properties
	 * @return List of requested objects.
	 */
	List<T> find(Map<String, Object> propertyValues);

	/**
	 * Allows getting an object using a map of the field and values
	 * 
	 * @param propertyNames
	 *            Names of the fields on which to search.
	 * @param values
	 *            Values of the fields on which this is searching.
	 * @return List of requested objects.
	 */
	List<T> find(String[] propertyNames, Object[] values);

	/**
	 * Does a query such as select yada from [yourclass] where field=value
	 * orderby field.
	 * 
	 * @param propertyNames
	 *            Names of the fields on which to search.
	 * @param values
	 *            Values of the fields on which this is searching.
	 * @param orderBy
	 *            fields to order by in descending order
	 * @return List of requested objects.
	 */
	List<T> find(String[] propertyNames, Object[] values, String[] orderBy);

	/**
	 * Does a query such as select yada from [yourclass] where field=value
	 * orderby field.
	 * 
	 * @param propertyValues
	 *            properties of VO to use as filters
	 * @param orderBy
	 *            fields to order by in descending order
	 * @return List of requested objects.
	 */
	List<T> find(Map<String, Object> propertyValues, String[] orderBy);

	/**
	 * Method takes a class object and string field, and value to get a list of
	 * objects. This is like a select "where something = value"
	 * 
	 * @param property
	 *            field in hibernate object
	 * @param value
	 *            value to search on
	 * @return list of the annotated objects
	 */
	List<T> find(String property, Object value);

	/**
	 * Generic method used to get all objects of a particular type. This is the
	 * same as lookup up all rows in a table.
	 * 
	 * @return List of populated objects
	 */
	List<T> find();

	int count();

	int count(Criterion... criteria);

	List<T> find(Criterion... criteria);

	List<T> find(String[] orderBy, Criterion... criteria);

	List<T> searchOrdered(Criterion criteria, String... orderBy);

	List<T> find(List<Criterion> criteria, List<String> orderBy);

	List<T> find(List<Criterion> criteria, String[] orderBy);

	List<T> find(Fetch[] fetches, String[] orderBy, Criterion... criteria);

	List<T> find(Fetch[] fetches, Criterion... criteria);

	List<T> find(Fetch... fetches);

	public List<T> find(Fetch[] fetches, String[] orderBy, int startPosition,
			int maxResults, Criterion... criteria);

	public List<T> find(Fetch[] fetches, OrderBy[] orderBy, int startPosition,
			int maxResults, Criterion... criteria);

	public List<T> find(String[] orderBy, int startPosition, int maxResults,
			Criterion... criteria);

	public List<T> find(int startPosition, int maxResults,
			Criterion... criteria);

	public List<T> find(int startPosition, int maxResults);

	List<T> find(OrderBy[] orderBy, int startPosition, int maxResults,
			Criterion... criteria);

	List<T> find(OrderBy[] orderBy, Criterion... criteria);

	public T readPopulated(final PK id);

	/**
	 * @deprecated Use store
	 */
	void create(T newInstance);

	/**
	 * @deprecated Use merge
	 */
	T update(T transientObject);

}