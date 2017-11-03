package pl.githussar.tx.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import pl.githussar.tx.entities.GlobalTransaction;
import pl.githussar.tx.entities.LocalTransaction;

public class TransactionDAOJpa implements TransactionDAO{

	static EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAService");
	static EntityManager em = emf.createEntityManager();

	  
	public GlobalTransaction create(GlobalTransaction globalTransaction){
		em.getTransaction().begin();
		em.persist(globalTransaction);
		em.flush();
		em.getTransaction().commit();
		return globalTransaction;
	}
	
	public LocalTransaction updateLocalTransaction(LocalTransaction localTransaction){
		em.getTransaction().begin();
		em.merge(localTransaction);
		em.flush();
		em.getTransaction().commit();
		return localTransaction;
	}
}
