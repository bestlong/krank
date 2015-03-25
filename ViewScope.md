# Introduction #

We added a new scope called `view` scope. View scope is the same lifecycle as the current JSF view. Objects mapped to `view` scope will be disposed of when the current view is disposed.


# Details #

Installing view scope in Spring

```
     <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="view">
                    <bean class="org.crank.config.spring.support.ViewScope"/>
                </entry>
            </map>
        </property>
    </bean>
```

To use this scope just specify scope as "view"

```
     <bean id="mybean" class="for.myclass.MyClass" scope="view"/>

```

Of if you are using Spring Java config (and you should be), then:

```
	@Bean(scope = "view")
	public JsfCrudAdapter empRecordCrud() {
                ...
        }

```