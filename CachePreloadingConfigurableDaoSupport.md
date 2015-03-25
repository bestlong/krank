# New Feature!  Preloading Configurable Dao's #

# Introduction #

If you have some static data - or any data you would like to preload into a cache on container startup, Crank now supports this capability through the GenericDao support.


# Details #

Purpose:
> Preloads data into second level cache on container startup (per configuration).  Note that in the current implementation entities MUST be annotated with @Cache to get preloaded/cached.
> (configurable generic dao's now support spring-configured caching setup).
> Tested only with EHCache so far.  EHCache 1.3.0 is required for JMX MBean integration.

> Use GenericDaoFactory

> Give it some cache configuration.
```
	     <bean id="preloadingEmployeeDao" parent="daoFactory" lazy-init="true">
	        <property name="interface">
	            <value>org.crank.crud.test.dao.EmployeeDAO</value>
	        </property>
	        <property name="bo">
	            <value>org.crank.crud.test.model.Employee</value>
	        </property>
	        <property name="preloadConfiguration">
	            <bean class="org.crank.crud.cache.PreloadConfiguration">
	                <property name="preloadingRecordCount" value="10" />
	            </bean>
	        </property>
	    </bean>
```
> It will get instanced as a PreloadableCacheableGenericDao rather than a standard GenericDao.

> From the above example, PreloadableCacheableGenericDao will load up the first ten records existing in the table the Employee pojo is mapped to.

> You can also configure the preload with some JQL.
```
        <property name="preloadConfiguration">
            <bean class="org.crank.crud.cache.PreloadConfiguration">
                <property name="preloadingHQL" value="from Employee e where e." />
            </bean>
        </property>
```

> Finally, if you need to override the lazy-load behavior of one of your model objects, forcing a full load for the cached copies, you can use the initialize children option:
```
        <bean id="preloadingEmployeeDao" parent="daoFactory" lazy-init="true">
            <property name="interface">
                 <value>org.crank.crud.test.dao.EmployeeDAO</value>
            </property>
            <property name="bo">
                <value>org.crank.crud.test.model.Employee</value>
            </property>
            <property name="preloadConfiguration">
                <bean class="org.crank.crud.cache.PreloadConfiguration">
                    <property name="preloadingRecordCount" value="10" />
                    <property name="childrenToInitialize">
                        <list>
                            <value>getDepartment</value>
                        </list>
                    </property>
                </bean>
            </property>
        </bean>
```
> EHCache support
> > To use with ehcache, you must configure your persistence.xml to use ehcache:
```
	<property name="hibernate.cache.provider_configuration_file_resource_path" value="org/crank/crud/cache/spring/crank-test-ehcache.xml" />
        <property name="hibernate.cache.provider_class" value="org.hibernate.cache.EhCacheProvider" />
        <property name="hibernate.cache.use_second_level_cache" value="true" />
```
> > > You will need a cache service entry in a context file, and utilize the EHCacheManagerFactoryBean.
```
	<bean id="ehCacheService" class="org.crank.crud.cache.EHCacheService">
	    <property name="setupMBeans" value="true" />
	</bean>
```

![http://krank.googlecode.com/svn/code/trunk/validation/docs/images/caching_and_preload_base.jpg](http://krank.googlecode.com/svn/code/trunk/validation/docs/images/caching_and_preload_base.jpg)









