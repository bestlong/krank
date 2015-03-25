# Setting up second level cache with Hibernate #
We opted to use XML for the setting up the 2nd level cache as the cache may be setup differently between applications that use the same object model (admin tools vs. customer facing app) and the thought of polluting my object model with vendor specific annotation is somewhat revolting (although sometimes needed).


For the tools we already include `jpa.properties` via `CrudDAOConfig`. (see org.crank.config.spring.support.CrudDAOConfig).

The jpa properties are as follows:
#### jpa.properties ####
```
hibernate.hbm2ddl.auto=update
hibernate.connection.hsqldb.default_table_type=cached
hibernate.show_sql=false
hibernate.format_sql=true
hibernate.use_sql_comments=true
hibernate.query.substitutions=true 1, false 0
hibernate.cache.provider_class=org.hibernate.cache.EhCacheProvider
hibernate.cache.use_query_cache=true
hibernate.max_fetch_depth=3
hibernate.ejb.cfgfile=/hibernate.cfg.xml
```

The above specifies that we want to use EhCacheProvider and that we are going to configure caching externally via an xml file hibernate.cfg.xml.

The xml file sets up the folowing caches (one for a realtionship and one for a class).

#### hibernate.cfg.xml ####
```
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>

    <session-factory name="crank-security">
    	<class-cache usage="read-write" class="org.crank.security.model.Role" />
    	<collection-cache usage="read-write" collection="org.crank.security.model.Subject.roles"/>
    </session-factory>

</hibernate-configuration>
```

The ehcache by default is expected to be in the root classpath as follows:

#### ehcache.xml ####

```
<ehcache>

	<!-- Sets the path to the directory where cache .data files are created.
		
		If the path is a Java System Property it is replaced by
		its value in the running VM.
		
		The following properties are translated:
		user.home - User's home directory
		user.dir - User's current working directory
		java.io.tmpdir - Default temp file path -->
	<diskStore path="java.io.tmpdir" />


	<!--Default Cache configuration. These will applied to caches programmatically created through
		the CacheManager.
		
		The following attributes are required:
		
		maxElementsInMemory            - Sets the maximum number of objects that will be created in memory
		eternal                        - Sets whether elements are eternal. If eternal,  timeouts are ignored and the
		element is never expired.
		overflowToDisk                 - Sets whether elements can overflow to disk when the in-memory cache
		has reached the maxInMemory limit.
		
		The following attributes are optional:
		timeToIdleSeconds              - Sets the time to idle for an element before it expires.
		i.e. The maximum amount of time between accesses before an element expires
		Is only used if the element is not eternal.
		Optional attribute. A value of 0 means that an Element can idle for infinity.
		The default value is 0.
		timeToLiveSeconds              - Sets the time to live for an element before it expires.
		i.e. The maximum time between creation time and when an element expires.
		Is only used if the element is not eternal.
		Optional attribute. A value of 0 means that and Element can live for infinity.
		The default value is 0.
		diskPersistent                 - Whether the disk store persists between restarts of the Virtual Machine.
		The default value is false.
		diskExpiryThreadIntervalSeconds- The number of seconds between runs of the disk expiry thread. The default value
		is 120 seconds.
	-->

	<defaultCache maxElementsInMemory="10000" eternal="false"
		overflowToDisk="true" timeToIdleSeconds="120" timeToLiveSeconds="120"
		diskPersistent="false" diskExpiryThreadIntervalSeconds="120" />

	<cache name="org.crank.security.model.Role" maxElementsInMemory="50"
		eternal="true" overflowToDisk="false" />

	<cache name="org.crank.security.model.User.roles"
		maxElementsInMemory="50" eternal="true" overflowToDisk="false" />

	<cache name="org.hibernate.cache.UpdateTimestampsCache"
		maxElementsInMemory="5000" eternal="true" overflowToDisk="true" />

	<cache name="org.hibernate.cache.StandardQueryCache"
		maxElementsInMemory="100000" eternal="false" timeToIdleSeconds="6000"
		timeToLiveSeconds="60000" overflowToDisk="true" />


</ehcache>
```

To observe the cache hits, we added the following to the log4j.xml file:

```
    <category name="org.hibernate.cache">
        <priority value="DEBUG" />
    </category>
```