# `OpenEntityManagerInTest complements OpenEntityManagerInView pattern` #

If you've felt the pain, you've seen the need.  Trying to get that unit test to pass with a lazy loading reference and it just won't do it?  parent.children.size() giving you endless grief in unit testing?  Here's the solution.

# Details #

Paul Hixson devised this workaround to default session behavior in spring/jpa.  Note that this feature has been integrated into the Crank SpringTestNGBase class, so if you use that for your test base, you get this for free.

**The Issue:**
Even with a JPAInterceptor, Spring doesn't want to keep an EntityManager alive after a transaction, or method completion.  Objects you load in your unit tests using Dao's (Crank GenericDao for example) are detached immediately.

**The Solution:**
OpenEntityManagerInTest fixes this for you.  It does so by borrowing essentially the same code Spring uses for OpenEntityManagerInView interceptor, in conjunction with a few alterations to your test case to support the interaction.

If you are using Crank's SpringTestNGBase class for your unit tests, all you need to do is add a bean configuration to your test beans file, like so.
```
    <!--  Test Fixtures  -->
    <bean id="openEntityManagerInTest" 
          class="org.crank.crud.test.OpenEntityManagerInTest">
        <property name="entityManagerFactory">
            <ref bean="entityManagerFactory" />
        </property>
    </bean>
```

If you are NOT using our base class, you can still get functionality from this by using the configuration noted above, along with your personalized version of the following code, to open and close the entity manager as appropriate.

```
@BeforeClass
private void setUpEntityManager() {

        try {
            openEntityManagerInTest =  
                getBean(OpenEntityManagerInTest.class, "openEntityManagerInTest");
                openEntityManagerInTest.loadEntityManager();
        } catch (NoSuchBeanDefinitionException e) {
            logger.warn("NO OpenEntityManagerInTest defined in application context.  You may have lazy initialization errors!!");
        }
        
    }
    
    @AfterClass
    public void afterClass() {
        if (openEntityManagerInTest != null) {
            openEntityManagerInTest.unloadEntityManager();
        }
    }
```

**Caveat Emptor:**
Paul reminded me that OpenEntityManagerInTest stores the EntityManagerFactory in the TransactionSynchronizationManager.  It is assumed at this time that this implies that Spring transaction management must be configured.


That's pretty much it!  Happy Testing.