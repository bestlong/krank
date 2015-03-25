Now you can join entities as follows:

```

        personDao.find(join(entityJoin("Employee", "e")), 
                        Comparison.eq("e.firstName", true, "Rick"), 
                        Comparison.eq("ssn", "333333311"), 
                        Comparison.objectEq("o","e")); 
```

This is important for using properties of subclasses in where clauses.


The above generates a query as follows:

```
    select o from Person o, Employee e where e.firstName='Rick' and 
o.ssn=''333333311' and o=e 
```

This was needed for DMT and now LMT.


You now also have regular joins which were needed for FMT (for
example):

```
     employeeDAO.find(join(join("o.department", true, "foo")), 
                      Comparison.eq("foo.name", true, "Engineering")); 
```




which produces a query that looks like this:

```
     select o from Employee o join o.department foo where 
foo.name='Engineering' 
```

FMT and DMT are internal apps we are developing. It also seems LMT needs this and a possible extention to the listing component.