package org.crank.test.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.IDatabaseTester;
import org.dbunit.DefaultDatabaseTester;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;


/**
 *  @version $Revision$
 *  @author Rick Hightower
 */
public abstract class DbUnitTestBase extends SpringTestNGBase {

    protected EntityManagerFactory entityManagerFactory;
    private IDatabaseTester databaseTester;
    protected final Logger log = LogManager.getLogger( this.getClass() );

    private IDataSet loadDataSet( IDatabaseConnection connection ) throws Exception {
        // load the data set
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream stream = cl.getResource( getDataSetXml() ).openStream();

        IDataSet data;

        if (getUseFlatXmlDataSet()) {
            data = new FlatXmlDataSet( stream );
        } else {
            data = new XmlDataSet( stream );
        }

        // order the data set
        DatabaseSequenceFilter filter = new DatabaseSequenceFilter( connection, data.getTableNames() );
        return new FilteredDataSet( filter, data );
    }

	private Connection getWithOpenJPASession(Object delegate)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SQLException {
		Method method = delegate.getClass().getMethod("getConnection",
				(Class[]) null);
		Object connDelegate = method.invoke(delegate, (Object[]) null);
		Method method2 = connDelegate.getClass().getMethod("getDelegate", (Class[])null);
		Connection jdbcConn = (Connection)method2.invoke(connDelegate, (Object[])null);
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
		if (delegate.getClass().getName().equals("org.hibernate.impl.SessionImpl")) {
			conn = getWithHibernateSession(delegate);
		} else if(delegate.getClass().getName().equals("org.apache.openjpa.persistence.EntityManagerImpl")) {
			conn = getWithOpenJPASession(delegate);
		}
		return conn;
	}

	@BeforeClass
	protected void initPersistenceStuff() throws Exception {
		EntityManager em = null;
		try {
			em = entityManagerFactory.createEntityManager();
			Connection conn = getConnection(em);
			IDatabaseConnection dbunitConn = new DatabaseConnection(conn);
			IDataSet dataSet = loadDataSet(dbunitConn);
			databaseTester = new DefaultDatabaseTester(dbunitConn);
			databaseTester.setDataSet(dataSet);
			databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
			databaseTester.onSetup();
		} catch (Exception ex) {
			log.debug("Exception in initializing database", ex);
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory( EntityManagerFactory vmcBizEntityManagerFactory ) {
        this.entityManagerFactory = vmcBizEntityManagerFactory;
    }

    protected boolean getUseFlatXmlDataSet() {
        return true;
    }

    public abstract String getDataSetXml();

}
