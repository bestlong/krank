I started to create another example app that will have a prestine JPA setup and Crank setup and then use that sample app as a guide for the code generator.

(I talked about this in depth here http://www.jroller.com/RickHigh/entry/creating_a_prestine_presto2_crank)

I made some good progress.

I can take the db tables created with this MySQL SQL DDL
```
--CREATE USER 'presto2'@'localhost' IDENTIFIED BY 'presto2';
--GRANT ALL PRIVILEGES ON *.* TO 'presto2'@'localhost' IDENTIFIED BY 'presto2' WITH GRANT OPTION;
--CREATE DATABASE presto2;
use presto2;

DROP TABLE IF EXISTS EMPLOYEE, DEPARTMENT, ROLE;

CREATE TABLE DEPARTMENT (
  ID INTEGER AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30)
) ENGINE=InnoDB;

CREATE TABLE ROLE (
    ROLE_ID INTEGER AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(30)
) ENGINE=InnoDB;

CREATE TABLE EMPLOYEE (
  EMP_ID INTEGER AUTO_INCREMENT PRIMARY KEY,
  FK_DEPARTMENT_ID INTEGER NULL,
  firstName VARCHAR(30),
  LAST_NAME VARCHAR(30),
  FOREIGN KEY (FK_DEPARTMENT_ID) REFERENCES DEPARTMENT(ID)
) ENGINE=InnoDB;


CREATE TABLE ROLE_EMPLOYEE (
    FK_ROLE_ID INTEGER,
    FK_EMP_ID INTEGER,
    FOREIGN KEY (FK_ROLE_ID) REFERENCES ROLE(ROLE_ID),
    FOREIGN KEY (FK_EMP_ID) REFERENCES EMPLOYEE(EMP_ID)
) ENGINE=InnoDB;
```

And reverse engineer it into this classes with codegen:

#### Department.java ####
```
package org.crank.examples.employeetask;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity  @Table(name="DEPARTMENT") 
@NamedQueries( {
		@NamedQuery(name = "Department.readPopulated", query = "SELECT DISTINCT department FROM Department department "
								+ " LEFT JOIN FETCH department.employees"
				+ " WHERE department.id=?1")
		})
public class Department implements Serializable {
    /** ID */
    @Id @Column(name="ID") 
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    /* ------- Relationships ------ */
   
    @OneToMany(mappedBy="department", cascade = CascadeType.ALL)
    private Set<Employee> employees = new HashSet<Employee>();
   

    /** Properties's fields */
   
    
    private String name;
   

    public Department () {

    }

    public void setId(Long id) {
    	this.id = id;
    }
    public Long getId() {
    	return id;
    }

   

    public Set<Employee> getEmployees() {
        return this.employees;
    }

    public void setEmployees(Set<Employee> employees ) {
        this.employees = employees;
    }
    
    public void addEmployee(Employee employee) {
    	employee.setDepartment(this);
    	employees.add(employee);
    }

    public void removeEmployee(Employee employee) {
    	employee.setDepartment(null);
    	employees.remove(employee);
    }
 

    


   
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
   

    public boolean equals(Object other) {
    	if (other==null) {
    		return false;
    	}
    	Department otherDepartment = (Department) other;
    	if (otherDepartment.id==null && this.id==null) {
    		return otherDepartment.hashCode() == this.hashCode();
    	} else if (this.id == null) {
    		return false;
    	} else {
    		return this.id.equals(otherDepartment.id);
    	}
    }
    
    public int hashCode() {
    	return id == null ? super.hashCode() : id.hashCode();
    }

}

```


#### Employee.java ####
```
package org.crank.examples.employeetask;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity  @Table(name="EMPLOYEE") 
@NamedQueries( {
		@NamedQuery(name = "Employee.readPopulated", query = "SELECT DISTINCT employee FROM Employee employee "
								+ " LEFT JOIN FETCH employee.roles"
				+ " LEFT JOIN FETCH employee.department"
				+ " WHERE employee.id=?1")
		})
public class Employee implements Serializable {
    /** ID */
    @Id @Column(name="EMP_ID") 
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    /* ------- Relationships ------ */
   
    
    @ManyToMany 
    @JoinTable(name="ROLE_EMPLOYEE",
    		joinColumns=@JoinColumn(name="FK_EMP_ID"),
    		inverseJoinColumns=@JoinColumn(name="FK_ROLE_ID"))	
    private Set <Role> roles = new HashSet<Role>();
   
    @ManyToOne (cascade = {CascadeType.REFRESH, CascadeType.MERGE}) @JoinColumn(name="FK_DEPARTMENT_ID")
    private Department department;
   

    /** Properties's fields */
   
    
    private String firstName;
   
    @Column(name="LAST_NAME")
    private String lastName;
   

    public Employee () {

    }

    public void setId(Long id) {
    	this.id = id;
    }
    public Long getId() {
    	return id;
    }

   

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles ) {
        this.roles = roles;
    }
    
    public void addRole(Role role) {
    	
    	roles.add(role);
    }

    public void removeRole(Role role) {
    	
    	roles.remove(role);
    }
 

    

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    


   
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
   
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
   

    public boolean equals(Object other) {
    	if (other==null) {
    		return false;
    	}
    	Employee otherEmployee = (Employee) other;
    	if (otherEmployee.id==null && this.id==null) {
    		return otherEmployee.hashCode() == this.hashCode();
    	} else if (this.id == null) {
    		return false;
    	} else {
    		return this.id.equals(otherEmployee.id);
    	}
    }
    
    public int hashCode() {
    	return id == null ? super.hashCode() : id.hashCode();
    }

}

```

#### Role.java ####
```
package org.crank.examples.employeetask;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity  @Table(name="ROLE") 
@NamedQueries( {
		@NamedQuery(name = "Role.readPopulated", query = "SELECT DISTINCT role FROM Role role "
								+ " LEFT JOIN FETCH role.employees"
				+ " WHERE role.id=?1")
		})
public class Role implements Serializable {
    /** ID */
    @Id @Column(name="ROLE_ID") 
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    /* ------- Relationships ------ */
   
    
    @ManyToMany 
    @JoinTable(name="ROLE_EMPLOYEE",
    		joinColumns=@JoinColumn(name="FK_ROLE_ID"),
    		inverseJoinColumns=@JoinColumn(name="FK_EMP_ID"))	
    private Set <Employee> employees = new HashSet<Employee>();
   

    /** Properties's fields */
   
    @Column(name="NAME")
    private String name;
   

    public Role () {

    }

    public void setId(Long id) {
    	this.id = id;
    }
    public Long getId() {
    	return id;
    }

   

    public Set<Employee> getEmployees() {
        return this.employees;
    }

    public void setEmployees(Set<Employee> employees ) {
        this.employees = employees;
    }
    
    public void addEmployee(Employee employee) {
    	
    	employees.add(employee);
    }

    public void removeEmployee(Employee employee) {
    	
    	employees.remove(employee);
    }
 

    


   
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
   

    public boolean equals(Object other) {
    	if (other==null) {
    		return false;
    	}
    	Role otherRole = (Role) other;
    	if (otherRole.id==null && this.id==null) {
    		return otherRole.hashCode() == this.hashCode();
    	} else if (this.id == null) {
    		return false;
    	} else {
    		return this.id.equals(otherRole.id);
    	}
    }
    
    public int hashCode() {
    	return id == null ? super.hashCode() : id.hashCode();
    }

}

```

Next I need to be able to generate:
faces-config.xml (modify an existing one?)
EmployeeTaskApplicationContext.java (modify an existing one?)
xForm.xhtml and xListing.xhtml (modify an existing one?)

Here are some minor tasks that I need to do as well:

  1. Reset button does not work (it does work in crank-crud-webapp so it should be an easy fix)
  1. Adjust column lengths (does not read value from db and store it and use it in generation)
  1. Make pom standalone (don't extend crank pom)
  1. Move ExcelControllerBean into crank-jsf-support project
  1. Create Archetype off of this example, blank project and todo
  1. Remove profiles out of pom.xml
  1. Need a way to define Enums in codegen and use them instead of int or string (where it makes sense)

I am considering getting rid of excel export as default operation and making it optional.