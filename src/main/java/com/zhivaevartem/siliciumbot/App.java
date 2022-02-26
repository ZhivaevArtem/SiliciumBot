package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.persistence.entities.BotConfigLocal;
import com.zhivaevartem.siliciumbot.utils.ConfigUtils;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Startup class.
 */
public class App {
  /**
   * Startup method.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    String token = ConfigUtils.getProp("token");
    System.out.println(token);

    EntityManagerFactory emFac = Persistence.createEntityManagerFactory("siliciumbot");
    EntityManager em = emFac.createEntityManager();

    BotConfigLocal cfg = new BotConfigLocal();
    cfg.setGuildId("456894735684567867985746");
    cfg.setPrefix("!");
    em.getTransaction().begin();
    em.persist(cfg);
    em.getTransaction().commit();
    em.close();
    emFac.close();
  }
}
