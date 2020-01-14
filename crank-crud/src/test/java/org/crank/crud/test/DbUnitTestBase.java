package org.crank.crud.test;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.BeforeClass;

/**
 * 
 * @version $Revision:$
 * @author Rick Hightower
 */
public abstract class DbUnitTestBase extends SpringTestNGBase {

    protected EntityManagerFactory entityManagerFactory;
    private IDatabaseTester databaseTester;
    protected final Logger log = LogManager.getLogger(this.getClass());
    protected boolean ignoreInitPersist = true;

    private IDataSet loadDataSet(IDatabaseConnection connection)
            throws Exception {
        // load the data set
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream stream = cl.getResource(getDataSetXml()).openStream();

        IDataSet data;

        if (getUseFlatXmlDataSet()) {
            data = new FlatXmlDataSet(stream);
        } else {
            data = new XmlDataSet(stream);
        }

        // order the data set
        DatabaseSequenceFilter filter = new DatabaseSequenceFilter(connection,
                data.getTableNames());
        return new FilteredDataSet(filter, data);
    }

    private Connection getWithOpenJPASession(Object delegate)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, SQLException {
        Method method = delegate.getClass().getMethod("getConnection",
                (Class[]) null);
        Object connDelegate = method.invoke(delegate, (Object[]) null);
        Method method2 = connDelegate.getClass().getMethod("getDelegate",
                (Class[]) null);
        Connection jdbcConn = (Connection) method2.invoke(connDelegate,
                (Object[]) null);
        jdbcConn.setAutoCommit(true);
        return jdbcConn;
    }

    private Connection getWithHibernateSession(Object delegate)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Connection conn = null;
        Method method = delegate.getClass().getMethod("connection",
                (Class[]) null);
        conn = (Connection) method.invoke(delegate, (Object[]) null);
        return conn;
    }

    private Connection getConnection(EntityManager em)
            throws SecurityException, IllegalArgumentException,
            NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, SQLException {
        Connection conn = null;

        Object delegate = (Object) em.getDelegate();
        if (delegate.getClass().getName().equals(
                "org.hibernate.impl.SessionImpl")) {
            conn = getWithHibernateSession(delegate);
        } else if (delegate.getClass().getName().equals(
                "org.apache.openjpa.persistence.EntityManagerImpl")) {
            conn = getWithOpenJPASession(delegate);
        }
        return conn;
    }

    @BeforeClass (groups="initPersist", dependsOnGroups={"class-init"})
    protected void initPersistenceStuff() throws Exception {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            Connection conn = getConnection(em);
            assert null != conn;
            IDatabaseConnection dbunitConn = new DatabaseConnection(conn);
            // only if we are running against HSQL
            String dbProdName = conn.getMetaData().getDatabaseProductName();
            if (dbProdName.toLowerCase().contains("hsql")) {
                DatabaseConfig config = dbunitConn.getConfig();
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                        new HsqlDataTypeFactory());
            }
            IDataSet dataSet = loadDataSet(dbunitConn);
            databaseTester = new DefaultDatabaseTester(dbunitConn);
            databaseTester.setDataSet(dataSet);
            databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
            databaseTester.onSetup();
        } catch (Exception ex) {
            log.debug("Exception in initializing database", ex);
            if (!this.ignoreInitPersist) {
                throw ex;            	
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(
            EntityManagerFactory vmcBizEntityManagerFactory) {
        this.entityManagerFactory = vmcBizEntityManagerFactory;
    }

    protected boolean getUseFlatXmlDataSet() {
        return true;
    }

    public abstract String getDataSetXml();

}
