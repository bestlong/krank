### Problem ###

Let's say we have classes as follows:

```
//inquiries
class Inquiry
class PetStoreInquiry extends Inquiry has property a
class PetClinicInquiry extends Inquiry has property b

//leads
class Lead has an Inquiry (one to one)
class PetStoreLead extends Lead
class PetClinicLead extends Lead
```

The question is while searching for `PetStoreLead`s how do you access properties of the inquiry subclass?

This query drives JPA(Hibernate) wild and causes an error:

```
SELECT lead FROM PetStoreLead lead WHERE lead.inquiry.a='a'
```

The error you get is that `lead.inquiry` does not have a property called `a`.
The problem is that `Inquiry` does no have a property `a`, its subclass `PetStoreInquiry` does. (We are using joined subclasses different mapping like class hierarchy in the same table seem to work.)

One possible solution to the above is to rewrite the query as:

```

        SELECT lead 
        FROM PetStoreLead lead, PetStoreInquiry inquiry 
        WHERE inquiry.a='a' AND lead.inquiry=inquiry
```

There might be a better way. If there is, please share it.

We updated our Criteria API to support entity joins see: http://code.google.com/p/krank/wiki/CriteriaDSLUpdate

### Proposed update ###

I added support for entity joins in the Crank Criteria DSL. The trick is to use those enity joins from the `FilterablePageable` controller.

```
public interface FilterablePageable extends Pageable {
    Map<String, FilterableProperty> getFilterableProperties();
    void filter();
    void clearAll();
    boolean isSorting();
    boolean isFiltering();
    void disableSorts();
    void disableFilters();
    Class getType();
    void addCriterion(Criterion criterion);
    List<Criterion> getCriteria();
    void addFilteringListener(FilteringListener listener);
    void removeFilteringListener(FilteringListener listener);
    void addOrderBy(OrderBy orderBy);
    /* List of Joins can be Fetch, Join or Entity Join. */
    List<Join> getJoins();
    void setJoins(List<Join> joins);

    /* New Method */
    void addSelect(Select select);


    @Deprecated
    List<Join> getFetches();
    @Deprecated
    void setFetches(List<Join> fetches);
}
```

We need to add a class to represent the item we are selecting (pseudo code).
```
class Select {
   boolean distinct;
   String name; //must be an alias entity join
}
```

We need an extra property added to `FilterableProperty`

```
public class FilterableProperty implements Serializable, Toggleable {
    public Comparison comparison;
    public OrderByWithEvents orderBy;
    private Class type;
    private boolean useAlias; //new property
...//setter / getter removed
```

Next we need to update `FilteringPaginator` to use this new property from `FilterableProperty.useAlias`.

```
public class FilteringPaginator extends Paginator implements FilterablePageable, Serializable {


	private List<OrderBy> prepareUserFiltersAndExtractOrderBysForFilter() {
        /* Clear the comparison group b/c we are about to recreate it */
        filterablePaginatableDataSource().group().clear();

		/* OrderBy collection list. */
        List<OrderBy> orderBys = new ArrayList<OrderBy>();
        
        /* Iterator through the filters. */
        Collection<FilterableProperty> values = filterableProperties.values();
        for (FilterableProperty fp : values) {
```

Next we need to add a new method to `GenericDao` that finds more than one object in the select.

```
	public List<Object[]> find(Select[] select, 
                                   Join[] fetches, 
                                   OrderBy[] orderBy, 
                                   int startPosition,
                                   int maxResults, 
                                   Criterion... criteria);

```

Notice this method uses the new `Select` class.

The `FilterablePageable` needs a new property to tell whether we are using entity joins or not. We need a getPage that returns Object[.md](.md) not the Entity (breaks generics a bit).

We need to change JsfCrudAdapter to use the new data based on the new flag. It needs to populate the new data in the Row.

```
public class JsfCrudAdapter<T extends Serializable, PK extends Serializable> implements EntityLocator<T>, Serializable {
    ...
    public DataModel getModel() {
    	if (page == null) {
    		page = paginator.getPage();
    	}
        //ADD TODO check for new entiy join mode and process as needed
        //Add to Row map a 'related' virtual property
        //Extract entity put as normal, add related objects in map
        /* Note if you wire in events from paginators, you will only have to change this
         * when there is a next page event.
         */
        List<Row> wrappedList = new ArrayList<Row>(page.size());
        for (Object rowData : page) {
            wrappedList.add(new Row(rowData));
        }
        model.setWrappedData( wrappedList );

        return model;
    }
```

Then change `listing` composition component to use row.related.inquiry.brand.name.

This is a rough list. I am sure there are 20 things I forgot.

#### Actual Sample model and update to DAO ####
Okay so I built out the model as follows:

