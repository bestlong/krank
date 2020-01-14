package org.crank.crud.controller;

import org.apache.logging.log4j.LogManager;
import org.crank.core.MapUtils;
import org.crank.core.TypeUtils;
import org.crank.core.LogUtils;
import org.crank.crud.controller.datasource.FilteringPagingDataSource;
import org.crank.crud.criteria.*;
import org.crank.crud.join.Join;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.*;

import static org.crank.core.LogUtils.debug;
import static org.crank.core.LogUtils.info;
import org.crank.message.MessageUtils;
import org.crank.message.MessageManagerUtils;
import org.apache.logging.log4j.Logger;

/**
 * This class is the main "backing bean" for paginated, filterable, sortable
 * listings. This class has the following:
 * 
 * <ul>
 * <li>Map of <code>filterableProperties</code>, which hold users sorts and
 * filters (comparisons)</li>
 * <li><code>criteria</code> set by programmers</li>
 * <li><code>orderBy</code> set by programmers</li>
 * </ul>
 * 
 * The main difference between <code>filterableProperties</code> and
 * <code>criteria</code> & <code>orderBy</code> is that
 * <code>filterableProperties</code> are set by users of the application while
 * <code>criteria</code> & <code>orderBy</code> are setup programmatically.
 * 
 * """True genius and creativity is always despised by the mundanes of the
 * world. When people tell their friends, "That (Rick) is such an (jerk)." Their
 * friends, reply: "I know. What a (jerk)." When I tell my friends that my
 * enemies are jerks they reply, "Who?"""" --Zed Shaw (with slight edits). RISA.
 * It is a joke so...
 * 
 * @author Rick Hightower
 * 
 */
