package org.crank.test.base;

//import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

//import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Base class for working with spring/testng tests.
 *
 * @author Chris Mathias, cmathias@arcmind.com, Rick Hightower rhightower@arc-mind.com
 * @version $Revision$
 */
public abstract class SpringTestNGBase {

    static {
//        URL log4j = Thread.currentThread().getContextClassLoader().getResource( "log4j.xml" );
//        PropertyConfigurator.configure( log4j );
    }

    protected static final Map<String, ConfigurableApplicationContext> contexts = new HashMap<String, ConfigurableApplicationContext>();
    protected ConfigurableApplicationContext applicationContext;
    protected OpenEntityManagerInTest openEntityManagerInTest;
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

    public abstract String getModuleName();

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
        applicationContext = SpringTestingUtility.getContext( this, getConfigLocations(), SpringTestNGBase.contexts, isIsolateConfigs(), getModuleName() );
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

}