package org.crank.crud.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//import org.apache.log4j.PropertyConfigurator;
import org.crank.crud.JNDIHelper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Base class for working with spring/testng tests.
 * 
 * @author Chris Mathias, cmathias@arcmind.com
 * @version $Revision: 1906 $
 */
public abstract class SpringTestNGBase {

//    static {
//        URL log4j = Thread.currentThread().getContextClassLoader().getResource( "log4j.xml" );
//        PropertyConfigurator.configure( log4j );
//    }

    private static final Map<String, ConfigurableApplicationContext> contexts = new HashMap<String, ConfigurableApplicationContext>();
    protected ConfigurableApplicationContext applicationContext;
    private OpenEntityManagerInTest openEntityManagerInTest;
    protected Logger logger = LogManager.getLogger( getClass() );

    public void setUpJndiSpring() {
        try {
            if (isSetupJndiContext()) {
                JNDIHelper.setupJndiDataSourceContext( "java:crankDS", logger );
            }
        } catch (Exception e) {
            // test will fail on its own
        }
    }

    @BeforeClass( groups = { "class-init" } )
    public void classInit() {
        setUpSpring();
        setUpEntityManager();

        if (isSetupJndiContext()) {
            setUpJndiSpring();
        }

        afterSetup();
    }

    protected void afterSetup() {
        //template method for extenders
    }

    private void setUpEntityManager() {

        try {
            openEntityManagerInTest =  
                getBean(OpenEntityManagerInTest.class, "openEntityManagerInTest");
                openEntityManagerInTest.loadEntityManager();
        } catch (NoSuchBeanDefinitionException e) {
            // It's entirely possible that there's no such bean.
            // There's probably a more Springy way to do this, but 
            // this exception swallow save adding more Springness to
            // this class.
            logger.warn("NO OpenEntityManagerInTest defined in application context.  You may have lazy initialization errors!!");
        }
        
    }
    
    @AfterClass
    public void afterClass() {
        if (openEntityManagerInTest != null) {
            openEntityManagerInTest.unloadEntityManager();
        }
    }
    
    public void setUpSpring() {
        applicationContext = SpringTestingUtility.getContext( this, getConfigLocations(), contexts, isIsolateConfigs() );
        logger.debug( "initialization complete" );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T getBean( Class<T> type, String beanName ) {
        return (T) applicationContext.getBean( beanName );
    }

    public List<String> getConfigLocations() {
        // changed from abstract to template method
        return new ArrayList<String>();
    }

    protected boolean isSetupJndiContext() {
        return false;
    }

    protected boolean isIsolateConfigs() {
        return false;
    }

    public String toString( Object obj ) {
        return ToStringBuilder.reflectionToString( obj, ToStringStyle.MULTI_LINE_STYLE );
    }

}