package org.crank.crud.spring.support;

import org.apache.logging.log4j.LogManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.dao.DataAccessResourceFailureException;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class OpenEntityManagerInViewUtil {

    private static Logger logger = LogManager.getLogger(OpenEntityManagerInViewUtil.class);

    public static void runWithEntityManager(EntityManagerFactory emf, Runnable runnable) {
        boolean participate = false;

        if (TransactionSynchronizationManager.hasResource(emf)) {
            participate = true;
        }
        else {
            try {
                EntityManager em = emf.createEntityManager();
                TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em));
            }
            catch (PersistenceException ex) {
                throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
            }
        }

        try {
            runnable.run();
        }
        finally {
            if (!participate) {
                EntityManagerHolder emHolder = (EntityManagerHolder)
                        TransactionSynchronizationManager.unbindResource(emf);
                closeEntityManager(emHolder.getEntityManager());
            }
        }
    }


	public static void closeEntityManager(EntityManager em) {
		if (em != null) {
			try {
				em.close();
			}
			catch (PersistenceException ex) {
                logger.debug("Problem closing entity manager ", ex);
            }
			catch (Throwable ex) {
                logger.debug("Unexpected exception when closing entity manager ", ex);
            }
		}
	}

}