@SuppressWarnings("serial")
public class FilteringPaginator extends Paginator implements
		FilterablePageable, Serializable {

    protected Logger log = LogManager.getLogger(FilteringPaginator.class);

    /** User filters. A filter is a combination of a Comparison and an OrderBy. */
	private Map<String, FilterableProperty> filterableProperties = new FilterMap();
	
	class FilterMap implements Map<String, FilterableProperty> {
		Map <String, FilterableProperty> map = new HashMap<String, FilterableProperty>();
		public void clear() {
			map.clear();			
		}

		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		public Set<java.util.Map.Entry<String, FilterableProperty>> entrySet() {
			return map.entrySet();
		}

		public FilterableProperty get(Object key) {
			FilterableProperty fp =  map.get(key);
			if (fp == null) {
				return null;
			}
		    if (fp.getType() == null) {
		    	throw new RuntimeException("GET THE TYPE WAS NULL FOR KEY " + key);
		    }
			return fp;
			
		}

		public boolean isEmpty() {
			return map.isEmpty();
		}

		public Set<String> keySet() {
			return map.keySet();
		}

		public FilterableProperty put(String key, FilterableProperty fp) {
		    if (fp.getType() == null) {
		    	throw new RuntimeException("PUT THE TYPE WAS NULL FOR KEY " + key);
		    }
			return map.put(key, fp);
		}

		public void putAll(Map<? extends String, ? extends FilterableProperty> t) {
			map.putAll(t);
		}

		public FilterableProperty remove(Object key) {
			return map.remove(key);
		}

		public int size() {
			return map.size();
		}

		public Collection<FilterableProperty> values() {
			return map.values();
		}
	}
	/**
	 * Programmaticly setup Criteria. This allows developer to pre-filter a
	 * listing, for example only showing employees in a certain department. This
	 * criteria can not be cleared by the end user.
	 */
	private List<Criterion> criteria;
	/**
	 * Programmaticly setup order by clause. This allows developer to sort a
	 * listing, for example sort by creation date. This orderBy is cleared as
	 * soon as the end user selects any sort.
	 */
	private List<OrderBy> orderBy;

	/** The type of object that we are creating a listing for. */
	@SuppressWarnings("unchecked")
	private Class type;

	/**
	 * Allows us to add joins: Relationship joins, entity joins and fetch joins
	 * to the listing. Note adding a fetch join is of dubious value for a
	 * listing, but not prevented.
	 */
	private List<Join> joins = new ArrayList<Join>();
	
	private List<Select> selects = new ArrayList<Select>();

    private List<String> propertyNames = new ArrayList<String>();


    /** The user friendly name of the object we are creating this listing for. */
	private String name;

	/** The orderBy sequence number. Used to order orderBy clauses. */
	private int sequence;

        
    /**
	 * Allows automatic joining to avoid n+1, this is a powerful feature that is
	 * under utilized. It probably needs to be revisited with our improved
	 * understanding of JPA.
	 * 
	 */
	private boolean autoJoin = false;

	/** Create a FilteringPaginator. */
	public FilteringPaginator() {
        debug(log, "FilteringPaginator() no arg constructor");
    }

	/** Create a FilteringPaginator. */
	@SuppressWarnings("unchecked")
	public FilteringPaginator(FilteringPagingDataSource dataSource, Class type) {
		super(dataSource);
		debug(log, "FilteringPaginator(dataSource=%s, type=%s)", dataSource, type);
        this.type = type;
		createFilterProperties();
	}

    @SuppressWarnings("unchecked")
	public FilteringPaginator(FilteringPagingDataSource dataSource, Class type, String... pNames) {
        super(dataSource);
        debug(log, "FilteringPaginator(dataSource=%s, type=%s)", dataSource, type);
        this.type = type;
        if (pNames == null || pNames.length==0) {
            createFilterProperties();
        } else {
            this.setPropertyNames(pNames);
            initFilterProperties();
        }
    }
    
    protected int count() {
    	debug(log, "count()");
    	if (assumedCount==NO_ASSUMED_COUNT) {
    		debug(log, "NO_ASSUMED_COUNT returning real count from datasource");
    		return dataSource.getCount();
    	} else {
    		if (!this.filterableProperties.isEmpty() || !criteria.isEmpty()) {
    			debug(log, "returning ASSUMED count from datasource");
    			return assumedCount;
    		} else {
    			debug(log, "returning real count from datasource because there are filters or criteria");
    			return dataSource.getCount();
    		}
    	}
    }
    

    private void initFilterProperties() {

        for (String propertyName : propertyNames) {

            /* Create the new filterableProperty. */

            FilterableProperty filterableProperty = null;

            try {

                filterableProperty = new FilterableProperty(
                    propertyName, TypeUtils.getPropertyType(this.type, propertyName), this.type);
            } catch (Exception ex) {
                info(log, "setupFilters(): unable to determine type=%s", filterableProperty);
                filterableProperty = new FilterableProperty(
                        propertyName, String.class, this.type, false);
            }



            debug(log, "setupFilters(): created new filterableProperty=%s", filterableProperty);

            /* Add it to our list of filterableProperties. */
            filterableProperties.put(propertyName, filterableProperty);

            /*
             * Register our toggle listener to be notified if the end user
             * activates this property.
             */
            filterableProperty
                    .addToggleListener(new FPToggleListener(propertyName));

        }

    }


    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public void setPropertyNames(String propertyNamesStr) {
        String[] pNames = propertyNamesStr.split("[.]");

        for (String property : pNames) {
            this.propertyNames.add(property.trim());
        }
    }

    public void setPropertyNames(String[] propertyNamesArray) {
        for (String property : propertyNamesArray) {
            this.propertyNames.add(property.trim());
        }
    }



    /** Gets the name of the listing. */
	public String getName() {
		return (name != null ? name : CrudUtils.getClassEntityName(type))
				+ "Paginator";
	}

	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	public List<Join> getFetches() {
		return joins;
	}

	@Deprecated
	public void setFetches(List<Join> fetches) {
		this.joins = fetches;
	}

	/**
	 * Allows us to add joins: Relationship joins, entity joins and fetch joins
	 * to the listing. Note adding a fetch join is of dubious value for a
	 * listing, but not prevented.
	 */
	public List<Join> getJoins() {
		return joins;
	}

	/**
	 * Allows us to add joins: Relationship joins, entity joins and fetch joins
	 * to the listing. Note adding a fetch join is of dubious value for a
	 * listing, but not prevented.
	 */
	public void setJoins(List<Join> joins) {
		this.joins = joins;
	}

	/** Helper method to cast dataSource into the one we need. */
	@SuppressWarnings("unchecked")
	private FilteringPagingDataSource filterablePaginatableDataSource() {
		return (FilteringPagingDataSource) this.dataSource;
	}

	/**
	 * This method automatically creates a list of FilterableProperty using
	 * reflection.
	 */
	private void createFilterProperties() {
		
		createFilterProperties(type, type, null, new PropertyScanner());
	}

	/**
	 * This class implements the toggle listener. We use this to register toggle
	 * listeners with FitlerableProperties so that when the user triggers a
	 * filter this class knows about it.
	 * 
	 * This implements the Observer/Observable pattern.
	 * 
	 * @author Rick Hightower
	 * 
	 */
	private class FPToggleListener implements ToggleListener, Serializable {
		private String property;

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		public FPToggleListener(String property) {
			this.property = property;
		}

		public FPToggleListener() {
		}

		/**
		 * This method is the callback listener and gets notified of
		 * filteableProperties that change.
		 */
		public void toggle(ToggleEvent event) {
			if (event.getSource() instanceof OrderBy) {
				OrderBy orderBy = (OrderBy) event.getSource();
				orderBy.setSequence(sequence);
				sequence++;
			}
			filter();
		}
	}

	/**
	 * This method creates the filter properties list using reflection.
	 * 
	 * This method is recursive in that it calls setupFilters which can call
	 * createFilterProperties.
	 * 
	 * REFACTOR: Seems spds and pds both get processed the same way so not sure
	 * why we need the delination of two lists. Please refactor.
	 * 
	 * @author Rick Hightower
	 * @param theType
	 * @param propertyName
	 * @param propertyScanner
	 */
	@SuppressWarnings("unchecked")
	private void createFilterProperties(final Class parentType,
			final Class theType, final String propertyName, PropertyScanner propertyScanner) {

        debug(log, "createFilters(parentType=%s, theType=%s, propertyName=%s, propertyScanner=%s)", parentType, theType, propertyName, propertyScanner);
        /* Get the beaninfo from the type object. */
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(theType);
		} catch (IntrospectionException ie) {
			throw new RuntimeException(ie);
		}
		/* Get the properties for the type. */
		PropertyDescriptor[] propertyDescriptors = beanInfo
				.getPropertyDescriptors();
		List<PropertyDescriptor> pds = new ArrayList<PropertyDescriptor>();
		List<PropertyDescriptor> spds = new ArrayList<PropertyDescriptor>();

		/* Iterate through the properties in this type. */
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

            debug(log, "createFilters(...): propertyDescriptor=%s, name=%s, type=%s",
                    propertyDescriptor, propertyDescriptor.getName(), propertyDescriptor.getPropertyType());

            /*
			 * If autoJoin is enabled, then left fetch join the property.
			 * 
			 */
			if (autoJoin
					&& CrudUtils.isEntity(propertyDescriptor.getPropertyType())) {
                debug(log, "autojoin was enabled left join fetching");
                joins.add(Join.leftJoinFetch(propertyDescriptor.getName()));
			}

			if (theType == propertyDescriptor.getPropertyType()) {
				spds.add(propertyDescriptor);
			} else {
				pds.add(propertyDescriptor);
			}
		}
		setupFilters(parentType, theType, propertyName, propertyScanner, pds);
		setupFilters(parentType, theType, propertyName, propertyScanner, spds);
	}

	/**
	 * 
	 * @param theType
	 * @param propertyName
	 * @param propertyScanner
	 * @param pds
	 */
	@SuppressWarnings("unchecked")
	private void setupFilters(final Class parentType, final Class theType,
			final String propertyName, PropertyScanner propertyScanner,
			List<PropertyDescriptor> pds) {

        debug(log, "setupFilters(parentType=%s, theType=%s, propertyName=%s, propertyScanner=%s, pds=%s)",
                parentType, theType, propertyName, propertyScanner, pds);
        
        String key;

		/* Iterate through the properties. */
		for (PropertyDescriptor propertyDescriptor : pds) {

			String property = null;

			/* Build the property path name. ex. o.employee.department.name */
			if (propertyName != null) {
				property = propertyName + "." + propertyDescriptor.getName();
			} else {
				property = propertyDescriptor.getName();
			}

            debug(log, "setupFilters(): property=%s", property);

            /* Create the new filterableProperty. */
			FilterableProperty filterableProperty = new FilterableProperty(
					property, propertyDescriptor.getPropertyType(), parentType);

            debug(log, "setupFilters(): created new filterableProperty=%s", filterableProperty);

            /* Add it to our list of filterableProperties. */
			filterableProperties.put(property, filterableProperty);

			/*
			 * Register our toggle listener to be notified if the end user
			 * activates this property.
			 */
			filterableProperty
					.addToggleListener(new FPToggleListener(property));

			/*
			 * Build a key to uniquely identify this property so we don't add it
			 * twice to the filterableProperties.
			 */
			/* Get the parent class name. */
			String parentClassName = theType.getName();
			String childClassName = propertyDescriptor.getPropertyType()
					.getName();

			key = parentClassName + "." + childClassName + "."
					+ propertyDescriptor.getName();


            debug(log, "setupFilters(): constructed key=%s", key);

            /*
			 * See if this property is an entity or a embeddable object.
			 */
			if (CrudUtils.isEntity(propertyDescriptor.getPropertyType())
					|| CrudUtils.isEmbeddable(propertyDescriptor
							.getPropertyType())) {
				/*
				 * Ask the property scanner if we can recurse on this property
				 * or if we have recursed on it too many time already. This is
				 * to help us get rid of infinite recursive loops which are as fun a
				 * barrel of monkey's infected with ebola virus.
				 */
				if (propertyScanner.canIAddThisToTheFilterableProperties(key)) {

					createFilterProperties(parentType, propertyDescriptor
							.getPropertyType(), property, propertyScanner);

				}
			}

		}
	}

	private static int pscount;

	/**
	 * This class helps getting rid of the recursive loops, e.g.,
	 * employee.manager where manager is an employee who has employees who have
	 * managers who have employees who have manager who have employees.
	 */
	class PropertyScanner implements Serializable {

		int number = 0;

		public PropertyScanner() {
			pscount++;
			this.number = pscount;
		}

		private Map<String, Integer> visitorSet = new HashMap<String, Integer>();

		private boolean canIAddThisToTheFilterableProperties(String key) {
			/* Check to see if we visited this unique key already. */
			Integer visits = visitorSet.get(key);

			/* Nope we have not been here before. */
			if (visits == null) {
				visitorSet.put(key, 0);
				return true;

				/*
				 * We have been here before and now lets see if we have the
				 * proper depth. For example we might allow
				 * employee.manager.employees.manager.employees.name but no
				 * further. The default property depth is 1 b/c this recurive
				 * loop only is a problem for 1 to 1 relationships. This code
				 * could use some refactoring.
				 */
			} else if (visits < propertyDepth) {
				int newVisits = visits.intValue() + 1;
				visitorSet.put(key, new Integer(newVisits));
				return true;
			} else {
				return false;
			}
		}
	}

	public int getPropertyDepth() {
		return propertyDepth;
	}

	public void setPropertyDepth(int propertyDepth) {
		this.propertyDepth = propertyDepth;
	}

	private int propertyDepth = 1;

    private boolean filtered = false;

    /**
	 * The filter method fires a before filter event, prepares user filters
	 * (comparisons), prepares orderBys, prepares programatic criteria, then
	 * sets up the joins for the datasource, calls reset, lastly fires an after
	 * filter event.
	 */
	public void filter() {

        filtered = true;

        debug(log, "filter()");

        fireBeforeFilter(filterablePaginatableDataSource().group());

		List<OrderBy> orderBys = prepareUserFiltersAndExtractOrderBysForFilter();

        prepareOrderByClauseProgramaticOrUserForFilter(orderBys);

		prepareProgramaticCriteriaForFilter();

		filterablePaginatableDataSource().setJoins(
				this.joins.toArray(new Join[this.joins.size()]));
		
		filterablePaginatableDataSource().setSelects(this.selects.toArray(new Select[this.selects.size()]));
		
		fireAfterFilter(filterablePaginatableDataSource().group());

		/* Reset the page count and such for pagination. */
		reset();
	}
    
    @SuppressWarnings("unchecked")
	public List getPage() {
        if (!filtered) {
            filter();
        }
        return super.getPage();
    }

    /** Builds the programmatic criteria list. */
	private void prepareProgramaticCriteriaForFilter() {
		/* Build the Criteria list. */
		if (criteria != null && criteria.size() > 0) {
			for (Criterion criterion : criteria) {
				filterablePaginatableDataSource().group().add(criterion);
			}
		}
	}

	/**
	 * Builds the order by clause for default <code>order by</code> or end
	 * user <code>order by</code>.
	 */
	private void prepareOrderByClauseProgramaticOrUserForFilter(
			List<OrderBy> orderBys) {
		/* Sort the orderBys. */
		Collections.sort(orderBys, new Comparator<OrderBy>() {
			public int compare(OrderBy ob1, OrderBy ob2) {
				return ob1.getSequence().compareTo(ob2.getSequence());
			}
		});

		/*
		 * Re-sequence the orderBys so the number show up correctly to the end
		 * user.
		 */
		int sequence = 0;
		for (OrderBy order : orderBys) {
			order.setSequence(sequence);
			sequence++;
		}

		/*
		 * If there were OrderBys passed by the end user, use them, if not, use
		 * the default orderBys.
		 */
		if (orderBys.size() > 0) {
			/* Set the orderBy list. */
			filterablePaginatableDataSource().setOrderBy(
					orderBys.toArray(new OrderBy[orderBys.size()]));
		} else {
			/* Use default sorts */
			if (orderBy != null && this.orderBy.size() > 0) {
				filterablePaginatableDataSource().setOrderBy(
						this.orderBy.toArray(new OrderBy[this.orderBy.size()]));
			}
		}
	}

	/** Prepare UserFilters and extract OrderBys for filtering. */
	@SuppressWarnings("unchecked")
	private List<OrderBy> prepareUserFiltersAndExtractOrderBysForFilter() {

        debug(log, "prepareUserFiltersAndExtractOrderBysForFilter()");

        
        
        
        /* Clear the comparison group b/c we are about to recreate it */
		filterablePaginatableDataSource().group().clear();

		/* OrderBy collection list. */
		List<OrderBy> orderBys = new ArrayList<OrderBy>();

		/* Iterator through the filters. */
		Collection<FilterableProperty> values = filterableProperties.values();

        for (FilterableProperty filterableProperty : values) {
            debug(log, "prepareUserFiltersAndExtractOrderBysForFilter() : filterableProperty=%s", filterableProperty);
            
            /* Add the comparison to the group. */
			if (filterableProperty.getComparison().isEnabled()
					&& filterableProperty.getComparison().getValue() != null) {

                debug(log, "prepareUserFiltersAndExtractOrderBysForFilter() : filterableProperty is enabled and value is set");

                /*
				 * If it is an Enumerator, let's convert it here. We let JSF
				 * manage the other types, but since there is no easy way to
				 * tell JSF about the Enumerator, we just convert it here since
				 * comparison.getValue() is an Object.
				 */
				if (filterableProperty.isEnum()) {
					Enum theEnumValue = Enum.valueOf(filterableProperty.getType(), (String) filterableProperty
							.getComparison().getValue());
					Comparison eqc = Comparison.eq(
							filterableProperty.getComparison().getName(), theEnumValue);
					filterablePaginatableDataSource().group().add(eqc);
				} else if (filterableProperty.isDate()){ //Handle date comparison with a between clause.
                    Between between = (Between)filterableProperty.getComparison();
                    Date date1 = (Date)between.getValue();
                    Date date2 = (Date)between.getValue2();
                    if (!date1.before(date2)){
                        MessageManagerUtils.getCurrentInstance().addErrorMessage("Start date must be before end date for %s",
                                MessageUtils.createLabel(between.getName()));
                    }
                    /* Advance the hour to the last hour, the minute to the last minute, and the second to the last second. */
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date2);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    date2 = calendar.getTime();
                    filterablePaginatableDataSource().group().add(
                            filterableProperty.getComparison());
                }  else if (!filterableProperty.isString() && filterableProperty.getComparison().getValue() instanceof String) {
                    if (filterableProperty.isInteger()){
                        Integer value = Integer.valueOf((String) filterableProperty.getComparison().getValue());
                        filterableProperty.getComparison().setValue(value);
                    } else if (filterableProperty.isLong()){
                        Long value = Long.valueOf((String) filterableProperty.getComparison().getValue());
                        filterableProperty.getComparison().setValue(value);
                    }  else if (filterableProperty.isShort()){
                        Short value = Short.valueOf((String) filterableProperty.getComparison().getValue());
                        filterableProperty.getComparison().setValue(value);
                    }
                    filterableProperty.getComparison().setOperator(Operator.EQ);
                    filterablePaginatableDataSource().group().add(
							filterableProperty.getComparison());
                }

                else {
					filterablePaginatableDataSource().group().add(
							filterableProperty.getComparison());
				}
			} else if (filterableProperty.getComparison().isEnabled()
					&& filterableProperty.getComparison().getValue() == null) {
                info(log, "prepareUserFiltersAndExtractOrderBysForFilter() : PROPERTY WAS ENABLED, but value is not set fp=%s", filterableProperty);

            }

			/* Add the order by clause to the list. */
			if (filterableProperty.getOrderBy().isEnabled()) {
                OrderByWithEvents orderBy = filterableProperty.getOrderBy();
                LogUtils.debug(log, "Adding an order by statement %s %s",
                        orderBy.getName(), orderBy.getSequence());
                orderBys.add(filterableProperty.getOrderBy());
			}

		}
        debug(log, "prepareUserFiltersAndExtractOrderBysForFilter() : orderBys=%s", orderBys);
        return orderBys;
	}

	/** Clear all of the filterable properties. */
	public void clearAll() {
		filterableProperties.clear();
		sequence = 0;
        if (getPropertyNames()==null || getPropertyNames().size()==0) {
            createFilterProperties();
        } else {
            initFilterProperties();
        }
        filter();
	}

	/** See if anyone is sorting any column. */
	public boolean isSorting() {
		Collection<FilterableProperty> values = filterableProperties.values();
		for (FilterableProperty fp : values) {
			if (fp.getOrderBy().isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/** See if anyone is filtering any column. */
	public boolean isFiltering() {
		Collection<FilterableProperty> values = filterableProperties.values();
		for (FilterableProperty fp : values) {
			if (fp.getComparison().isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/** Disable all of the sorts. */
	public void disableSorts() {
		sequence = 0;
		Collection<FilterableProperty> values = filterableProperties.values();
		for (FilterableProperty fp : values) {
			fp.getOrderBy().setEnabled(false);
		}
		filter();
	}

	/** Disable all of the filters. */
	public void disableFilters() {
		Collection<FilterableProperty> values = filterableProperties.values();
		for (FilterableProperty fp : values) {
			fp.getComparison().setEnabled(false);
		}
		filter();
	}

	/* Get the filterable properties. */
	public Map<String, FilterableProperty> getFilterableProperties() {
		return filterableProperties;
	}

	@SuppressWarnings("unchecked")
	public Class getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public void setType(Class type) {
		this.type = type;
	}

	public List<OrderBy> getOrderBy() {
		if (orderBy == null) {
			orderBy = new ArrayList<OrderBy>();
		}
		return orderBy;
	}

	public void setOrderBy(List<OrderBy> orderBy) {
		this.orderBy = orderBy;
	}

	public void addCriterion(Criterion criterion) {
		List<Criterion> criteriaList = getCriteria();
		criteriaList.add(criterion);
	}

	public void addOrderBy(OrderBy orderBy) {
		getOrderBy().add(orderBy);
	}

	public List<Criterion> getCriteria() {
		if (criteria == null) {
			criteria = new ArrayList<Criterion>();
		}
		return criteria;
	}

	private List<FilteringListener> listeners = new ArrayList<FilteringListener>();

	public void addFilteringListener(FilteringListener listener) {
		listeners.add(listener);
	}

	public void removeFilteringListener(FilteringListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Fire and event to the listeners.
	 * 
	 */
	protected void fireBeforeFilter(Group group) {
        debug (log, "fireBeforeFilter(group=%s)", group);
        FilteringEvent fe = new FilteringEvent(this, group);
		for (FilteringListener fl : listeners) {
			fl.beforeFilter(fe);
		}
	}

	/**
	 * Fire and event to the listeners.
	 * 
	 */
	protected void fireAfterFilter(Group group) {
        debug (log, "fireAfterFilter(group=%s)", group);
        FilteringEvent fe = new FilteringEvent(this, group);
		for (FilteringListener fl : listeners) {
			fl.afterFilter(fe);
		}
	}

	public boolean isAutoJoin() {
		return autoJoin;
	}

	public void setAutoJoin(boolean autoJoin) {
		this.autoJoin = autoJoin;
	}


	public void addSelect(Select select) {
		if (selects == null) {
			selects = new ArrayList<Select>();
		}
		selects.add(select);
	}

	/**
	 * 
	 * <p>
	 * The addFilterableEntityJoin method allows us to join subclasses.
	 * </p>
	 * 
	 * <p>
	 * To call addFilterableEntityJoin we pass the class we are joining to, the
	 * name of the entity, the name of the alias, an array of property names,
	 * and an optional join that will be added to the where clause.
	 * </p>
	 * 
	 * <p>
	 * See http://code.google.com/p/krank/w/list and search for this method name
	 * to see how to use this method. It is in the crank-crud webapp example.
	 * Other methods which they could be like this method.
	 * </p>
	 * 
	 * <code><pre>
	 * paginator.addFilterableEntityJoin(PetClinicInquiry.class, //Class we are joining 
	 * 		&quot;PetClinicInquiry&quot;, //Entity name         
	 * 		&quot;inquiry&quot;, //Alias   
	 * 		new String[] { &quot;anotherProp&quot; }, //Array of property names we want to join to. 
	 * 		&quot;o.inquiry&quot;); //How to join to the PetClinicLead
	 * </pre></code>
	 * 
	 * @author Rick Hightower
	 * @param entityClass
	 *            the class we are joining to
	 * @param alias
	 *            the name of the alias
	 * @param properties
	 *            array of property names
	 * @param joinBy
	 *            (optional if null ignored) How to join to the PetClinicLead
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void addFilterableEntityJoin(Class entityClass, String entityName,
			String alias, String properties[], String joinBy) {
		addSelect(Select.select(alias, false));
		joins.add(Join.entityJoin(entityName, alias));

		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(entityClass);
		} catch (IntrospectionException ie) {
			throw new RuntimeException(ie);
		}

		Map<String, PropertyDescriptor> props = MapUtils.convertArrayToMap(
				"name", beanInfo.getPropertyDescriptors());

		for (String property : properties) {

            LogUtils.debug(log, "#### Creating filterable property for entity join %s property %s", alias, property);

            String propertyName = alias + "." + property;

			/* Create the new filterableProperty. */
			FilterableProperty filterableProperty = new FilterableProperty(
					propertyName, props.get(property) == null ? String.class : props.get(property).getPropertyType(),
					entityClass, false);

			/*
			 * Register our toggle listener to be notified if the end user
			 * activates this property.
			 */
			filterableProperty
					.addToggleListener(new FPToggleListener(property));

			/* Add it to our list of filterableProperties. */
			filterableProperties.put(propertyName, filterableProperty);

		}

		if (joinBy != null) {
			this.addCriterion(Comparison.objectEq(joinBy, alias));
		}

	}

	public List<Select> getSelects() {
		return selects;
	}
}
