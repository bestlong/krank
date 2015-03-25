# Get Lazy #

OK, this seems totally obvious now that it's all worked out.  However, working it out was...well...less than obvious.  Once again Paul Hixon turns the Crank.  Here's the 'gold standard' configuration for making your Entity Manager/Hibernate Session/Domain Objects live long enough to see their children.

The obvious part is employing the OpenEntityManagerInView interceptor (or filter - but I never did get that one working right).

```
        <bean id="openEntityInView"
		class="org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor">
		<property name="entityManagerFactory">
			<ref local="cep-webEntityManagerFactory" />
		</property>
	</bean>
```

The not-so-obvious part is how to get this to actually apply.  I had tried several configurations of bean name proxies and whatnot, before Paul finally hit the spot with this one.  Again - duh - but somehow just from reading the docs it didn't all quite click in.  Note the reference to to the intereceptor in the servlet handler.

```
        <bean id="handlerMapping"
		class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping">
		<property name="interceptors">
			<list>
            	            <ref bean="openEntityInView" />
			</list>
		</property>
	</bean>
```

S'alright?! Sok!  Thank you Paul.  You saved my hairy...well...n/m.  You really helped me out again.