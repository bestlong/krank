package org.crank.config.spring.support;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.crank.crud.GenericDao;
import org.crank.crud.GenericDaoFactory;
import org.crank.crud.controller.CrudManagedObject;
import org.crank.crud.jsf.support.JsfCrudAdapter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.util.DefaultScopes;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.apache.logging.log4j.Logger;

public abstract class CrudDAOConfig implements InitializingBean {

    protected Logger logger = LogManager.getLogger(CrudDAOConfig.class);

    private DataSource dataSource;

	private Properties jpaProperties;

	public void afterPropertiesSet() throws Exception {

        if (jpaProperties == null) {
            logger.debug("jpaProperties was null so trying to load it from the classpath");
            try {
                jpaProperties = new Properties();
                jpaProperties.load(CrudDAOConfig.class.getResourceAsStream("jpa.properties"));
                logger.debug("jpaProperties was loaded " + jpaProperties);
            }catch (Exception ex) {
                jpaProperties = null;
                logger.debug("jpaProperties was NOT loaded ");
            }
        }
    }

	@ExternalBean
	public abstract List<CrudManagedObject> managedObjects();

	@SuppressWarnings("unchecked")
	@ExternalBean
	public abstract Map<String, GenericDao> repos();

	@SuppressWarnings("unchecked")
	@ExternalBean
	public abstract Map<String, JsfCrudAdapter> cruds();

	/**
	 * Create gaggle of DAO objects.
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Bean(scope = DefaultScopes.SINGLETON, aliases = "repos")
	public Map<String, GenericDao> repositories() throws Exception {

        DeferredResourceCreator drc = new DeferredResourceCreator(){
               public void createResource(Map map, CrudManagedObject cmo) throws Exception{

                   GenericDao dao = createDao(cmo);
                   map.put(cmo.getName(), dao);

               }
        };

        Map<String, GenericDao> repositories = new ManagedObjectsLazyInitMap<String, GenericDao>(managedObjects(), drc);
        return repositories;
	}

	/**
	 * Register EntityManager
	 * 
	 * @return
	 * @throws Exception
	 */
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryFactory()
			throws Exception {

        logger.debug("Creating entity manager factory factory ");
        LocalContainerEntityManagerFactoryBean entityManagerFactoryFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryFactory
				.setPersistenceUnitName(persistenceUnitName());
		logger.debug(String.format("Creating entity manager factory factory with PERSISTENT UNIT NAME %s", persistenceUnitName()));
        
        entityManagerFactoryFactory.setJpaVendorAdapter(jpaVendorAdapter());

        if (jpaProperties != null) {
            logger.debug("Setting JPA properties into entity manager factory factory");
            entityManagerFactoryFactory.setJpaProperties(jpaProperties);
		} else {
			entityManagerFactoryFactory.setJpaProperties(new Properties());
		}
		if (dataSource != null) {
			entityManagerFactoryFactory.setDataSource(dataSource);
		}

		entityManagerFactoryFactory.afterPropertiesSet();
		return entityManagerFactoryFactory;
	}

	@ExternalBean
	public abstract String persistenceUnitName();

	@ExternalBean
	public abstract JpaVendorAdapter jpaVendorAdapter();

	/**
	 * Create an entity manager factory.
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean(scope = DefaultScopes.SINGLETON, aliases="entityManagerFactory")
	public EntityManagerFactory entityManagerFactoryConfig() throws Exception {
		return entityManagerFactoryFactory().getObject();
	}

	@ExternalBean
	public abstract EntityManagerFactory entityManagerFactory();
	
	@Bean(scope = DefaultScopes.SINGLETON, aliases="transactionManager")
	public PlatformTransactionManager transactionManagerConfig() throws Exception {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory());
		return transactionManager;
	}
	
	@ExternalBean
	public abstract PlatformTransactionManager transactionManager(); 

	@Bean(scope = DefaultScopes.SINGLETON)
	public TransactionInterceptor transactionInterceptor() throws Exception {
		TransactionInterceptor transactionInterceptor = new TransactionInterceptor(
				(PlatformTransactionManager) transactionManager(),
				new AnnotationTransactionAttributeSource());
		return transactionInterceptor;
	}

	@SuppressWarnings("unchecked")
	public GenericDao createDao(CrudManagedObject cmo)
			throws Exception {
		GenericDaoFactory genericDaoFactory = new GenericDaoFactory();
		genericDaoFactory.setTransactionInterceptor(transactionInterceptor());
		if (cmo.getDaoInterface() == null) {
			genericDaoFactory.setInterface(GenericDao.class);
		} else {
			genericDaoFactory.setInterface(cmo.getDaoInterface());
		}
		genericDaoFactory.setQueryHints(cmo.getQueryHints());
		genericDaoFactory.setNewSelect(cmo.getNewSelect());
		genericDaoFactory.setBo(cmo.getEntityType());
		genericDaoFactory.setEntityManagerFactory(entityManagerFactory());
		genericDaoFactory.afterPropertiesSet();
		return (GenericDao) genericDaoFactory.getObject();
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setJpaProperties(Properties jpaProperties) {
		this.jpaProperties = jpaProperties;
	}

}
