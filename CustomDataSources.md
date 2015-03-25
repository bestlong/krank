# Introduction #

Crank is a great framework if you plan to use JPA and you want to do simple CRUD operations. Once you want to do something completely custom, you are not quite on your own either.

For example if you want to implement a paginateable listing that is not JPA enabled you can. You simply implement one of the following interfaces:

  * `FilteringDataSource` if you want a filterable, sortable listing
  * `PagingDataSource` is you want a paginateable listing
  * `FilteringPagingDataSource` if you want a filterable, sortable listing

By default, the framework uses a `FilteringPagingDataSource` called `DaoFilteringPagingDataSource` which uses the custom `GenericDao` interface to read object from a persistent store (typically the `GenericDaoJpa` which knows how to use JPA).

There could be a `GenericDaoIBatis` and `GenericDaoSpringJDBC` but `GenericDao` is a large interface and difficult to extend and really relies on the functionality that a framework like Hibernate, TopLink, etc. provides.

Conversely, it is much easier for custom non-JPA retrieval to implement `FilteringPagingDataSource` than try to implement the much more complex `GenericDao`.

The `FilteringPagingDataSource` is plugged into the `FilteringPaginator`. The `FilteringPaginator` does all of the GUI logic for filtering and paginating the listing. This is logic that you do not have to repeat. The `FilteringPagingDataSource` is small in comparison to the `FilteringPaginator`.

Let's demonstrate a really simple example of implementing a `FilteringPagingDataSource` as follows:

```
public class EmployeeDataSource implements FilteringPagingDataSource {
	
	/** The group holds conditions used by the listing to filter results. */
	private Group group = new Group();
	/** The orderBys holds a list of how the user wants the listings sorted. */
	private OrderBy[] orderBys = new OrderBy []{};
	/** Used to execute queries. */
	private JdbcTemplate jdbcTemplate;
	
	/** Used for calculating count of employees. */
	private static final String SELECT_COUNT = "select count(*) from Employee ";
	/** Used to calculate employee list. */
	private static final String SELECT_ROW = "select id, firstName, lastName, 5 from Employee ";
	
	
    /** Get the count of employees based on the current generated where clause.
     * The where clause is generated based on the values. */
	public int getCount() {
		return  jdbcTemplate.queryForInt(SELECT_COUNT + constructWhereClause(), extractValues());
	}

	/**
	 * Get the employees for the current page of data. 
	 * @param startItem where to start retrieving records.
	 * @param how many records to retrieve.
	 */
	@SuppressWarnings("unchecked")
	public List list(int startItem, int numItems) {
		String query = SELECT_ROW + constructWhereClause() + constructOrderByClause() + 
		" LIMIT " + (startItem + numItems) + " OFFSET " + startItem;
		return jdbcTemplate.query(
				query, 
				extractValues(), new EmployeeReportObjectMapper());
	}

	/**
	 * Get all employees.
	 */
	@SuppressWarnings("unchecked")
	public List list() {
		return jdbcTemplate.query(
				SELECT_ROW + constructWhereClause() + constructOrderByClause(), 
				extractValues(), new EmployeeReportObjectMapper());
	}


	/** Group gets manipualted by the paginator. */
	public Group group() {
		return group;
	}

	/** Order by gets used by the paginator class. */
	public OrderBy[] orderBy() {
		return this.orderBys;
	}

	/** Not used. */
	public void setFetches(Fetch[] fetches) {
	}

	/** Not used. */
	public Fetch[] fetches() {
		return null;
	}

	
	public void setOrderBy(OrderBy[] orderBy) {
		this.orderBys = orderBy;
	}

	/** Used for dependency injection of jdbcTemplate. */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

```

Most of the methods above just implement the `FilteringPagingDataSource` interface. The `getCount`, and two `list` methods use Spring's `jdbcTemplate` to actually retrieve records from the database. They really don't do much as they delegate most of the effort to the `jdbcTemplate` and the other helper methods.

The bulk of the logic in the `EmployeeDataSource` is to work with the conditions (`group`) and sorting information (`orderBys`) that were passed to this class by the `FilteringPaginator` (from the end user clicking sort links, etc.).

The bulk of the work is performed by the helper methods that process the comparison group and the orderBys.

