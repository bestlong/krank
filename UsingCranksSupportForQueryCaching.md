Cranks has support for query caching. You merely need to specify query hints that will be passed. The query hints are tied to a specific vendor. This feature has been tested with Hibernate and ehcache. Note: Crank does work out of the box with Hibernate and OpenJPA (and possibly others).

Here is what the configuration might look like before query caching:


```
@Configuration(defaultLazy = Lazy.TRUE)
public abstract class SecurityApplicationContext extends CrudJSFConfig {

	private static List<CrudManagedObject> managedObjects;

	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
			managedObjects.add(new CrudManagedObject(User.class));
			managedObjects.add(new CrudManagedObject(Role.class));
			managedObjects.add(new CrudManagedObject(Group.class));
		}
		return managedObjects;

	}

```

To enable query caching you simply pass `QueryHint`s, there are some helper methods that make this an easier affair as follows:

```
@Configuration(defaultLazy = Lazy.TRUE)
public abstract class SecurityApplicationContext extends CrudJSFConfig {

	private static List<CrudManagedObject> managedObjects;

	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
			managedObjects.add(new CrudManagedObject(User.class));
			CrudManagedObject roleCMO = new CrudManagedObject(Role.class);
			roleCMO.setQueryHints(
					queryHintList(queryHintTrue("org.hibernate.cacheable"), 
								  queryHintValue("org.hibernate.cacheRegion","security")
					));
			
			managedObjects.add(roleCMO);
			managedObjects.add(new CrudManagedObject(Group.class));
		}
		return managedObjects;

	}

```

If caching is setup correctly, the second call in this example will not hit the database:

```
		System.out.println("1-#######################################-");
		roleRepo().find();
		System.out.println("1-#######################################-");
		
		System.out.println("2-#######################################-");
		roleRepo().find();
		System.out.println("2-#######################################-");
```

Thus when you turn on debugging you get the following:

```
1-#######################################-
[2008-02-15 15:37:33,992] INFO  org.hibernate.cache.StandardQueryCache starting query cache at region: security 
[2008-02-15 15:37:33,998] DEBUG org.hibernate.cache.StandardQueryCache checking cached query results in region: security 
[2008-02-15 15:37:33,998] DEBUG org.hibernate.cache.EhCache key: sql: select role0_.id as id2_, role0_.name as name2_ from Role role0_; parameters: ; named parameters: {} 
[2008-02-15 15:37:33,998] DEBUG org.hibernate.cache.EhCache Element for sql: select role0_.id as id2_, role0_.name as name2_ from Role role0_; parameters: ; named parameters: {} is null 
[2008-02-15 15:37:33,998] DEBUG org.hibernate.cache.StandardQueryCache query results were not found in cache 
[2008-02-15 15:37:33,999] DEBUG org.hibernate.SQL 
    /* SELECT
        instance 
    FROM
        Role instance */ select
            role0_.id as id2_,
            role0_.name as name2_ 
        from
            Role role0_ 
[2008-02-15 15:37:34,005] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,005] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,005] DEBUG org.hibernate.cache.EhCache Element for org.crank.security.model.Role#1 is null 
[2008-02-15 15:37:34,007] DEBUG org.hibernate.cache.ReadWriteCache Cached: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,007] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,007] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.EhCache Element for org.crank.security.model.Role#2 is null 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.ReadWriteCache Cached: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.EhCache Element for org.crank.security.model.Role#3 is null 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.ReadWriteCache Cached: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,008] DEBUG org.hibernate.cache.StandardQueryCache caching query results in region: security; timestamp=4927974006702080 
1-#######################################-
2-#######################################-
[2008-02-15 15:37:34,009] DEBUG org.hibernate.cache.StandardQueryCache checking cached query results in region: security 
[2008-02-15 15:37:34,009] DEBUG org.hibernate.cache.EhCache key: sql: select role0_.id as id2_, role0_.name as name2_ from Role role0_; parameters: ; named parameters: {} 
[2008-02-15 15:37:34,009] DEBUG org.hibernate.cache.StandardQueryCache Checking query spaces for up-to-dateness: [Role] 
[2008-02-15 15:37:34,009] DEBUG org.hibernate.cache.EhCache key: Role 
[2008-02-15 15:37:34,009] DEBUG org.hibernate.cache.UpdateTimestampsCache [Role] last update timestamp: 4927974006628354, result set timestamp: 4927974006702080 
[2008-02-15 15:37:34,009] DEBUG org.hibernate.cache.StandardQueryCache returning cached query results 
[2008-02-15 15:37:34,011] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,011] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,011] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,011] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,011] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,011] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,012] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,012] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,012] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#3 
2-#######################################-

```

