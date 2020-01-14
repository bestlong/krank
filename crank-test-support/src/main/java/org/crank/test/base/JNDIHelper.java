package org.crank.test.base;

import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;

/**
 * This class was required because jboss cache wants to load
 * prior to the JNDI context being available.  Chikn n Egg issue.
 * This class supports SpringTestNGBase.
 * @author Chris Mathias
 *
 */
public class JNDIHelper {

    private static List<String> jndiRelatedProperties;
    private static SimpleNamingContextBuilder builder;

    private static void setup() throws Exception {
        JNDIHelper.setupJndiPropertyList();
        if (JNDIHelper.builder == null) {
            JNDIHelper.builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        }
    }

    public static void setupJndiDataSourceContext(String jndiPath, Logger logger) throws Exception {
        JNDIHelper.setup();
        SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        DriverManagerDataSource ds = new DriverManagerDataSource();
        Map<String, String> jndiValuesMap = JNDIHelper.getJndiProperties( logger );
        ds.setDriverClassName( jndiValuesMap.get( "hibernate.connection.driver_class" ) );
        ds.setUrl( jndiValuesMap.get( "hibernate.connection.url" ) );
        ds.setUsername( jndiValuesMap.get( "hibernate.connection.username" ) );
        ds.setPassword( jndiValuesMap.get( "hibernate.connection.password" ) );
        builder.bind( jndiPath, ds );
        builder.bind( "java:/TransactionManager", new MockTransactionManager() );
    }

    public static void addToJndi(String jndiPath, Object toAdd) throws Exception {
        JNDIHelper.setup();
        JNDIHelper.builder.bind( jndiPath, toAdd );
    }

    private static void setupJndiPropertyList() {
        JNDIHelper.jndiRelatedProperties = new ArrayList<String>();
        JNDIHelper.jndiRelatedProperties.add( "hibernate.connection.driver_class" );
        JNDIHelper.jndiRelatedProperties.add( "hibernate.connection.url" );
        JNDIHelper.jndiRelatedProperties.add( "hibernate.connection.username" );
        JNDIHelper.jndiRelatedProperties.add( "hibernate.connection.password" );
        JNDIHelper.jndiRelatedProperties.add( "hibernate.dialect" );
    }

    private static Map<String, String> getJndiProperties( Logger logger ) {
        Map<String, String> jndiProperties = new HashMap<String, String>();

        Map<String, String> propertiesMap = JNDIHelper.getPropertiesFromPersistenceXml(logger);

        for (String property : propertiesMap.keySet()) {
            if (JNDIHelper.jndiRelatedProperties.contains( property )) {
                jndiProperties.put( property, propertiesMap.get( property ) );
                logger.debug("Added jndi property to map: " + property + "=" + propertiesMap.get( property ));
            }
        }

        return jndiProperties;
    }

    private static Map<String, String> getPropertiesFromPersistenceXml( Logger logger ) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream stream = cl.getResource( "META-INF/persistence.xml" ).openStream();
            Document doc = docBuilder.parse( stream );

            // normalize text representation
            doc.getDocumentElement().normalize();
            NodeList properties = doc.getChildNodes();
            properties = ((Element)properties.item( 0 )).getElementsByTagName( "property" );

            for (int s = 0; s < properties.getLength(); s++) {

                Node datasetNode = properties.item( s );

                if (datasetNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element propertyElement = (Element) datasetNode;
                    String name = propertyElement.getAttribute( "name" );
                    String value = propertyElement.getAttribute( "value" );
                    propertiesMap.put( name, value );
                }

            }

        } catch (SAXParseException err) {
            logger.debug( "** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() );
            logger.debug( " " + err.getMessage() );

        } catch (SAXException e) {
            Exception x = e.getException();
            ( ( x == null ) ? e : x ).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return propertiesMap;
    }
}
