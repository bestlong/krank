#We created a checked Criteria to verify the property names are valid.

# Introduction #

We created a checked Criteria to verify the property names are valid.



# Details #


```
and(Employee.class, between("age", 1, 100) );
```

```
VerifiedGroup group = new VerifiedGroup(Employee.class);
group.eq("firstName", "bar").eq("lastName", "foo").eq("department.name", "qa");

```

Essentially if you use bad property names, you get error messages right away.

You don't have to use verified criterias, but they might make debugging easier if you do.