```
...
@Entity
@Inheritance( strategy = InheritanceType.JOINED )
@DiscriminatorColumn( name = "INQUIRY_TYPE_ID", discriminatorType = DiscriminatorType.STRING )
@Table( name = "BASE_INQUIRY" )
public class Inquiry {
	
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
	private Long id;
    
	private String name;
...
@Entity
@Inheritance( strategy = InheritanceType.JOINED )
@DiscriminatorColumn( name = "LEAD_TYPE_ID", discriminatorType = DiscriminatorType.STRING )
@Table( name = "BASE_LEAD" )
public class Lead {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )	
	private Long id;
    
	private String name;
	
    @ManyToOne
    @JoinColumn( name = "INQUIRY_ID" )
	private Inquiry inquiry;
...
@Entity
@DiscriminatorValue( "PC_INQ" )
public class PetClinicInquiry extends Inquiry {
	private String bb;

...
@Entity
@DiscriminatorValue( "PC_LEAD" )
public class PetClinicLead extends Lead{
...

```

Here is the test showing the new DAO/Criteria DSL feature being used.

The first test method shows the nature of the failure we are trying to fix:

```
	@Test (groups="reads")
    public void testFailure() {
		long time = System.currentTimeMillis();
		PetClinicInquiry inquiry = new PetClinicInquiry();
		inquiry.setName("inq1");
		inquiry.setBb("ricky" + time);
		this.petClinicInquiryDao.store(inquiry);
		
		PetClinicLead lead = new PetClinicLead();
		lead.setInquiry(inquiry);
		
		this.petClinicLeadDao.store(lead);
		try {
			this.petClinicLeadDao.find(Comparison.eq("inquiry.bb", "ricky" + time));
			fail();
		} catch (Exception ex) {
			/* Unable to run query : SELECT  o 
			 * FROM PetClinicLead o  WHERE  o.inquiry.bb = :inquiry_bb */
			/* java.sql.SQLException: 
			 * Column not found: INQUIRY1_2_.BB in statement
			 * SQL 
			 * select petclinicl0_.id as id4_, petclinicl0_1_.name as name4_, 
			 * petclinicl0_1_.INQUIRY_ID as INQUIRY3_4_ 
			 * from PetClinicLead petclinicl0_ 
			 * inner join BASE_LEAD petclinicl0_1_ 
			 * on petclinicl0_.id=petclinicl0_1_.id, 
			 * BASE_INQUIRY inquiry1_ 
			 * where petclinicl0_1_.INQUIRY_ID=inquiry1_.id 
			 * and inquiry1_2_.bb=?] */			
		}
		
		this.petClinicLeadDao.delete(lead);
		this.petClinicInquiryDao.delete(inquiry);
		
		
    }

```

The second test method show that the new feature works:

```
	@Test (groups="reads")
    public void testSuccess() {
		long time = System.currentTimeMillis();
		PetClinicInquiry inquiry = new PetClinicInquiry();
		inquiry.setName("inq1");
		inquiry.setBb("ticky" + time);
		petClinicInquiryDao.store(inquiry);
		
		PetClinicLead lead = new PetClinicLead();
		lead.setInquiry(inquiry);
		lead.setName("golden boy" + time);
		
		petClinicLeadDao.store(lead);
		
		List<Object[]> results = petClinicLeadDao.find(Select.select(Select.select("inquiry")), 
					Join.join(Join.entityJoin("PetClinicInquiry", "inquiry")),
					null,
					0,
					1000,
					Comparison.eq("inquiry.bb", true, "ticky" + time));
		
		Object[] objects = results.get(0);
		lead = (PetClinicLead) objects[0];
		inquiry = (PetClinicInquiry) objects[1];
		assertEquals("golden boy" + time, lead.getName());
		assertEquals("inq1", inquiry.getName());
		
		//SELECT  o, inquiry FROM PetClinicLead o, PetClinicInquiry inquiry WHERE  inquiry.bb = :inquiry_bb 
		petClinicLeadDao.delete(lead);
		petClinicInquiryDao.delete(inquiry);
		
		
    }

```

The following code:

```
List<Object[]> results = petClinicLeadDao.find(Select.select(Select.select("inquiry")), 
		Join.join(Join.entityJoin("PetClinicInquiry", "inquiry")),
		null, 0, 1000, Comparison.eq("inquiry.bb", true, "ticky" + time));

```

Generates this query:

```
SELECT  o, inquiry FROM PetClinicLead o, PetClinicInquiry inquiry WHERE  inquiry.bb = :inquiry_bb
```

The code can be rewritten with static includes for a more DSL feel.

```
List<Object[]> results = petClinicLeadDao.find(select(select("inquiry")), 
		join(entityJoin("PetClinicInquiry", "inquiry")),
		null, 0, 1000, eq("inquiry.bb", true, "ticky" + time));

```