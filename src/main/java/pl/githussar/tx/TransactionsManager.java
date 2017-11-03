package pl.githussar.tx;

import java.util.List;

import pl.githussar.tx.dao.TransactionDAOJpa;


public class TransactionsManager {

	public Operation.Status processOperations(List<Operation> operations){
		TwoCommitsPhaseExceutor transactionExecutor = TwoCommitsPhaseExceutor.createInstance(operations, new TransactionDAOJpa());
		try {
			return transactionExecutor.executeTwoPhaseCommit();
		} catch (TransactionException e){
			//TODO end interrupted transaction
			return Operation.Status.ERROR;
		}
	}

}