```
	/** Construct the where clause from the group of Comparison objects. 
	 * @return the where clause based on the group of comparisons
	 */
	protected String constructWhereClause() {
		StringBuilder builder = new StringBuilder();
		/* If the group has something in it build a where clause. */
		if (group.size() > 0) {
			builder.append(" WHERE ");
			/* If the current iteration is a comparison, use it. */
			for (Criterion criterion : group) {
				if (criterion instanceof Comparison) {
					Comparison comparison = (Comparison) criterion;
					if (comparison.getName().equals("taskCount")) {
						continue;
					}
					builder.append(comparison.getName());
					builder.append(" " + comparison.getOperator().getOperator());
					builder.append(" ? ");
					builder.append("AND");
				}
			}
			return builder.toString().substring(0,builder.length()-3);
		} else {
			return "";
		}
	}

	/**
	 * Construct the order by clause.  
	 * @return
	 */
	protected String constructOrderByClause() {
		StringBuilder builder = new StringBuilder();
		if (this.orderBys.length > 0) {
			builder.append(" ORDER BY ");
			for (OrderBy orderBy : orderBys) {
				builder.append(" " + orderBy.getName() + " " + orderBy.getDirection() + ",");
			}
			
			return builder.toString().substring(0,builder.length()-1);
		} else {
			return "";
		}
	}

	/**
	 * Extract the values for the prepared statement calls.
	 * @return extracted values.
	 */
	protected Object[] extractValues() {
		List <Object> values = new ArrayList<Object>();
		for (Criterion criterion : group) {
			if (criterion instanceof Comparison) {
				Comparison comparison = (Comparison) criterion;
				if (comparison.getOperator() == Operator.LIKE || comparison.getOperator() == Operator.LIKE_START) {
					String sValue = (String) comparison.getValue();
					values.add(sValue + "%");
				} else {
					values.add(comparison.getValue());
				}
			}
		}
		return values.toArray(new Object[values.size()]);
	}

```

Next we need to configure this as follows:

```
	@SuppressWarnings({ "unchecked", "serial" })
	@Bean(scope = DefaultScopes.SESSION)
	public JsfCrudAdapter empRecordCrud() {
		EmployeeDataSource dataSource = new EmployeeDataSource();
		dataSource.setJdbcTemplate(new JdbcTemplate(employeeDataSource()));
		FilteringPaginator filteringPaginator = new FilteringPaginator(dataSource, EmployeeReportObject.class);
		JsfCrudAdapter adapter = new JsfCrudAdapter(filteringPaginator, (CrudController)empCrud().getController()){
		    public Serializable getEntity() {
		        Object object = ((Row)getModel().getRowData()).getObject();
		        EmployeeReportObject employeeReportObject = (EmployeeReportObject) object;
		        Employee employee = new Employee();
		        employee.setId(employeeReportObject.getId());
		        return employee;
		     }			
		};
		
		return adapter;
	}

```

Lastly, we use it as follows:

```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:ui="http://java.sun.com/jsf/facelets"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:a4j="https://ajax4jsf.dev.java.net/ajax"
	xmlns:rich="http://richfaces.ajax4jsf.org/rich"
	xmlns:c="http://java.sun.com/jstl/core"
	xmlns:crank="http://www.googlecode.com/crank"
	>
<ui:composition template="/templates/layout.xhtml">
	<ui:define name="content">
	
	
	    <span class="pageTitle">Employee Record Listing</span>


		<a4j:form id="employeeListingForm">
				<a4j:outputPanel ajaxRendered="true">
					<h:panelGroup rendered="${empRecordCrud.controller.showForm}">
						<crank:form crud="${empRecordCrud.controller}"
							parentForm="employeeListingForm" 
							propertyNames="firstName,lastName,numberOfPromotions,age,department" ajax="${true}" />
					</h:panelGroup>
				</a4j:outputPanel>

				<crank:listing  jsfCrudAdapter="${empRecordCrud}" 
					propertyNames="firstName,lastName"
					parentForm="employeeListingForm"
					/>
		</a4j:form>
				
	</ui:define>
</ui:composition>
</html>

```

Example of the mapper class note that you can just pull out the bits of the object you want:

```
package org.crank.sample.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;


public class EmployeeReportObjectMapper implements RowMapper {

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		EmployeeReportObject employeeReportObject = new EmployeeReportObject();
		employeeReportObject.setFirstName(rs.getString("firstName"));
		employeeReportObject.setLastName(rs.getString("lastName"));
		employeeReportObject.setId(rs.getLong("id"));
		return employeeReportObject;
	}


}

```