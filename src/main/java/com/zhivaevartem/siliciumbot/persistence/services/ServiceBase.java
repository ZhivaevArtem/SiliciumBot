package com.zhivaevartem.siliciumbot.persistence.services;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Base abstract class for all services interact with database.
 */
public abstract class ServiceBase {
  private EntityManager entityManager;
  private EntityManagerFactory entityManagerFactory;

  protected void endTransaction() {
    if (null != this.entityManager) {
      this.entityManager.getTransaction().commit();
      this.entityManager.close();
    }
    if (null != this.entityManagerFactory) {
      this.entityManagerFactory.close();
    }
  }

  protected EntityManager startTransaction() {
    this.entityManagerFactory = Persistence.createEntityManagerFactory("siliciumbot");
    this.entityManager = this.entityManagerFactory.createEntityManager();
    this.entityManager.getTransaction().begin();
    return this.entityManager;
  }
}
