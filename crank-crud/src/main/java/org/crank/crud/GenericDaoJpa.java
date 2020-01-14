package org.crank.crud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.crank.crud.criteria.*;
import org.crank.crud.criteria.OrderBy;
import org.crank.crud.join.*;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;


/**
 * This is our GenericDAO interface that supports its own Criteria API and
 * finder AOP mixins.
 * 
 * @param <T>
 *            Dao class
 * @param <PK>
 *            id class
 * @version $Revision:$
 * @author Rick Hightower
 */
public class GenericDaoJpa<T, PK extends Serializable> extends JpaDaoSupport
		implements GenericDao<T, PK>, DaoMethods {
	
	protected Class<T> type = null;

	protected boolean distinct = false;

	protected Logger logger = LogManager.getLogger(GenericDaoJpa.class);
	
	private String newSelectStatement = null;
	
	private List<QueryHint<?>> queryHints;
	
	protected String idPropertyName = null;



    public void setQueryHints(List<QueryHint<?>> queryHints) {
		this.queryHints = queryHints;
	}

	public GenericDaoJpa(final Class<T> aType) {
		this.type = aType;
	}

	public GenericDaoJpa() {
	}

    @Transactional
    public T store(T entity) {
        logger.debug(String.format("store(entity) called, %s", entity));
        T persistedEntity = entity;
        /*
         * If the entity has a null id, then use the persist method,
         * otherwise use the merge method.
         */
        if (!hasId(entity)) {
            // TODO: the error reporting could be deferred (the entity has an ID
            // that is in the db but not in the entity manager)
            logger.debug("Calling perist on JPA");
            getJpaTemplate().persist(entity);
        } else {
            logger.debug("Calling merge since an id was found");
            persistedEntity = (T) getJpaTemplate().merge(entity);
        }
        return persistedEntity;
    }



	protected boolean hasId(T entity) {
		return GenericDaoUtils.hasId(entity, this.getIdPropertyName());
	}

	@Transactional
	public void persist(T entity) {
		getJpaTemplate().persist(entity);
	}

	@Transactional
	public T merge(T entity) {
		return getJpaTemplate().merge(entity);
	}

	@Transactional
	public <RE> RE mergeRelated(RE entity) {
		return getJpaTemplate().merge(entity);
	}
	
	@Transactional
	public Collection<T> store(Collection<T> entities) {
		Collection<T> storedResults = new ArrayList<T>(entities.size());
		for (T entity : entities) {		
			storedResults.add(store(entity));
		}
		return storedResults;
	}

	@Transactional
	public void persist(Collection<T> entities) {
		for (T entity : entities) {		
			persist(entity);
		}
	}

	@Transactional
	public Collection<T> merge(Collection<T> entities) {
		Collection<T> mergedResults = new ArrayList<T>(entities.size());
		for (T entity : entities) {		
			mergedResults.add(merge(entity));
		}
		return mergedResults;
	}
	
	@Transactional
	public <RE> Collection<RE> mergeRelated(Collection<RE> entities) {
		Collection<RE> mergedResults = new ArrayList<RE>(entities.size());
		for (RE entity : entities) {		
			mergedResults.add(mergeRelated(entity));
		}
		return mergedResults;
	}
	

	public void setIsDistinct(boolean isDistinct) {
		this.distinct = isDistinct;
	}

	public void setDistinct(boolean isDistinct) {
		this.distinct = isDistinct;
	}

	public boolean isDistinct() {
		return this.distinct;
	}

	@Transactional
	public void delete(final PK id) {
		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager entityManager)
					throws PersistenceException {
				String queryString = "DELETE FROM " + getEntityName()
						+ " WHERE " + getIdPropertyName() + " = " + id;
				Query query = entityManager.createQuery(queryString);
				query.executeUpdate();
				return null;
			}
		});
	}
	
	@Transactional
	public void delete(final Collection<T> entities) {
		for (T entity : entities) {
			delete(entity);
		}
	}

	public T read(PK id) {
		if (type == null) {
			throw new UnsupportedOperationException(
					"The type must be set to use this method.");
		}
		return read(type, id);
	}

	@SuppressWarnings("unchecked")
	public T read(Class<?> clazz, PK id) {
		return (T) getJpaTemplate().find(clazz, id);
	}

	@SuppressWarnings("unchecked")
	public T readExclusive(Class clazz, PK id) {
		EntityManager em = getTransactionEntityManager();
		Object entity = getJpaTemplate().find(clazz, id);
		em.lock(entity, LockModeType.READ);
		return (T) entity;
	}

	public T readExclusive(PK id) {
		if (type == null) {
			throw new UnsupportedOperationException(
					"The type must be set to use this method.");
		}
		return readExclusive(type, id);
	}

	@SuppressWarnings("unchecked")
	public T refresh(final T transientObject) {
		return (T)getJpaTemplate().execute(new JpaCallback() {		
			public Object doInJpa(EntityManager em) throws PersistenceException {
				T managedEntity = null;
				if (em.contains(transientObject)) {
					managedEntity = transientObject;
				}
				else {
					managedEntity = em.merge(transientObject);
				}
				// now refresh the state of the managed object, discarding any changes that were made
				em.refresh(managedEntity);
				return managedEntity;
			}
		}); 
	}
	
	@SuppressWarnings("unchecked")
	public T refresh(final PK id) {
		if (type == null) {
			throw new UnsupportedOperationException(
					"The type must be set to use this method.");
		}
		return (T)getJpaTemplate().execute(new JpaCallback() {		
			public Object doInJpa(EntityManager em) throws PersistenceException {
				T managedEntity = em.find(type, id);
				// now refresh the state of the managed object, discarding any changes that were made
				em.refresh(managedEntity);
				return managedEntity;
			}
		}); 
	}	

	public Collection<T> refresh(final Collection<T> entities) {
		Collection<T> refreshedResults = new ArrayList<T>(entities.size());
		for (T entity : entities) {
			refreshedResults.add(refresh(entity));
		}
		return refreshedResults;
	}
	
	@SuppressWarnings("unchecked")
	protected T findManagedEntity(EntityManager em, final T entity) {
		T managedObject = null;
		if (em.contains(entity)) {
			// the entity is already managed in this context
			managedObject = entity;
		}
		else {
			// the entity is not managed in this context, presumedly detached
			// we'll find it in the context by primary key
			BeanWrapper bw = new BeanWrapperImpl(entity);
			PK key = (PK)bw.getPropertyValue(getIdPropertyName());
			Class<T> entityClass = (Class<T>)entity.getClass();
			managedObject = em.find(entityClass, key);
			if (managedObject == null) {
				throw new EntityNotFoundException();
			}
		}
		return managedObject;
	}
	

	@Transactional
	public void flushAndClear() {
		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager entityManager)
					throws PersistenceException {
				entityManager.flush();
				entityManager.clear();
				return null;
			}
		});
	}

	
	public void clear() {
		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager entityManager)
					throws PersistenceException {
				entityManager.clear();
				return null;
			}
		});
	}

	@Transactional
	public void flush() {
		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager entityManager)
					throws PersistenceException {
				entityManager.flush();
				return null;
			}
		});
	}
	
	/**
	 * @deprecated use store
	 */
	@Transactional
	public void create(final T newInstance) {
		getJpaTemplate().merge(newInstance);
	}

	/**
	 * @deprecated use merge
	 */
	@Transactional
	public T update(final T transientObject) {
		return getJpaTemplate().merge(transientObject);
	}

	public void setType(final Class<T> aType) {
		this.type = aType;
	}

	public List<T> searchOrdered(Criterion criteria, String... orderBy) {
		return this.find(orderBy, criteria);
	}

	public List<T> searchOrdered(Class<T> clazz, Criterion criteria,
			String... orderBy) {
		return this.find(clazz, orderBy, criteria);
	}

	public List<T> find(List<Criterion> criteria, List<String> orderBy) {
		return find(type, criteria, orderBy);
	}

	public List<T> find(Class<T> clazz, List<Criterion> criteria,
			List<String> orderBy) {
		return find(clazz, orderBy.toArray(new String[orderBy.size()]),
				criteria.toArray(new Criterion[criteria.size()]));
	}

	public List<T> find(List<Criterion> criteria, String[] orderBy) {
		return find(type, criteria, orderBy);
	}

	public List<T> find(Class<T> clazz, List<Criterion> criteria,
			String[] orderBy) {
		return find(clazz, orderBy, criteria.toArray(new Criterion[criteria
				.size()]));
	}

	public List<T> find(Map<String, Object> propertyValues) {
		return find(propertyValues, null);
	}

	public List<T> find(Class<T> clazz, Map<String, Object> propertyValues) {
		return find(clazz, propertyValues, null);
	}

	public List<T> find(Map<String, Object> propertyValues, String[] orderBy) {
		return find(orderBy, Group.and(propertyValues));
	}

	public List<T> find(Class<T> clazz, Map<String, Object> propertyValues,
			String[] orderBy) {
		return find(clazz, orderBy, Group.and(propertyValues));
	}

	public List<T> find(String property, Object value) {
		return find(type, property, value);
	}

	public List<T> find(Class<T> clazz, String property, Object value) {
		HashMap<String, Object> propertyValues = new HashMap<String, Object>();
		propertyValues.put(property, value);
		return find(clazz, propertyValues);
	}

	public List<T> find() {
		return find(type);
	}

	
	public int count() {
		logger.debug("count() called");
		return (Integer) getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = createCountQuery(em);
				prepareQueryHintsIfNeeded(query);
				Number count = (Number) query.getSingleResult();
				return count.intValue();
			}

		});
	}

	private Query createCountQuery(EntityManager em) {
		Query query = null;
		try {
			query = em.createNamedQuery(getEntityName() + ".countAll");
			logger
					.debug("using native countAll query for entity "
							+ getEntityName() );
		} catch (IllegalArgumentException iae) {
			// thrown if a query has not been defined with the given name
			query = em.createQuery("SELECT count(*) FROM " + getEntityName() 
					+ " instance");
			logger.debug("using JPA countAll query for entity " + getEntityName() );
		} catch (PersistenceException pe) {
			// JPA spec says IllegalArgumentException should be thrown, yet
			// hibernate throws PersistenceException instead
			query = em.createQuery("SELECT count(*) FROM " + getEntityName() 
					+ " instance");
			logger.debug("using JPA countAll query for entity " + getEntityName() );
		}
		return query;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<T> find(Class<T> clazz) {
		String entityName = getEntityName(clazz);
		String sQuery = null;
		if (newSelectStatement == null) {
			sQuery = "SELECT instance FROM " + entityName + " instance";
		} else {
			sQuery =  "SELECT " + newSelectStatement + " FROM " + entityName + " o";
		}
		final String fQuery = sQuery; 
		return (List<T>) getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(fQuery);
				prepareQueryHintsIfNeeded(query);
				return query.getResultList();
			}});
	}

	public List<T> find(String[] propertyNames, Object[] values) {
		return find(propertyNames, values, null);
	}

	public List<T> find(Class<T> clazz, String[] propertyNames, Object[] values) {
		return find(clazz, propertyNames, values, null);
	}

	public List<T> find(Criterion... criteria) {
		return find((String[]) null, criteria);
	}

	public int count(final Criterion... criteria) {
		if (criteria == null || criteria.length == 0) {
			// count all if no criteria specified
			return count();
		}
		
        if (logger.isDebugEnabled()) {
            logger.debug("count called with Criteria " + criteria);
        }
        final Group group = criteria != null ? Group.and(criteria) : null;

		final String squery = CriteriaUtils.createCountQuery(group, this.type, this.distinct);
		return executeCountQuery(group, squery, criteria);
	}



	private int executeCountQuery(final Group group,
			final String squery, final Criterion... criteria) {
		try {
			return (Integer) this.getJpaTemplate().execute(new JpaCallback() {
				public Object doInJpa(EntityManager em)
						throws PersistenceException {
					Query query = em.createQuery(squery.toString());
					if (criteria != null) {
						GenericDaoUtils.addGroupParams(query, group, null);
					}
					return ((Long) query.getResultList().get(0)).intValue();
				}
			});
		} catch (Exception ex) {
			throw new RuntimeException("Unable to run query : " + squery, ex);
		}
	}

	public int count(Join[] joins, final Criterion... criteria) {
		if ((joins == null || joins.length == 0) &&
			(criteria == null || criteria.length == 0)	)
		{
			// count all if no joins or criteria specified
			return count();
		}
		
		
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("count called with Criteria=%s and joins=%s ", criteria,
                    joins!=null ? Arrays.asList(joins) : "no joins"));
        }

		final Group group = criteria != null ? Group.and(criteria) : null;

		final StringBuilder sbquery = new StringBuilder("SELECT count("
				+ (this.distinct ? "DISTINCT " : "") + "o )  ");
		sbquery.append(CriteriaUtils.constructFrom(type, joins));
		sbquery.append(CriteriaUtils.constructJoins(joins));
		sbquery.append(" ");
		sbquery.append(CriteriaUtils.constuctWhereClause(group));
		return executeCountQuery(group, sbquery.toString(), criteria);
		
	}
	

	public List<T> find(Class<T> clazz, Criterion... criteria) {
		return find(clazz, (String[]) null, criteria);
	}

	public List<T> find(Class<T> clazz, String[] propertyNames,
			Object[] values, String[] orderBy) {
		if (propertyNames.length != values.length) {
			throw new RuntimeException(
					"You are not using this API correctly. The propertynames length should always match values length.");
		}
		Map<String, Object> propertyValues = new HashMap<String, Object>(
				propertyNames.length);
		int index = 0;
		for (String propertyName : propertyNames) {
			propertyValues.put(propertyName, values[index]);
			index++;
		}
		return find(clazz, propertyValues, orderBy);
	}

	public List<T> find(String[] propertyNames, Object[] values,
			String[] orderBy) {
		return find(type, propertyNames, values, orderBy);
	}

	public List<T> find(Join[] fetches, String[] orderBy,
			Criterion... criteria) {
		return doFind(this.type, orderBy, criteria, fetches);
	}

	public List<T> find(Join[] fetches, Criterion... criteria) {
		return doFind(this.type, null, criteria, fetches);
	}

	public List<T> find(Join... fetches) {
		return doFind(this.type, null, null, fetches);
	}

	public List<T> find(Join[] fetches, String[] orderBy, int startPosition,
			int maxResults, Criterion... criteria) {
		return doFind(this.type, orderBy, criteria, fetches, startPosition,
				maxResults);
	}

	public List<T> find(String[] orderBy, int startPosition, int maxResults,
			Criterion... criteria) {
		return doFind(this.type, orderBy, criteria, null, startPosition,
				maxResults);
	}

	public List<T> find(int startPosition, int maxResults,
			Criterion... criteria) {
		return doFind(this.type, (OrderBy[]) null, criteria, null,
				startPosition, maxResults);
	}

	public List<T> find(int startPosition, int maxResults) {
		return doFind(this.type, (OrderBy[]) null, null, null, startPosition,
				maxResults);
	}

	public List<T> find(Join[] fetches, OrderBy[] orderBy, int startPosition,
			int maxResults, Criterion... criteria) {
		return doFind(this.type, orderBy, criteria, fetches, startPosition,
				maxResults);
	}

	public List<T> find(OrderBy[] orderBy, int startPosition, int maxResults,
			Criterion... criteria) {
		return doFind(this.type, orderBy, criteria, null, startPosition,
				maxResults);
	}

	public List<T> find(OrderBy[] orderBy, Criterion... criteria) {
		return doFind(this.type, orderBy, criteria, null, -1, -1);
	}

	public List<T> find(Class<T> clazz, String[] orderBy, Criterion... criteria) {
		return doFind(clazz, orderBy, criteria, null);
	}



	private List<T> doFind(Class<T> clazz, OrderBy[] orderBy,
			final Criterion[] criteria, Join[] fetches,
			final int startPosition, final int maxResult) {
		return doFind(clazz, null, this.distinct, orderBy, criteria, fetches, startPosition,
				maxResult);
	}
	
	@SuppressWarnings("unchecked")
	private List<T> doFind(Class<T> clazz, Select[] selects, boolean distinctFlag, OrderBy[] orderBy,
			final Criterion[] criteria, Join[] joins, final int startPosition, final int maxResult) {

		final Group group = criteria != null ? Group.and(criteria)
				: new Group();

		final String sQuery = CriteriaUtils.createQuery(clazz, selects, this.newSelectStatement, distinctFlag,
				orderBy, joins, group);
		
		

		return (List<T>)executeQueryWithJPA(criteria, startPosition, maxResult, group,
				sQuery);
	}

	@SuppressWarnings("unchecked")
	private List executeQueryWithJPA(final Criterion[] criteria,
			final int startPosition, final int maxResult, final Group group,
			final String sQuery) {
		try {
			return (List) this.getJpaTemplate().execute(new JpaCallback() {
				public Object doInJpa(EntityManager em)
						throws PersistenceException {
					Query query = em.createQuery(sQuery);
					if (criteria != null) {
						GenericDaoUtils.addGroupParams(query, group, null);
					}
					if (startPosition != -1 && maxResult != -1) {
						query.setFirstResult(startPosition);
						query.setMaxResults(maxResult);
					}
					prepareQueryHintsIfNeeded(query);
					return query.getResultList();
				}

			});
		} catch (Exception ex) {
			logger.debug("failed to run a query", ex);
			throw new RuntimeException("Unable to run query : " + sQuery, ex);
		}
	}

	private final void prepareQueryHintsIfNeeded(Query query) {
		if (queryHints!=null && queryHints.size()>0) {
			for (QueryHint<?> qh : queryHints) {
				query.setHint(qh.getName(), qh.getValue());
			}
		}
	}


	private List<T> doFind(Class<T> clazz, String[] orderBy,
			final Criterion[] criteria, Join[] fetches,
			final int startPosition, final int maxResult) {

		if (orderBy != null) {
			List<OrderBy> list = new ArrayList<OrderBy>();
			for (String order : orderBy) {
				list.add(new OrderBy(order, OrderDirection.ASC));
			}
			return doFind(clazz, list.toArray(new OrderBy[orderBy.length]),
					criteria, fetches, startPosition, maxResult);

		} else {
			return doFind(clazz, (OrderBy[]) null, criteria, fetches,
					startPosition, maxResult);
		}

	}

	private List<T> doFind(Class<T> clazz, String[] orderBy,
			Criterion[] criteria, Join[] fetches) {
		return doFind(clazz, orderBy, criteria, fetches, -1, -1);
	}

	public List<T> find(String[] orderBy, Criterion... criteria) {
		return find(type, orderBy, criteria);
	}


	protected String getEntityName() {
		if (type == null) {
			throw new UnsupportedOperationException(
					"The type must be set to use this method.");
		}
		return getEntityName(this.type);
	}

	private String getEntityName(Class<T> type) {
		return GenericDaoUtils.getEntityName(type);
	}


	public Object executeFinder(final Method method, final Object[] queryArgs) {
		final String queryName = queryNameFromMethodName(method.getName());
		return getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNamedQuery(queryName);
				int index = 1;
				for (Object arg : queryArgs) {
					query.setParameter(index, arg);
					index++;
				}
				if (List.class.isAssignableFrom(method.getReturnType())) {
					return query.getResultList();
				} else {
					return query.getSingleResult();
				}
			}
		});
	}

    public Object executeDelete(final Method method, final Object[] queryArgs) {
        final String queryName = queryNameFromMethodName(method.getName());
        return getJpaTemplate().execute(new JpaCallback() {

            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query query = em.createNamedQuery(queryName);
                int index = 1;
                for (Object arg : queryArgs) {
                    query.setParameter(index, arg);
                    index++;
                }
                return query.executeUpdate();
            }
        });
    }

    public Object executeUpdate(final Method method, final Object[] queryArgs) {
        final String methodName = method.getName();
        final String queryName = queryNameFromMethodName(methodName);
        return getJpaTemplate().execute(new JpaCallback() {
            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query query = em.createNamedQuery(queryName);
                int index = 0;
                
                for (Object arg : queryArgs) {
                    query.setParameter(index+1, arg);
                    index++;
                }
                return query.executeUpdate();
            }
        });
    }

    public T readPopulated(Class<?> clazz, final PK id) {
		try {
			return doReadPopulated(id);
		} catch (JpaSystemException jpaSystemException) {
			return read(id);
		} catch (IllegalArgumentException iae) {
			return read(id);
		} catch (InvalidDataAccessApiUsageException idaaue) {
			return read(id);
		} catch (UncategorizedDataAccessException udae) {
			return read(id);
		}
	}

	@SuppressWarnings("unchecked")
	private T doReadPopulated(final PK id) {
		final String queryName = getEntityName()
				+ ".readPopulated";
		return (T) getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNamedQuery(queryName);
				query.setParameter(1, id);
				return query.getSingleResult();
			}
		});
	}

	public T readPopulated(final PK id) {
		return readPopulated(type, id);
	}

	public String queryNameFromMethodName(String finderMethod) {
		return getEntityName() + "." + finderMethod;
	}

	@Transactional
	public void delete(final T entity) {
		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager entityManager) throws PersistenceException {
				T managedEntity = entity;
				if (!entityManager.contains(managedEntity)) {
					managedEntity = entityManager.merge(entity);
				}
				entityManager.remove(managedEntity);
				return null;
			}
		
		});
	}

	private EntityManager getTransactionEntityManager() {
		EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager
				.getResource(getJpaTemplate().getEntityManagerFactory());
		return emHolder.getEntityManager();
	}

	public void setNewSelectStatement(String newSelectStatement) {
		this.newSelectStatement = newSelectStatement;
	}
	
	@Transactional
	public void run(Runnable runnable) {
		runnable.run();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> find(Select[] selects, Join[] joins,
			OrderBy[] orderBy, final int startPosition, final int maxResults,
			final Criterion... criteria) {
		final Group group = criteria != null ? Group.and(criteria)
				: new Group();

		final String sQuery = CriteriaUtils.createQuery(this.type, selects, this.newSelectStatement, this.distinct,
				orderBy, joins, group);

		return (List<Object[]>)executeQueryWithJPA(criteria, startPosition, maxResults, group,
				sQuery);
	}


	
	public String getIdPropertyName() {
		if (idPropertyName==null) {       
			idPropertyName = GenericDaoUtils.searchFieldsForPK(this.type);
			if (idPropertyName==null) {
				logger.debug("Unable to find @Id in fields looking in getter methods");
				idPropertyName = GenericDaoUtils.searchMethodsForPK(this.type);
			}
			if (idPropertyName==null) {
				logger.debug("Unable to find @Id using default of id");
				idPropertyName="id";
			}
		}
		return idPropertyName;
	}

	public void setIdPropertyName(String idPropertyName) {
		this.idPropertyName = idPropertyName;
	}

    public <Z> Z getReference(Class<Z> entityClass, Object primaryKey) {
        return this.getJpaTemplate().getReference(entityClass, primaryKey);
    }

	public List<T> find(Map<String, Object> propertyValues, int startRecord,
			int numRecords) {
		return doFind(this.type, (Select[])null, true, (OrderBy[]) null,
				new Criterion[]{Group.and(propertyValues)}, (Join[]) null, startRecord, numRecords);
	}

}