Notice that the first call populates the cache and the 2nd call uses the cache.

This also works with the criteria DSL as follows:

```
		System.out.println("8-#######################################-");
		roleRepo().find(like("name", "r_%"));
		System.out.println("8-#######################################-");
		
		System.out.println("9-#######################################-");
		roleRepo().find(like("name", "r_%"));
		System.out.println("9-#######################################-");

```

The above generates the following debug logging:

```
8-#######################################-
[2008-02-15 15:37:34,035] DEBUG org.hibernate.cache.StandardQueryCache checking cached query results in region: security 
[2008-02-15 15:37:34,036] DEBUG org.hibernate.cache.EhCache key: sql: select role0_.id as id2_, role0_.name as name2_ from Role role0_ where role0_.name like ?; parameters: ; named parameters: {name=r_%} 
[2008-02-15 15:37:34,036] DEBUG org.hibernate.cache.EhCache Element for sql: select role0_.id as id2_, role0_.name as name2_ from Role role0_ where role0_.name like ?; parameters: ; named parameters: {name=r_%} is null 
[2008-02-15 15:37:34,036] DEBUG org.hibernate.cache.StandardQueryCache query results were not found in cache 
[2008-02-15 15:37:34,036] DEBUG org.hibernate.SQL 
    /* SELECT
        o 
    FROM
        Role o  
    WHERE
        o.name LIKE :name  */ select
            role0_.id as id2_,
            role0_.name as name2_ 
        from
            Role role0_ 
        where
            role0_.name like ? 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Item was already cached: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Item was already cached: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Item was already cached: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.ReadWriteCache Caching: org.crank.security.model.Role#4 
[2008-02-15 15:37:34,037] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#4 
[2008-02-15 15:37:34,039] DEBUG org.hibernate.cache.ReadWriteCache Item was already cached: org.crank.security.model.Role#4 
[2008-02-15 15:37:34,039] DEBUG org.hibernate.cache.StandardQueryCache caching query results in region: security; timestamp=4927974006915072 
8-#######################################-
9-#######################################-
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.StandardQueryCache checking cached query results in region: security 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.EhCache key: sql: select role0_.id as id2_, role0_.name as name2_ from Role role0_ where role0_.name like ?; parameters: ; named parameters: {name=r_%} 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.StandardQueryCache Checking query spaces for up-to-dateness: [Role] 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.EhCache key: Role 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.UpdateTimestampsCache [Role] last update timestamp: 4927974006882304, result set timestamp: 4927974006915072 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.StandardQueryCache returning cached query results 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,040] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#1 
[2008-02-15 15:37:34,041] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,041] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,041] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#2 
[2008-02-15 15:37:34,041] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,041] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,041] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#3 
[2008-02-15 15:37:34,046] DEBUG org.hibernate.cache.ReadWriteCache Cache lookup: org.crank.security.model.Role#4 
[2008-02-15 15:37:34,046] DEBUG org.hibernate.cache.EhCache key: org.crank.security.model.Role#4 
[2008-02-15 15:37:34,046] DEBUG org.hibernate.cache.ReadWriteCache Cache hit: org.crank.security.model.Role#4 
9-#######################################-
```