#Crand Crud Introduction

# What is Crank? #


[Crank](http://code.google.com/p/krank/) is a framework for doing CRUD based applications very quickly. It is a nascent framework, but it has made some recent progress. Crank is based on ideas we explored with [Presto](http://jroller.com/RickHigh/entry/automated_master_detail_facelets_hiberante) and a much earlier framework that I worked on at [eBlox ](http://www.eblox.com/). And even a framework (Swing JDBC based) that Scott Fauerbach and I wrote earlier.

Scott Fauerbach, Chris Mathias and many others worked on Presto. Scott Fauerbach and many others worked on the original framework (which I believe we called CRUD which was Struts/EJB2 based circa 2001). Currently Paul Tabor, Chris Mathias, and many others are working on Crank.

The major improvements from Presto and Crank is that Crank is geared towards annotation driven development, Generics with less AOP than Presto (but still some). There are 20 to 40 application launched on top of Presto and a fulltime ArcMind guy maintaining it.

Out of the box with Crank you can easily create CRUD listings. For example the following code would produce a CRUD listing that allows all columns to be sorted and filtered:

```
<h:form id="expListForm" rendered="#{controllerBean.showListing}">
    <crank:listing paginator="${paginators['Employee']}" 
		jsfCrudAdapter="${cruds['Employee']}" 
                  propertyNames="firstName,lastName,active,dob,age,phone,email,department.name,description"
		pageTitle="Employees"
		parentForm="expListForm"
		reRender="${reRender}"
		crud="${cruds['Employee'].controller}"/>
</h:form>

```

The above produces the following listing (from the example crud app, which you can [download](http://code.google.com/p/krank/downloads/list) or get from [http://code.google.com/p/krank/source SVN ](.md) or [read more about](http://code.google.com/p/krank/w/list) and try it out).

![http://www.thearcmind.com/confluence/download/attachments/4173/EmployeeListing.jpg](http://www.thearcmind.com/confluence/download/attachments/4173/EmployeeListing.jpg)


The listing has pagination, each column is filterable (dates, strings, booleans, relationships). By filterable, I mean you can click the inspection icon (magnifying glass) and enter in filters for that column. You can apply as many filters and sorts (order bys) as  you need. You can clear filters and sorts as well. You can sort more than one field.

There is no code you need to write. Underneath the covers, the listing using our own Criteria API which has a translator that translates that to JPA QL then uses are GenericDAO to retrieve the results from the database. This is done without writing any additional code. The idea is that if you have a model with JPA annotations that works, then you should be able to easily create a listing.

The forms management is nice in that it will automatically render the right kind of input field. It will render file upload, date input, one to many relationship selection (list box), booleans (check box), strings (text input), large strings (text areas) and more.

Let's say for example that you have an Employee object with JPA annotations as follows:

```
@Entity
public class Employee implements Serializable {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    @Column( nullable = false, length = 32 )
    private String firstName;

    private String lastName;

    @Column( length = 150 )
    private String description;

    private boolean active;

    private int age;

    private String email;

    private String phone;

    private Integer numberOfPromotions;

    private EmployeeStatus status;

    private Integer rank;

    private Date dob;

    @OneToMany( cascade = CascadeType.ALL )
    private Set<Task> tasks;

    @ManyToOne( )
    private Department department;

    private PersistedFile file;

    public PersistedFile getFile() {
        return file;
    }

    public void setFile( PersistedFile file ) {
        this.file = file;
    }

    public void addTask( Task task ) {
        this.tasks.add( task );
    }

    public void removeTask( Task task ) {
        this.tasks.remove( task );
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    @Required
    @ProperNoun
    @Length( min = 2, max = 35 )
    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment( Department department ) {
        System.out.println( "SET DEPARTMENT " + department );
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive( boolean active ) {
        this.active = active;
    }

    public int getAge() {
        return age;
    }

    @Required
    @LongRange( min = 18L, max = 135L )
    public void setAge( int age ) {
        this.age = age;
    }

    public Integer getNumberOfPromotions() {
        return numberOfPromotions;
    }

    public void setNumberOfPromotions( Integer numberOfPromotions ) {
        this.numberOfPromotions = numberOfPromotions;
    }

    public String getLastName() {
        return lastName;
    }

    @Required
    @ProperNoun
    @Length( min = 2, max = 35 )
    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus( EmployeeStatus status ) {
        this.status = status;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank( Integer rank ) {
        this.rank = rank;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob( Date dob ) {
        this.dob = dob;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks( Set<Task> tasks ) {
        this.tasks = tasks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    @Email
    public void setEmail( String email ) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    @Phone
    public void setPhone( String phone ) {
        this.phone = phone;
    }
}

```

We can easily render the above object in a form without writng a lot of code. We can allow editing of the OneToMany relationship (Employee->Tasks) and the ManyToOne relationship (Employee->Department) without much effort and practically no code. The idea behind Crank Crud is reasonable behavior. Also we read and process the meta-data stored in the JPA annotations, Java types, and validation annotations to render the right kind of input fields and validation.

Here is the page that renders a form to edit the Employee object and its relationship.

{code}


&lt;h:form id="pagForm" prependId="false" enctype="multipart/form-data"&gt;


> <crank:form crud="${cruds['Employee'].controller}"
> > propertyNames="firstName,lastName,active,dob,age,phone,email,department,description,file">


> 

&lt;c:set var="taskDetailController" value="${employeeCrud.controller.children.tasks}" /&gt;


> > <crank:detailListing detailController="${taskDetailController}"
> > > propertyNames="name,startDate,endDate,complete"
> > > relationshipCollection="#{taskDetailController.model}"/>

> > 

Unknown end tag for &lt;/form&gt;




&lt;/h:form&gt;



{code}

The above renders this form:

![http://www.thearcmind.com/confluence/download/attachments/4173/EmployeeForm.jpg](http://www.thearcmind.com/confluence/download/attachments/4173/EmployeeForm.jpg)


There is quite a bit going on. The above allows you to edit first name, last name, active, and dob (date of birth) using text input, checkboxes and calendar widget with no effort. More amazingly, you are able to change department which is a ManyToOne relationship with no additonal code. Also a file input field is rendered for the file property. Even more amazingly the above allows you to easily render a master detail within the form (Employee has Tasks). I have hoped I piqued your interests.

In addtion, to provide validation you can use validation. Notice that the email and phone (as well as others properties) are annotated with validation rules which get processed as runtime:

```
    @Required
    @ProperNoun
    @Length( min = 2, max = 35 )
    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    @Email
    public void setEmail( String email ) {
        this.email = email;
    }

    @Phone
    public void setPhone( String phone ) {
        this.phone = phone;
    }

```

Here is an example of runtime processing (the example needs some CSS love... sorry):

![http://www.thearcmind.com/confluence/download/attachments/4173/EmployeeFormValidation.jpg](http://www.thearcmind.com/confluence/download/attachments/4173/EmployeeFormValidation.jpg)

A couple of parting shots, Crank is a tiered framework much of the code has nothing to do with JSF and can work easily with other frameworks namely Spring MVC. We are using quite a bit of Crank with a Spring MVC application. There is an example of integating Crank Validation with Spring MVC and a Crank Crud Spring MVC example is in the works (Chris Mathias).

The example Crank Crud is based on JSF, Ajax4JSF, Facelets, Spring, and JPA (Hibernate based). The listings are Ajax enabled thanks to Ajax4JSF.