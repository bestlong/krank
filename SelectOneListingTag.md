# Introduction #

The SelectOneListing Tag is an ajax enabled searchable listing that allows you to select a single item. It uses Cranks sorting, filtering listings.



# Details #


Backing bean configuration:
```
	@Bean(scope = DefaultScopes.SESSION)
	public JsfSelectOneListingController<Skill, Long> employeeToSkillController()
			throws Exception {
		JsfSelectOneListingController<Skill, Long> controller = 
                      new JsfSelectOneListingController<Skill, Long>(
				Skill.class, "primarySkill", paginators().get("Skill"),
				empCrud().getController());
		return controller;
	}

```

In Facelet page:
```
           <crank:selectOneListing jsfSelectOneController="${employeeToSkillController}"
                                   propertyNames="name"
                                   parentForm="employeeForm" />

```

Where the Employee has a primarySkill property:

```
public class Employee extends Person {

    ...
    @ManyToOne()
    private Skill primarySkill;

```

**Result:**

![http://krank.googlecode.com/svn/wiki/img/selectOneListing.jpg](http://krank.googlecode.com/svn/wiki/img/selectOneListing.jpg)